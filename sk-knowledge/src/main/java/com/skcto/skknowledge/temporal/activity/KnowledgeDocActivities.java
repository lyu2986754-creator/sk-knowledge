package com.skcto.skknowledge.temporal.activity;

import com.skcto.skknowledge.domain.KnowledgeDoc;
import io.temporal.activity.ActivityInterface;
import org.springframework.web.multipart.MultipartFile;

@ActivityInterface
public interface KnowledgeDocActivities {
    void uploadFileCompensate(String url);

    /*
            将文本块存到weaviate里面
             */
    void storeTextToDB(KnowledgeDoc knowledgeDoc);

    void storeTextToDBCompensation(KnowledgeDoc knowledgeDoc);

    void saveToDB(KnowledgeDoc knowledgeDoc);

    void saveToDBCompensate(KnowledgeDoc knowledgeDoc);
}
