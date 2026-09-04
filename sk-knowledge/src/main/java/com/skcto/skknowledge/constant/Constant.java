package com.skcto.skknowledge.constant;

/**
 * 常量类
 *
 */
public class Constant {

    //JWT的签名
    public static final String SECRET = "12d3845Phd2h3OPYs0q2";

    public static final String REDIS_TOKEN_KEY = "user:login";

    public static final String VECTOR_CLASS_NAME = "Knowledge";

    public static final String SSE_SESSION = "sse:session:";

    public static final String DOC_TASK_QUEUE = "DOC_TASK_QUEUE";

    public static final int CHUNK_SIZE = 512;  // Token大小，调整根据模型
    public static final int OVERLAP = 100;// 重叠大小，调整根据模型

    // 相似度阈值（例如 0.98，超过此值视为重复）
    public static final float SIMILARITY_THRESHOLD = 0.98f;
}
