package com.skcto.skknowledge.controller;

import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.result.Result;
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
