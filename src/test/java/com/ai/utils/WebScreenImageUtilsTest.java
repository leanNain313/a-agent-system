package com.ai.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScreenImageUtilsTest {

    @Test
    void saveImage() {
        String testUrl = "https://www.codefather.cn";
        String webPageScreenshot = WebScreenImageUtils.executeScreenImage(testUrl);
        Assertions.assertNotNull(webPageScreenshot);
    }

}