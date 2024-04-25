package com.jpatest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpatest.domain.ProductV1;

@Repository
public interface ProductRepository extends JpaRepository<ProductV1, Long> {
}
