package com.jpatest.repository;

import java.util.List;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jpatest.domain.Stock;

import jakarta.persistence.LockModeType;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
	@Transactional
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Stock s WHERE s.productV2.id = :productId AND s.orderV2 IS NULL ORDER BY s.id")
	List<Stock> findByProductIdAndNonOrdered(long productId, Limit limit);

	@Transactional
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM Stock s WHERE s.productV2.id = :productId AND s.orderV2 IS NULL ORDER BY FUNCTION('RAND')")
	List<Stock> findByProductIdAndNonOrderedRandom(long productId, Limit limit);

	@Transactional
	@Query(value = """
		SELECT *
		FROM stocks s, (
			select floor(rand() * (count(*) - :limit)) as base from stocks
		) tmp
		where
			s.product_v2_id = :productId
			and s.order_v2_id is null
			and s.stock_id >= tmp.base
		order by s.stock_id
		limit :limit
		for update
		""", nativeQuery = true)
	List<Stock> findByProductIdAndNonOrderedRandomOptimize(long productId, int limit);
}
