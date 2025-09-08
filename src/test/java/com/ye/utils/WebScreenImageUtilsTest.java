package com.ye.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WebScreenImageUtilsTest {

    @Test
    void saveImage() {
        String testUrl = "https://www.codefather.cn";
        String webPageScreenshot = WebScreenImageUtils.executeScreenImage(testUrl);
        Assertions.assertNotNull(webPageScreenshot);
    }

}