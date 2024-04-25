package com.jpatest.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Stock {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "stock_id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_v2_id")
	private ProductV2 productV2;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_v2_id")
	@Setter
	private OrderV2 orderV2;

	@Builder
	public Stock(ProductV2 productV2, OrderV2 orderV2) {
		this.productV2 = productV2;
		this.orderV2 = orderV2;
	}
}
