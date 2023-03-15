package com.tjise.mall.cart.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 整个购物车
 * 需要计算的属性，必须重写他的get方法，保证每次获取属性都会计算
 * @auther 刘子敬
 * @create 2023-03-15-18:38
 */
public class Cart {

    private List<CartItem> Items;
    private Integer countNum;
    private Integer countType;
    private BigDecimal totalAmount;
    private BigDecimal reduce = new BigDecimal(0);

    public List<CartItem> getItems() {
        return Items;
    }

    public void setItems(List<CartItem> items) {
        Items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if (Items != null && Items.size() > 0) {
            for (CartItem item : Items) {
                countNum += item.getCount();
            }
        }
        return count;
    }

    public Integer getCountType() {
        int count = 0;
        if (Items != null && Items.size() > 0) {
            for (CartItem item : Items) {
                countNum += 1;
            }
        }
        return countType;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        //1、计算购物项总价
        if (Items != null && Items.size() > 0) {
            for (CartItem item : Items) {
                BigDecimal totalPrice = item.getTotalPrice();
                amount = amount.add(totalPrice);
            }
        }

        //2、减去优惠总价
        BigDecimal subtract = amount.subtract(getReduce());

        return subtract;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }
}
