package com.weiz.familia.infrastructure.services;

import com.weiz.familia.api.requests.PersonaRequest;
import com.weiz.familia.api.responses.PersonaResponse;
import com.weiz.familia.domain.entities.CiudadEntity;
import com.weiz.familia.domain.entities.PersonaEntity;
import com.weiz.familia.domain.repositories.CiudadRepository;
import com.weiz.familia.domain.repositories.PersonaRepository;
import com.weiz.familia.infrastructure.mappers.PersonaMapper;
import com.weiz.familia.shared.enums.OcupacionEnum;
import com.weiz.familia.shared.exceptions.CustomException;
import com.weiz.familia.shared.exceptions.RegistroNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    private static final String DOCUMENTO = "1234567890";
    private static final int ID_CIUDAD = 1;

    @Mock
    private CiudadRepository ciudadRepository;
    @Mock
    private PersonaRepository personaRepository;
    @Mock
    private PersonaMapper personaMapper;
    @InjectMocks
    private PersonaService personaService;

    @Test
    @DisplayName("crear con datos válidos persiste y retorna el response mapeado")
    void crear_conDatosValidos_persisteYRetornaResponse() {
        PersonaRequest request = requestValido();
        PersonaEntity entity = entityConFecha();
        PersonaResponse response = responseEsperado();

        when(personaRepository.findById(DOCUMENTO)).thenReturn(Optional.empty());
        when(ciudadRepository.findById(ID_CIUDAD)).thenReturn(Optional.of(ciudadEntity()));
        when(personaMapper.toEntity(request)).thenReturn(entity);
        when(personaRepository.save(entity)).thenReturn(entity);
        when(personaMapper.toResponse(entity)).thenReturn(response);

        PersonaResponse resultado = personaService.crear(request);

        assertThat(resultado).isEqualTo(response);
        verify(personaRepository).save(entity);
    }

    @Test
    @DisplayName("crear con documento existente lanza CustomException y no persiste")
    void crear_conDocumentoDuplicado_lanzaCustomException() {
        when(personaRepository.findById(DOCUMENTO)).thenReturn(Optional.of(entityConFecha()));

        assertThatThrownBy(() -> personaService.crear(requestValido()))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("ya existe");

        verify(personaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("crear con ciudad inexistente lanza RegistroNoEncontradoException")
    void crear_conCiudadInexistente_lanzaRegistroNoEncontrado() {
        when(personaRepository.findById(DOCUMENTO)).thenReturn(Optional.empty());
        when(ciudadRepository.findById(ID_CIUDAD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.crear(requestValido()))
                .isInstanceOf(RegistroNoEncontradoException.class)
                .hasMessageContaining("Ciudad");
    }

    @Test
    @DisplayName("consultarPorId existente retorna el response")
    void consultarPorId_existente_retornaResponse() {
        PersonaEntity entity = entityConFecha();
        PersonaResponse response = responseEsperado();
        when(personaRepository.findById(DOCUMENTO)).thenReturn(Optional.of(entity));
        when(personaMapper.toResponse(entity)).thenReturn(response);

        assertThat(personaService.consultarPorId(DOCUMENTO)).isEqualTo(response);
    }

    @Test
    @DisplayName("consultarPorId inexistente lanza RegistroNoEncontradoException")
    void consultarPorId_inexistente_lanzaRegistroNoEncontrado() {
        when(personaRepository.findById(DOCUMENTO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.consultarPorId(DOCUMENTO))
                .isInstanceOf(RegistroNoEncontradoException.class)
                .hasMessageContaining("Persona");
    }

    @Test
    @DisplayName("actualizar con datos válidos delega en el mapper y persiste")
    void actualizar_conDatosValidos_actualizaYRetornaResponse() {
        PersonaRequest request = requestValido();
        PersonaEntity existente = entityConFecha();
        PersonaResponse response = responseEsperado();

        when(personaRepository.findById(DOCUMENTO)).thenReturn(Optional.of(existente));
        when(ciudadRepository.findById(ID_CIUDAD)).thenReturn(Optional.of(ciudadEntity()));
        when(personaRepository.save(existente)).thenReturn(existente);
        when(personaMapper.toResponse(existente)).thenReturn(response);

        PersonaResponse resultado = personaService.actualizar(request, DOCUMENTO);

        assertThat(resultado).isEqualTo(response);
        verify(personaMapper).actualizar(existente, request);
        verify(personaRepository).save(existente);
    }

    @Test
    @DisplayName("actualizar con persona inexistente lanza RegistroNoEncontradoException")
    void actualizar_personaInexistente_lanzaRegistroNoEncontrado() {
        when(personaRepository.findById(DOCUMENTO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.actualizar(requestValido(), DOCUMENTO))
                .isInstanceOf(RegistroNoEncontradoException.class)
                .hasMessageContaining("Persona");
    }

    @Test
    @DisplayName("actualizar con ciudad inexistente lanza RegistroNoEncontradoException")
    void actualizar_ciudadInexistente_lanzaRegistroNoEncontrado() {
        when(personaRepository.findById(DOCUMENTO)).thenReturn(Optional.of(entityConFecha()));
        when(ciudadRepository.findById(ID_CIUDAD)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personaService.actualizar(requestValido(), DOCUMENTO))
                .isInstanceOf(RegistroNoEncontradoException.class)
                .hasMessageContaining("Ciudad");

        verify(personaRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("eliminar existente borra por id")
    void eliminar_existente_borraPorId() {
        when(personaRepository.existsById(DOCUMENTO)).thenReturn(true);

        personaService.eliminar(DOCUMENTO);

        verify(personaRepository).deleteById(DOCUMENTO);
    }

    @Test
    @DisplayName("eliminar inexistente lanza RegistroNoEncontradoException y no borra")
    void eliminar_inexistente_lanzaRegistroNoEncontrado() {
        when(personaRepository.existsById(DOCUMENTO)).thenReturn(false);

        assertThatThrownBy(() -> personaService.eliminar(DOCUMENTO))
                .isInstanceOf(RegistroNoEncontradoException.class)
                .hasMessageContaining("Persona");

        verify(personaRepository, never()).deleteById(anyString());
    }

    @Test
    @DisplayName("consultarTodas retorna el conjunto de responses mapeados")
    void consultarTodas_conRegistros_retornaSetMapeado() {
        PersonaEntity entityUno = entityConFecha();
        PersonaEntity entityDos = PersonaEntity.builder()
                .numeroDocumento("9876543210")
                .fechaNacimiento(LocalDate.of(1985, 3, 10))
                .build();
        PersonaResponse responseUno = responseEsperado();
        PersonaResponse responseDos = PersonaResponse.builder().numeroDocumento("9876543210").build();

        when(personaRepository.findAll()).thenReturn(List.of(entityUno, entityDos));
        when(personaMapper.toResponse(entityUno)).thenReturn(responseUno);
        when(personaMapper.toResponse(entityDos)).thenReturn(responseDos);

        Set<PersonaResponse> resultado = personaService.consultarTodas();

        assertThat(resultado).containsExactlyInAnyOrder(responseUno, responseDos);
    }

    private PersonaRequest requestValido() {
        return PersonaRequest.builder()
                .numeroDocumento(DOCUMENTO)
                .nombre("Juan")
                .apellidos("Pérez Gómez")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .correoElectronico("juan.perez@example.com")
                .telefono("3001234567")
                .ocupacion(OcupacionEnum.INDEPENDIENTE)
                .idCiudad(ID_CIUDAD)
                .build();
    }

    private PersonaEntity entityConFecha() {
        return PersonaEntity.builder()
                .numeroDocumento(DOCUMENTO)
                .nombre("Juan")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .build();
    }

    private CiudadEntity ciudadEntity() {
        return CiudadEntity.builder().id(ID_CIUDAD).nombre("Bogotá").departamento("Cundinamarca").build();
    }

    private PersonaResponse responseEsperado() {
        return PersonaResponse.builder().numeroDocumento(DOCUMENTO).build();
    }
}
