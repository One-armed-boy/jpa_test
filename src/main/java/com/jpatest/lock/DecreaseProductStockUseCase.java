package com.jpatest.lock;

public interface DecreaseProductStockUseCase {
	void decreaseStock(long productId, int amount);
}
