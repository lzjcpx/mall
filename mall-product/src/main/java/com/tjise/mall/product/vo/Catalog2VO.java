package com.tjise.mall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 二级分类VO
 * @auther 刘子敬
 * @create 2022-12-22-16:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Catalog2VO {

    private String catalog1Id;  //一级父分类id

    private List<Catalog3Vo> catalog3List; //三级子分类

    private String id;

    private String name;

    /**
     * 三级分类VO
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Catalog3Vo{
        private String catalog2Id;
        private String id;
        private String name;
    }

}
