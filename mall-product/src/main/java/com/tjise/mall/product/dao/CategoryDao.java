package com.tjise.mall.product.dao;

import com.tjise.mall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:08:10
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
