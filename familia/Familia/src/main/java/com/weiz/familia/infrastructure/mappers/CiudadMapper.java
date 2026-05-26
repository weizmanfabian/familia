package com.weiz.familia.infrastructure.mappers;

import com.weiz.familia.api.responses.CiudadResponse;
import com.weiz.familia.domain.entities.CiudadEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CiudadMapper {

    CiudadResponse toResponse(CiudadEntity entity);
}
