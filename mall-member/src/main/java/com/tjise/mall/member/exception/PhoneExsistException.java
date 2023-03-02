package com.tjise.mall.member.exception;

/**
 * @auther 刘子敬
 * @create 2023-03-02-18:05
 */
public class PhoneExsistException extends RuntimeException {
    public PhoneExsistException() {
        super("手机号已存在");
    }
}
