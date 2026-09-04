package com.skcto.skknowledge;

import jakarta.annotation.Resource;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@MapperScan("com.skcto.skknowledge.mapper")
public class SkKnowledgeApplication implements CommandLineRunner {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(SkKnowledgeApplication.class, args);
    }

    @Override
    public void run(String... args)  {
        redisTemplate.setKeySerializer(RedisSerializer.string()); //redis的key采用string进行序列化
        redisTemplate.setValueSerializer(RedisSerializer.json()); //如果放入redis的时候是一个对象，那么建议采用json序列化
        redisTemplate.setHashKeySerializer(RedisSerializer.string()); //redis的hashKey采用string进行序列化
        redisTemplate.setHashValueSerializer(RedisSerializer.string()); //redis的hashValue采用string进行序列化
    }
}
