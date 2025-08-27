package com.ai.service;

import com.ai.ai.enums.CodeGenTypeEnum;
import com.ai.common.ResultPage;
import com.ai.model.dto.app.*;
import com.ai.model.entity.App;
import com.ai.model.vo.app.AppVO;
import com.ai.model.vo.user.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import reactor.core.publisher.Flux;

/**
 * @author 惘念
 * @description 针对表【app(应用)】的数据库操作Service
 * @createDate 2025-08-22 15:38:10
 */
public interface AppService extends IService<App> {

    /** 用户创建应用（需填写 initPrompt） */
    Long createApp(AppCreateRequest request, Long loginUserId);

    /** 用户按 id 修改自己的应用（仅名称） */
    void updateMyAppById(AppUpdateByUserRequest request, Long loginUserId);

    /** 用户按 id 删除自己的应用 */
    void removeMyAppById(Long appId, Long loginUserId);

    /** 用户按 id 查看自己的应用详情（或公开详情） */
    AppVO getAppDetailById(Long appId, Long loginUserId);

    /** 用户分页查自己的应用（按名筛，每页最多20） */
    ResultPage<AppVO> pageMyApps(AppPageMyRequest request, Long loginUserId);

    /** 分页查询精选应用（按名筛，每页最多20，采用优先级倒序） */
    ResultPage<AppVO> pageFeaturedApps(AppPageRequest request);

    /** 管理员删除任意应用 */
    void removeAppByAdmin(Long appId);

    /** 管理员更新任意应用（名称、封面、优先级） */
    void updateAppByAdmin(AppAdminUpdateRequest request);

    /** 管理员分页查询（支持除时间外任意字段） */
    ResultPage<AppVO> pageAppsByAdmin(AppPageRequest request);

    /** 管理员根据 id 查看应用详情 */
    AppVO getAppDetailByAdmin(Long appId);

    /** 对话生成页面（会记录聊天历史） */
    Flux<String> chatGenerateCode(AppChatRequest request, Long loginUserId);

    /** 一键部署 */
    String deployWeb(Long id, UserVO userVO, CodeGenTypeEnum codeGenTypeEnum);

    void generateAppScreenshotAsync(Long appId, String appUrl);
}
