package com.tjise.mall.search.constant;

/**
 * @auther 刘子敬
 * @create 2022-10-10-16:38
 */
public class EsConstant {

    //sku数据在es中的索引
    public static final String PRODUCT_INDEX = "mall_product";

    public static final Integer PRODUCT_PAGESIZE = 2;//sku数据在es中的索引

    /**
     * 检索的常用常量、用于构建检索请求
     */
    public static final String KEYWORD = "keyword";
    public static final String CATALOG_ID = "catalogId";
    public static final String HAS_STOCK = "hasStock";
    public static final String SKU_PRICE = "skuPrice";
    public static final String BRAND_ID = "brandId";
    public static final String BRAND_NAME = "brandName";
    public static final String ATTRS = "attrs";
    public static final String BRAND_IMG = "brandImg";
    public static final String SKU_TITLE = "skuTitle";
    public static final String CATALOG_NAME = "catalogName";

    public enum Attrs{
        attrId("attrs.attrId"),
        attrName("attrs.attrName"),
        attrValue("attrs.attrValue");
        Attrs(String s) {

        }
    }

}
