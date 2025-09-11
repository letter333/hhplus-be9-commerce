import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const chargeLatency = new Trend('charge_latency_ms');

export const options = {
    scenarios: {
        point_charge_performance: {
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
        'http_req_duration': ['p(95) < 100', 'p(99)<300'],
        'http_req_failed': ['rate<0.01'],
        'http_reqs': ['count>8500'],
    }
};

const BASE_URL = 'http://localhost:8080';

const testUsers = Array.from({ length: 2000 }, (_, i) => ({
    id: i + 4642,
    name: `loadtest_user_${String(i + 4642).padStart(4, '0')}`,
}));


export default function () {
    const user = testUsers[Math.floor(Math.random() * testUsers.length)];
    const userId = user.id;
    const amount = 1000;
    const payload = JSON.stringify({ amount });

    const res = http.post(`${BASE_URL}/api/v1/users/${userId}/points`, payload, {
        headers: { 'Content-Type': 'application/json' },
        timeout: '5s',
    });

    if (res.status !== 200) {
        console.log(`Error - Status: ${res.status}, Body: ${res.body}`);
    }

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response body not empty': (r) => r.body && r.body.length > 0,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    if (res.timings && res.timings.duration) {
        chargeLatency.add(res.timings.duration);
    }
}