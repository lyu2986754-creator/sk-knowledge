package com.skcto.skknowledge.controller;

import com.skcto.skknowledge.dto.KnowledgeBaseDTO;
import com.skcto.skknowledge.result.Result;
import com.skcto.skknowledge.service.KnowledgeBaseService;
import com.skcto.skknowledge.vo.KnowledgeBaseVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 创建知识库
     */
    @PostMapping("/base")
    public Result addBase(@RequestBody KnowledgeBaseDTO knowledgeBaseDTO) {
        boolean flag = knowledgeBaseService.insertByDTO(knowledgeBaseDTO);

        return flag ? Result.OK() : Result.FAIL();
    }

    /**
     * 查询知识库
     */
    @GetMapping("/base")
    public Result queryBase() {
        List<KnowledgeBaseVo> list = knowledgeBaseService.selectBaseVo();

        return Result.OK(list);
    }

}
