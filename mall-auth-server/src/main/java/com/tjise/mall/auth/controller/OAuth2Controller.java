package com.tjise.mall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tjise.common.utils.HttpUtils;
import com.tjise.common.utils.R;
import com.tjise.mall.auth.feign.MemberFeignService;
import com.tjise.common.vo.MemberRespVo;
import com.tjise.mall.auth.vo.SociaUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理社交登录请求
 * @auther 刘子敬
 * @create 2023-03-03-14:27
 */
@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2.0/weibo/seccess")
    public String weibo(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> querys = new HashMap<>();
        //1、根据code换取accessToken
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "2414812581");
        map.put("client_secret", "d63cb57b71209cd75480f357b97fdcd6");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.mymall.com/oauth2.0/weibo/seccess");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", headers, querys, map);

        //2、处理
        if (200 == response.getStatusLine().getStatusCode()) {
            //获取到了accessToken
            String json = EntityUtils.toString(response.getEntity());
            SociaUser sociaUser = JSON.parseObject(json, SociaUser.class);

            //知道当前是哪个社交用户
            //1、当前用户如果是第一次进入网站，自动注册进来（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员）
            //登录或者注册社交用户
            R oauthLogin = memberFeignService.oauthLogin(sociaUser);
            if (oauthLogin.getCode() == 0) {
                MemberRespVo data = oauthLogin.getData("data", new TypeReference<MemberRespVo>() {
                });
                log.info("登录成功，用户信息:{}", data.toString());
                //登录成功调回首页
                //第一次使用session,命令浏览器保存卡号,JsessionId的cookie
                //子域之间，发卡的时候(指定域名为父域名),即使是子域发卡,父域也可使用
                session.setAttribute("loginUser",data);
                servletResponse.addCookie(new Cookie("JSESSIONID","data"));
                return "redirect:http://mymall.com";
            } else {
                return "redirect:http://auth.mymall.com/login";
            }
        } else {
            return "redirect:http://auth.mymall.com/login";
        }
    }

}
