package com.weiz.familia.domain.repositories;

import com.weiz.familia.domain.entities.PersonaEntity;
import org.springframework.data.repository.CrudRepository;

public interface PersonaRepository extends CrudRepository<PersonaEntity, String> {
}
