package com.skcto.skknowledge.temporal;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface LearnWorkflow {

    //工作流入口方法
    @WorkflowMethod
    String process();
}
