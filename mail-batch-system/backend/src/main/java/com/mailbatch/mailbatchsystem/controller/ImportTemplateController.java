package com.mailbatch.mailbatchsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

/**
 * 导入模板控制器
 */
@Slf4j
@RestController()
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class ImportTemplateController {

    /**
     * 下载客户导入模板（CSV 格式）
     * GET /api/customers/import-template
     */
    @GetMapping("/import-template")
    public ResponseEntity<byte[]> downloadTemplate() {
        log.info("下载客户导入模板");
        
        String csvHeader = "姓名,称呼,邮箱,标签,备注\n";
        String csvExample = "张三,张先生,zhangsan@example.com,VIP|老客户,重要客户\n";
        String csvExample2 = "李四,李女士,lisi@example.com,潜在客户,需要跟进\n";
        String csvContent = csvHeader + csvExample + csvExample2;
        
        byte[] csvBytes = csvContent.getBytes(StandardCharsets.UTF_8);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8"));
        headers.setContentDispositionFormData("attachment", "客户导入模板.csv");
        // 添加 BOM 让 Excel 正确识别 UTF-8
        byte[] bom = new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
        byte[] responseBytes = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, responseBytes, 0, bom.length);
        System.arraycopy(csvBytes, 0, responseBytes, bom.length, csvBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(responseBytes);
    }
}
