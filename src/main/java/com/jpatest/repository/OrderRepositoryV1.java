package com.jpatest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpatest.domain.OrderV1;

@Repository
public interface OrderRepositoryV1 extends JpaRepository<OrderV1, Long> {
}
