package com.skcto.skknowledge.config;

import com.skcto.skknowledge.constant.Constant;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.schema.model.DataType;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.Schema;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WeaviateConfig {

    private final WeaviateClient weaviateClient;

    /**
     *构建schema，描述collection结构
     */
    @PostConstruct
    public void init(){
        String className = Constant.VECTOR_CLASS_NAME;

        //检查schema是否存在，若不存在则创建
        Result<Schema> schemaResult = weaviateClient.schema().getter().run();
        Schema schema = schemaResult.getResult();

        boolean classExists = schema.getClasses().stream()
                .anyMatch(weaviateClass -> weaviateClass.getClassName().equals(className));

        if(!classExists){
            //创建schema
            //定义属性
            List<Property> propertyList = List.of(
                    Property.builder().name("content").dataType(List.of(DataType.TEXT)).build(),
                    Property.builder().name("meta_knowledgeId").dataType(List.of(DataType.TEXT)).build(),
                    Property.builder().name("meta_source").dataType(List.of(DataType.TEXT)).build(),//pdf文件在minio中的url
                    Property.builder().name("metadata").dataType(List.of(DataType.TEXT)).build()
            );

            //创建class
            WeaviateClass build = WeaviateClass.builder()
                    .className(className)
                    .vectorizer("none")//不使用weaviate中的向量模型，使用外部向量模型
                    .properties(propertyList)
                    .build();

            Result<Boolean> createResult = weaviateClient.schema().classCreator().withClass(build).run();
            if(createResult.hasErrors()){
                System.out.println("创建schema失败" + createResult.getError());
            }

        }
    }
}
