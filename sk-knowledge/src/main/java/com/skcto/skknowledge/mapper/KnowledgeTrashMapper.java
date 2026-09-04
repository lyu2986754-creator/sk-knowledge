package com.skcto.skknowledge.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skcto.skknowledge.domain.KnowledgeTrash;
import com.skcto.skknowledge.dto.KnowledgeTrashQueryDTO;
import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.vo.KnowledgeTrashVo;
import org.apache.ibatis.annotations.Param;


public interface KnowledgeTrashMapper extends BaseMapper<KnowledgeTrash> {

    Page<KnowledgeTrashQueryDTO> selectPage(Page<KnowledgeTrashQueryDTO> page, @Param(Constants.WRAPPER) Wrapper<KnowledgeTrash> wrapper);
}




