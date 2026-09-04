package com.skcto.skknowledge.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.domain.KnowledgeTrash;
import com.skcto.skknowledge.mapper.KnowledgeTrashMapper;
import com.skcto.skknowledge.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.K;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyTask {

    private final KnowledgeTrashMapper knowledgeTrashMapper;

    private final MinioService minioService;

    /**
     * 定时任务
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void deleteTrash() {
        //获取30天前的数据
        LocalDate localDate = LocalDate.now().minusDays(30);

        LambdaQueryWrapper<KnowledgeTrash> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.le(KnowledgeTrash::getTrashTime, localDate);

        //获取要删除的文档数据
        List<KnowledgeTrash> knowledgeTrashes = knowledgeTrashMapper.selectList(queryWrapper);

        knowledgeTrashes.forEach(knowledgeTrash -> {
            //删除trash中的数据
            knowledgeTrashMapper.deleteById(knowledgeTrash);

            //获取url
            String url = knowledgeTrash.getUrl();

            int index = url.indexOf("/");
            String buckName = url.substring(0, index);
            String objectName = url.substring(index + 1);

            //删除minio中的数据
            try {
                minioService.deleteFile(buckName, objectName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        });


    }


}