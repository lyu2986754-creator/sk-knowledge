package com.skcto.skknowledge.temporal;

import io.temporal.spring.boot.ActivityImpl;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl
public class LearnActivitiesImpl implements LearnActivities {

    @Override
    public void storeTextToVectorDB() {
        System.out.println("存储数据到向量数据库里面");
    }

    @Override
    public void storeTextToVectorDBCompensate() {
        System.out.println("向量库补偿");
    }

    @Override
    public void saveToDB() {
        System.out.println("存储数据到数据库");
    }

    @Override
    public void saveToDBCompensate() {
        System.out.println("数据库数据补偿");
    }


}
