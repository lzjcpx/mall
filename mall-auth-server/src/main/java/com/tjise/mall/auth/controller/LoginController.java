package com.tjise.mall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.tjise.common.constant.AuthServerConstant;
import com.tjise.common.exception.BizCodeEnume;
import com.tjise.common.utils.R;
import com.tjise.mall.auth.feign.MemberFeignService;
import com.tjise.mall.auth.feign.ThirdPartFeignService;
import com.tjise.mall.auth.util.RandomUtil;
import com.tjise.mall.auth.vo.UserLoginVo;
import com.tjise.mall.auth.vo.UserRegistVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @auther 刘子敬
 * @create 2023-02-27-15:43
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 发送一个请求直接跳转到一个页面
     * Springmvc viewcontroller将请求和页面映射过来
     *
     * @return
     */
    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {

        String redisCode = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long l = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - l < 600000) {
                //60秒内不能再发
                return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        //1、接口防止刷新

        //2、验证码的持久化校验
        String code = RandomUtil.getFourBitRandom();
        String insertRedisCode = code + "_" + System.currentTimeMillis();
        //redis缓存验证码，防止同一个phone在60秒内在此发送
        redisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, insertRedisCode, 5, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code);
        return R.ok();
    }

//    @GetMapping("/login.html")
//    public String loginPage() {
//        return "login";
//    }

    @PostMapping("/regist")
    public String regist(@Valid UserRegistVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()){
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            //如果校验出错，转发到注册页
            return "redirect:http://auth.mymall.com/reg.html";
        }

        //1、校验验证码
        String code = vo.getCode();
        String s = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (!StringUtils.isEmpty(s)) {
            if (code.equals(s.split("_")[0])){
                //验证码通过，删除验证码
                redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
                //真正注册，调用远程服务进行注册
                R r = memberFeignService.regist(vo);
                if (r.getCode() == 0) {
                    return "redirect:http://auth.mymall.com/login.html";
                } else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", r.getData("msg", new TypeReference<String>(){}));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mymall.com/reg.html";
                }
            } else {
                Map<String, String> errors = new HashMap<>();
                errors.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.mymall.com/reg.html";
            }
        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mymall.com/reg.html";
        }
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes) {
        //远程登录
        R login = memberFeignService.login(vo);
        if (login.getCode() == 0) {
            //成功
            return "redirect:http://mymall.com/reg.html";

        } else {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>(){}));
            redirectAttributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.mymall.com/login.html";
        }
    }

}
