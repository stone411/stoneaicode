package com.stone.aicode.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.stone.aicode.exception.ErrorCode;
import com.stone.aicode.exception.ThrowUtils;
import com.stone.aicode.manager.LocalFileManager;
import com.stone.aicode.service.ScreenshotService;
import com.stone.aicode.utils.WebScreenshotUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class ScreenshotServiceImpl implements ScreenshotService {

    @Resource
    private LocalFileManager localFileManager;

    @Override
    public String generateAndUploadScreenshot(String webUrl) {
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(webUrl), ErrorCode.PARAMS_ERROR, "截图的网址不能为空");
        log.info("开始生成网页截图，URL：{}", webUrl);
        // 本地截图
        String localScreenshotPath = WebScreenshotUtils.saveWebPageScreenshot(webUrl);
        ThrowUtils.throwIf(StrUtil.isBlank(localScreenshotPath), ErrorCode.OPERATION_ERROR, "生成网页截图失败");
        // 保存截图到本地文件存储
        try {
            String screenshotUrl = saveScreenshot(localScreenshotPath);
            ThrowUtils.throwIf(StrUtil.isBlank(screenshotUrl), ErrorCode.OPERATION_ERROR, "保存截图到本地失败");
            log.info("截图保存成功，URL：{}", screenshotUrl);
            return screenshotUrl;
        } finally {
            // 清理临时截图文件
            cleanupLocalFile(localScreenshotPath);
        }
    }

    /**
     * 把截图保存到本地文件存储
     *
     * @param localScreenshotPath 本地截图路径
     * @return 文件访问 URL，失败返回 null
     */
    private String saveScreenshot(String localScreenshotPath) {
        if (StrUtil.isBlank(localScreenshotPath)) {
            return null;
        }
        File screenshotFile = new File(localScreenshotPath);
        if (!screenshotFile.exists()) {
            log.error("截图文件不存在: {}", localScreenshotPath);
            return null;
        }
        // 生成文件存储的相对路径
        String fileName = UUID.randomUUID().toString().substring(0, 8) + "_compressed.jpg";
        String key = generateScreenshotKey(fileName);
        return localFileManager.uploadFile(key, screenshotFile);
    }

    /**
     * 生成截图在本地存储中的相对路径
     * 格式：/screenshots/2025/07/31/filename.jpg
     */
    private String generateScreenshotKey(String fileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return String.format("/screenshots/%s/%s", datePath, fileName);
    }

    /**
     * 清理本地文件
     *
     * @param localFilePath 本地文件路径
     */
    private void cleanupLocalFile(String localFilePath) {
        File localFile = new File(localFilePath);
        if (localFile.exists()) {
            FileUtil.del(localFile);
            log.info("清理本地文件成功: {}", localFilePath);
        }
    }
}
