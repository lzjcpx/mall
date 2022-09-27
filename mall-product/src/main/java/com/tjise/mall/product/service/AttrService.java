package com.tjise.mall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjise.common.utils.PageUtils;
import com.tjise.mall.product.entity.AttrEntity;

import java.util.Map;

/**
 * 商品属性
 *
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:08:10
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

