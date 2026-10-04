package com.skcto.skknowledge.controller;

import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.result.Result;
import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.service.KnowledgeDocService;
import com.skcto.skknowledge.service.MinioService;
import com.skcto.skknowledge.vo.KnowledgeDocVo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 知识库文档
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/knowledge/")
public class KnowledgeDocController {

    private final KnowledgeDocService knowledgeDocService;
    private final MinioService minioService;


    /**
     * 上传文档
     */
    @PostMapping("/doc")
    public Result addDoc(@RequestPart("file") MultipartFile file, Integer knowledgeId){
        return knowledgeDocService.uploadDoc(file, knowledgeId) ? Result.OK() : Result.FAIL();
    }

    /**
     * 查询文档
     */
    @GetMapping("/doc")
    public Result queryDoc(PageQuery pageQuery, Integer knowledgeId){
        DataPageInfo<KnowledgeDocVo> pageInfo = knowledgeDocService.selectDocByPage(pageQuery, knowledgeId);
        return Result.OK(pageInfo);
    }

    /**
     * 读取文档的原始文本。
     *
     * 存在的意义：向量检索返回的是**被切碎的片段**，拿不到完整条文、也做不了
     * 字面精确匹配。而外部编排层（如 Agentic 服务）需要基于原始文本做两件事：
     *   1. 按条文号取出**完整**的条文原文
     *   2. 按关键词做字面匹配（编号、专有名词这类，向量检索天然不敏感）
     *
     * 只读，不改动任何数据。
     */
    @GetMapping("/doc/{id}/text")
    public Result readDocText(@PathVariable Integer id) {
        KnowledgeDoc doc = knowledgeDocService.getById(id);
        if (doc == null) {
            return Result.FAIL("文档不存在：" + id);
        }
        String url = doc.getUrl() == null ? "" : doc.getUrl();
        // url 形如 knowledge/xxx.txt，MinIO 的对象名不含 bucket 前缀
        int index = url.indexOf("/");
        String objectName = index != -1 ? url.substring(index + 1) : url;

        try (InputStream inputStream = minioService.downloadFile(objectName)) {
            String text = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, Object> data = new HashMap<>();
            data.put("docId", doc.getId());
            data.put("docName", doc.getDocName());
            data.put("text", text);
            return Result.OK(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载文档
     */
    @GetMapping("/download")
    public void download(String fileName,  HttpServletResponse response){
        int index = fileName.indexOf("/");
        String result = index != -1 ? fileName.substring(index + 1) : fileName;

        try(InputStream inputStream = minioService.downloadFile(result)){
            //设置响应头
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String encodedFileName = URLEncoder.encode(result, StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment; filename*=" + encodedFileName);

            try(OutputStream outputStream = response.getOutputStream()){
                inputStream.transferTo(outputStream);
                outputStream.flush();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 放入回收站
     */
    @DeleteMapping("/doc/{id}")
    public Result deleteDoc(@PathVariable Integer id){
        Boolean flag = knowledgeDocService.docToTrash(id);

        return flag ? Result.OK() : Result.FAIL();
    }

}
