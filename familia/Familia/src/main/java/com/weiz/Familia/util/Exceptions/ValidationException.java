package com.weiz.Familia.util.Exceptions;


import com.weiz.Familia.util.constans.ErrorCategory;

/**
 * Excepción personalizada para errores de validación con categorización.
 *
 * <p>Esta excepción se lanza cuando ocurren errores de validación en los DTOs
 * y permite incluir una {@link ErrorCategory} para proporcionar ayuda contextual
 * automática al desarrollador que consume la API.</p>
 *
 * <p><strong>Ventajas:</strong></p>
 * <ul>
 * <li>Categorización automática del error</li>
 * <li>Ayuda contextual con ejemplos en la respuesta</li>
 * <li>Mensajes claros y accionables</li>
 * <li>Mejor experiencia de desarrollo</li>
 * </ul>
 *
 * <p><strong>Ejemplo de uso:</strong></p>
 * <pre>{@code
 * if (dto.getWhereClause() == null || dto.getWhereClause().isEmpty()) {
 *     throw new ValidationException(
 *         "El whereClause no puede estar vacío",
 *         ErrorCategory.DELETE_WHERE_CLAUSE_EMPTY,
 *         "whereClause"
 *     );
 * }
 * }</pre>
 *
 * <p>El {@link com.credifamilia.dbmaster.exception.GlobalExceptionHandler}
 * capturará esta excepción y generará una respuesta con ayuda contextual automática.</p>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 * @see ErrorCategory
 * @see com.credifamilia.dbmaster.exception.GlobalExceptionHandler
 */
public class ValidationException extends RuntimeException {

    /**
     * Categoría del error de validación.
     *
     * <p>Utilizado por el GlobalExceptionHandler para seleccionar
     * el mensaje de ayuda apropiado del HelpMessageRepository.</p>
     */
    private final ErrorCategory category;

    /**
     * Nombre del campo que causó el error de validación.
     *
     * <p>Ayuda a identificar específicamente qué campo del DTO
     * necesita ser corregido.</p>
     */
    private final String fieldName;

    /**
     * Construye una nueva ValidationException con categoría y campo.
     *
     * @param message Mensaje descriptivo del error
     * @param category Categoría del error para ayuda contextual
     * @param fieldName Nombre del campo que causó el error
     */
    public ValidationException(String message, ErrorCategory category, String fieldName) {
        super(message);
        this.category = category;
        this.fieldName = fieldName;
    }

    /**
     * Construye una nueva ValidationException con categoría, campo y causa.
     *
     * @param message Mensaje descriptivo del error
     * @param category Categoría del error para ayuda contextual
     * @param fieldName Nombre del campo que causó el error
     * @param cause Causa raíz de la excepción
     */
    public ValidationException(String message, ErrorCategory category, String fieldName, Throwable cause) {
        super(message, cause);
        this.category = category;
        this.fieldName = fieldName;
    }

    /**
     * Obtiene la categoría del error.
     *
     * @return Categoría del error de validación
     */
    public ErrorCategory getCategory() {
        return category;
    }

    /**
     * Obtiene el nombre del campo que causó el error.
     *
     * @return Nombre del campo
     */
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return String.format("ValidationException{category=%s, fieldName='%s', message='%s'}",
                category, fieldName, getMessage());
    }
}
