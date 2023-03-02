package com.tjise.mall.thirdparty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @auther shkstart
 * @create 2022-03-11-12:01
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rencent.sms")
public class SmsProperties implements InitializingBean {

    private int templateId;
    private int SDKAppId;
    private String SDKAppKey;

    public static int TEMPLATEDID;
    public static int SDKAPPID;
    public static String SDKAPPKEY;

    @Override
    public void afterPropertiesSet() throws Exception {
        TEMPLATEDID = templateId;
        SDKAPPID = SDKAppId;
        SDKAPPKEY = SDKAppKey;
    }
}
