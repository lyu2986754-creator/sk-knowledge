package com.skcto.skknowledge.temporal.workflow;

import com.skcto.skknowledge.domain.KnowledgeDoc;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import org.springframework.web.multipart.MultipartFile;

@WorkflowInterface
public interface KnowledgeDocWorkflow {

    @WorkflowMethod
    String processKnowledgeDoc(KnowledgeDoc knowledgeDoc);
}