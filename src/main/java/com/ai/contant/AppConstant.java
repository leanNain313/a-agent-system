package com.ai.contant;

import java.io.File;

public interface AppConstant {

    /**
     * 应用源文件路径
     */
    String CODE_OUT_DIR = System.getProperty("user.dir") +File.separator + "templates" + File.separator +"code_output";

    /**
     * 应用部署路径
     */
    String CODE_DEPLOY_DIR = System.getProperty("user.dir") + File.separator + "templates" + File.separator +"code_deploy";

    /**
     * 应用部署的域名
     */
    String CODE_DEPLOY_HOST = "http://localhost:80";
}
