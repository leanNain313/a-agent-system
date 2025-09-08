package com.ye.model.vo.user;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "账户功能封禁状态(封禁时间是解除封禁的剩余秒数)")
public class AccountFunctionStateVO implements Serializable {

    @Schema(description = "所有功能")
    private boolean allFunction;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "所有功能封禁时间")
    private Long allFunctionTime;

    @Schema(description = "ai对话功能")
    private boolean aiFunction;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "ai对话功能封禁时间")
    private Long aiFunctionTime;

    @Schema(description = "代码部署功能")
    private boolean deployFunction;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "代码部署功能封禁时间")
    private Long deployFunctionTime;

    @Schema(description = "文件上传功能")
    private boolean fileUpLoadFunction;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "文件上传功能封禁时间")
    private Long fileUpLoadFunctionTime;

}
