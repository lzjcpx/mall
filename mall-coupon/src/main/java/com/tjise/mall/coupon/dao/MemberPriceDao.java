package com.tjise.mall.coupon.dao;

import com.tjise.mall.coupon.entity.MemberPriceEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品会员价格
 * 
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:12:18
 */
@Mapper
public interface MemberPriceDao extends BaseMapper<MemberPriceEntity> {
	
}
