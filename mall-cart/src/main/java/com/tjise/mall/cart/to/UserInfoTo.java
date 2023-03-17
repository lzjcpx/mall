package com.tjise.mall.cart.to;

import lombok.Data;
import lombok.ToString;

/**
 * @auther 刘子敬
 * @create 2023-03-16-17:05
 */
@Data
@ToString
public class UserInfoTo {

    private Long userId;
    private String userKey;
    private boolean tempUser = false;

}
