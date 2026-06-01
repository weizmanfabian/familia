package com.weiz.familia.infrastructure.mappers;

import com.weiz.familia.api.requests.PersonaRequest;
import com.weiz.familia.api.responses.PersonaResponse;
import com.weiz.familia.domain.entities.CiudadEntity;
import com.weiz.familia.domain.entities.PersonaEntity;
import com.weiz.familia.shared.enums.OcupacionEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(PersonaMapperTest.MapperTestConfig.class)
class PersonaMapperTest {

    @Configuration
    @ComponentScan("com.weiz.familia.infrastructure.mappers")
    static class MapperTestConfig {
    }

    @Autowired
    private PersonaMapper personaMapper;

    @Test
    @DisplayName("toEntity mapea los campos del request e ignora ciudad y esViable")
    void toEntity_desdeRequest_mapeaCamposEIgnoraCiudadYEsViable() {
        PersonaRequest request = PersonaRequest.builder()
                .numeroDocumento("1234567890")
                .nombre("Juan")
                .apellidos("Pérez Gómez")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .correoElectronico("juan.perez@example.com")
                .telefono("3001234567")
                .ocupacion(OcupacionEnum.INDEPENDIENTE)
                .idCiudad(1)
                .build();

        PersonaEntity entity = personaMapper.toEntity(request);

        assertThat(entity.getNumeroDocumento()).isEqualTo("1234567890");
        assertThat(entity.getNombre()).isEqualTo("Juan");
        assertThat(entity.getApellidos()).isEqualTo("Pérez Gómez");
        assertThat(entity.getFechaNacimiento()).isEqualTo(LocalDate.of(1990, 1, 15));
        assertThat(entity.getCorreoElectronico()).isEqualTo("juan.perez@example.com");
        assertThat(entity.getTelefono()).isEqualTo("3001234567");
        assertThat(entity.getOcupacion()).isEqualTo(OcupacionEnum.INDEPENDIENTE);
        assertThat(entity.getCiudad()).isNull();
        assertThat(entity.isEsViable()).isFalse();
    }

    @Test
    @DisplayName("toResponse mapea la entidad incluyendo la ciudad anidada")
    void toResponse_desdeEntidad_mapeaCamposYCiudadAnidada() {
        CiudadEntity ciudad = CiudadEntity.builder()
                .id(1)
                .nombre("Bogotá")
                .departamento("Cundinamarca")
                .build();
        PersonaEntity entity = PersonaEntity.builder()
                .numeroDocumento("1234567890")
                .nombre("Juan")
                .apellidos("Pérez Gómez")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .correoElectronico("juan.perez@example.com")
                .telefono("3001234567")
                .ocupacion(OcupacionEnum.INDEPENDIENTE)
                .esViable(true)
                .ciudad(ciudad)
                .build();

        PersonaResponse response = personaMapper.toResponse(entity);

        assertThat(response.getNumeroDocumento()).isEqualTo("1234567890");
        assertThat(response.getNombre()).isEqualTo("Juan");
        assertThat(response.getOcupacion()).isEqualTo(OcupacionEnum.INDEPENDIENTE);
        assertThat(response.isEsViable()).isTrue();
        assertThat(response.getCiudad()).isNotNull();
        assertThat(response.getCiudad().getId()).isEqualTo(1);
        assertThat(response.getCiudad().getNombre()).isEqualTo("Bogotá");
        assertThat(response.getCiudad().getDepartamento()).isEqualTo("Cundinamarca");
    }

    @Test
    @DisplayName("actualizar copia solo los no nulos y respeta numeroDocumento, ciudad y esViable")
    void actualizar_conMappingTarget_ignoraNullsYCamposProtegidos() {
        CiudadEntity ciudadOriginal = CiudadEntity.builder().id(1).nombre("Bogotá").departamento("Cundinamarca").build();
        PersonaEntity target = PersonaEntity.builder()
                .numeroDocumento("1234567890")
                .nombre("Juan")
                .apellidos("Pérez Gómez")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .correoElectronico("juan.perez@example.com")
                .telefono("3001234567")
                .ocupacion(OcupacionEnum.EMPLEADO)
                .esViable(true)
                .ciudad(ciudadOriginal)
                .build();

        PersonaRequest request = PersonaRequest.builder()
                .numeroDocumento("9999999999")  // debe ignorarse
                .nombre("Juan Carlos")          // debe actualizarse
                .telefono("3009999999")          // debe actualizarse
                .idCiudad(99)                    // no mapea a la entidad
                .build();                        // el resto null → no debe sobreescribir

        personaMapper.actualizar(target, request);

        assertThat(target.getNombre()).isEqualTo("Juan Carlos");
        assertThat(target.getTelefono()).isEqualTo("3009999999");
        assertThat(target.getNumeroDocumento()).isEqualTo("1234567890");
        assertThat(target.getApellidos()).isEqualTo("Pérez Gómez");
        assertThat(target.getCorreoElectronico()).isEqualTo("juan.perez@example.com");
        assertThat(target.getFechaNacimiento()).isEqualTo(LocalDate.of(1990, 1, 15));
        assertThat(target.getOcupacion()).isEqualTo(OcupacionEnum.EMPLEADO);
        assertThat(target.isEsViable()).isTrue();
        assertThat(target.getCiudad()).isSameAs(ciudadOriginal);
    }
}
