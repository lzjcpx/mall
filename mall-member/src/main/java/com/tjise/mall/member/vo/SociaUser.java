package com.tjise.mall.member.vo;

import lombok.Data;

/**
 * @auther 刘子敬
 * @create 2023-03-03-14:52
 */
@Data
public class SociaUser {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;

}
