package com.jpatest.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import com.jpatest.domain.Member;
import com.jpatest.domain.ProductV2;
import com.jpatest.domain.Stock;
import com.jpatest.repository.MemberRepository;
import com.jpatest.repository.OrderRepositoryV2;
import com.jpatest.repository.ProductRepositoryV2;
import com.jpatest.repository.StockRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class OrderServiceV2Test {
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private OrderRepositoryV2 orderRepositoryV2;
	@Autowired
	private ProductRepositoryV2 productRepositoryV2;
	@Autowired
	private StockRepository stockRepository;
	@Autowired
	private OrderServiceV2 orderServiceV2;
	private List<Long> userIds;
	private Long productId;

	private final static int STOCK_CNT = 30000;
	private final static int USER_CNT = 20;
	private final static int ORDER_AMOUNT_PER_USER = 3;

	@BeforeEach
	@Transactional
	void initTable() {
		var product = productRepositoryV2.save(ProductV2.builder().name("product").build());
		var stocks = IntStream.rangeClosed(1, STOCK_CNT).boxed().map((i)-> Stock.builder().productV2(product).build()).toList();
		stockRepository.saveAll(stocks);
		var userNames = new ArrayList<String>();
		for (var num = 1; num <= USER_CNT; num++) {
			userNames.add("name" + num);
		}
		userIds = userNames.stream().map(this::createMemberAndReturnId).toList();
		productId = product.getId();
	}

	@AfterEach
	void clearTable() {
		stockRepository.deleteAllInBatch();
		orderRepositoryV2.deleteAllInBatch();
		productRepositoryV2.deleteAllInBatch();
		memberRepository.deleteAllInBatch();
	}

	@Test
	@DisplayName("30000개 재고 > 3개씩 20개의 유저(스레드)가 구매 요청 > 60개의 재고가 팔림")
	void purchaseOrder() {
		execTest(orderServiceV2::purchaseOrderWithPLock);
	}

	@Test
	@DisplayName("30000개 재고 > 3개씩 20개의 유저가 구매 요청, 단 재고 랜덤 추출 방식 사용 > 60개 재고가 팔림")
	void purchaseOrderRandom() {
		execTest(orderServiceV2::purchaseOrderWithPLockRandom);
	}

	@Test
	@DisplayName("30000개 재고 > 3개씩 20개의 유저가 구매 요청, 단 최적화된 재고 랜덤 추출 방식 사용 > 60개 재고가 팔림")
	void purchaseOrderRandomOptimize() {
		execTest(orderServiceV2::purchaseOrderWithPLockRandomOptimize);
	}

	private void execTest(PurchaseOrder purchaseOrder) {
		// given
		var executorService = Executors.newFixedThreadPool(USER_CNT);
		var startLatch = new CountDownLatch(1);
		var endLatch = new CountDownLatch(USER_CNT);

		for (var i = 0; i < USER_CNT; i++) {
			var userIdx = i;
			executorService.submit(()->{
				try {
					startLatch.await();
					purchaseOrder.exec(userIds.get(userIdx), productId, ORDER_AMOUNT_PER_USER);
				} catch (Exception err) {
					throw new RuntimeException(err);
				} finally {
					endLatch.countDown();
				}
			});
		}

		wrapWithTimeConsole(()->{
			try {
				startLatch.countDown();
				endLatch.await();
			} catch (InterruptedException err) {
				throw new RuntimeException(err);
			}
		});

		Assertions.assertThat(orderRepositoryV2.findAll().size()).isEqualTo(USER_CNT);
		var unsaledStocks = stockRepository.findByProductIdAndNonOrdered(productId, Limit.unlimited());
		Assertions.assertThat(unsaledStocks.size()).isEqualTo(STOCK_CNT - USER_CNT * ORDER_AMOUNT_PER_USER);
	}

	private long createMemberAndReturnId(String name) {
		return memberRepository.save(Member.builder().name(name).build()).getId();
	}

	private void wrapWithTimeConsole(Runnable runnable) {
		var stopWatch = new StopWatch();
		stopWatch.start();
		runnable.run();
		stopWatch.stop();
		log.info("소요 시간: {} ms", stopWatch.getTotalTimeMillis());
	}

	@FunctionalInterface
	private interface PurchaseOrder {
		void exec(long userId, long productId, int amount);
	}
}
