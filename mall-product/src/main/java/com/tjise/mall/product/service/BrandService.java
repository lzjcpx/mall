package com.tjise.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjise.common.utils.PageUtils;
import com.tjise.mall.product.entity.BrandEntity;

import java.util.Map;

/**
 * 品牌
 *
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-28 14:31:52
 */
public interface BrandService extends IService<BrandEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

