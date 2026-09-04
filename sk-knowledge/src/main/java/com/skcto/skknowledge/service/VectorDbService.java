package com.skcto.skknowledge.service;

import com.skcto.skknowledge.domain.VectorDb;
import com.baomidou.mybatisplus.extension.service.IService;
import com.skcto.skknowledge.vo.VectorDbVo;

import java.util.List;

public interface VectorDbService extends IService<VectorDb> {


    List<VectorDbVo> queryVectorDbVo();
}
