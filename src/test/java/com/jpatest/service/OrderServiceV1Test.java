package com.jpatest.service;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.jpatest.domain.Member;
import com.jpatest.domain.ProductV1;
import com.jpatest.repository.MemberRepository;
import com.jpatest.repository.OrderRepository;
import com.jpatest.repository.ProductRepository;

@SpringBootTest
public class OrderServiceV1Test {
	@Autowired
	private OrderServiceV1 orderServiceV1;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private ProductRepository productRepository;

	@Test
	@DisplayName("15개 재고 > 2개씩 8개의 유저(스레드)가 구매 요청 > 7개의 유저가 성공하고 하나의 유저가 실패, 잔여 재고 1")
	void purchaseOrder() throws Exception {
		// given
		var stockCnt = 15;
		var userCnt = 8;
		var orderAmountPerUser = 2;
		var expectedFailCnt = 1;

		var productId = productRepository.save(ProductV1.builder().stock(stockCnt).name("product").build()).getId();
		var userNames = new ArrayList<String>();
		for (var num = 1; num <= userCnt; num++) {
			userNames.add("name" + num);
		}
		var userIds = userNames.stream().map(this::createMemberAndReturnId).toList();
		var executorService = Executors.newFixedThreadPool(userCnt);
		var failCnt = new AtomicInteger();
		var startLatch = new CountDownLatch(1);
		var endLatch = new CountDownLatch(userCnt);

		for (var i = 0; i < userCnt; i++) {
			var userIdx = i;
			executorService.submit(()->{
				try {
					startLatch.await();
					orderServiceV1.purchaseOrderWithDLock(userIds.get(userIdx), productId, orderAmountPerUser);
				} catch (Exception err) {
					failCnt.addAndGet(1);
				} finally {
					endLatch.countDown();
				}
			});
		}

		startLatch.countDown();
		endLatch.await();

		Assertions.assertThat(failCnt.get()).isEqualTo(expectedFailCnt);
		Assertions.assertThat(orderRepository.findAll().size()).isEqualTo(userCnt - expectedFailCnt);
		var product = productRepository.findById(productId).orElseThrow();
		Assertions.assertThat(product.getStock()).isEqualTo(stockCnt - (userCnt - expectedFailCnt) * orderAmountPerUser);
	}

	private long createMemberAndReturnId(String name) {
		return memberRepository.save(Member.builder().name(name).build()).getId();
	}
}
