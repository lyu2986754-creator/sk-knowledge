package com.skcto.skknowledge.util;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;

public class SnowflakeUtil {
    // 雪花算法实例
    private static Snowflake snowflake;
    
    static {
        // 获取本机IP的最后一个字节作为数据中心ID
        long dataCenterId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr()) >> 24 & 0xFF;

        // ID，这里先简化处理 todo
        long workerId = 1;
        dataCenterId = 1;
        // 初始化雪花算法实例
        snowflake = IdUtil.createSnowflake(workerId, dataCenterId);
    }
    
    /**
     * 生成下一个唯一ID
     * @return 雪花算法生成的唯一ID
     */
    public static long nextId() {
        return snowflake.nextId();
    }
    
    /**
     * 生成下一个唯一ID的字符串形式
     * @return 雪花算法生成的唯一ID字符串
     */
    public static String nextIdStr() {
        return snowflake.nextIdStr();
    }

}