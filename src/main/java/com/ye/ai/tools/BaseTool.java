package com.ye.ai.tools;

import cn.hutool.json.JSONObject;

public abstract class BaseTool {

    /**
     * 工具名称，方法名
     */
    public abstract String getToolName();

    /**
     * 工具中文名， 有也就是工具的描述
     */
    public abstract String getDisplayName();

    /**
     * 获取工具请求信息
     * @return 将工具的请求信息返回给用户
     */
    public String getToolRequest() {
        return String.format("\n\n[tool select]：%s\n\n", getDisplayName());
    }

    /**
     * 获取工具的执行结果
     */
    public abstract String getToolExecutedResult(JSONObject arguments);
}
