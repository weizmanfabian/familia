package com.weiz.Familia.dto.validation;

import com.weiz.Familia.dto.ComplexDeleteDto;
import com.weiz.Familia.dto.ComplexQueryDto;
import com.weiz.Familia.dto.ComplexSaveDto;
import com.weiz.Familia.dto.ComplexUpdateDto;
import com.weiz.Familia.util.Exceptions.ValidationException;
import com.weiz.Familia.util.constans.ErrorCategory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validador centralizado para todos los DTOs complejos.
 *
 * <p>Proporciona validación con ayuda contextual automática para:
 * <ul>
 * <li>{@link ComplexDeleteDto}</li>
 * <li>{@link ComplexUpdateDto}</li>
 * <li>{@link ComplexQueryDto}</li>
 * <li>{@link ComplexSaveDto}</li>
 * </ul>
 *
 * <p><strong>Operadores soportados:</strong>
 * <ul>
 * <li>Comparación: $gt, $gte, $lt, $lte, $ne</li>
 * <li>Patrones: $like, $in</li>
 * <li>Nulos: $null</li>
 * <li>Lógicos: $condition (OR/AND)</li>
 * <li>Intervalos temporales: $ltInterval, $gtInterval, $lteInterval, $gteInterval</li>
 * </ul>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@Component
public class DtoValidator {

    private static final String[] VALID_OPERATORS = {
            "$gt", "$gte", "$lt", "$lte", "$ne", "$like", "$in", "$null", "$condition",
            "$ltInterval", "$gtInterval", "$lteInterval", "$gteInterval"
    };

    // ==================== ComplexDeleteDto ====================

    public void validateDelete(ComplexDeleteDto dto) {
        if (dto == null) {
            throw new ValidationException(
                "El DTO no puede ser null",
                ErrorCategory.MISSING_REQUIRED_FIELD,
                "body"
            );
        }

        validateTableName(dto.getTableName(), "DELETE");
        validateWhereClauseNotEmpty(dto.getWhereClause(), "DELETE");
        validateDbType(dto.getDbType(), "DELETE");
    }

    // ==================== ComplexUpdateDto ====================

    public void validateUpdate(ComplexUpdateDto dto) {
        if (dto == null) {
            throw new ValidationException(
                "El DTO no puede ser null",
                ErrorCategory.MISSING_REQUIRED_FIELD,
                "body"
            );
        }

        validateTableName(dto.getTableName(), "UPDATE");
        validateValues(dto.getValues());
        validateWhereClauseNotEmpty(dto.getWhereClause(), "UPDATE");
        validateDbType(dto.getDbType(), "UPDATE");
    }

    // ==================== ComplexQueryDto ====================

    public void validateQuery(ComplexQueryDto dto) {
        if (dto == null) {
            throw new ValidationException(
                "El DTO no puede ser null",
                ErrorCategory.MISSING_REQUIRED_FIELD,
                "body"
            );
        }

        validateQueryField(dto.getQuery());
        // whereClause es opcional en queries
        if (dto.getWhereClause() != null && !dto.getWhereClause().isEmpty()) {
            validateWhereClauseOperators(dto.getWhereClause(), "QUERY");
        }
        validateDbType(dto.getDbType(), "QUERY");
    }

    // ==================== ComplexSaveDto ====================

    public void validateSave(ComplexSaveDto dto) {
        if (dto == null) {
            throw new ValidationException(
                "El DTO no puede ser null",
                ErrorCategory.MISSING_REQUIRED_FIELD,
                "body"
            );
        }

        validateTableName(dto.getTableName(), "SAVE");
        validateValues(dto.getValues());
        validateDbType(dto.getDbType(), "SAVE");
    }

    // ==================== Validaciones Comunes ====================

    /**
     * Valida el campo tableName (solo verifica que no esté vacío).
     */
    private void validateTableName(String tableName, String operation) {
        ErrorCategory emptyCategory = getTableNameEmptyCategory(operation);

        if (tableName == null || tableName.trim().isEmpty()) {
            throw new ValidationException(
                "El campo 'tableName' es obligatorio y no puede estar vacío",
                emptyCategory,
                "tableName"
            );
        }
    }

    /**
     * Valida el campo query para consultas SELECT.
     */
    private void validateQueryField(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new ValidationException(
                "El campo 'query' es obligatorio y no puede estar vacío",
                ErrorCategory.QUERY_TABLE_NAME_EMPTY,
                "query"
            );
        }

