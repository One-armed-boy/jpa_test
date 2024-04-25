package com.jpatest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpatest.domain.OrderV2;

@Repository
public interface OrderRepositoryV2 extends JpaRepository<OrderV2, Long> {
}
