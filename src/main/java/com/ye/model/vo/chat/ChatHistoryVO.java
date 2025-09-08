package com.ye.model.vo.chat;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@Schema(description = "对话历史返回视图")
public class ChatHistoryVO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "消息id")
    private Long id;

    @Schema(description = "消息内容")
    private String message;

    @Schema(description = "消息类型：user, ye")
    private String messageType;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "关联的appid")
    private Long appId;

    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "关联的用户id")
    private Long userId;

    @Schema(description = "消息发送时间")
    private Date createTime;
}
