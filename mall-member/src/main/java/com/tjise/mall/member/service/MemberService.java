package com.tjise.mall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tjise.common.utils.PageUtils;
import com.tjise.mall.member.entity.MemberEntity;
import com.tjise.mall.member.exception.PhoneExsistException;
import com.tjise.mall.member.exception.UsernameExistException;
import com.tjise.mall.member.vo.MemberLoginVo;
import com.tjise.mall.member.vo.MemberRegistVo;
import com.tjise.mall.member.vo.SociaUser;

import java.util.Map;

/**
 * 会员
 *
 * @author LZJ
 * @email 240582597@qq.com
 * @date 2019-10-08 09:47:05
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void regist(MemberRegistVo vo);

    void checkPhone(String phone) throws PhoneExsistException;

    void checkUserName(String userName) throws UsernameExistException;

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SociaUser vo) throws Exception;
}

