package com.jpatest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jpatest.domain.OrderV1;
import com.jpatest.lock.DistributedLockWrapper;
import com.jpatest.repository.MemberRepository;
import com.jpatest.repository.OrderRepository;
import com.jpatest.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * OrderServiceV1: Product 이하의 재고를 단순 필드 값으로 관리
 */
@Slf4j
@Service
public class OrderServiceV1 {
	private OrderRepository orderRepository;
	private MemberRepository memberRepository;
	private ProductRepository productRepository;
	private DistributedLockWrapper distributedLockWrapper;

	@Autowired
	public OrderServiceV1(OrderRepository orderRepository, MemberRepository memberRepository, ProductRepository productRepository, DistributedLockWrapper distributedLockWrapper) {
		this.orderRepository = orderRepository;
		this.memberRepository = memberRepository;
		this.productRepository = productRepository;
		this.distributedLockWrapper = distributedLockWrapper;
	}
	@Transactional
	public void purchaseOrderWithDLock(long memberId, long productId, int amount) {
		distributedLockWrapper.wrapWithLock(String.valueOf(productId), ()->{
			var product = productRepository.findById(productId).orElseThrow();
			product.decreaseStock(amount);
			var member = memberRepository.findById(memberId).orElseThrow();
			orderRepository.save(OrderV1.builder().member(member).productV1(product).amount(amount).build());
			return null;
		});
	}

	@Transactional
	public void purchaseOrderWithPLock(long memberId, long productId, int amount) {
		var product = productRepository.findById(productId).orElseThrow();
		product.decreaseStock(amount);
		var member = memberRepository.findById(memberId).orElseThrow();
		orderRepository.save(OrderV1.builder().member(member).productV1(product).amount(amount).build());
	}
}
