package com.tjise.mall.search.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装页面所有可能传递过来的条件
 *
 * catalog3Id=223&keyword=小米&sort=asleCount_asc&hasStock=0/1&brandId=1&brandId=2&attrs=1_其他:安卓&attrs=2_5寸:6寸
 * @auther 刘子敬
 * @create 2023-01-04-11:44
 */
@Data
public class SearchParam {

    private String keyword;//页面传递过来的全文匹配关键字

    private Long catalog3Id;//三级分类id

    /**
     * sort=asleCount_asc/desc
     * sort=skuPrice_asc/desc
     * sort=hotScore_asc/desc
     */
    private String sort;

    /**
     * 好多的过滤条件
     * hasStock（是否有货）、skuPrice、brandId、catalog3Id、attrs
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     * brandId=1
     */
    private Integer hasStock;//是否只显示有货  (0无库存，1有库存)

    private String skuPrice;//价格区间排序

    private List<Long> brandId;//品牌id进行查询

    private List<String> attrs;//属性查询

    private Integer pageNum = 1;//页码

    private String _queryString;//原生的所有查询条件

}
