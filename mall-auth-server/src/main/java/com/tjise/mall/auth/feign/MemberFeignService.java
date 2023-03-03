package com.tjise.mall.auth.feign;

import com.tjise.common.utils.R;
import com.tjise.mall.auth.vo.SociaUser;
import com.tjise.mall.auth.vo.UserLoginVo;
import com.tjise.mall.auth.vo.UserRegistVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @auther 刘子敬
 * @create 2023-03-02-18:33
 */
@FeignClient("mall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/regist")
    R regist(@RequestBody UserRegistVo vo);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    R oauthLogin(@RequestBody SociaUser vo) throws Exception;

}
