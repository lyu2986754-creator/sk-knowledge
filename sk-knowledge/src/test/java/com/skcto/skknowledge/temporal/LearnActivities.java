package com.skcto.skknowledge.temporal;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface LearnActivities {

    void storeTextToVectorDB();
    //补偿
    void storeTextToVectorDBCompensate();

    void saveToDB();
    //补偿
    void saveToDBCompensate();

}
