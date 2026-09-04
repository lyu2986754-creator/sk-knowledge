package com.skcto.skknowledge.splitter;

import com.skcto.skknowledge.service.KnowledgeBaseService;
import com.skcto.skknowledge.vo.KnowledgeBaseVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
@Primary
public class CharacterTextSplitter implements TextSplitter {

    private final KnowledgeBaseService knowledgeBaseService;


}
