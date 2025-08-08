package kr.hhplus.be.server.infrastructure.mapper;

import kr.hhplus.be.server.domain.model.Point;
import kr.hhplus.be.server.infrastructure.entity.PointEntity;

public class PointMapper {
    public static Point toPoint(PointEntity pointEntity) {
        return Point.builder()
                .id(pointEntity.getId())
                .userId(pointEntity.getUserId())
                .balance(pointEntity.getBalance())
                .version(pointEntity.getVersion())
                .build();
    }

    public static PointEntity toPointEntity(Point point) {
        return PointEntity.builder()
                .id(point.getId())
                .userId(point.getUserId())
                .balance(point.getBalance())
                .version(point.getVersion())
                .build();
    }
}
