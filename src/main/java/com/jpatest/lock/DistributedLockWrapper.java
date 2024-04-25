package com.jpatest.lock;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DistributedLockWrapper {
	private final static long LOCK_WAIT_SECOND = 5;
	private final static long LOCK_LEASE_SECOND = 3;
	private final RedissonClient redissonClient;

	@Autowired
	public DistributedLockWrapper(RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}

	@Transactional
	public <T> T wrapWithLock(String lockName, InnerLockCallback<? extends T> innerLockCallback) {
		var lock = redissonClient.getLock(lockName);
		try {
			log.info("Try D-Lock!!");
			var available = lock.tryLock(LOCK_WAIT_SECOND, LOCK_LEASE_SECOND, TimeUnit.SECONDS);

			if (!available) {
				log.error("Lock 획득 Exception");
				throw new RuntimeException("Concurrency Exception");
			}
			log.info("Lock 획득 성공!!");
			return innerLockCallback.exec();
		} catch (InterruptedException err) {
			log.error("Concurrency Exception");
			throw new RuntimeException("Concurrency Exception");
		} finally {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCompletion(int status) {
					log.info("Unlock D-Lock!!");
					lock.unlock();
				}
			});
		}
	}

	@FunctionalInterface
	public interface InnerLockCallback<T> {
		T exec();
	}
}
