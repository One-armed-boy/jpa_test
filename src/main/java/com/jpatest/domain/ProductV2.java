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

/**
 * ProductV2: Product 이하의 재고를 새로운 Stock 테이블의 레코드로 관리
 */
@Entity
@Table(name = "products_v2")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProductV2 {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "product_v2_id", nullable = false, updatable = false)
	private Long id;

	@Column(nullable = false)
	private String name;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", nullable = false)
	private Date createdAt;

	@Builder
	public ProductV2(String name) {
		this.name = name;
	}
}
