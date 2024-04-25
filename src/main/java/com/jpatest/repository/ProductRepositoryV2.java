package com.jpatest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpatest.domain.ProductV2;

@Repository
public interface ProductRepositoryV2 extends JpaRepository<ProductV2, Long> {
}
