package com.tjise.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjise.common.utils.PageUtils;
import com.tjise.mall.product.entity.SkuSaleAttrValueEntity;

import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:08:10
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

