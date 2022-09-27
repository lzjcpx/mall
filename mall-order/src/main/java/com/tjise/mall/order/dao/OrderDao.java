package com.tjise.mall.order.dao;

import com.tjise.mall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:23:39
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
