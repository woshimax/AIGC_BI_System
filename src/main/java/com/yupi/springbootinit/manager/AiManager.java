package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


//测试方法：alt+enter，选择测试（junit5）
@Service
public class AiManager {
    //写一个AiManager类，用来对接第三方接口
    @Resource
    private YuCongMingClient yuCongMingClient;

    public String doChat(long modelId,String msg){
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(modelId);
        //这就是和ai交互时传入的msg
        devChatRequest.setMessage(msg);
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if(response == null){
            //编码习惯：判空
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI响应错误");
        }
        return response.getData().getContent();

    }

}
