package com.jpatest.lock;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DecreaseProductStockUseCaseWithDLock implements DecreaseProductStockUseCase{
	private final static long LOCK_WAIT_SECOND = 5;
	private final static long LOCK_LEASE_SECOND = 3;
	private RedissonClient redissonClient;
	private ProductRepository productRepository;

	@Autowired
	public DecreaseProductStockUseCaseWithDLock(RedissonClient redissonClient, ProductRepository productRepository) {
		this.redissonClient = redissonClient;
		this.productRepository = productRepository;
	}
	@Override
	@Transactional
	public void decreaseStock(long productId, int amount) {
		var lockName = String.valueOf(productId);
		var lock = redissonClient.getLock(lockName);
		try {
			log.info("Try D-Lock!!");
			var available = lock.tryLock(LOCK_WAIT_SECOND, LOCK_LEASE_SECOND, TimeUnit.SECONDS);

			if (!available) {
				log.error("Lock 획득 Exception");
				throw new RuntimeException("Concurrency Exception");
			}
			log.info("Lock 획득 성공!!");
			var product = productRepository.findById(productId).orElseThrow();

			if (product.getStock() < amount) {
				log.error("NotEnoughStock Exception");
				throw new RuntimeException("NotEnoughStock Exception");
			}

			product.setStock(product.getStock() - amount);
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
}
