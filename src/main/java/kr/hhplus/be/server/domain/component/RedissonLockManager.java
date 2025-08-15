package kr.hhplus.be.server.domain.component;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RedissonLockManager {
    private final RedissonClient redissonClient;

    public RLock getLock(String key) {
        return redissonClient.getLock(key);
    }

    public RLock getMultiLock(List<RLock> locks) {
        RLock[] lockArray = locks.toArray(new RLock[0]);
        return redissonClient.getMultiLock(lockArray);
    }
}
