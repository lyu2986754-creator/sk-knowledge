package com.skcto.skknowledge.controller;

import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.result.Result;
import com.skcto.skknowledge.service.KnowledgeTrashService;
import com.skcto.skknowledge.vo.KnowledgeDocVo;
import com.skcto.skknowledge.vo.KnowledgeTrashVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/knowledge/")
public class KnowledgeTrashController {

    private final KnowledgeTrashService knowledgeTrashService;

    /**
     * 查询回收站
     */
    @GetMapping("/trash")
    public Result queryTrash(PageQuery pageQuery, Integer knowledgeId) {
        DataPageInfo<KnowledgeTrashVo> pageInfo = knowledgeTrashService.selectTrashByPage(pageQuery, knowledgeId);
        return Result.OK(pageInfo);
    }

    /**
     * 还原
     */
    @PutMapping("/trash/{id}")
    public Result recoverTrash(@PathVariable Integer id) {
        Boolean result =  knowledgeTrashService.recoverTrash(id);

        return result ? Result.OK() : Result.FAIL();
    }



}
