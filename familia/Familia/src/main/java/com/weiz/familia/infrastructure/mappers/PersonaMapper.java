package com.weiz.familia.infrastructure.mappers;

import com.weiz.familia.api.requests.PersonaRequest;
import com.weiz.familia.api.responses.PersonaResponse;
import com.weiz.familia.domain.entities.PersonaEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = CiudadMapper.class)
public interface PersonaMapper {

    @Mapping(target = "ciudad", ignore = true)
    @Mapping(target = "esViable", ignore = true)
    PersonaEntity toEntity(PersonaRequest request);

    PersonaResponse toResponse(PersonaEntity entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "numeroDocumento", ignore = true)
    @Mapping(target = "ciudad", ignore = true)
    @Mapping(target = "esViable", ignore = true)
    void actualizar(@MappingTarget PersonaEntity target, PersonaRequest request);
}
