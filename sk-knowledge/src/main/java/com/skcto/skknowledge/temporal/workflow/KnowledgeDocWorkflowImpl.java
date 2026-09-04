package com.skcto.skknowledge.temporal.workflow;

import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.temporal.activity.KnowledgeDocActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl
public class KnowledgeDocWorkflowImpl implements KnowledgeDocWorkflow {

    private final KnowledgeDocActivities knowledgeDocActivities =
            Workflow.newActivityStub(KnowledgeDocActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(20)) //超时
                            .setRetryOptions(
                                    RetryOptions.newBuilder()
                                            .setMaximumAttempts(3)  //最大重试次数
                                            .setInitialInterval(Duration.ofSeconds(1))
                                            .setMaximumInterval(Duration.ofSeconds(10))
                                            .build()
                            ).build()
            );

    /**
     * 编排工作流
     */
    @Override
    public String processKnowledgeDoc(KnowledgeDoc knowledgeDoc) {
        //saga 串行补偿 长事务
        Saga saga = new Saga(new Saga.Options.Builder().setParallelCompensation(false).build());

        try {
            //补偿minio
            saga.addCompensation(knowledgeDocActivities::uploadFileCompensate, knowledgeDoc.getUrl());

            //补偿向量数据库
            saga.addCompensation(knowledgeDocActivities::storeTextToDBCompensation, knowledgeDoc);
            //向量数据库存储
            knowledgeDocActivities.storeTextToDB(knowledgeDoc);

            //补偿mysql
            saga.addCompensation(knowledgeDocActivities::saveToDBCompensate, knowledgeDoc);
            knowledgeDocActivities.saveToDB(knowledgeDoc);

            return "处理成功";
        } catch (Exception e) {
            saga.compensate();//补偿
            throw new RuntimeException(e);
        }
    }


}
