package com.weiz.Familia.dto.errors;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Representa un ejemplo de uso para ayudar al desarrollador.
 *
 * <p>Se utiliza dentro del campo {@code help} de {@link ErrorResponse}
 * para mostrar ejemplos prácticos de cómo usar correctamente un DTO.</p>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Example {

    /**
     * Título descriptivo del ejemplo.
     *
     * <p><strong>Ejemplos:</strong>
     * <ul>
     * <li>"Eliminar por ID único"</li>
     * <li>"Eliminar por lista de IDs"</li>
     * <li>"Eliminar con operadores complejos"</li>
     * </ul>
     */
    private String title;

    /**
     * Código del ejemplo en formato JSON.
     *
     * <p>Debe ser un JSON válido y bien formateado que el desarrollador
     * pueda copiar y adaptar directamente.</p>
     *
     * <p><strong>Ejemplo:</strong>
     * <pre>{@code
     * {
     *   "tableName": "originacionseguros.tipos_documento",
     *   "whereClause": {"id": 4},
     *   "dbType": "db_openfinance"
     * }
     * }</pre>
     */
    private String code;

    // Constructors

    public Example() {
    }

    public Example(String title, String code) {
        this.title = title;
        this.code = code;
    }

    /**
     * Factory method para crear ejemplos de forma fluida.
     *
     * @param title Título del ejemplo
     * @param code Código JSON del ejemplo
     * @return Nueva instancia de Example
     */
    public static Example of(String title, String code) {
        return new Example(title, code);
    }

    // Getters and Setters

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
