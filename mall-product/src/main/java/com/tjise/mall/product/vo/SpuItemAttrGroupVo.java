package com.tjise.mall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @auther 刘子敬
 * @create 2023-02-24-18:21
 */
@ToString
@Data
public class SpuItemAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}
