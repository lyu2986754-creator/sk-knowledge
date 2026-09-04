package com.skcto.skknowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.skcto.skknowledge.domain.KnowledgeTrash;
import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.vo.KnowledgeDocVo;
import com.skcto.skknowledge.vo.KnowledgeTrashVo;


public interface KnowledgeTrashService extends IService<KnowledgeTrash> {


    DataPageInfo<KnowledgeTrashVo> selectTrashByPage(PageQuery pageQuery, Integer knowledgeId);

    Boolean recoverTrash(Integer id);
}
