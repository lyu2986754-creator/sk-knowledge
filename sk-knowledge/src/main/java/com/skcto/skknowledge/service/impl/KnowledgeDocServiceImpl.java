package com.skcto.skknowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skcto.skknowledge.constant.Constant;
import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.domain.KnowledgeTrash;
import com.skcto.skknowledge.dto.KnowledgeDocQueryDTO;
import com.skcto.skknowledge.mapper.KnowledgeDocMapper;
import com.skcto.skknowledge.mapper.KnowledgeTrashMapper;
import com.skcto.skknowledge.mapstuct.KnowledgeDocMapstruct;
import com.skcto.skknowledge.mapstuct.KnowledgeTrashMapstruct;
import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.service.KnowledgeDocService;
import com.skcto.skknowledge.service.MinioService;
import com.skcto.skknowledge.service.VectorService;
import com.skcto.skknowledge.temporal.workflow.KnowledgeDocWorkflow;
import com.skcto.skknowledge.util.LoginInfoUtil;
import com.skcto.skknowledge.vo.KnowledgeDocVo;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class KnowledgeDocServiceImpl extends ServiceImpl<KnowledgeDocMapper, KnowledgeDoc>
    implements KnowledgeDocService{

    private final KnowledgeDocMapstruct knowledgeDocMapstruct;

    private final KnowledgeTrashMapstruct knowledgeTrashMapstruct;

    private final VectorService vectorService;

    private final KnowledgeDocMapper knowledgeDocMapper;

    private final KnowledgeTrashMapper knowledgeTrashMapper;

    private final WorkflowClient workflowClient;

    private final MinioService minioService;


    @Override
    public boolean uploadDoc(MultipartFile file, Integer knowledgeId) {

        try {
            //启动工作流
            KnowledgeDocWorkflow knowledgeDocWorkflow = workflowClient.newWorkflowStub(
                    KnowledgeDocWorkflow.class, WorkflowOptions.newBuilder()
                            .setTaskQueue(Constant.DOC_TASK_QUEUE) // 队列名称
                            .build()
            );

            //pdf存入minio
            //获取文件后缀名
            String originalFilename = file.getOriginalFilename();
            String extFilename = originalFilename.substring(originalFilename.lastIndexOf("."));

            //上传文件minio
            String url = minioService.uploadFile(file, "knowledge");

            //判断文件是否存在
            if("exists".equals(url)){
                return true;
            }

            //构建mysql实体
            KnowledgeDoc knowledgeDoc = new KnowledgeDoc();
            knowledgeDoc.setDocName(originalFilename);
            if(extFilename != null){
                //文件后缀名，去除
                knowledgeDoc.setDocType(extFilename.substring(1));
            }
            knowledgeDoc.setKnowledgeId(knowledgeId);
            knowledgeDoc.setUrl(url);
            knowledgeDoc.setDocSize(file.getSize());

            //temporal中的work 处理mysql相关操作，开始新的线程,无法获取当前登录的用户信息
            knowledgeDoc.setCreateBy(LoginInfoUtil.getCurrentLoginUser().getId());

            //调用工作流 传入knowledge
            knowledgeDocWorkflow.processKnowledgeDoc(knowledgeDoc);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    @Override
    public DataPageInfo<KnowledgeDocVo> selectDocByPage(PageQuery pageQuery, Integer knowledgeId) {
        Page<KnowledgeDocQueryDTO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        QueryWrapper<KnowledgeDoc> queryWrapper = new QueryWrapper<>();
        //传入knowledge
        if(knowledgeId != null){
            queryWrapper.eq("kd.knowledge_id", knowledgeId);
        }

        Page<KnowledgeDocQueryDTO> knowledgeDocQueryDTOPage = knowledgeDocMapper.selectPage(page, queryWrapper);

        DataPageInfo<KnowledgeDocVo> dataPageInfo = knowledgeDocMapstruct.dtoToDataPageInfo(knowledgeDocQueryDTOPage);

        return dataPageInfo;
    }

    /**
     *根据url删除weaviate中的数据
     * 将数据放入回收站表
     * 删除mysql中的数据
     */
    @Override
    public Boolean docToTrash(Integer id) {
        KnowledgeDoc knowledgeDoc = getById(id);

        //根据url删除weaviate中的数据
        vectorService.deleteBySourceUrl(knowledgeDoc.getUrl());

        //将数据放入回收站（trash）表
        KnowledgeTrash knowledgeTrash = knowledgeTrashMapstruct.docToKnowledgeTrash(knowledgeDoc);

        knowledgeTrashMapper.insert(knowledgeTrash);

        //删除mysql中的数据
        removeById(id);

        return true;
    }
}




