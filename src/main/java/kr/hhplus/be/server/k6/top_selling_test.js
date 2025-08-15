import http from 'k6/http';
import { check } from 'k6';

export const options = {
    scenarios: {
        baseline_performance: {
            executor: 'ramping-vus',
            startVUs: 10,
            stages: [
                { duration: '10s', target: 10 },
                { duration: '30s', target: 50 },
                { duration: '30s', target: 100 },
                { duration: '10s', target: 0 },
            ],
            exec: 'topSellingTest',
        }
    }
};

export function topSellingTest() {
    const res = http.get('http://localhost:8080/api/v1/products/top-selling');
    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 300ms': (r) => r.timings.duration < 300,
    });
}