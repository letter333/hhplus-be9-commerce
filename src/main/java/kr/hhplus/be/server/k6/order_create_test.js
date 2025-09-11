import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const orderCreationLatency = new Trend('order_creation_latency_ms');

export const options = {
    scenarios: {
        order_creation_performance: {
            executor: 'ramping-arrival-rate',
            startRate: 0,
            timeUnit: '1s',
            preAllocatedVUs: 600,
            maxVUs: 1000,
            stages: [
                { duration: '2s',  target: 50 },
                { duration: '6s',  target: 200 },
                { duration: '20s', target: 400 },
                { duration: '6s',  target: 200 },
                { duration: '2s',  target: 0 }
            ],
        }
    },
    thresholds: {
        'http_req_duration': ['p(95) < 200', 'p(99) < 400'],
        'http_req_failed': ['rate < 0.01'], // 실패율 1% 미만
        'http_reqs': ['count > 8500'], // 총 요청 수
        'order_creation_latency_ms': ['p(95) < 250'],
    }
};

const BASE_URL = 'http://localhost:8080';

const testUsers = Array.from({ length: 2000 }, (_, i) => ({
    id: i + 4642,
    name: `loadtest_user_${String(i + 4642).padStart(4, '0')}`,
}));

const testProducts = Array.from({ length: 416 }, (_, i) => {
    const productId = 1549 + i;
    const seq = i + 1;

    let price;
    switch (seq % 5) {
        case 0: price = 100; break;
        case 1: price = 250; break;
        case 2: price = 500; break;
        case 3: price = 750; break;
        default: price = 1000; break;
    }

    return {
        id: productId,
        name: `부하테스트상품_${String(seq).padStart(3, '0')}`,
        description: `TPS 1000 부하테스트용 상품 ${seq}`,
        price: price,
        stock: 9999
    };
});

const testAddresses = [
    {
        shippingAddress1: "서울특별시 강남구 테헤란로 10",
        shippingAddress2: "101호",
        shippingZipCode: "06236"
    },
    {
        shippingAddress1: "서울특별시 서초구 서초대로 78",
        shippingAddress2: "202호",
        shippingZipCode: "06615"
    },
    {
        shippingAddress1: "서울특별시 송파구 올림픽로 35",
        shippingAddress2: "303호",
        shippingZipCode: "05551"
    },
    {
        shippingAddress1: "서울특별시 중구 을지로 100",
        shippingAddress2: "401호",
        shippingZipCode: "04533"
    },
    {
        shippingAddress1: "서울특별시 마포구 홍익로 20",
        shippingAddress2: "501호",
        shippingZipCode: "04039"
    }
];

const testPhoneNumbers = [
    "01012341234",
    "01098765432",
    "01055556666",
    "01077778888",
    "01011112222"
];

export default function () {
    const startTime = Date.now();
    const user = testUsers[Math.floor(Math.random() * testUsers.length)];
    const userId = user.id;

    const orderItemCount = Math.floor(Math.random() * 5) + 1;
    const orderProductList = [];
    const usedProductIds = new Set();

    for (let i = 0; i < orderItemCount; i++) {
        let product;

        do {
            product = testProducts[Math.floor(Math.random() * testProducts.length)];
        } while (usedProductIds.has(product.id));

        usedProductIds.add(product.id);
        const quantity = Math.floor(Math.random() * 5) + 1;

        orderProductList.push({
            productId: product.id,
            quantity: quantity
        });
    }

    const shippingAddress = testAddresses[Math.floor(Math.random() * testAddresses.length)];
    const recipientNumber = testPhoneNumbers[Math.floor(Math.random() * testPhoneNumbers.length)];

    const orderPayload = JSON.stringify({
        userId: userId,
        orderProductList: orderProductList,
        shippingAddress: shippingAddress,
        recipientNumber: recipientNumber
    });

    const res = http.post(`${BASE_URL}/api/v1/orders`, orderPayload, {
        headers: { 'Content-Type': 'application/json' },
        timeout: '8s',
    });

    const endTime = Date.now();

    if (res.status !== 200 && res.status !== 201) {
        console.log(`Order creation error - Status: ${res.status}, Body: ${res.body}`);
        console.log(`Order payload: ${orderPayload}`);
    }

    const orderSuccess = check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
        'response body not empty': (r) => r.body && r.body.length > 0,
        'response time < 800ms': (r) => r.timings.duration < 800,
        'has order id': (r) => {
            try {
                const data = JSON.parse(r.body);
                return data.data.id;
            } catch (e) {
                return false;
            }
        }
    });

    if (res.timings && res.timings.duration) {
        orderCreationLatency.add(res.timings.duration);
    }
}