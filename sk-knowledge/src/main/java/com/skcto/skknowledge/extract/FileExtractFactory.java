package com.skcto.skknowledge.extract;

import com.skcto.skknowledge.constant.FileType;

public class FileExtractFactory {

    /**
     * 获取文件解析器
     * @param docType
     * @return
     */
    public static FileExtract getFileExtract(String docType) {
        if(FileType.isTextFile(docType)){

        } else if (FileType.isWord(docType)) {

        } else if (FileType.isPdf(docType)) {
            return new PdfExtract();
        }else{
            return null;
        }

        return null;
    }
}
