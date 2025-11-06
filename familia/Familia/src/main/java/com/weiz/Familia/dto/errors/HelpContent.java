package com.weiz.Familia.dto.errors;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Contenido de ayuda contextual para errores de validación.
 *
 * <p>Proporciona información detallada para ayudar al desarrollador a corregir
 * errores en la estructura de los DTOs, incluyendo ejemplos prácticos y
 * enlaces a documentación.</p>
 *
 * <p><strong>Ejemplo de uso en ErrorResponse:</strong>
 * <pre>{@code
 * {
 *   "timestamp": 1759338368198,
 *   "status": 400,
 *   "errorType": "VALIDATION_ERROR",
 *   "message": "whereClause inválido",
 *   "help": {
 *     "field": "whereClause",
 *     "description": "El whereClause debe ser un objeto JSON con condiciones válidas",
 *     "examples": [
 *       {
 *         "title": "Eliminar por ID único",
 *         "code": "{\"whereClause\": {\"id\": 4}}"
 *       }
 *     ],
 *     "documentation": "https://docs.credifamilia.com/dbmaster/complex-delete#whereclause"
 *   }
 * }
 * }</pre>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HelpContent {

    /**
     * Nombre del campo que causó el error.
     *
     * <p><strong>Ejemplos:</strong> {@code "whereClause"}, {@code "tableName"}, {@code "dbType"}
     */
    private String field;

    /**
     * Descripción breve y clara del problema y cómo resolverlo.
     *
     * <p>Debe ser una explicación concisa que el desarrollador pueda entender
     * rápidamente sin necesidad de consultar documentación adicional.</p>
     */
    private String description;

    /**
     * Lista de ejemplos prácticos que muestran el uso correcto.
     *
     * <p>Cada ejemplo debe incluir un título descriptivo y código JSON
     * que el desarrollador pueda copiar y adaptar.</p>
     */
    private List<Example> examples;

    /**
     * URL a la documentación completa (opcional).
     *
     * <p>Puede apuntar a documentación interna, Swagger, o wikis del equipo.</p>
     */
    private String documentation;

    // Constructors

    public HelpContent() {
        this.examples = new ArrayList<>();
    }

    public HelpContent(String field, String description) {
        this();
        this.field = field;
        this.description = description;
    }

    public HelpContent(String field, String description, List<Example> examples) {
        this.field = field;
        this.description = description;
        this.examples = examples != null ? examples : new ArrayList<>();
    }

    public HelpContent(String field, String description, List<Example> examples, String documentation) {
        this(field, description, examples);
        this.documentation = documentation;
    }

    // Builder pattern

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String field;
        private String description;
        private List<Example> examples = new ArrayList<>();
        private String documentation;

        public Builder field(String field) {
            this.field = field;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder examples(List<Example> examples) {
            this.examples = examples;
            return this;
        }

        public Builder addExample(String title, String code) {
            this.examples.add(Example.of(title, code));
            return this;
        }

        public Builder documentation(String documentation) {
            this.documentation = documentation;
            return this;
        }

        public HelpContent build() {
            return new HelpContent(field, description, examples, documentation);
        }
    }

    // Getters and Setters

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Example> getExamples() {
        return examples;
    }

    public void setExamples(List<Example> examples) {
        this.examples = examples;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }
}
