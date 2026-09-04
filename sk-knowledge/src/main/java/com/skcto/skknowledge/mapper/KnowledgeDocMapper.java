package com.skcto.skknowledge.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.dto.KnowledgeDocQueryDTO;
import org.apache.ibatis.annotations.Param;


public interface KnowledgeDocMapper extends BaseMapper<KnowledgeDoc> {
    Page<KnowledgeDocQueryDTO> selectPage(Page<KnowledgeDocQueryDTO> page ,@Param(Constants.WRAPPER) QueryWrapper queryWrapper);
}




