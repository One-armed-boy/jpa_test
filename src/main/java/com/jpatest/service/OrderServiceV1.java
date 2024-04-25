package com.jpatest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jpatest.domain.OrderV1;
import com.jpatest.lock.DistributedLockWrapper;
import com.jpatest.repository.MemberRepository;
import com.jpatest.repository.OrderRepositoryV1;
import com.jpatest.repository.ProductRepositoryV1;

import lombok.extern.slf4j.Slf4j;

/**
 * OrderServiceV1: Product 이하의 재고를 단순 필드 값으로 관리
 */
@Slf4j
@Service
public class OrderServiceV1 {
	private OrderRepositoryV1 orderRepositoryV1;
	private MemberRepository memberRepository;
	private ProductRepositoryV1 productRepositoryV1;
	private DistributedLockWrapper distributedLockWrapper;

	@Autowired
	public OrderServiceV1(OrderRepositoryV1 orderRepositoryV1, MemberRepository memberRepository, ProductRepositoryV1 productRepositoryV1, DistributedLockWrapper distributedLockWrapper) {
		this.orderRepositoryV1 = orderRepositoryV1;
		this.memberRepository = memberRepository;
		this.productRepositoryV1 = productRepositoryV1;
		this.distributedLockWrapper = distributedLockWrapper;
	}
	@Transactional
	public void purchaseOrderWithDLock(long memberId, long productId, int amount) {
		distributedLockWrapper.wrapWithLock(String.valueOf(productId), ()->{
			var product = productRepositoryV1.findById(productId).orElseThrow();
			product.decreaseStock(amount);
			var member = memberRepository.findById(memberId).orElseThrow();
			orderRepositoryV1.save(OrderV1.builder().member(member).productV1(product).amount(amount).build());
			return null;
		});
	}
}
