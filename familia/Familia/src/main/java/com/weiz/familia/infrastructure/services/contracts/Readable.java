package com.weiz.familia.infrastructure.services.contracts;

public interface Readable<Res, Id> {
    Res consultarPorId(Id id);
}
