package com.skcto.skknowledge.controller;

import com.skcto.skknowledge.result.Result;
import com.skcto.skknowledge.service.VectorDbService;
import com.skcto.skknowledge.vo.VectorDbVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vector/")
public class VectorDbController {

    private final VectorDbService vectorDbService;

    /**
     * 查询向量库信息
     * @return
     */
    @GetMapping("/db")
    public Result queryVectorDb() {
        List<VectorDbVo> list = vectorDbService.queryVectorDbVo();

        return Result.OK(list);
    }



}
