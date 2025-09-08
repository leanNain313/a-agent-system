package com.ye.ai.model;

import lombok.Data;

@Data
public class MultiFileCodeResult {

    /**
     * html代码
     */
    private String htmlCode;

    /**
     * css代码
     */
    private String cssCode;

    /**
     * js 代码
     */
    private String jsCode;

    /**
     * 描述
     */
    private String description;

}
