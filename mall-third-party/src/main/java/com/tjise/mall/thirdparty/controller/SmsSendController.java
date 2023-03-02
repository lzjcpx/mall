package com.tjise.mall.thirdparty.controller;

import com.tjise.common.utils.R;
import com.tjise.mall.thirdparty.service.MsmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @auther 刘子敬
 * @create 2023-03-02-14:28
 */
@RestController
@RequestMapping("/sms")
public class SmsSendController {

    @Autowired
    MsmService msmService;

    /**
     * 提供给别的服务进行调用
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone,@RequestParam("code") String code) {
        String[] params = new String[2];
        params[0] = code;
        params[1] = "5";
        msmService.send(params, phone);
        return R.ok();
    }

}
