package com.jpatest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jpatest.domain.Order;
import com.jpatest.lock.DistributedLockWrapper;
import com.jpatest.repository.MemberRepository;
import com.jpatest.repository.OrderRepository;
import com.jpatest.repository.ProductRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderService {
	private OrderRepository orderRepository;
	private MemberRepository memberRepository;
	private ProductRepository productRepository;
	private DistributedLockWrapper distributedLockWrapper;

	@Autowired
	public OrderService(OrderRepository orderRepository, MemberRepository memberRepository, ProductRepository productRepository, DistributedLockWrapper distributedLockWrapper) {
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
			orderRepository.save(Order.builder().member(member).product(product).amount(amount).build());
			return null;
		});
	}
}
