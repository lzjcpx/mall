package com.tjise.mall.member.dao;

import com.tjise.mall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:17:28
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
