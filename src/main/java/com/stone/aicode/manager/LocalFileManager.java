package com.stone.aicode.manager;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * 本地文件存储管理器
 * 用本地磁盘替代对象存储，保存截图、架构图等图片文件
 */
@Slf4j
@Component
public class LocalFileManager {

    /**
     * 文件存储根目录
     */
    @Value("${local-file.root-dir:${user.dir}/tmp/local_files}")
    private String rootDir;

    /**
     * 文件访问地址前缀，需与 server.port、server.servlet.context-path 保持一致
     */
    @Value("${local-file.access-host:http://localhost:8123/api/files}")
    private String accessHost;

    /**
     * 保存文件到本地存储，并返回可访问的 URL
     *
     * @param key  相对存储路径，例如 /screenshots/2025/01/01/xxx.jpg
     * @param file 要保存的源文件
     * @return 文件访问 URL，失败返回 null
     */
    public String uploadFile(String key, File file) {
        if (StrUtil.isBlank(key) || file == null || !file.exists() || file.length() == 0) {
            log.error("本地文件保存失败：参数不合法或源文件不存在，key={}", key);
            return null;
        }
        try {
            String relativeKey = normalizeKey(key);
            File targetFile = new File(rootDir, relativeKey);
            FileUtil.mkParentDirs(targetFile);
            FileUtil.copy(file, targetFile, true);
            String url = buildAccessUrl(relativeKey);
            log.info("文件保存到本地成功：{} -> {}", file.getName(), url);
            return url;
        } catch (Exception e) {
            log.error("文件保存到本地失败：{}，key={}", file.getName(), key, e);
            return null;
        }
    }

    /**
     * 根据相对路径获取本地文件，路径非法或越界时返回 null
     *
     * @param key 相对存储路径
     * @return 本地文件
     */
    public File getFile(String key) {
        if (StrUtil.isBlank(key)) {
            return null;
        }
        try {
            File root = new File(rootDir).getCanonicalFile();
            File target = new File(root, normalizeKey(key)).getCanonicalFile();
            if (!target.getPath().startsWith(root.getPath() + File.separator)) {
                log.warn("非法的本地文件路径：{}", key);
                return null;
            }
            return target;
        } catch (IOException | IllegalArgumentException e) {
            log.error("获取本地文件失败，key={}", key, e);
            return null;
        }
    }

    /**
     * 删除本地文件
     *
     * @param key 相对存储路径
     * @return 是否删除成功
     */
    public boolean deleteFile(String key) {
        File file = getFile(key);
        if (file == null || !file.exists()) {
            return false;
        }
        return FileUtil.del(file);
    }

    /**
     * 获取存储根目录
     */
    public String getRootDir() {
        return rootDir;
    }

    /**
     * 规整路径：去掉开头的斜杠，统一分隔符，并拒绝越权路径
     */
    private String normalizeKey(String key) {
        String normalized = key.replace('\\', '/').trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isEmpty() || normalized.contains("..")) {
            throw new IllegalArgumentException("非法的文件存储路径：" + key);
        }
        return normalized;
    }

    /**
     * 拼接文件访问 URL
     */
    private String buildAccessUrl(String relativeKey) {
        String host = StrUtil.removeSuffix(StrUtil.nullToEmpty(accessHost).trim(), "/");
        return host + "/" + relativeKey;
    }
}
