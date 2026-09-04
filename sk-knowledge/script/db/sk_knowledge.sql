/*
 Navicat MySQL Dump SQL

 Source Server         : 192.168.91.130
 Source Server Type    : MySQL
 Source Server Version : 80400 (8.4.0)
 Source Host           : 192.168.91.130:3306
 Source Schema         : sk_knowledge

 Target Server Type    : MySQL
 Target Server Version : 80400 (8.4.0)
 File Encoding         : 65001

*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chat_model
-- ----------------------------
DROP TABLE IF EXISTS `chat_model`;
CREATE TABLE `chat_model`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键，自动增长，用户ID',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型分类',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型名称',
  `describe` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型描述',
  `price` double NULL DEFAULT NULL COMMENT '模型价格',
  `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型类型',
  `api_host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求地址',
  `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密钥',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '编辑人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '编辑时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '聊天模型' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_model
-- ----------------------------
INSERT INTO `chat_model` VALUES (2, 'chat', 'Qwen/Qwen3-8B', 'qwen', 1, 'chat', 'https://api.siliconflow.cn/', 'sk-snclmbrvfkuxdynpkhjyckopuzqsguyfjstcxixydosiijhc', 1, '2026-01-31 14:12:22', NULL, NULL, 'nostrud amet dolore laboris');
INSERT INTO `chat_model` VALUES (3, 'chat', 'Qwen/Qwen3-8B', 'incididunt', 1, 'chat', 'https://api.siliconflow.cn/', 'sk-snclmbrvfkuxdynpkhjyckopuzqsguyfjstcxixydosiijhc', 1, '2026-01-31 14:34:47', NULL, NULL, 'nostrud amet dolore laboris');
INSERT INTO `chat_model` VALUES (17, 'gpt', 'BAAI', '向量数据库', 0, 'embedding', 'https://api.siliconflow.cn/v1/embeddings/BAAI/bge-m3', 'nvkxmwncudizdyovgilvrydsfxpgvpkpbkwqguicdjcmrgep', 1, '2026-01-31 10:36:38', NULL, NULL, '');

-- ----------------------------
-- Table structure for chat_prompt
-- ----------------------------
DROP TABLE IF EXISTS `chat_prompt`;
CREATE TABLE `chat_prompt`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `chat_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提示词模板名称',
  `chat_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '提示词模板内容',
  `category` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提示词分类，knowledge 知识库类型，chat 对话类型，draw绘画类型 ...',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '提示词模板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_prompt
-- ----------------------------

-- ----------------------------
-- Table structure for knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `knowledge_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '知识库名称',
  `vector_model_id` int NULL DEFAULT NULL COMMENT '向量库id',
  `embedding_model_id` int NULL DEFAULT NULL COMMENT '向量模型id',
  `share` tinyint NULL DEFAULT NULL COMMENT '是否公开知识库（0 否 1是）',
  `description` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '描述',
  `knowledge_separator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '知识分隔符',
  `question_separator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提问分隔符',
  `overlap_char` int NULL DEFAULT NULL COMMENT '重叠字符数',
  `retrieve_limit` int NULL DEFAULT NULL COMMENT '知识库中检索的条数',
  `text_block_size` int NULL DEFAULT NULL COMMENT '文本块大小',
  `system_prompt` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '系统提示词',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '知识库' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_base
-- ----------------------------

-- ----------------------------
-- Table structure for knowledge_doc
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_doc`;
CREATE TABLE `knowledge_doc`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `knowledge_id` int NOT NULL COMMENT '知识库ID',
  `doc_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文档名称',
  `doc_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档类型',
  `doc_size` bigint NULL DEFAULT NULL COMMENT '文档大小 单位 字节',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '对象存储地址',
  `vector_status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '写入向量数据库状态1未开始，2进行中，3已完成',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '知识库附件' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_doc
-- ----------------------------

-- ----------------------------
-- Table structure for knowledge_fragment
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_fragment`;
CREATE TABLE `knowledge_fragment`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `knowledge_id` int NOT NULL COMMENT '知识库ID',
  `doc_id` int NOT NULL COMMENT '文档ID',
  `idx` int NOT NULL COMMENT '片段索引下标',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档内容',
  `dept_id` int NOT NULL COMMENT '部门',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1961333201387827202 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '知识片段' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_fragment
-- ----------------------------

-- ----------------------------
-- Table structure for knowledge_trash
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_trash`;
CREATE TABLE `knowledge_trash`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `knowledge_id` int NOT NULL COMMENT '知识库ID',
  `doc_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文档名称',
  `doc_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文档类型',
  `doc_size` bigint NULL DEFAULT NULL COMMENT '文档大小 单位 字节',
  `url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '对象存储地址',
  `trash_time` datetime NULL DEFAULT NULL COMMENT '放入回收站时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '知识库回收站' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of knowledge_trash
-- ----------------------------

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `url` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `parent_id` int NULL DEFAULT NULL,
  `order_no` int NULL DEFAULT NULL,
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `component` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单对应要渲染的Vue组件名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1114 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, '聊天', NULL, '/api/chat', 'menu', 0, 1, NULL, NULL);
INSERT INTO `sys_permission` VALUES (2, '发送聊天', 'chat:query', NULL, 'button', 1, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `role` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'admin', '管理员');

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `role_id` int NULL DEFAULT NULL,
  `permission_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sys_role_permission_ibfk_1`(`role_id` ASC) USING BTREE,
  INDEX `sys_role_permission_ibfk_2`(`permission_id` ASC) USING BTREE,
  CONSTRAINT `sys_role_permission_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `sys_role_permission_ibfk_2` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 77 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色权限关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, 1);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键，自动增长，用户ID',
  `username` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '登录账号',
  `password` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '登录密码',
  `name` varchar(32) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '用户姓名',
  `phone` varchar(18) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '用户手机',
  `email` varchar(64) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '用户邮箱',
  `account_no_expired` int NULL DEFAULT NULL COMMENT '账户是否没有过期，0已过期 1正常',
  `credentials_no_expired` int NULL DEFAULT NULL COMMENT '密码是否没有过期，0已过期 1正常',
  `account_no_locked` int NULL DEFAULT NULL COMMENT '账号是否没有锁定，0已锁定 1正常',
  `account_enabled` int NULL DEFAULT NULL COMMENT '账号是否启用，0禁用 1启用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '编辑时间',
  `update_by` int NULL DEFAULT NULL COMMENT '编辑人',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最近登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `login_act`(`username` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 50 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$xE4D9VDyp9ANFhc1I9/a.OOIss4buU0JbepVN04btz7UbuwhjPHwq', '管理员', '13700000000', 'admin@skcto.com', 1, 1, 1, 1, '2026-01-21 09:37:12', 1, '2026-01-21 09:37:12', 1, '2026-01-21 09:37:12');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NULL DEFAULT NULL,
  `role_id` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sys_user_role_ibfk_1`(`user_id` ASC) USING BTREE,
  INDEX `sys_user_role_ibfk_2`(`role_id` ASC) USING BTREE,
  CONSTRAINT `sys_user_role_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `sys_user_role_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关系表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);

-- ----------------------------
-- Table structure for vector_db
-- ----------------------------
DROP TABLE IF EXISTS `vector_db`;
CREATE TABLE `vector_db`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `vector_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '向量库类型：MILVUS/weaviate',
  `host` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务地址',
  `port` int NOT NULL COMMENT '端口',
  `api_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证密钥（如Qdrant的API Key）',
  `protocol` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '协议(http/https)',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
  `create_by` int NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` int NULL DEFAULT NULL COMMENT '编辑人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '编辑时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of vector_db
-- ----------------------------
INSERT INTO `vector_db` VALUES (1, 'weaviate', '192.168.255.130', 8088, 'demo-key', 'http', NULL, NULL, 1, NULL, NULL, NULL, NULL);
INSERT INTO `vector_db` VALUES (2, 'milvus', '192.168.255.130', 19530, '', NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
