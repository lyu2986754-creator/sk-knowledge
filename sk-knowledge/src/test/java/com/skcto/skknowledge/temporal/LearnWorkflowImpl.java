package com.skcto.skknowledge.temporal;

import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Saga;
import io.temporal.workflow.Workflow;

import java.time.Duration;

@WorkflowImpl
public class LearnWorkflowImpl implements LearnWorkflow {

    //配置
    private final LearnActivities learnActivities =
            Workflow.newActivityStub(LearnActivities.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(Duration.ofSeconds(20))//超时时间
                            .setRetryOptions(
                                    RetryOptions.newBuilder()
                                            .setMaximumAttempts(0)  //最大重试次数
                                            /*
                                                指数退避
                                                1s,2s,4s,8s,16s(10s)
                                             */
                                            .setInitialInterval(Duration.ofSeconds(1))  //初始重试间隔
                                            .setMaximumInterval(Duration.ofSeconds(10))  //最大重试间隔
                                            .build()
                            ).build());


    /**
     * 编排工作流
     */
    @Override
    public String process(){
        //saga 串行补偿 长事务
        Saga saga = new Saga(new Saga.Options.Builder().setParallelCompensation(false).build());
        try {
            //先注册补偿操作
            saga.addCompensation(learnActivities::storeTextToVectorDBCompensate);
            //业务逻辑
            learnActivities.storeTextToVectorDB();

            //注册补偿操作
            saga.addCompensation(learnActivities::saveToDBCompensate);
            //业务逻辑
            learnActivities.saveToDB();

            return "处理成功";
        }catch (Exception e){
            saga.compensate();//补偿
            throw new RuntimeException(e);
        }
    }
}
