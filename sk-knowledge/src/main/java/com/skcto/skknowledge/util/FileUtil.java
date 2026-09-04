package com.skcto.skknowledge.util;

public class FileUtil {


    public static final String DOC = "doc";
    public static final String DOCX = "docx";
    public static final String PDF = "pdf";
    public static final String XLS = "xls";
    public static final String XLSX = "xlsx";

    public static final String LOG = "log";
    public static final String XML = "xml";

    public static final String TXT = "txt";
    public static final String CSV = "csv";
    public static final String MD = "md";

    public static final String PROPERTIES = "properties";
    public static final String YAML = "yaml";
    public static final String YML = "yml";

    public static boolean isTextFile(String type) {
        if (type.equalsIgnoreCase(TXT) || type.equalsIgnoreCase(CSV) || type.equalsIgnoreCase(PROPERTIES)
                || type.equalsIgnoreCase(YAML) || type.equalsIgnoreCase(YML)
                || type.equalsIgnoreCase(LOG) || type.equalsIgnoreCase(XML)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isMdFile(String type) {
        if (type.equalsIgnoreCase(MD)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isWord(String type) {
        if (type.equalsIgnoreCase(DOC) || type.equalsIgnoreCase(DOCX)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPdf(String type) {
        if (type.equalsIgnoreCase(PDF)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isExcel(String type) {
        if (type.equalsIgnoreCase(XLS) || type.equalsIgnoreCase(XLSX)) {
            return true;
        } else {
            return false;
        }
    }


    public static String generateFileName(String originalFilename) {
        // 1. 提取原文件后缀
        String suffix = "";
        if (originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 2. 生成唯一标识（可雪花 ID）
        String uniqueId = SnowflakeUtil.nextIdStr();

        return uniqueId + suffix;
    }
}
