package com.jpatest.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
	private OrderRepository orderRepository;
	private MemberRepository memberRepository;
	private ProductRepository productRepository;
	private DecreaseProductStockUseCase decreaseProductStockUseCase;

	@Autowired
	public OrderService(OrderRepository orderRepository, MemberRepository memberRepository, ProductRepository productRepository, DecreaseProductStockUseCase decreaseProductStockUseCase) {
		this.orderRepository = orderRepository;
		this.memberRepository = memberRepository;
		this.productRepository = productRepository;
		this.decreaseProductStockUseCase = decreaseProductStockUseCase;
	}
	@Transactional
	public void purchaseOrder(long memberId, long productId, int amount) {
		// 여기에서 첫 쿼리를 날릴 경우 이 시점에 스냅샷 생성 -> 락이 걸려 있지 않기 때문에 전부 동일 시점의 재고 수를 가짐
		// var member = memberRepository.findById(memberId).orElseThrow();
		decreaseProductStockUseCase.decreaseStock(productId, amount);
		// 아래에서 조회할 경우 문제가 되지 않음
		var member = memberRepository.findById(memberId).orElseThrow();
		var product = productRepository.findById(productId).orElseThrow();
		orderRepository.save(Order.builder().member(member).product(product).amount(amount).build());
	}
}
