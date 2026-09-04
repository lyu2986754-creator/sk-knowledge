package com.skcto.skknowledge.extract;

import com.skcto.skknowledge.result.PdfExtractionResult;
import com.skcto.skknowledge.service.impl.TikaSemanticChunkService;
import com.skcto.skknowledge.util.SnowflakeUtil;
import com.skcto.skknowledge.util.SpringContextUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.PageIterator;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
public class PdfExtract implements FileExtract{

    /**
     * 提取pdf中的文本数据
     * @param inputStream
     * @return
     * @throws Exception
     */
    @Override
    public List<String> extractText(InputStream inputStream) throws Exception {
        //获取spring容器中的tikaSemanticChunkService对象
        TikaSemanticChunkService tikaSemanticChunkService = SpringContextUtil.getBean(TikaSemanticChunkService.class);
        return tikaSemanticChunkService.pdfToChunk(inputStream);
    }

    /**
     * 提取pdf中的元数据 文本数据 图片数据
     * @param inputStream
     * @return
     */
    @Override
    public PdfExtractionResult extractMetaAndTextAndImage(InputStream inputStream) {
        return null;
    }
}

