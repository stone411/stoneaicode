package com.stone.aicode.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 本地文件访问配置
 * 把 /files/** 映射到本地文件存储目录，供截图、架构图等图片直接访问
 */
@Slf4j
@Configuration
public class LocalFileConfig implements WebMvcConfigurer {

    /**
     * 文件存储根目录
     */
    @Value("${local-file.root-dir:${user.dir}/tmp/local_files}")
    private String rootDir;

    /**
     * 应用启动时创建存储目录
     */
    @PostConstruct
    public void initStorageDir() {
        Path storagePath = Path.of(rootDir).toAbsolutePath();
        try {
            Files.createDirectories(storagePath);
            log.info("本地文件存储目录：{}", storagePath);
        } catch (Exception e) {
            throw new IllegalStateException("创建本地文件存储目录失败：" + storagePath, e);
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = "file:" + Path.of(rootDir).toAbsolutePath().toString().replace('\\', '/') + "/";
        registry.addResourceHandler("/files/**").addResourceLocations(location);
        log.info("本地文件访问映射：/files/** -> {}", location);
    }
}
