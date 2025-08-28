package com.ai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.Exception.BusinessException;
import com.ai.Exception.ErrorCode;
import com.ai.Exception.ThrowUtils;
import com.ai.ai.builder.VueProjectBuilder;
import com.ai.ai.core.AiCodeGeneratorFacade;
import com.ai.ai.core.StreamHandlerExecutor;
import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.ai.service.AiCodeGeneratorService;
import com.ai.ai.service.AiSmartRouterGeneratorService;
import com.ai.common.ResultPage;
import com.ai.contant.AppConstant;
import com.ai.mapper.AppMapper;
import com.ai.model.dto.app.*;
import com.ai.model.entity.App;
import com.ai.model.enums.MessageTypeEnum;
import com.ai.model.vo.app.AppVO;
import com.ai.model.vo.user.UserVO;
import com.ai.service.AppService;
import com.ai.service.ChatHistoryService;
import com.ai.service.ScreenshotService;
import com.ai.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 惘念
 * @description 针对表【app(应用)】的数据库操作Service实现
 * @createDate 2025-08-22 15:38:10
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>
        implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private AiSmartRouterGeneratorService aiSmartRouterGeneratorService;

    @Override
    public AppVO createApp(AppCreateRequest request, Long loginUserId) {
        if (request == null || loginUserId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        if (StrUtil.isBlank(request.getInitPrompt())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StrUtil.isBlank(request.getAppName())) {
            request.setAppName("新建应用");
        }
        // ai智能选择生成模式
        CodeGenTypeEnum codeGenTypeEnum = aiSmartRouterGeneratorService.smartRouterSelect(request.getInitPrompt());
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.SYSTEM_ERROR, "ai对话初始化失败");
        ThrowUtils.throwIf(CodeGenTypeEnum.getEnumByValue(codeGenTypeEnum.getValue()) == null, ErrorCode.SYSTEM_ERROR,  "ai对话初始化失败");
        App app = new App();
        app.setAppName(request.getAppName());
        app.setInitPrompt(request.getInitPrompt());
        app.setCodeType(codeGenTypeEnum.getValue());
        app.setUserId(loginUserId);
        app.setCreateTime(new Date());
        app.setUpdateTime(new Date());
        boolean saved = this.save(app);
        ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "创建失败");
        AppVO appVO = BeanUtil.copyProperties(app, AppVO.class);
        return appVO;
    }

    @Override
    public void updateMyAppById(AppUpdateByUserRequest request, Long loginUserId) {
        if (request == null || request.getId() == null || loginUserId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        App exist = this.getById(request.getId());
        ThrowUtils.throwIf(exist == null, ErrorCode.SYSTEM_ERROR, "应用不存在");
        if (!loginUserId.equals(exist.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (StrUtil.isBlank(request.getAppName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        App update = new App();
        update.setId(exist.getId());
        update.setAppName(request.getAppName());
        update.setEditTime(new Date());
        update.setUpdateTime(new Date());
        boolean ok = this.updateById(update);
        ThrowUtils.throwIf(!ok, ErrorCode.SYSTEM_ERROR, "修改失败");
    }

    @Override
    public void removeMyAppById(Long appId, Long loginUserId) {
        if (appId == null || loginUserId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        App exist = this.getById(appId);
        ThrowUtils.throwIf(exist == null, ErrorCode.SYSTEM_ERROR, "应用不存在");
        if (!loginUserId.equals(exist.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 先删除关联对话历史
        chatHistoryService.removeByAppId(appId);
        boolean ok = this.removeById(appId);
        ThrowUtils.throwIf(!ok, ErrorCode.SYSTEM_ERROR, "删除失败");
    }

    @Override
    public AppVO getAppDetailById(Long appId, Long loginUserId) {
        if (appId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.SYSTEM_ERROR, "应用不存在");
        // 用户能看自己的；如后续有公开逻辑再扩展
        if (loginUserId != null && !loginUserId.equals(app.getUserId())) {
            // 允许查看非自己的详情，需求未限制仅本人；如需限制，可开启下面一行
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return BeanUtil.copyProperties(app, AppVO.class);
    }

    @Override
    public ResultPage<AppVO> pageMyApps(AppPageMyRequest request, Long loginUserId) {
        if (request == null || loginUserId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Integer pageNo = ObjectUtil.defaultIfNull(request.getPageNo(), 1);
        Integer pageSize = ObjectUtil.defaultIfNull(request.getPageSize(), 10);
        if (pageSize > 20) {
            pageSize = 20;
        }
        QueryWrapper<App> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", loginUserId);
        if (StrUtil.isNotBlank(request.getAppName())) {
            wrapper.like("app_name", request.getAppName());
        }
        wrapper.orderByDesc("update_time");
        Page<App> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<AppVO> list = BeanUtil.copyToList(page.getRecords(), AppVO.class);
        // 封装userVO
        List<AppVO> appVOS = appToVO(list);
        ResultPage<AppVO> rp = new ResultPage<>();
        rp.setData(appVOS);
        rp.setTotal(page.getTotal());
        return rp;
    }

    @Override
    public ResultPage<AppVO> pageFeaturedApps(AppPageRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Integer pageNo = ObjectUtil.defaultIfNull(request.getPageNo(), 1);
        Integer pageSize = ObjectUtil.defaultIfNull(request.getPageSize(), 10);
        if (pageSize > 20) {
            pageSize = 20;
        }
        QueryWrapper<App> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(request.getAppName())) {
            wrapper.like("app_name", request.getAppName());
        }
        if (request.getUserId() != null) {
            wrapper.eq("user_id", request.getUserId());
        }
        if (request.getCodeType() != null) {
            wrapper.eq("code_type", request.getCodeType());
        }
        // 以优先级倒序作为精选排序
        wrapper.orderByDesc("priority").orderByDesc("update_time");
        Page<App> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<AppVO> list = BeanUtil.copyToList(page.getRecords(), AppVO.class);
        List<AppVO> appVOS = appToVO(list);
        ResultPage<AppVO> rp = new ResultPage<>();
        rp.setData(appVOS);
        rp.setTotal(page.getTotal());
        return rp;
    }

    @Override
    public void removeAppByAdmin(Long appId) {
        ThrowUtils.throwIf(appId == null, ErrorCode.NULL_ERROR);
        // 先删除关联对话历史
        chatHistoryService.removeByAppId(appId);
        boolean ok = this.removeById(appId);
        ThrowUtils.throwIf(!ok, ErrorCode.SYSTEM_ERROR, "删除失败");
    }

    @Override
    public void updateAppByAdmin(AppAdminUpdateRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null, ErrorCode.NULL_ERROR);
        App exist = this.getById(request.getId());
        ThrowUtils.throwIf(exist == null, ErrorCode.SYSTEM_ERROR, "应用不存在");
        App update = new App();
        update.setId(request.getId());
        if (StrUtil.isNotBlank(request.getAppName())) {
            update.setAppName(request.getAppName());
        }
        if (StrUtil.isNotBlank(request.getCover())) {
            update.setCover(request.getCover());
        }
        if (request.getPriority() != null) {
            update.setPriority(request.getPriority());
        }
        update.setUpdateTime(new Date());
        boolean ok = this.updateById(update);
        ThrowUtils.throwIf(!ok, ErrorCode.SYSTEM_ERROR, "更新失败");
    }

    @Override
    public ResultPage<AppVO> pageAppsByAdmin(AppPageRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.NULL_ERROR);
        Integer pageNo = ObjectUtil.defaultIfNull(request.getPageNo(), 1);
        Integer pageSize = ObjectUtil.defaultIfNull(request.getPageSize(), 10);
        QueryWrapper<App> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(request.getAppName())) {
            wrapper.like("app_name", request.getAppName());
        }
        if (request.getUserId() != null) {
            wrapper.eq("user_id", request.getUserId());
        }
        if (request.getPriority() != null) {
            wrapper.eq("priority", request.getPriority());
        }
        if (StrUtil.isNotBlank(request.getCodeType())) {
            wrapper.eq("code_type", request.getCodeType());
        }
        wrapper.orderByDesc("update_time");
        Page<App> page = this.page(new Page<>(pageNo, pageSize), wrapper);
        List<AppVO> list = BeanUtil.copyToList(page.getRecords(), AppVO.class);
        List<AppVO> appVOS = appToVO(list);
        ResultPage<AppVO> rp = new ResultPage<>();
        rp.setData(appVOS);
        rp.setTotal(page.getTotal());
        return rp;
    }

    @Override
    public AppVO getAppDetailByAdmin(Long appId) {
        ThrowUtils.throwIf(appId == null, ErrorCode.NULL_ERROR);
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.SYSTEM_ERROR, "应用不存在");
        return BeanUtil.copyProperties(app, AppVO.class);
    }

    @Override
    public Flux<String> chatGenerateCode(AppChatRequest request, Long loginUserId) {
        App app = this.getById(request.getId());
        ThrowUtils.throwIf(app == null, ErrorCode.SYSTEM_ERROR, "该应用不存在");
        // 仅应用创建者可触发对话生成
        if (!app.getUserId().equals(loginUserId)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 1) 记录用户消息
        chatHistoryService.saveChatMessage(app.getId(), loginUserId, request.getMessage(), MessageTypeEnum.USER.getValue());
        // 2) 调用 AI 生成
        Flux<String> streamFlux = aiCodeGeneratorFacade.generateAndSaveCode(request.getMessage(),
                CodeGenTypeEnum.getEnumByValue(request.getCodeType()), request.getId());
         // 3) 累积 AI 回复，成功后保存；若出错，保存错误信息
        return streamHandlerExecutor.executeHandler(streamFlux, chatHistoryService, request.getId(), loginUserId, CodeGenTypeEnum.getEnumByValue(request.getCodeType()));
//        StringBuilder aiReply = new StringBuilder();
//        return stream.map(chunk -> {
//                    aiReply.append(chunk);
//                    return chunk;
//                })
//                .doOnError(error -> chatHistoryService.saveChatMessage(app.getId(), loginUserId, error.getMessage(), MessageTypeEnum.ERROR.getValue()))
//                .doOnComplete(() -> chatHistoryService.saveChatMessage(app.getId(), loginUserId, aiReply.toString(), MessageTypeEnum.AI.getValue()));
    }

    @Override
    public String deployWeb(Long id, UserVO userVO, CodeGenTypeEnum codeGenTypeEnum) {
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.SYSTEM_ERROR, "该应用不存在");
        // 校验是否是本人
        if (!app.getUserId().equals(userVO.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 校验是否是第一次部署
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(8);
        }
        // 构建资源路径
        String fileName = app.getCodeType() + "_" + id;
        String resourcesPath = AppConstant.CODE_OUT_DIR + File.separator + fileName;
        // 检查目标资源是否存在
        File file = new File(resourcesPath);
        if (!file.exists() || !file.isDirectory()) {
            // 资源不存在
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "资源不存在, 请先生成生成应用");
        }
        // 文件生成路径
        String deployDir = AppConstant.CODE_DEPLOY_DIR + File.separator + deployKey;
        // vue项目特殊处理
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            boolean b = vueProjectBuilder.buildProject(resourcesPath);
            ThrowUtils.throwIf(!b, ErrorCode.PROJECT_BUILD_ERROR, "项目构建失败, 请见检查代码依赖");
            // 检查dist目录是否存在
            File file1 = new File(resourcesPath, "dist");
            ThrowUtils.throwIf(!file1.exists(), ErrorCode.PROJECT_BUILD_ERROR, "项目未完成构建");
            file = file1;
        }
        try {
            FileUtil.copyContent(file, new File(deployDir), true);
        } catch (Exception e) {
            log.error("部署失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败");
        }
        // 获取应用封面并更新数据库
        String appUrl = String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
        generateAppScreenshotAsync(id, appUrl);
        return appUrl;
    }

    /**
     * 虚拟线程异步执行
     * @param appId 应用id
     * @param appUrl 应用部署路由
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        // 使用虚拟线程异步执行
        Thread.startVirtualThread(() -> {
            // 调用截图
            String imageUrl = screenshotService.generateScreenshot(appUrl);
            App app = new App();
            app.setId(appId);
            app.setDeployedTime(new Date());
            app.setDeployKey(appUrl);
            app.setCover(imageUrl);
            boolean b = this.saveOrUpdate(app);
            ThrowUtils.throwIf(!b, ErrorCode.SYSTEM_ERROR, "部署失败");
        });
    }

    /**
     * 补充userVO信息
     */
    private List<AppVO> appToVO(List<AppVO> appList) {
        return appList.stream().peek(appVO -> {
            UserVO userDetailById = userService.getUserDetailById(appVO.getUserId());
            ThrowUtils.throwIf(userDetailById == null, ErrorCode.SYSTEM_ERROR, "关联用户为空");
            appVO.setUserVO(userDetailById);
        }).collect(Collectors.toList());
    }
}
