package com.skcto.skknowledge.temporal;

import com.skcto.skknowledge.constant.Constant;
import io.temporal.client.WorkflowClient;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class TemporalConfig {

    /**
     * 创建 Worker 工厂
     * @param workflowClient
     * @return
     */
    @Bean(destroyMethod = "shutdown")//关闭容器时调用shutdown方法
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        WorkerFactory factory = WorkerFactory.newInstance(workflowClient);
        factory.start(); // 启动工厂，所有 Worker 会同时开始监听任务
        return factory;
    }

    /**
     * 创建 Worker
     */
    @Bean
    public Worker orderWorker(WorkerFactory factory, LearnActivitiesImpl learnActivities) {
        // 创建 Worker，绑定任务队列
        Worker worker = factory.newWorker(Constant.DOC_TASK_QUEUE);

        // 注册相关的 Workflow 和 Activity
        worker.registerWorkflowImplementationTypes(LearnWorkflowImpl.class);
        worker.registerActivitiesImplementations(learnActivities);
        return worker;
    }
}