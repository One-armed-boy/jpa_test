package com.jpatest.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jpatest.domain.OrderV2;
import com.jpatest.repository.MemberRepository;
import com.jpatest.repository.OrderRepositoryV2;
import com.jpatest.repository.ProductRepositoryV2;
import com.jpatest.repository.StockRepository;

/**
 * OrderServiceV2: Product 이하의 재고를 새로운 레코드로 관리
 */
@Service
public class OrderServiceV2 {
	private MemberRepository memberRepository;
	private ProductRepositoryV2 productRepositoryV2;
	private OrderRepositoryV2 orderRepositoryV2;
	private StockRepository stockRepository;

	@Autowired
	public OrderServiceV2(MemberRepository memberRepository, ProductRepositoryV2 productRepositoryV2, OrderRepositoryV2 orderRepositoryV2, StockRepository stockRepository) {
		this.memberRepository = memberRepository;
		this.productRepositoryV2 = productRepositoryV2;
		this.orderRepositoryV2 = orderRepositoryV2;
		this.stockRepository = stockRepository;
	}

	@Transactional
	public void purchaseOrderWithPLock(long memberId, long productId, int amount) {
		var product = productRepositoryV2.findById(productId).orElseThrow();
		var member = memberRepository.findById(memberId).orElseThrow();
		var order = OrderV2.builder().member(member).productV2(product).build();
		orderRepositoryV2.save(order);
		var stocks = stockRepository.findByProductIdAndNonOrdered(productId, Limit.of(amount));
		stocks.forEach((stock -> stock.setOrderV2(order)));
	}

	@Transactional
	public void purchaseOrderWithPLockRandom(long memberId, long productId, int amount) {
		var product = productRepositoryV2.findById(productId).orElseThrow();
		var member = memberRepository.findById(memberId).orElseThrow();
		var order = OrderV2.builder().member(member).productV2(product).build();
		orderRepositoryV2.save(order);
		var stocks = stockRepository.findByProductIdAndNonOrderedRandom(productId, Limit.of(amount));
		stocks.forEach((stock -> stock.setOrderV2(order)));
	}

	@Transactional
	public void purchaseOrderWithPLockRandomOptimize(long memberId, long productId, int amount) {
		var product = productRepositoryV2.findById(productId).orElseThrow();
		var member = memberRepository.findById(memberId).orElseThrow();
		var order = OrderV2.builder().member(member).productV2(product).build();
		orderRepositoryV2.save(order);
		var stocks = stockRepository.findByProductIdAndNonOrderedRandomOptimize(productId, amount);
		stocks.forEach((stock -> stock.setOrderV2(order)));
	}
}
