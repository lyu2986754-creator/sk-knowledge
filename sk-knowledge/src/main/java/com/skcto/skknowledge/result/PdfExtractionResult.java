package com.skcto.skknowledge.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfExtractionResult {
    // 提取的文本内容
    private String content;
    
    // PDF元数据
    private Map<String, String> metadata = new HashMap<>();

    
    // 提取的图片保存路径列表
    private List<String> imagePaths;

    
    // 处理时间(毫秒)
    private long processingTime;
}