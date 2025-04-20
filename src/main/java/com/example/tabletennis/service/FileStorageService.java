package com.example.tabletennis.service;

import com.example.tabletennis.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    // 配置项保持原有注解即可
    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}") // 添加默认值
    private String baseUrl;

    @Value("${app.upload.path:/uploads/}") // 添加默认值
    private String uploadPath;

    /**
     * 增强版文件存储（含安全校验）
     */
    public String storeFileWithUrl(MultipartFile file) {
        validateFile(file); // 新增校验
        String fileName = storeFile(file);
        return buildFileUrl(fileName);
    }

    /**
     * 原始存储方法（添加日志）
     */
    public String storeFile(MultipartFile file) {
        try {
            // 增强路径处理（兼容Windows/Linux）
            Path storagePath = buildStoragePath();

            String originalName = sanitizeFilename(file);
            String fileName = generateUniqueName(originalName);
            Path targetPath = storagePath.resolve(fileName);

            Files.copy(file.getInputStream(), targetPath);
            return fileName;

        } catch (IOException ex) {
            throw new FileStorageException("文件存储失败: " + ex.getMessage(), ex);
        }
    }

    /**
     * 智能构建文件URL（自动处理路径格式）
     */
    private String buildFileUrl(String fileName) {
        String normalizedBase = baseUrl.endsWith("/") ?
                baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        String normalizedPath = uploadPath.startsWith("/") ?
                uploadPath : "/" + uploadPath;

        return normalizedBase + normalizedPath + fileName;
    }

    // 新增辅助方法 --------------------------------

    private Path buildStoragePath() throws IOException {
        Path path = Paths.get(uploadDir.replace('\\', '/')) // 统一斜杠
                .toAbsolutePath()
                .normalize();
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return path;
    }

    private String sanitizeFilename(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        // 防止路径穿越攻击
        if (filename.contains("..")) {
            throw new FileStorageException("非法文件名: " + filename);
        }
        return filename;
    }

    private String generateUniqueName(String originalName) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid + "_" + originalName;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("上传文件不能为空");
        }
        // 可扩展其他校验（如文件类型、大小等）
    }
}