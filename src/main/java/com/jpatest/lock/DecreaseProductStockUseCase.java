package com.jpatest.lock;

import org.springframework.transaction.annotation.Transactional;

public interface DecreaseProductStockUseCase {
	void decreaseStock(long productId, int amount);
}
