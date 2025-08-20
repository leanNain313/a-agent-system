package com.ai.model.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "账户封禁参数类")
public class AccountDisableRequest {

    /**
     * 0 - 完全封禁账户， 1 - 封禁账户部分功能, 2 - 解除封禁
     */
    @Schema(description = "0 - 完全封禁账户， 1 - 封禁账户部分功能, 2 - 解除封禁")
    private Integer disableType;

    /**
     * 账户封禁时间（单位s）
     */
    @Schema(description = "账户封禁时间（单位s）")
    private Long disableTime;

    /**
     * 账户封禁时间（单位s）
     */
    @Schema(description = "账户封禁时间（单位s）")
    private String functionType;

    /**
     * 封禁用户的id
     */
    @Schema(description = "封禁用户的id")
    private Long userId;
}
