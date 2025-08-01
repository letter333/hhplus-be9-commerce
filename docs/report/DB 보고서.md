# DB 성능 개선 보고서
## 상품 목록 조회
### 1. 문제가 되는 이유
- 전체 `Product` 테이블을 풀 스캔해야 하므로 정렬 비용이 급격히 증가
- OFFSET 기반 페이징은 페이지가 깊어질수록  OFFSET 까지 데이터를 모두 조회하고 페이지 크기 만큼만 반환

### 2.솔루션
- 인덱스 적용
- 커서 기반 페이징 방식으로 변경

**기존 쿼리**
```sql
SELECT *
FROM products
WHERE price BETWEEN 500000 AND 550000
ORDER BY created_at DESC
LIMIT 50 OFFSET 5000
```

| 항목    | 내용                          | 해석                               |
|-------|-----------------------------|----------------------------------|
| type  | ALL                         | Full Table Scan                  |
| key   | NULL                        | 인덱스 미사용                          |
| rows  | 1000000                     | 전체 레코드 탐색으로 I/O부하                |
| extra | Using where; Using filesort | 정렬을 위해 별도의 메모리/디스크에서 추가 정렬 작업 수행 |

**인덱스 적용 및 쿼리 개선**
- offset 기반 페이징에서 커서 기반 페이징으로 변경
```sql
CREATE INDEX idx_products_created_at ON products (created_at DESC);
CREATE INDEX idx_products_price ON products (price);
CREATE INDEX idx_products_created_price ON products (created_at DESC, price);

SELECT * FROM products
WHERE id IN (
    SELECT id FROM products
    WHERE price BETWEEN 500000 AND 550000 ORDER BY created_at DESC
) AND id < 499050;
```

| 항목    | 내용                          | 해석          |
|-------|-----------------------------|-------------|
| type  | RANGE                       | 범위 스캔으로 개선  |
| key   | PRIMARY, idx_products_price | 생성된 인덱스 활용  |
| rows  | 91910                       | 스캔량 대폭 감소   |
| extra | Using index condition | filesort 제거 |

----