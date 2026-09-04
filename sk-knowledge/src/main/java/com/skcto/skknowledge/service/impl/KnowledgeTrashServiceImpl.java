package com.skcto.skknowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.domain.KnowledgeTrash;
import com.skcto.skknowledge.dto.KnowledgeTrashQueryDTO;
import com.skcto.skknowledge.extract.FileExtract;
import com.skcto.skknowledge.extract.FileExtractFactory;
import com.skcto.skknowledge.mapper.KnowledgeDocMapper;
import com.skcto.skknowledge.mapper.KnowledgeTrashMapper;
import com.skcto.skknowledge.mapstuct.KnowledgeTrashMapstruct;
import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.service.KnowledgeTrashService;
import com.skcto.skknowledge.service.MinioService;
import com.skcto.skknowledge.service.VectorService;
import com.skcto.skknowledge.vo.KnowledgeDocVo;
import com.skcto.skknowledge.vo.KnowledgeTrashVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;


@Service
@RequiredArgsConstructor
public class KnowledgeTrashServiceImpl extends ServiceImpl<KnowledgeTrashMapper, KnowledgeTrash>
    implements KnowledgeTrashService{

    private final KnowledgeTrashMapper knowledgeTrashMapper;

    private final KnowledgeTrashMapstruct knowledgeTrashMapstruct;

    private final KnowledgeDocMapper knowledgeDocMapper;

    private final MinioService minioService;

    private final VectorService vectorService;


    @Override
    public DataPageInfo<KnowledgeTrashVo> selectTrashByPage(PageQuery pageQuery, Integer knowledgeId) {

        Page<KnowledgeTrashQueryDTO> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        QueryWrapper<KnowledgeTrash> queryWrapper = new QueryWrapper<>();
        if (knowledgeId != null) {
            queryWrapper.eq("kt.knowledge_id", knowledgeId);
        }

        Page<KnowledgeTrashQueryDTO> knowledgeTrashQueryDTOPage = knowledgeTrashMapper.selectPage(page, queryWrapper);

        DataPageInfo<KnowledgeTrashVo> knowledgeTrashVoDataPageInfo = knowledgeTrashMapstruct.dtoToDataPageInfo(knowledgeTrashQueryDTOPage);

        return knowledgeTrashVoDataPageInfo;
    }

    /**
     * 恢复回收站
     * 删除回收站表中的数据
     * 将数据放入doc表中
     * 解析pdf将chunk放入向量数据库中
     * @param id
     * @return
     */
    @Override
    public Boolean recoverTrash(Integer id) {
        try {
            //删除回收站表中的数据
            KnowledgeTrash knowledgeTrash = knowledgeTrashMapper.selectById(id);
            knowledgeTrashMapper.deleteById(id);

            //将数据放入doc表
            KnowledgeDoc knowledgeDoc = knowledgeTrashMapstruct.knowledgeTrashToDoc(knowledgeTrash);
            knowledgeDocMapper.insert(knowledgeDoc);

            //从minio中查询文件
            String url = knowledgeDoc.getUrl();
            int index = url.indexOf("/");
            String bucketName = url.substring(0,index);
            String objectName = url.substring(index+1);

            InputStream inputStream = minioService.downloadFile(bucketName, objectName);

            //考虑这里查询出的可能是多种不同类型的文件pdf word excel md
            //我们需要根据文件类型进行不同的处理
            FileExtract fileExtract = FileExtractFactory.getFileExtract(knowledgeDoc.getDocType());

            //根据返回的对象数据来解析文件内容
            List<String> chunks = fileExtract.extractText(inputStream);

            //解析pdf将chunk放入向量数据库中
            vectorService.storeText(knowledgeDoc.getKnowledgeId(), chunks, url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}




