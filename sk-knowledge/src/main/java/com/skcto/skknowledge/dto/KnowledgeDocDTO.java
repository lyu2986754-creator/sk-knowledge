package com.skcto.skknowledge.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class KnowledgeDocDTO {
    private Integer knowledgeBaseId;
    private MultipartFile file;
}
