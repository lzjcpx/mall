package com.tjise.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjise.common.utils.PageUtils;
import com.tjise.mall.ware.entity.WareSkuEntity;

import java.util.Map;

/**
 * 商品库存
 *
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:25:40
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

