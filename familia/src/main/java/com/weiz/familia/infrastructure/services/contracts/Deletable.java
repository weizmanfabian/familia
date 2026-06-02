package com.weiz.familia.infrastructure.services.contracts;

public interface Deletable<Id> {
    void eliminar(Id id);
}
