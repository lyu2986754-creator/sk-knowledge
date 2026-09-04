package com.skcto.skknowledge.extract;

import com.skcto.skknowledge.result.PdfExtractionResult;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface FileExtract {
    //提取文本数据
    List<String> extractText(InputStream inputStream) throws Exception;

    //提取元数据 文本 图片
    PdfExtractionResult extractMetaAndTextAndImage(InputStream inputStream);
}
