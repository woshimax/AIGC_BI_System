package com.yupi.springbootinit.bizmq;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.rabbitmq.client.Channel;
import com.yupi.springbootinit.api.QianfanAiApi;
import com.yupi.springbootinit.common.BaseResponse;
import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.common.ResultUtils;
import com.yupi.springbootinit.exception.BusinessException;
import com.yupi.springbootinit.exception.ThrowUtils;
import com.yupi.springbootinit.model.dto.chart.GenChartByAiRequest;
import com.yupi.springbootinit.model.entity.Chart;
import com.yupi.springbootinit.model.entity.User;
import com.yupi.springbootinit.model.vo.BiResponse;
import com.yupi.springbootinit.service.ChartService;
import com.yupi.springbootinit.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class BiConsumer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private QianfanAiApi qianfanAiApi;
    @Resource
    private ChartService chartService;
    @RabbitListener(queues = {BiConstant.QUEUE_NAME},ackMode = "MANUAL")//这个注解会帮我们自动填充msg,channel和delivery对象;指定监听的消息队列
    public void receiveMessage(String promptWithChartId, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG)long deliveryTag) throws IOException {
        log.info("receive message = {}",promptWithChartId);
        //先拆分数据为id和预设
        String[] msg = promptWithChartId.split("_");
        if(StrUtil.isBlank(msg[0])){
            channel.basicNack(deliveryTag,false,false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"系统错误");
        }
        Long chartId = Long.valueOf(msg[0]);
        StringBuilder userInput = new StringBuilder(msg[1]);
        Chart chart = chartService.getById(chartId);
        //预设拼接用户输入及数据——其中分析需求和原始数据利用id从数据库中查——减少队列消息大小
        userInput.append("分析需求：").append("\n");
        userInput.append(chart.getGoal()).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(chart.getChartData()).append("\n");
        //修改状态为running
        Chart updateChart = new Chart();
        updateChart.setId(chart.getId());
        updateChart.setStatus("running");
        boolean isUpdate = chartService.updateById(updateChart);
        if(!isUpdate){
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"更新图表信息失败");
            return;
        }
        //喂给ai
        String result = null;
        try {
            result = qianfanAiApi.doChat(userInput.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //处理一下返回的“数据分析结果”
        //使用split规整的返回ai的输出
        String[] split = result.split("【【【【【【");

        if(split.length < 3){
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"AI生成错误");
            return;
        }
        String genChart = split[1];
        String genResult = split[2];
        //处理\\n
        genChart = genChart.replace("\\n","\n");
        genResult = genResult.replace("\\n","");
        //处理掉genResult后面的元数据
        int index = genResult.indexOf("\"");
        // 如果找到"，则截取"之前的字符串
        if (index != -1) {
            genResult = genResult.substring(0, index);
        }
        //再次更新状态，设置生成后的图表信息和结论信息
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chart.getId());
        updateChartResult.setGenChart(genChart);
        updateChartResult.setGenResult(genResult);
        //todo 定义状态为枚举

        updateChartResult.setStatus("succeed");
        boolean isUpdateResult = chartService.updateById(updateChartResult);
        if(!isUpdateResult){
            //出现问题，要给一个nack
            channel.basicNack(deliveryTag,false,false);
            handleChartUpdateError(chart.getId(),"更新结果失败");

        }

        channel.basicAck(deliveryTag,false);
    }

    private void handleChartUpdateError(Long chartId,String execMessage){
        //更新数据库状态为失败，并且抛出异常
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage(execMessage);
        boolean suc = chartService.updateById(updateChart);
        if(!suc){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"处理更新错误的更新操作异常");
        }
    }
}
