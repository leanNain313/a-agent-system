package com.ai.model.vo.app;

import com.ai.model.vo.user.UserVO;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "应用返回体")
public class AppVO {

    @Schema(description = "应用id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "应用封面")
    private String cover;

    @Schema(description = "初始化用户消息")
    private String initPrompt;

    @Schema(description = "代码生成格式")
    private String codeType;

    @Schema(description = "部署链接")
    private String deployKey;

    @Schema(description = "部署时间")
    private Date deployedTime;

    @Schema(description = "插查询优先级， 数字越大优先级越高")
    private Integer priority;

    @Schema(description = "关联用户id")
    private Long userId;

    @Schema(description = "编辑时间")
    private Date editTime;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "创建者信息")
    private UserVO userVO;
}
