package com.tjise.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjise.common.utils.PageUtils;
import com.tjise.mall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:23:39
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

