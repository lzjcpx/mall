package com.tjise.mall.thirdparty.service.impl;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.tjise.mall.thirdparty.config.SmsProperties;
import com.tjise.mall.thirdparty.service.MsmService;
import org.springframework.stereotype.Service;
import javax.xml.ws.http.HTTPException;
import java.io.IOException;

/**
 * @auther shkstart
 * @create 2021-06-22-17:02
 */
@Service
public class MsmServiceImpl implements MsmService {
    @Override
    public boolean send(String[] params, String phone) {
        try {
            SmsSingleSender ssender = new SmsSingleSender(SmsProperties.SDKAPPID, SmsProperties.SDKAPPKEY);
            SmsSingleSenderResult result = ssender.sendWithParam("86", phone, SmsProperties.TEMPLATEDID, params, "TJISE项目实训", "", "");
            if (result.result == 0){
                return false;
            }
        }catch (HTTPException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }


}
