package com.tjise.mall.cart.service;

import com.tjise.mall.cart.vo.Cart;
import com.tjise.mall.cart.vo.CartItem;

import java.util.concurrent.ExecutionException;

/**
 * @auther 刘子敬
 * @create 2023-03-16-16:31
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getcart() throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer check);
}
