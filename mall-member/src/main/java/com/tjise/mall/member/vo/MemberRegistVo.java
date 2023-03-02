package com.tjise.mall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @auther 刘子敬
 * @create 2023-03-02-16:46
 */
@Data
public class MemberRegistVo {

    private String userName;
    private String password;
    private String phone;

}
