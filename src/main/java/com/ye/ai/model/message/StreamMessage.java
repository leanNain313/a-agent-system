package com.ye.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StreamMessage {

    /**
     * 消息类型
     */
    private String type;
}
