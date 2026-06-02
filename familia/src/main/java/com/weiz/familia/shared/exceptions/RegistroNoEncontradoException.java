package com.weiz.familia.shared.exceptions;

public class RegistroNoEncontradoException extends RuntimeException {
    private static final String ERROR_MESSAGE = "Registro no existe en %s";

    public RegistroNoEncontradoException(String tableName) {
        super(String.format(ERROR_MESSAGE, tableName));
    }

}
