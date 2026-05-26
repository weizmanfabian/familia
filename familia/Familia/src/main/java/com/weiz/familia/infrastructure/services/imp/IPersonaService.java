package com.weiz.familia.infrastructure.services.imp;

import com.weiz.familia.api.requests.PersonaRequest;
import com.weiz.familia.api.responses.PersonaResponse;
import com.weiz.familia.infrastructure.abstractService.CrudService;

public interface IPersonaService extends CrudService<PersonaRequest, PersonaResponse, String> {
}
