package com.tjise.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @auther 刘子敬
 * @create 2022-10-09-16:26
 */
@Data
public class SkuEsModel {

    private Long skuId;
    private Long spuId;
    private String skuTitle;
    private BigDecimal skuPrice;
    private String skuImg;
    private Long saleCount;     // 商品的销量
    private boolean hasStock;   // 商品的库存
    private Long hotScore;      // 热度评分
    private Long brandId;       // 品牌ID
    private Long catalogId;     // 分类ID
    private String brandName;   // 品牌名
    private String brandImg;    // 品牌图片
    private String catalogName; //分类的名字
    private List<Attrs> attrs;

    /**
     * 商品规格
     */
    @Data
    public static class Attrs{
        private Long attrId;       // 属性id
        private String attrName;    // 属性名
        private String attrValue;   // 属性值
    }

}
