package com.skcto.skknowledge.controller;

import com.skcto.skknowledge.domain.ChatModel;
import com.skcto.skknowledge.dto.ChatModelDTO;
import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.result.Result;
import com.skcto.skknowledge.service.ChatModelService;
import com.skcto.skknowledge.vo.ChatModelVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/chat/")
@RequiredArgsConstructor
public class ChatModelController {

    private final ChatModelService chatModelService;

    /**
     * 创建模型
     *
     * @param chatModelDTO
     * @return
     */
    @PostMapping("/model")
    public Result addModel(@RequestBody ChatModelDTO chatModelDTO) {
        boolean flag = chatModelService.insertByDTO(chatModelDTO);

        return flag ? Result.OK() : Result.FAIL();
    }

    /**
     * 分页查询
     */
    @GetMapping("/model")
    public Result queryModel(PageQuery pageQuery) {
        DataPageInfo<ChatModelVo> pageInfo = chatModelService.selectModelByPage(pageQuery);

        return Result.OK(pageInfo);
    }

    /**
     * 根据id查询模型信息
     */
    @GetMapping("/model/{id}")
    public Result queryModelById(@PathVariable Integer id) {
        ChatModelVo chatModelVo = chatModelService.getVoById(id);
        return Result.OK(chatModelVo);
    }

    /**
     * 更新模型
     */
    @PutMapping("/model")
    public Result updateModel(@RequestBody ChatModelDTO chatModelDTO) {
        boolean flag = chatModelService.updateByDTO(chatModelDTO);
        return flag ? Result.OK() : Result.FAIL();
    }

    /**
     * 删除模型
     */
    @DeleteMapping("/model/{id}")
    public Result deleteModel(@PathVariable Integer id) {
        boolean flag = chatModelService.removeById(id);

        return flag ? Result.OK() : Result.FAIL();
    }

    /**
     * 根据条件查询模型
     */
    @GetMapping("/models")
    public Result queryModelByDTO(ChatModelDTO chatModelDTO) {
        List<ChatModelVo> chatModelVos = chatModelService.selectModelByDTO(chatModelDTO);

        return Result.OK(chatModelVos);
    }

}