        // Validar que sea un SELECT
        if (!query.trim().toUpperCase().startsWith("SELECT")) {
            throw new ValidationException(
                "El campo 'query' debe ser una sentencia SELECT válida",
                ErrorCategory.QUERY_SELECT_FIELDS_INVALID,
                "query"
            );
        }
    }

    /**
     * Valida el campo values (para UPDATE y SAVE).
     */
    private void validateValues(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            throw new ValidationException(
                "El campo 'values' es obligatorio y no puede estar vacío",
                ErrorCategory.UPDATE_VALUES_EMPTY,
                "values"
            );
        }
    }

    /**
     * Valida que whereClause no esté vacío (para DELETE y UPDATE).
     */
    private void validateWhereClauseNotEmpty(Map<String, Object> whereClause, String operation) {
        ErrorCategory emptyCategory = getWhereClauseEmptyCategory(operation);

        if (whereClause == null || whereClause.isEmpty()) {
            String message = operation.equals("DELETE")
                ? "El campo 'whereClause' es obligatorio. DELETE sin WHERE eliminaría TODOS los registros"
                : "El campo 'whereClause' es obligatorio. UPDATE sin WHERE modificaría TODOS los registros";

            throw new ValidationException(message, emptyCategory, "whereClause");
        }

        if (whereClause.size() == 1 && whereClause.containsKey("$condition")) {
            ErrorCategory malformedCategory = getWhereClauseMalformedCategory(operation);
            throw new ValidationException(
                "El whereClause solo contiene '$condition' sin condiciones reales",
                malformedCategory,
                "whereClause"
            );
        }

        validateWhereClauseOperators(whereClause, operation);
    }

    /**
     * Valida los operadores en whereClause.
     */
    private void validateWhereClauseOperators(Map<String, Object> whereClause, String operation) {
        ErrorCategory invalidOpCategory = getWhereClauseInvalidOperatorCategory(operation);

        for (Map.Entry<String, Object> entry : whereClause.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key.startsWith("$") && !isValidOperator(key)) {
                throw new ValidationException(
                        String.format("Operador inválido '%s' en whereClause. Operadores válidos: $gt, $gte, $lt, $lte, $ne, $like, $in, $null, $condition, $ltInterval, $gtInterval, $lteInterval, $gteInterval", key),
                        invalidOpCategory,
                        "whereClause"
                );
            }

            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedCondition = (Map<String, Object>) value;
                for (String operator : nestedCondition.keySet()) {
                    if (!isValidOperator(operator)) {
                        throw new ValidationException(
                            String.format("Operador inválido '%s' en campo '%s'", operator, key),
                            invalidOpCategory,
                            "whereClause"
                        );
                    }
                }
            }
        }
    }

    /**
     * Valida el campo dbType (opcional - si se proporciona debe ser válido).
     */
    private void validateDbType(String dbType, String operation) {
        // dbType es opcional - si es null o vacío, simplemente retornamos
        if (dbType == null || dbType.trim().isEmpty()) {
            return;
        }

        // No validamos contra una lista específica - dejamos que DatabaseFactory lo maneje
        // Si el dbType no existe, DatabaseFactory lanzará una excepción apropiada
    }

    // ==================== Helpers ====================

    private boolean isValidOperator(String operator) {
        for (String valid : VALID_OPERATORS) {
            if (valid.equals(operator)) {
                return true;
            }
        }
        return false;
    }

    // ==================== Category Mappers ====================

    private ErrorCategory getTableNameEmptyCategory(String operation) {
        switch (operation) {
            case "DELETE": return ErrorCategory.DELETE_TABLE_NAME_EMPTY;
            case "UPDATE": return ErrorCategory.UPDATE_TABLE_NAME_EMPTY;
            case "QUERY": return ErrorCategory.QUERY_TABLE_NAME_EMPTY;
            case "SAVE": return ErrorCategory.SAVE_TABLE_NAME_EMPTY;
            default: return ErrorCategory.MISSING_REQUIRED_FIELD;
        }
    }

    private ErrorCategory getWhereClauseEmptyCategory(String operation) {
        switch (operation) {
            case "DELETE": return ErrorCategory.DELETE_WHERE_CLAUSE_EMPTY;
            case "UPDATE": return ErrorCategory.UPDATE_WHERE_CLAUSE_EMPTY;
            default: return ErrorCategory.MISSING_REQUIRED_FIELD;
        }
    }

    private ErrorCategory getWhereClauseMalformedCategory(String operation) {
        switch (operation) {
            case "DELETE": return ErrorCategory.DELETE_WHERE_CLAUSE_MALFORMED;
            case "UPDATE": return ErrorCategory.UPDATE_WHERE_CLAUSE_MALFORMED;
            case "QUERY": return ErrorCategory.QUERY_WHERE_CLAUSE_MALFORMED;
            default: return ErrorCategory.INVALID_JSON_STRUCTURE;
        }
    }

    private ErrorCategory getWhereClauseInvalidOperatorCategory(String operation) {
        switch (operation) {
            case "DELETE": return ErrorCategory.DELETE_WHERE_CLAUSE_INVALID_OPERATOR;
            case "UPDATE": return ErrorCategory.UPDATE_WHERE_CLAUSE_INVALID_OPERATOR;
            case "QUERY": return ErrorCategory.QUERY_WHERE_CLAUSE_INVALID_OPERATOR;
            default: return ErrorCategory.INVALID_DATA_TYPE;
        }
    }

    private ErrorCategory getDbTypeEmptyCategory(String operation) {
        switch (operation) {
            case "DELETE": return ErrorCategory.DELETE_DB_TYPE_EMPTY;
            case "UPDATE": return ErrorCategory.UPDATE_DB_TYPE_EMPTY;
            case "QUERY": return ErrorCategory.QUERY_DB_TYPE_EMPTY;
            case "SAVE": return ErrorCategory.SAVE_DB_TYPE_EMPTY;
            default: return ErrorCategory.MISSING_REQUIRED_FIELD;
        }
    }
}
