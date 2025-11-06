package com.weiz.Familia.dto.errors;


import com.weiz.Familia.infraestructure.services.HelpMessageRepository;
import com.weiz.Familia.util.constans.ErrorCategory;

import java.time.LocalDateTime;

/**
 * Builder para construir instancias de {@link ErrorResponse} de forma fluida.
 *
 * <p>Facilita la creación de respuestas de error con o sin ayuda contextual,
 * y asegura que el campo {@code help} solo se incluya para errores de tipo
 * {@code VALIDATION_ERROR}.</p>
 *
 * <p><strong>Uso básico sin ayuda:</strong></p>
 * <pre>{@code
 * ErrorResponse error = ErrorResponseBuilder.create()
 *     .status(500)
 *     .errorType("DATABASE_ERROR")
 *     .message("Error al conectar con la base de datos")
 *     .detail("Connection timeout after 30 seconds")
 *     .path("/api/v1/delete")
 *     .build();
 * }</pre>
 *
 * <p><strong>Uso con ayuda contextual:</strong></p>
 * <pre>{@code
 * ErrorResponse error = ErrorResponseBuilder.create()
 *     .status(400)
 *     .errorType("VALIDATION_ERROR")
 *     .message("whereClause inválido")
 *     .detail("El campo whereClause está vacío")
 *     .path("/api/v1/delete")
 *     .withHelp(ErrorCategory.DELETE_WHERE_CLAUSE_EMPTY, helpRepo)
 *     .build();
 * }</pre>
 *
 * <p><strong>Uso con ayuda personalizada:</strong></p>
 * <pre>{@code
 * HelpContent customHelp = HelpContent.builder()
 *     .field("customField")
 *     .description("Descripción personalizada")
 *     .addExample("Título", "{\"ejemplo\": \"valor\"}")
 *     .build();
 *
 * ErrorResponse error = ErrorResponseBuilder.create()
 *     .status(400)
 *     .errorType("VALIDATION_ERROR")
 *     .message("Error personalizado")
 *     .withHelp(customHelp)
 *     .build();
 * }</pre>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
public class ErrorResponseBuilder {

    private final ErrorResponse errorResponse;

    private ErrorResponseBuilder() {
        this.errorResponse = new ErrorResponse();
    }

    /**
     * Crea una nueva instancia del builder.
     *
     * @return Nueva instancia de ErrorResponseBuilder
     */
    public static ErrorResponseBuilder create() {
        return new ErrorResponseBuilder();
    }

    /**
     * Establece el timestamp (por defecto se usa LocalDateTime.now()).
     *
     * @param timestamp Momento en que ocurrió el error
     * @return Este builder
     */
    public ErrorResponseBuilder timestamp(LocalDateTime timestamp) {
        errorResponse.setTimestamp(timestamp);
        return this;
    }

    /**
     * Establece el código de estado HTTP.
     *
     * @param status Código HTTP (400, 500, etc.)
     * @return Este builder
     */
    public ErrorResponseBuilder status(int status) {
        errorResponse.setStatus(status);
        return this;
    }

    /**
     * Establece el tipo de error.
     *
     * <p><strong>Valores comunes:</strong></p>
     * <ul>
     * <li>VALIDATION_ERROR - Errores de validación de estructura (incluye campo help)</li>
     * <li>DATABASE_ERROR - Errores de base de datos (sin campo help)</li>
     * <li>BUSINESS_ERROR - Errores de lógica de negocio (sin campo help)</li>
     * <li>SYSTEM_ERROR - Errores del sistema (sin campo help)</li>
     * </ul>
     *
     * @param errorType Tipo de error
     * @return Este builder
     */
    public ErrorResponseBuilder errorType(String errorType) {
        errorResponse.setErrorType(errorType);
        return this;
    }

    /**
     * Establece el mensaje descriptivo del error.
     *
     * @param message Mensaje para el usuario
     * @return Este builder
     */
    public ErrorResponseBuilder message(String message) {
        errorResponse.setMessage(message);
        return this;
    }

    /**
     * Establece el detalle técnico del error (opcional).
     *
     * @param detail Información técnica adicional
     * @return Este builder
     */
    public ErrorResponseBuilder detail(String detail) {
        errorResponse.setDetail(detail);
        return this;
    }

    /**
     * Establece el path del endpoint donde ocurrió el error.
     *
     * @param path Ruta del endpoint
     * @return Este builder
     */
    public ErrorResponseBuilder path(String path) {
        errorResponse.setPath(path);
        return this;
    }

    /**
     * Agrega ayuda contextual usando una categoría de error predefinida.
     *
     * <p><strong>⚠️ IMPORTANTE:</strong> Solo usar con errorType "VALIDATION_ERROR".
     * Para otros tipos de error, no llamar este método.</p>
     *
     * @param category Categoría del error de validación
     * @param helpRepo Repositorio de mensajes de ayuda
     * @return Este builder
     */
    public ErrorResponseBuilder withHelp(ErrorCategory category, HelpMessageRepository helpRepo) {
        if (helpRepo != null) {
            HelpContent help = helpRepo.getHelp(category);
            if (help != null) {
                errorResponse.setHelp(help);
            }
        }
        return this;
    }

    /**
     * Agrega ayuda contextual personalizada.
     *
     * <p>Útil cuando necesitas crear un mensaje de ayuda específico
     * que no está en el repositorio predefinido.</p>
     *
     * @param help Contenido de ayuda personalizado
     * @return Este builder
     */
    public ErrorResponseBuilder withHelp(HelpContent help) {
        errorResponse.setHelp(help);
        return this;
    }

    /**
     * Construye la instancia final de ErrorResponse.
     *
     * @return Instancia configurada de ErrorResponse
     */
    public ErrorResponse build() {
        return errorResponse;
    }

    // ==================== Métodos de conveniencia ====================

    /**
     * Crea un ErrorResponse para error de validación con ayuda contextual.
     *
     * @param status Código HTTP (normalmente 400)
     * @param message Mensaje del error
     * @param detail Detalle técnico
     * @param path Path del endpoint
     * @param category Categoría del error
     * @param helpRepo Repositorio de ayuda
     * @return ErrorResponse con ayuda incluida
     */
    public static ErrorResponse validationError(
            int status,
            String message,
            String detail,
            String path,
            ErrorCategory category,
            HelpMessageRepository helpRepo) {

        return ErrorResponseBuilder.create()
                .status(status)
                .errorType("VALIDATION_ERROR")
                .message(message)
                .detail(detail)
                .path(path)
                .withHelp(category, helpRepo)
                .build();
    }

    /**
     * Crea un ErrorResponse para error de base de datos (sin ayuda).
     *
     * @param status Código HTTP (normalmente 500)
     * @param message Mensaje del error
     * @param detail Detalle técnico
     * @param path Path del endpoint
     * @return ErrorResponse sin campo help
     */
    public static ErrorResponse databaseError(
            int status,
            String message,
            String detail,
            String path) {

        return ErrorResponseBuilder.create()
                .status(status)
                .errorType("DATABASE_ERROR")
                .message(message)
                .detail(detail)
                .path(path)
                .build();
    }

    /**
     * Crea un ErrorResponse para error de negocio (sin ayuda).
     *
     * @param status Código HTTP
     * @param message Mensaje del error
     * @param detail Detalle técnico
     * @param path Path del endpoint
     * @return ErrorResponse sin campo help
     */
    public static ErrorResponse businessError(
            int status,
            String message,
            String detail,
            String path) {

        return ErrorResponseBuilder.create()
                .status(status)
                .errorType("BUSINESS_ERROR")
                .message(message)
                .detail(detail)
                .path(path)
                .build();
    }

    /**
     * Crea un ErrorResponse para error del sistema (sin ayuda).
     *
     * @param status Código HTTP (normalmente 500)
     * @param message Mensaje del error
     * @param detail Detalle técnico
     * @param path Path del endpoint
     * @return ErrorResponse sin campo help
     */
    public static ErrorResponse systemError(
            int status,
            String message,
            String detail,
            String path) {

        return ErrorResponseBuilder.create()
                .status(status)
                .errorType("SYSTEM_ERROR")
                .message(message)
                .detail(detail)
                .path(path)
                .build();
    }
}
