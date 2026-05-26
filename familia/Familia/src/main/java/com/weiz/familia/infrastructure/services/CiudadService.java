package com.weiz.familia.infrastructure.services;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.weiz.familia.api.responses.CiudadResponse;
import com.weiz.familia.domain.entities.CiudadEntity;
import com.weiz.familia.domain.repositories.CiudadRepository;
import com.weiz.familia.infrastructure.services.contracts.Listable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CiudadService implements Listable<CiudadResponse> {

    private final CiudadRepository ciudadRepository;

    @Override
    public Set<CiudadResponse> consultarTodas() {
        return StreamSupport.stream(ciudadRepository.findAll().spliterator(), false)
                .map(CiudadEntity::entityToResponse)
                .collect(Collectors.toSet());
    }
}
