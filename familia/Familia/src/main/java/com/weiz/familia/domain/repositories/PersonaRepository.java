package com.weiz.Familia.domain.repositories;

import com.weiz.Familia.domain.entities.PersonaEntity;
import org.springframework.data.repository.CrudRepository;

public interface PersonaRepository extends CrudRepository<PersonaEntity, String> {
}
