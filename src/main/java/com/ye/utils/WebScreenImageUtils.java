package com.ye.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ye.Exception.BusinessException;
import com.ye.Exception.ErrorCode;
import com.ye.contant.AppConstant;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;

@Slf4j
public class WebScreenImageUtils {

    // 创建一个浏览器驱动线程池，处理并发执行
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    // 图片宽高配置
    private static final int DEFAULT_WIDTH = 1600;

    private static final int DEFAULT_HEIGHT = 900;


    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            driver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            driverThreadLocal.set(driver);
        }
        return driver;
    }

    /**
     * 用完就停止
     */
    @PreDestroy
    public void destroy(WebDriver webDriver) {
        webDriver.quit();
    }

    private static WebDriver initChromeDriver(int width, int height) {
        try {
            // 自动管理 ChromeDriver
            WebDriverManager.chromedriver().setup();
            // 配置 Chrome 选项
            ChromeOptions options = new ChromeOptions();
            // 无头模式
            options.addArguments("--headless");
            // 禁用GPU（在某些环境下避免问题）
            options.addArguments("--disable-gpu");
            // 禁用沙盒模式（Docker环境需要）
            options.addArguments("--no-sandbox");
            // 禁用开发者shm使用
            options.addArguments("--disable-dev-shm-usage");
            // 设置窗口大小
            options.addArguments(String.format("--window-size=%d,%d", width, height));
            // 禁用扩展
            options.addArguments("--disable-extensions");
            // 设置用户代理
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            // 创建驱动
            WebDriver driver = new ChromeDriver(options);
            // 设置页面加载超时
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        } catch (Exception e) {
            log.error("浏览器初始化失败:{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "浏览器初始化失败");
        }
    }

    /**
     * 保存截屏图片
     *
     * @param imageBytes 图片文件的二进制数组
     * @param filePath   保存环境
     */
    private static void saveImage(byte[] imageBytes, String filePath) {
        try {
            FileUtil.writeBytes(imageBytes, filePath);
        } catch (Exception e) {
            log.error("截屏图片保存失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "截屏图片写入失败");
        }
    }

    /**
     * 压缩图片
     *
     * @param originalImagePath 原图片路径
     * @param compressImagePath 压缩后的图片路径
     */
    private static void imageCompress(String originalImagePath, String compressImagePath) {
        // 图片压缩质量
        final float COMPRESSION_QUALITY = 0.5f;
        try {
            ImgUtil.compress(
                    FileUtil.file(originalImagePath),
                    FileUtil.file(compressImagePath),
                    COMPRESSION_QUALITY
            );
        } catch (Exception e) {
            log.error("图片压缩失败：{}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片压缩失败");
        }
    }

    /**
     * 等待网页加载
     */
    private static void waitWebLoad(WebDriver webDriver) {
        try {
            // 等待页面加载， 10秒钟
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            // 等待 document.readyState 为complete
            wait.until(driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
            log.info("网页加载成功.");
            Thread.sleep(4000);
        } catch (Exception e) {
            log.warn("网页加载异常：{}, 继续执行截图操作", e.getMessage());
        }
    }

    /**
     * 执行截图操作
     *
     * @param url 网页的路由
     * @return 返回生成图片的路由
     */
    public static String executeScreenImage(String url) {
        if (StrUtil.isBlank(url)) {
            log.error("要截图的路由为空");
            return null;
        }
        try {
            // 生成临时路径
            String imageTmpPath = AppConstant.SCREEN_IMAGE_DIR + File.separator + RandomUtil.randomString(8);
            // 制作文件夹
            FileUtil.mkdir(imageTmpPath);
            // 访问网页
            WebDriver webDriver = getDriver();
            webDriver.get(url);
            // 等待网页加载
            waitWebLoad(webDriver);
            // 获取图片
            byte[] screenshotAs = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
            if (ObjUtil.isEmpty(screenshotAs)) {
                return null;
            }
            // 保存图片
            final String IMAGE_SUFFIX = ".png";
            String savePath =  imageTmpPath + File.separator + IdUtil.simpleUUID() + IMAGE_SUFFIX;
            saveImage(screenshotAs, savePath);
            log.info("原始图片保存成功， 路径：{}", imageTmpPath);
            // 压缩图片
            final String COMPRESSION_SUFFIX = ".jpg";
            String compressedImagePath = imageTmpPath + File.separator + IdUtil.simpleUUID() + COMPRESSION_SUFFIX;
            imageCompress(savePath, compressedImagePath);
            // 删除临时文件
            FileUtil.del(savePath);
            log.info("截图保存成功, 路径：{}", compressedImagePath);
            // 将文件上传到云服务
            return compressedImagePath;
        } catch (Exception e) {
            log.error("网页截图失败：{}", e.getMessage());
            return null;
        }
    }
}