package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.VectorDb;
import com.skcto.skknowledge.dto.VectorDbDTO;
import com.skcto.skknowledge.vo.VectorDbVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface VectorDbMapstruct extends BasePageMapstruct<VectorDb, VectorDbVo, VectorDbDTO> {
    VectorDbMapstruct INSTANCE = Mappers.getMapper(VectorDbMapstruct.class);

}