package com.ye.monitor;

import lombok.extern.slf4j.Slf4j;

/**
 * 上下文管理器
 */
@Slf4j
public class MonitorContextHolder {

    private static final ThreadLocal<MonitorContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置监控上下文
     */
    public static void setContext(MonitorContext monitorContext) {
        CONTEXT_HOLDER.set(monitorContext);
    }

    /**
     * 获取当前上下文
     */
    public static MonitorContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清空监控上下文
     */
    public static void removeContext() {
        CONTEXT_HOLDER.remove();
    }
}
