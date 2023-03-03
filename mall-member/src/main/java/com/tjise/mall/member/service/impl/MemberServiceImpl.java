package com.tjise.mall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tjise.common.utils.HttpUtils;
import com.tjise.mall.member.dao.MemberLevelDao;
import com.tjise.mall.member.entity.MemberLevelEntity;
import com.tjise.mall.member.exception.PhoneExsistException;
import com.tjise.mall.member.exception.UsernameExistException;
import com.tjise.mall.member.vo.MemberLoginVo;
import com.tjise.mall.member.vo.MemberRegistVo;
import com.tjise.mall.member.vo.SociaUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tjise.common.utils.PageUtils;
import com.tjise.common.utils.Query;

import com.tjise.mall.member.dao.MemberDao;
import com.tjise.mall.member.entity.MemberEntity;
import com.tjise.mall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelDao memberLevelDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void regist(MemberRegistVo vo) {
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = new MemberEntity();

        //设置默认等级
        MemberLevelEntity levelEntity = memberLevelDao.getDefaultLevel();
        entity.setLevelId(levelEntity.getId());

        //检查用户名和手机号是否唯一，为了让controller能感知异常，异常机制
        checkPhone(vo.getPhone());
        checkUserName(vo.getUserName());

        entity.setMobile(vo.getPhone());
        entity.setUsername(vo.getUserName());

        //加密存储，盐值加密，随机值
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        entity.setPassword(encode);

        //保存
        memberDao.insert(entity);
    }

    @Override
    public void checkPhone(String phone) throws PhoneExsistException {
        MemberDao memberDao = this.baseMapper;
        Integer mobile = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (mobile > 0) {
            throw new PhoneExsistException();
        }
    }

    @Override
    public void checkUserName(String userName) throws UsernameExistException {
        MemberDao memberDao = this.baseMapper;
        Integer username = memberDao.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (username > 0) {
            throw new UsernameExistException();
        }
    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();

        //1、去数据库查询
        MemberDao memberDao = this.baseMapper;
        MemberEntity entity = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("username", loginacct)
                .or()
                .eq("mobile", loginacct));
        if (entity == null) {
            //登录失败
            return null;
        } else {
            //1、获取到数据库的password
            String passwordDb = entity.getPassword();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            //2、密码匹配
            boolean matches = passwordEncoder.matches(password, passwordDb);
            if (matches) {
                return entity;
            } else {
                return null;
            }
        }
    }

    @Override
    public MemberEntity login(SociaUser vo) throws Exception {
        //登录和注册合并逻辑
        String uid = vo.getUid();
        //1、判断当前用户是否已经登录过系统
        MemberDao memberDao = this.baseMapper;
        MemberEntity member = memberDao.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (member != null) {
            //这个用户已经注册
            MemberEntity update = new MemberEntity();
            update.setId(member.getId());
            update.setAccessToken(vo.getAccess_token());
            update.setExpiresIn(vo.getExpires_in());
            memberDao.updateById(update);

            member.setAccessToken(vo.getAccess_token());
            member.setExpiresIn(vo.getExpires_in());
            return member;
        } else {
            //2、没有查到当前社交用户对应的记录，我们需要注册
            MemberEntity regist = new MemberEntity();
            try {
                //3、查出当前社交用户的社交账号信息（昵称、性别等）
                Map<String, String> query = new HashMap<>();
                query.put("access_token", vo.getAccess_token());
                query.put("uid", vo.getUid());
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<String, String>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    //查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);
                    //昵称
                    String name = jsonObject.getString("name");
                    String gender = jsonObject.getString("gender");
                    //......
                    regist.setNickname(name);
                    regist.setGender("m".equals(gender) ? 1 : 0);
                    //......
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            regist.setSocialUid(vo.getUid());
            regist.setAccessToken(vo.getAccess_token());
            regist.setExpiresIn(vo.getExpires_in());
            memberDao.insert(regist);
            return regist;
        }
    }

}