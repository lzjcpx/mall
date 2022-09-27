package com.tjise.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjise.common.utils.PageUtils;
import com.tjise.mall.ware.entity.PurchaseEntity;

import java.util.Map;

/**
 * 采购信息
 *
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:25:40
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

