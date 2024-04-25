package com.jpatest.domain;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ProductV1: Product 이하의 재고를 단순 수치 칼럼으로 관리
 */
@Entity
@Table(name = "products_v1")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductV1 {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_v1_id", nullable = false, updatable = false)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Integer stock;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Builder
	public ProductV1(String name, int stock) {
		this.name = name;
		this.stock = stock;
	}

	public void decreaseStock(int amount) {
		if (stock < amount) {
			throw new RuntimeException("NotEnoughStock Exception");
		}
		stock -= amount;
	}
}
