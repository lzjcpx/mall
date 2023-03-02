package com.tjise.mall.member.exception;

/**
 * @auther 刘子敬
 * @create 2023-03-02-18:05
 */
public class UsernameExistException extends RuntimeException {
    public UsernameExistException() {
        super("用户名已存在");
    }
}
