package com.woshimax.bi_service.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.woshimax.bi_service.annotation.AuthCheck;
import com.woshimax.bi_service.api.QianfanAiApi;
import com.woshimax.bi_service.bizmq.BiProducer;
import com.woshimax.bi_service.common.BaseResponse;
import com.woshimax.bi_service.common.DeleteRequest;
import com.woshimax.bi_service.common.ErrorCode;
import com.woshimax.bi_service.common.ResultUtils;
import com.woshimax.bi_service.constant.CommonConstant;
import com.woshimax.bi_service.constant.UserConstant;
import com.woshimax.bi_service.exception.BusinessException;
import com.woshimax.bi_service.exception.ThrowUtils;
import com.woshimax.bi_service.manager.RedisLimiterManager;
import com.woshimax.bi_service.model.dto.chart.*;
import com.woshimax.bi_service.model.entity.Chart;
import com.woshimax.bi_service.model.entity.User;
import com.woshimax.bi_service.model.vo.BiResponse;
import com.woshimax.bi_service.service.ChartService;
import com.woshimax.bi_service.service.UserService;
import com.woshimax.bi_service.totest.CSVToListMap;
import com.woshimax.bi_service.utils.ExcelUtils;
import com.woshimax.bi_service.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private CSVToListMap csvToListMap;
    @Resource
    private ChartService chartService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private UserService userService;
    @Resource
    private QianfanAiApi qianfanAiApi;
    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Resource
    private BiProducer biProducer;
    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }


    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }


    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }

        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        String name = chartQueryRequest.getName();
        Long id = chartQueryRequest.getId();
        String chartType = chartQueryRequest.getChartType();
        String goal = chartQueryRequest.getGoal();
        Long userId = chartQueryRequest.getUserId();

        // 拼接查询条件
        queryWrapper.eq(id != null && id>0,"id",id);
        queryWrapper.like(StringUtils.isNotBlank(name),"name",name);
        queryWrapper.eq(StringUtils.isNotBlank(goal),"goal",goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType),"chartType",chartType);

        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete",false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 智能分析（根据用户上传文件分析数据）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiResponse> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws JSONException, IOException {

        //获取用户输入
        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();
        String chartType = genChartByAiRequest.getChartType();

        //拿到用户输入就是做数据校验先
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100,ErrorCode.PARAMS_ERROR,"名称太长");

        //文件校验逻辑——上传功能必做
        //1、大小
        long size = multipartFile.getSize();
        final long ONE_MB = 1024*1024L;
        ThrowUtils.throwIf(size > ONE_MB,ErrorCode.PARAMS_ERROR,"文件过大");
        //2、后缀
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        List<String> standardSuffix = Arrays.asList("xls", "xlsx");
        ThrowUtils.throwIf(!standardSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀非法");

        //限流判断-每个用户针对这个方法一个限流器
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());
        //方法一：写一个ai的prompt，这是针对最原始的ai接口：（如国外的openai，国内的百度千帆等）
        //这种prompt需要写样例，格式，等等，以期待ai的回复能满足我们的要求
        //读取用户上传的excel文件，进行处理
        String res = ExcelUtils.excelToCsv(multipartFile);
        //将目标，类别等文字进行区分以及拼接——这个userInput就是要传给ai接口进行生成的输入
        StringBuilder userInput = new StringBuilder();
        //预设
        userInput.append("你是一个数据分析刊师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求和目标}\n" +
                "原始数据:\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【【\n" +
                "{前端Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化}\n" +
                "【【【【【【\n" +
                "{ 明确的数据分析结论、越详细越好，不要生成多余的注释或代码}\n");
        //示例
        userInput.append("示例：\n");
        userInput.append("【【【【【【\n");
        userInput.append("option ={\n" +
                "xAxis: {\n" +
                "type: 'category',\n" +
                "data: ['1', '2', '3']\n" +
                "},\n" +
                "yAxis: {\n" +
                "type: 'value'\n" +
                "},\n" +
                "series: [{\n" +
                "data: [10, 20, 30],\n" +
                "type: 'line'\n" +
                "}]\n" +
                "}").append("\n");
        userInput.append("【【【【【【\n");
        userInput.append("根据提供的数据，网站用户增长情况呈现出稳定的上升趋势。从数据可以看出，第一天用户人数为10人，第二天增长到20人，第三天继续增长到30人。这表明网站在这段时间内吸引了越来越多的用户。").append("\n");
        //拼接用户输入及数据
        userInput.append("分析需求：").append("\n");
        userInput.append(goal).append("\n");
        userInput.append("原始数据：").append("\n");
        userInput.append(res).append("\n");

        System.out.println(userInput.toString());

        //TODO:方法二：使用sdk，里面有丰富的ai模型，不需要prompt——这种直接传入要求即可——鱼聪明ai是有的


        //异步化：step1-将任务存入数据库
        //存入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(res);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());

        boolean isSave = chartService.save(chart);
        if(isSave == false){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"未能存入数据库");
        }

        //将ai生成异步化
        //todo 给任务添加超时时间，超时自动设置为失败
        CompletableFuture.runAsync(()->{
            //修改状态为running
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean isUpdate = chartService.updateById(updateChart);
            if(!isUpdate){
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
            boolean isUpdateResult = chartService.updateById(updateChart);
            if(!isUpdateResult){
                handleChartUpdateError(chart.getId(),"更新结果失败");

            }

        },threadPoolExecutor);

        //作为封装进vo返回前端
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);

    }
    @PostMapping("data")
    public void getChartData(Long chartId) throws IOException {
        long startTime = System.currentTimeMillis();
        Chart chart = chartService.getById(chartId);
        csvToListMap.csvToListMap(chart.getChartData());
        long endTime = System.currentTimeMillis();
        log.info("分表之前查询时间（处理成表格）：{}",endTime - startTime);
        long startTime1 = System.currentTimeMillis();
        chartService.getChartData("chart_" + chartId);
        long endTime1 = System.currentTimeMillis();
        log.info("分表之后查询时间（不包括处理成表格）：{}",endTime1 - startTime1);
    }
    /**
     * 智能分析（根据用户上传文件分析数据）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<BiResponse> genChartByAiMq(@RequestPart("file") MultipartFile multipartFile,
                                                 GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) throws JSONException, IOException {

        //获取用户输入
        String goal = genChartByAiRequest.getGoal();
        String name = genChartByAiRequest.getName();
        String chartType = genChartByAiRequest.getChartType();

        //拿到用户输入就是做数据校验先
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.PARAMS_ERROR,"目标为空");
        ThrowUtils.throwIf(StringUtils.isBlank(name) && name.length() > 100,ErrorCode.PARAMS_ERROR,"名称太长");

        //文件校验逻辑——上传功能必做
        //1、大小
        long size = multipartFile.getSize();
        final long ONE_MB = 1024*1024L;
        ThrowUtils.throwIf(size > ONE_MB,ErrorCode.PARAMS_ERROR,"文件过大");
        //2、后缀
        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        List<String> standardSuffix = Arrays.asList("xls", "xlsx");
        ThrowUtils.throwIf(!standardSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件后缀非法");

        //限流判断-每个用户针对这个方法一个限流器
        User loginUser = userService.getLoginUser(request);
        redisLimiterManager.doRateLimit("genChartByAi_"+loginUser.getId());
        //方法一：写一个ai的prompt，这是针对最原始的ai接口：（如国外的openai，国内的百度千帆等）
        //这种prompt需要写样例，格式，等等，以期待ai的回复能满足我们的要求
        //读取用户上传的excel文件，进行处理
        String res = ExcelUtils.excelToCsv(multipartFile);
        //将目标，类别等文字进行区分以及拼接——这个userInput就是要传给ai接口进行生成的输入
        StringBuilder userInput = new StringBuilder();
        //预设
        userInput.append("你是一个数据分析刊师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求和目标}\n" +
                "原始数据:\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，按照以下格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【【\n" +
                "{前端Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化}\n" +
                "【【【【【【\n" +
                "{ 明确的数据分析结论、越详细越好，不要生成多余的注释或代码}\n");
        //示例
        userInput.append("示例：\n");
        userInput.append("【【【【【【\n");
        userInput.append("option ={\n" +
                "xAxis: {\n" +
                "type: 'category',\n" +
                "data: ['1', '2', '3']\n" +
                "},\n" +
                "yAxis: {\n" +
                "type: 'value'\n" +
                "},\n" +
                "series: [{\n" +
                "data: [10, 20, 30],\n" +
                "type: 'line'\n" +
                "}]\n" +
                "}").append("\n");
        userInput.append("【【【【【【\n");
        userInput.append("根据提供的数据，网站用户增长情况呈现出稳定的上升趋势。从数据可以看出，第一天用户人数为10人，第二天增长到20人，第三天继续增长到30人。这表明网站在这段时间内吸引了越来越多的用户。").append("\n");



        //TODO:方法二：使用sdk，里面有丰富的ai模型，不需要prompt——这种直接传入要求即可——鱼聪明ai是有的


        //异步化：step1-将任务存入数据库
        //存入数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(res);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());

        boolean isSave = chartService.save(chart);
        if(isSave == false){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"未能存入数据库");
        }

        Long chartId = chart.getId();

        String message = chartId+"_"+userInput;
        //发消息给消息队列，然后消费者中嵌套处理程序（ai生成）
        biProducer.sentMessage(message);

        //作为封装进vo返回前端
        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        return ResultUtils.success(biResponse);
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
