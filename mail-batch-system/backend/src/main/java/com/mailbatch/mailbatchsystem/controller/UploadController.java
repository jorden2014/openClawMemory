package com.mailbatch.mailbatchsystem.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 文件上传控制器
 * 处理图片上传请求
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${file.upload.path:/home/admin/.openclaw/workspace/mail-batch-system/uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;
    
    @Value("${file.upload.server-domain:}")
    private String serverDomain;

    /**
     * 上传图片
     * POST /api/upload/image
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("收到图片上传请求: {}", file.getOriginalFilename());

        try {
            // 1. 验证文件
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "文件不能为空"));
            }

            // 2. 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "只能上传图片文件"));
            }

            // 3. 验证文件大小（限制 10MB）
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "图片大小不能超过 10MB"));
            }

            // 4. 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 5. 按日期组织目录
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            Path uploadDir = Paths.get(uploadPath, "images", datePath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建上传目录: {}", uploadDir);
            }

            // 6. 保存文件
            Path filePath = uploadDir.resolve(newFilename);
            file.transferTo(filePath.toFile());
            log.info("图片保存成功: {}", filePath);

            // 7. 生成访问 URL
            String fileUrl = urlPrefix + "/images/" + datePath + "/" + newFilename;
            
            // 使用配置的域名或请求中的域名
            String baseUrl = "";
            // 优先使用配置文件中的域名
            if (serverDomain != null && !serverDomain.isEmpty()) {
                baseUrl = serverDomain;
            }
            String fullUrl = baseUrl + fileUrl;

            // 8. 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "上传成功");
            result.put("url", fullUrl);
            result.put("filename", originalFilename);
            result.put("size", file.getSize());

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            log.error("图片上传失败", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "上传失败: " + e.getMessage()));
        }
    }

    /**
     * 获取服务器 IP（简单实现）
     */
    private String getServerIp() {
        try {
            java.net.InetAddress localHost = java.net.InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }

    /**
     * 删除图片（可选功能）
     * POST /api/upload/delete
     */
    @PostMapping("/delete")
    public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> request) {
        String fileUrl = request.get("url");
        log.info("收到图片删除请求: {}", fileUrl);

        try {
            // 从 URL 中提取文件路径
            // 支持两种格式：带域名和不带域名
            String relativePath;
            if (fileUrl.startsWith(serverDomain)) {
                relativePath = fileUrl.replace(serverDomain + urlPrefix, "");
            } else {
                relativePath = fileUrl.replace(urlPrefix, "");
            }
            Path filePath = Paths.get(uploadPath, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("图片删除成功: {}", filePath);
                return ResponseEntity.ok(Map.of("success", true, "message", "删除成功"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "文件不存在"));
            }

        } catch (IOException e) {
            log.error("图片删除失败", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "删除失败: " + e.getMessage()));
        }
    }

    /**
     * 获取图片列表
     * GET /api/upload/list?page=1&pageSize=12
     */
    @GetMapping("/list")
    public ResponseEntity<?> listImages(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "12") int pageSize) {
        log.info("收到图片列表请求: page={}, pageSize={}", page, pageSize);

        try {
            // 1. 扫描上传目录
            Path imagesDir = Paths.get(uploadPath, "images");
            if (!Files.exists(imagesDir)) {
                log.info("图片目录不存在，返回空列表");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                        "items", Collections.emptyList(),
                        "total", 0,
                        "page", page,
                        "pageSize", pageSize
                    )
                ));
            }

            // 2. 递归遍历所有图片文件
            List<Map<String, Object>> allImages = new ArrayList<>();
            Files.walk(imagesDir)
                .filter(Files::isRegularFile)
                .filter(path -> isImageFile(path.toString()))
                .forEach(path -> {
                    try {
                        Map<String, Object> imageInfo = new HashMap<>();
                        
                        // 文件名
                        String filename = path.getFileName().toString();
                        imageInfo.put("filename", filename);
                        
                        // 文件大小
                        long size = Files.size(path);
                        imageInfo.put("size", size);
                        
                        // 上传时间
                        Date uploadTime = new Date(Files.getLastModifiedTime(path).toMillis());
                        imageInfo.put("uploadTime", uploadTime);
                        
                        // 生成访问 URL
                        String relativePath = imagesDir.relativize(path).toString();
                        String fileUrl = urlPrefix + "/images/" + relativePath.replace("\\", "/");
                        String fullUrl = serverDomain + fileUrl;
                        imageInfo.put("url", fullUrl);
                        
                        allImages.add(imageInfo);
                    } catch (Exception e) {
                        log.error("处理图片文件失败: {}", path, e);
                    }
                });

            // 3. 按上传时间倒序排序（最新的在前面）
            allImages.sort((a, b) -> {
                Date timeA = (Date) a.get("uploadTime");
                Date timeB = (Date) b.get("uploadTime");
                return timeB.compareTo(timeA);
            });

            // 4. 分页
            int total = allImages.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            
            List<Map<String, Object>> pageItems;
            if (startIndex < total) {
                pageItems = allImages.subList(startIndex, endIndex);
            } else {
                pageItems = Collections.emptyList();
            }

            // 5. 返回结果
            Map<String, Object> data = new HashMap<>();
            data.put("items", pageItems);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);

            log.info("图片列表查询成功: total={}, pageItems={}", total, pageItems.size());
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", data
            ));

        } catch (Exception e) {
            log.error("获取图片列表失败", e);
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "获取列表失败: " + e.getMessage()));
        }
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String filename) {
        String lowerFilename = filename.toLowerCase();
        return lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg") ||
               lowerFilename.endsWith(".png") || lowerFilename.endsWith(".gif") ||
               lowerFilename.endsWith(".bmp") || lowerFilename.endsWith(".webp");
    }
}
