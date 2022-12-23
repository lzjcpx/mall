package com.tjise.mall.search.service;

import com.tjise.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @auther 刘子敬
 * @create 2022-10-10-16:34
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
