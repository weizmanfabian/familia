package com.weiz.Familia.infraestructure.services;

import com.weiz.Familia.api.requests.PersonaRequest;
import com.weiz.Familia.api.responses.PersonaResponse;
import com.weiz.Familia.domain.entities.PersonaEntity;
import com.weiz.Familia.domain.repositories.CiudadRepository;
import com.weiz.Familia.domain.repositories.PersonaRepository;
import com.weiz.Familia.infraestructure.services.imp.IPersonaService;
import com.weiz.Familia.util.Exceptions.CustomException;
import com.weiz.Familia.util.Exceptions.IdNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonaService implements IPersonaService {

    private final CiudadRepository ciudadRepository;
    private final PersonaRepository personaRepository;

    @Override
    public Set<PersonaResponse> readAll() {
        return StreamSupport.stream(personaRepository.findAll().spliterator(), false)
                .map(PersonaEntity::entityToResponse)
                .collect(Collectors.toSet());
    }

    @Override
    public PersonaResponse create(PersonaRequest request) {
        personaRepository.findById(request.getNumero_documento())
                .ifPresent(p -> {
                    throw new CustomException(String.format("La persona con documento %s ya existe", request.getNumero_documento()));
                });

        var ciudad = ciudadRepository.findById(request.getIdCiudad()).orElseThrow(() -> new IdNotFoundException("Ciudad"));
        var personaPrePersist = PersonaEntity.requestToEntity(request);
        personaPrePersist.setCiudad(ciudad);
        var personaPersisted = personaRepository.save(personaPrePersist);
        return PersonaEntity.entityToResponse(personaPersisted);
    }

    @Override
    public PersonaResponse readById(String numero_documento) {
        var persona = personaRepository.findById(numero_documento).orElseThrow(()-> new IdNotFoundException("Persona"));
        return PersonaEntity.entityToResponse(persona);
    }

    @Override
    public PersonaResponse update(PersonaRequest request, String numero_documento) throws InvocationTargetException, IllegalAccessException {
        var personaSaved = personaRepository.findById(numero_documento).orElseThrow(() -> new IdNotFoundException("Persona"));
        var ciudad = ciudadRepository.findById(request.getIdCiudad()).orElseThrow(() -> new IdNotFoundException("Ciudad"));
        var personaCurrent = PersonaEntity.requestToEntity(request);
        personaCurrent.setCiudad(ciudad);
        personaSaved.merge(personaCurrent);
        var personaPersisted = personaRepository.save(personaSaved);
        return PersonaEntity.entityToResponse(personaPersisted);
    }

    @Override
    public void delete(String numero_documento) {
        personaRepository.findById(numero_documento).orElseThrow(() -> new IdNotFoundException("Persona"));
        personaRepository.deleteById(numero_documento);
    }
}
