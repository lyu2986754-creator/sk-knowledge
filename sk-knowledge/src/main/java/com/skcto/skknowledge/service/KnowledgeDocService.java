package com.skcto.skknowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.vo.KnowledgeDocVo;
import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeDocService extends IService<KnowledgeDoc> {


    boolean uploadDoc(MultipartFile file, Integer knowledgeId);

    DataPageInfo<KnowledgeDocVo> selectDocByPage(PageQuery pageQuery, Integer knowledgeId);

    Boolean docToTrash(Integer id);
}
