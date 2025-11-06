package com.weiz.Familia.util.constans;


/**
 * Categorías de errores de validación para proporcionar ayuda contextual.
 *
 * <p>Define los diferentes tipos de errores de estructura/validación que pueden
 * ocurrir al usar los DTOs complejos (ComplexDeleteDto, ComplexQueryDto, etc.).</p>
 *
 * <p>Cada categoría tiene asociados mensajes de ayuda específicos con ejemplos
 * que se incluyen en el campo {@code help} de {@link ErrorResponse}.</p>
 *
 * <p><strong>⚠️ IMPORTANTE:</strong> Estas categorías solo aplican para errores de tipo
 * {@code VALIDATION_ERROR}. Los errores de base de datos, negocio o sistema no incluyen
 * campo {@code help}.</p>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 * @see com.weiz.Familia.dto.errors.ErrorResponse
 * @see com.weiz.Familia.dto.errors.HelpContent
 */
public enum ErrorCategory {

    // ==================== ComplexDeleteDto ====================

    /**
     * whereClause está vacío o es null en ComplexDeleteDto.
     *
     * <p><strong>Riesgo:</strong> DELETE sin WHERE eliminaría toda la tabla.</p>
     */
    DELETE_WHERE_CLAUSE_EMPTY,

    /**
     * whereClause tiene un operador inválido o mal formateado.
     *
     * <p><strong>Ejemplo:</strong> {@code {"id": {"$equals": 123}}} - el operador correcto es usar valor directo
     * o uno de los operadores válidos: $gt, $gte, $lt, $lte, $ne, $like, $in, $null</p>
     */
    DELETE_WHERE_CLAUSE_INVALID_OPERATOR,

    /**
     * whereClause tiene una estructura JSON inválida.
     *
     * <p><strong>Ejemplo:</strong> Falta llave de cierre, coma extra, etc.</p>
     */
    DELETE_WHERE_CLAUSE_MALFORMED,

    /**
     * tableName está vacío o es null en ComplexDeleteDto.
     */
    DELETE_TABLE_NAME_EMPTY,

    /**
     * tableName tiene formato inválido (caracteres especiales no permitidos, etc.).
     */
    DELETE_TABLE_NAME_INVALID,

    /**
     * dbType está vacío o es null en ComplexDeleteDto.
     */
    DELETE_DB_TYPE_EMPTY,

    /**
     * dbType no corresponde a ninguna configuración válida en DatabaseFactory.
     *
     * <p><strong>Valores válidos:</strong> db_openfinance, db_ods, etc.</p>
     */
    DELETE_DB_TYPE_INVALID,

    // ==================== ComplexUpdateDto ====================

    /**
     * whereClause está vacío o es null en ComplexUpdateDto.
     *
     * <p><strong>Riesgo:</strong> UPDATE sin WHERE modificaría todos los registros de la tabla.</p>
     */
    UPDATE_WHERE_CLAUSE_EMPTY,

    /**
     * whereClause tiene un operador inválido en ComplexUpdateDto.
     */
    UPDATE_WHERE_CLAUSE_INVALID_OPERATOR,

    /**
     * whereClause tiene estructura JSON inválida en ComplexUpdateDto.
     */
    UPDATE_WHERE_CLAUSE_MALFORMED,

    /**
     * updates (valores a actualizar) está vacío o es null en ComplexUpdateDto.
     */
    UPDATE_VALUES_EMPTY,

    /**
     * updates contiene campos o valores inválidos.
     */
    UPDATE_VALUES_INVALID,

    /**
     * tableName está vacío o es null en ComplexUpdateDto.
     */
    UPDATE_TABLE_NAME_EMPTY,

    /**
     * tableName tiene formato inválido en ComplexUpdateDto.
     */
    UPDATE_TABLE_NAME_INVALID,

    /**
     * dbType está vacío o es null en ComplexUpdateDto.
     */
    UPDATE_DB_TYPE_EMPTY,

    /**
     * dbType no corresponde a ninguna configuración válida en ComplexUpdateDto.
     */
    UPDATE_DB_TYPE_INVALID,

    // ==================== ComplexQueryDto ====================

    /**
     * whereClause tiene un operador inválido en ComplexQueryDto.
     */
    QUERY_WHERE_CLAUSE_INVALID_OPERATOR,

    /**
     * whereClause tiene estructura JSON inválida en ComplexQueryDto.
     */
    QUERY_WHERE_CLAUSE_MALFORMED,

    /**
     * selectFields (columnas a seleccionar) tiene formato inválido.
     */
    QUERY_SELECT_FIELDS_INVALID,

    /**
     * tableName está vacío o es null en ComplexQueryDto.
     */
    QUERY_TABLE_NAME_EMPTY,

    /**
     * tableName tiene formato inválido en ComplexQueryDto.
     */
    QUERY_TABLE_NAME_INVALID,

    /**
     * dbType está vacío o es null en ComplexQueryDto.
     */
    QUERY_DB_TYPE_EMPTY,

    /**
     * dbType no corresponde a ninguna configuración válida en ComplexQueryDto.
     */
    QUERY_DB_TYPE_INVALID,

    /**
     * orderBy tiene formato inválido.
     */
    QUERY_ORDER_BY_INVALID,

    /**
     * limit o offset tienen valores inválidos (negativos, no numéricos, etc.).
     */
    QUERY_PAGINATION_INVALID,

    // ==================== ComplexSaveDto ====================

    /**
     * data (datos a insertar) está vacío o es null en ComplexSaveDto.
     */
    SAVE_DATA_EMPTY,

    /**
     * data contiene valores inválidos o tipos incompatibles.
     */
    SAVE_DATA_INVALID,

    /**
     * tableName está vacío o es null en ComplexSaveDto.
     */
    SAVE_TABLE_NAME_EMPTY,

    /**
     * tableName tiene formato inválido en ComplexSaveDto.
     */
    SAVE_TABLE_NAME_INVALID,

    /**
     * dbType está vacío o es null en ComplexSaveDto.
     */
    SAVE_DB_TYPE_EMPTY,

    /**
     * dbType no corresponde a ninguna configuración válida en ComplexSaveDto.
     */
    SAVE_DB_TYPE_INVALID,

    // ==================== Genéricos ====================

    /**
     * El JSON enviado en el body no se pudo parsear.
     */
    INVALID_JSON_STRUCTURE,

    /**
     * Falta un campo requerido en el DTO.
     */
    MISSING_REQUIRED_FIELD,

    /**
     * Tipo de dato incorrecto en algún campo.
     */
    INVALID_DATA_TYPE,

    /**
     * Valor fuera de rango o límites permitidos.
     */
    VALUE_OUT_OF_RANGE
}
