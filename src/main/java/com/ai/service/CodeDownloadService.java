package com.ai.service;

import jakarta.servlet.http.HttpServletResponse;

public interface CodeDownloadService {

    void codeDownload(String rootPath, String downloadName, HttpServletResponse response);
}
