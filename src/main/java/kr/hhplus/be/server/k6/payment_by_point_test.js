import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const paymentLatency = new Trend('payment_latency_ms');

export const options = {
    scenarios: {
        payment_performance: {
            executor: 'ramping-arrival-rate',
            startRate: 0,
            timeUnit: '1s',
            preAllocatedVUs: 200,
            maxVUs: 300,
            stages: [
                { duration: '2s',  target: 25 },
                { duration: '6s',  target: 100 },
                { duration: '20s', target: 200 },
                { duration: '6s',  target: 100 },
                { duration: '2s',  target: 0 }
            ],
        }
    },
    thresholds: {
        'http_req_duration': ['p(95) < 500', 'p(99) < 1000'],
        'http_req_failed': ['rate < 0.02'],
        'http_reqs': ['count > 5000'],
        'payment_latency_ms': ['p(95) < 400'],
    }
};

const BASE_URL = 'http://localhost:8080';

const MIN_USER_ID = 4643;
const MAX_USER_ID = 6642;
const TOTAL_USER_COUNT = MAX_USER_ID - MIN_USER_ID + 1;

const MIN_ORDER_ID = 1020;
const MAX_ORDER_ID = 92499;
const TOTAL_ORDER_COUNT = MAX_ORDER_ID - MIN_ORDER_ID + 1;

let availableUserIds = [];
let availableOrderIds = [];
let usedUserIds = new Set();
let usedOrderIds = new Set();

function initializeUserIdPool() {
    const vuId = __VU;
    const totalVUs = 300;

    const usersPerVU = Math.floor(TOTAL_USER_COUNT / totalVUs);
    const startIndex = (vuId - 1) * usersPerVU;
    const endIndex = vuId === totalVUs ? TOTAL_USER_COUNT : startIndex + usersPerVU;

    availableUserIds = [];
    for (let i = startIndex; i < endIndex; i++) {
        availableUserIds.push(MIN_USER_ID + i);
    }

    for (let i = availableUserIds.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [availableUserIds[i], availableUserIds[j]] = [availableUserIds[j], availableUserIds[i]];
    }

    console.log(`👤 [VU ${vuId} USER INIT] Assigned user ID range: ${availableUserIds[0]} ~ ${availableUserIds[availableUserIds.length - 1]} (${availableUserIds.length} users)`);
}

function initializeOrderIdPool() {
    const vuId = __VU;
    const totalVUs = 300; // maxVUs와 맞춤

    const ordersPerVU = Math.floor(TOTAL_ORDER_COUNT / totalVUs);
    const startIndex = (vuId - 1) * ordersPerVU;
    const endIndex = vuId === totalVUs ? TOTAL_ORDER_COUNT : startIndex + ordersPerVU;

    availableOrderIds = [];
    for (let i = startIndex; i < endIndex; i++) {
        availableOrderIds.push(MIN_ORDER_ID + i);
    }

    for (let i = availableOrderIds.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [availableOrderIds[i], availableOrderIds[j]] = [availableOrderIds[j], availableOrderIds[i]];
    }
}

function getUniqueUserId() {
    if (availableUserIds.length === 0) {
        initializeUserIdPool();
    }

    if (availableUserIds.length === 0) {
        throw new Error(`VU ${__VU}: No more available user IDs in assigned range`);
    }

    const userId = availableUserIds.pop();
    usedUserIds.add(userId);

    return userId;
}

function getUniqueOrderId() {
    if (availableOrderIds.length === 0) {
        initializeOrderIdPool();
    }

    if (availableOrderIds.length === 0) {
        throw new Error(`VU ${__VU}: No more available order IDs in assigned range`);
    }

    const orderId = availableOrderIds.pop();
    usedOrderIds.add(orderId);

    return orderId;
}

let totalRequests = 0;

export default function () {
    totalRequests++;

    const startTime = Date.now();

    let userId, orderId;
    try {
        userId = getUniqueUserId();
        orderId = getUniqueOrderId();
    } catch (error) {
        console.log(`❌ [VU ${__VU}] ${error.message}`);
        return;
    }

    const paymentPayload = JSON.stringify({
        orderId: orderId,
        paymentMethod: 'POINT',
    });

    const res = http.post(`${BASE_URL}/api/v1/orders/${userId}/payments`, paymentPayload, {
        headers: { 'Content-Type': 'application/json' },
        timeout: '10s',
    });

    const endTime = Date.now();

    const paymentSuccess = check(res, {
        'status is 200 or 201': (r) => r.status === 200 || r.status === 201,
        'response body not empty': (r) => r.body && r.body.length > 0,
        'response time < 800ms': (r) => r.timings.duration < 800,
        'has payment id': (r) => {
            try {
                const data = JSON.parse(r.body);
                return !!(data.data && data.data.id);
            } catch (e) {
                return false;
            }
        },
    });

    // 지연 시간 기록
    if (res.timings && res.timings.duration) {
        paymentLatency.add(res.timings.duration);
    }
}