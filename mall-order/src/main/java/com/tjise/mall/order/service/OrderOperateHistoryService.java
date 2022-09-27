package com.tjise.mall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjise.common.utils.PageUtils;
import com.tjise.mall.order.entity.OrderOperateHistoryEntity;

import java.util.Map;

/**
 * 订单操作历史记录
 *
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:23:39
 */
public interface OrderOperateHistoryService extends IService<OrderOperateHistoryEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

