package com.weiz.Familia.dto.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.weiz.Familia.config.EpochMillisToLocalDateTimeDeserializer;
import com.weiz.Familia.config.LocalDateTimeToEpochMillisSerializer;

import java.time.LocalDateTime;

/**
 * DTO estandarizado para respuestas de error.
 *
 * <p>Proporciona información detallada sobre errores que ocurren en la aplicación,
 * clasificándolos por tipo para facilitar el debugging y la resolución de problemas.</p>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /**
     * Timestamp cuando ocurrió el error.
     *
     * <p><strong>Formato de serialización:</strong> Epoch milliseconds (Long)</p>
     * <p>Se serializa como un número entero de 13 dígitos representando milisegundos desde 1970-01-01T00:00:00Z</p>
     *
     * <p><strong>Ejemplo JSON:</strong> {@code "timestamp": 1759338368198}</p>
     *
     * @see LocalDateTimeToEpochMillisSerializer
     * @see EpochMillisToLocalDateTimeDeserializer
     */
    @JsonSerialize(using = LocalDateTimeToEpochMillisSerializer.class)
    @JsonDeserialize(using = EpochMillisToLocalDateTimeDeserializer.class)
    private LocalDateTime timestamp;

    /**
     * Código HTTP del error (400, 500, etc.)
     */
    private int status;

    /**
     * Tipo de error para clasificación
     * Valores posibles: VALIDATION_ERROR, DATABASE_ERROR, BUSINESS_ERROR, SYSTEM_ERROR
     */
    private String errorType;

    /**
     * Mensaje descriptivo del error para el usuario
     */
    private String message;

    /**
     * Detalle técnico adicional del error (opcional, útil para debugging)
     */
    private String detail;

    /**
     * Path del endpoint donde ocurrió el error
     */
    private String path;

    /**
     * Información de ayuda contextual para errores de validación.
     *
     * <p><strong>Solo se incluye para errores de tipo VALIDATION_ERROR.</strong>
     * Proporciona ejemplos prácticos y guías para corregir el error.</p>
     *
     * <p>Los errores de base de datos (DATABASE_ERROR), negocio (BUSINESS_ERROR)
     * o sistema (SYSTEM_ERROR) no incluyen este campo, ya que el {@code message}
     * y {@code detail} son suficientes para diagnosticarlos.</p>
     *
     * <p><strong>Ejemplo de uso:</strong></p>
     * <pre>{@code
     * {
     *   "timestamp": 1759338368198,
     *   "status": 400,
     *   "errorType": "VALIDATION_ERROR",
     *   "message": "whereClause inválido",
     *   "help": {
     *     "field": "whereClause",
     *     "description": "El whereClause debe contener al menos una condición válida",
     *     "examples": [
     *       {
     *         "title": "Eliminar por ID único",
     *         "code": "{\"whereClause\": {\"id\": 4}}"
     *       }
     *     ]
     *   }
     * }
     * }</pre>
     *
     * @see HelpContent
     */
    private HelpContent help;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String errorType, String message, String path) {
        this();
        this.status = status;
        this.errorType = errorType;
        this.message = message;
        this.path = path;
    }

    public ErrorResponse(int status, String errorType, String message, String detail, String path) {
        this(status, errorType, message, path);
        this.detail = detail;
    }

    // Getters y Setters
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HelpContent getHelp() {
        return help;
    }

    public void setHelp(HelpContent help) {
        this.help = help;
    }
}