package com.ye.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "账户封禁参数类")
public class AccountDisableRequest {

    /**
     * 0 - 完全封禁账户， 1 - 封禁账户部分功能, 2 - 解除封禁
     */
    @Schema(description = "0 - 完全封禁账户， 1 - 封禁账户部分功能, 2 - 解除封禁(必须)")
    private Integer disableType;

    /**
     * 账户封禁时间（单位s）
     */
    @Schema(description = "账户封禁时间（单位s）(可选)")
    private Long disableTime;

    /**
     * 账户封禁时间（单位s）
     */
    @Schema(description = "部分封禁的功能枚举值：seed_message(向Ai发送信息), deploy(网页部署) (可选)")
    private String functionType;

    /**
     * 封禁用户的id
     */
    @Schema(description = "封禁用户的id(必须)")
    private Long userId;
}
