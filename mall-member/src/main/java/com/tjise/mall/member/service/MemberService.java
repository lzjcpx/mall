package com.tjise.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjise.common.utils.PageUtils;
import com.tjise.mall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author liuzijing
 * @email liuzijing@qq.com
 * @date 2022-09-27 18:17:28
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

