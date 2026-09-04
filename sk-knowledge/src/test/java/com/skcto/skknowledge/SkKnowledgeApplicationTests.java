package com.skcto.skknowledge;


import com.skcto.skknowledge.constant.Constant;
import com.skcto.skknowledge.eneity.Book;
import com.skcto.skknowledge.eneity.User;
import com.skcto.skknowledge.eneity.UserDTO;
import com.skcto.skknowledge.mapstruct.MyUserMapStruct;
import com.skcto.skknowledge.service.VectorService;
import com.skcto.skknowledge.service.WeaviateVectorService;
import com.skcto.skknowledge.service.impl.TikaSemanticChunkService;
import com.skcto.skknowledge.temporal.LearnWorkflow;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

@SpringBootTest
class SkKnowledgeApplicationTests {

    @Resource
    private MyUserMapStruct myUserMapStruct;

    @Resource
    private WeaviateVectorService weaviateVectorService;

    @Resource
    private TikaSemanticChunkService tikaSemanticChunkService;

    @Resource
    private VectorService vectorService;

    @Resource
    private WorkflowClient workflowClient;

    @Test
    public void testMapStruct(){
        UserDTO userDTO = UserDTO.builder().userId(1001).username("jack").build();
        User user = myUserMapStruct.dtoDomain(userDTO);

        System.out.println(user);

    }


    @Test
    public void testWeaviateVectorSave(){
        Book book = Book.builder()
                .id(UUID.randomUUID().toString())
                .name("红楼梦")
                .author("曹雪芹")
                .price(new BigDecimal(10))
                .totalPages(100)
                .build();
        weaviateVectorService.save(book);
    }

    @Test
    public void testWeaviateSearch(){
        List<Document> documents = weaviateVectorService.similaritySearch("文学名著", 10, 0.5);

        System.out.println(documents);
    }

    @Test
    public void testWeaviateUpdate(){
        Book book = Book.builder()
                .id("e2edd416-f108-43cb-93f5-93ede26f3f06")
                .name("红楼梦测试")
                .author("曹雪芹")
                .price(new BigDecimal(10))
                .totalPages(100)
                .build();
        weaviateVectorService.update(book);
    }

    /**
     * minio hello world
     */
    @Test
    void testMinio() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        //构建minio client对象
        MinioClient minioClient = MinioClient.builder()
                .endpoint("http://127.0.0.1:19000")
                .credentials("admin", "admin123456")
                .build();

        //判断桶是否存在
        boolean flag =  minioClient.bucketExists(BucketExistsArgs.builder().bucket("knowledge").build());

        if(!flag){
            //创建桶
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("knowledge").build());
        }

        //读取文件
        FileInputStream fis = new FileInputStream("D:\\sc.png");

        //构建对象上传实例
        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket("knowledge")  //桶名称
                .stream(fis, fis.available(), -1) //上传文件流
                .object("sc.png") //minio中文件名称
                .build();

        //开始上传
        minioClient.putObject(putObjectArgs);

        //获取文件在minio中的url
        String fileUrl = "http://127.0.0.1:19000/knowledge/sc.png";

        System.out.println(fileUrl);
    }

    @Test
    public void textTika() throws FileNotFoundException {
        FileInputStream fis = new FileInputStream("C:\\Users\\鲁瑜\\OneDrive\\ドキュメント\\个人简历.pdf");
        List<String> chunks = tikaSemanticChunkService.pdfToChunk(fis);
        System.out.println(chunks);
    }

    @Test
    public void testVectorService() throws FileNotFoundException {
        FileInputStream fis = new FileInputStream("C:\\Users\\鲁瑜\\OneDrive\\ドキュメント\\个人简历.pdf");
        vectorService.storeText(1,fis, "http://127.0.0.1:19000/knowledge/sc.png");
    }

    @Test
    void testTemporal() {
        LearnWorkflow learnWorkflow = workflowClient.newWorkflowStub(LearnWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(Constant.DOC_TASK_QUEUE) // 任务队列
                        .build()
        );

        //执行工作流
        learnWorkflow.process();
    }

    @Test
    void testRemoveWeaviate(){
        vectorService.deleteBySourceUrl("knowledge/144fac80d41b77b13188b8ddcc869711.pdf");

    }

    @Test
    void testDeleteSchema(){
        vectorService.deleteSchema("Knowledge");
    }
}
