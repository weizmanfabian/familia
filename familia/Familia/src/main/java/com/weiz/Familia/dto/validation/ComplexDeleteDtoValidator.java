package com.weiz.Familia.dto.validation;

import com.weiz.Familia.dto.ComplexDeleteDto;
import com.weiz.Familia.util.Exceptions.ValidationException;
import com.weiz.Familia.util.constans.ErrorCategory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Validador para {@link ComplexDeleteDto}.
 *
 * <p>Valida que todos los campos requeridos estén presentes y tengan el formato correcto
 * antes de ejecutar operaciones DELETE. Lanza {@link ValidationException} con categorías
 * específicas para proporcionar ayuda contextual automática.</p>
 *
 * <p><strong>Validaciones realizadas:</strong></p>
 * <ul>
 * <li>tableName: Obligatorio, no vacío, formato válido</li>
 * <li>whereClause: Obligatorio, no vacío (seguridad contra DELETE masivo)</li>
 * <li>dbType: Obligatorio, no vacío</li>
 * </ul>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@Component
public class ComplexDeleteDtoValidator {

    /**
     * Valida un ComplexDeleteDto completo.
     *
     * <p>Ejecuta todas las validaciones necesarias en el orden apropiado.
     * Si alguna validación falla, lanza {@link ValidationException} con la
     * categoría de error correspondiente.</p>
     *
     * @param dto DTO a validar
     * @throws ValidationException si alguna validación falla
     */
    public void validate(ComplexDeleteDto dto) {
        if (dto == null) {
            throw new ValidationException(
                "El DTO no puede ser null",
                ErrorCategory.MISSING_REQUIRED_FIELD,
                "body"
            );
        }

        validateTableName(dto.getTableName());
        validateWhereClause(dto.getWhereClause());
        validateDbType(dto.getDbType());
    }

    /**
     * Valida el campo tableName.
     *
     * @param tableName Nombre de la tabla
     * @throws ValidationException si tableName es inválido
     */
    private void validateTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new ValidationException(
                "El campo 'tableName' es obligatorio y no puede estar vacío",
                ErrorCategory.DELETE_TABLE_NAME_EMPTY,
                "tableName"
            );
        }

        // Validar formato: solo letras, números, guiones bajos y punto
        if (!tableName.matches("^[a-zA-Z0-9_.]+$")) {
            throw new ValidationException(
                String.format("El tableName '%s' tiene formato inválido. " +
                    "Solo se permiten letras, números, guiones bajos y punto", tableName),
                ErrorCategory.DELETE_TABLE_NAME_INVALID,
                "tableName"
            );
        }
    }

    /**
     * Valida el campo whereClause.
     *
     * <p><strong>⚠️ CRÍTICO:</strong> whereClause es obligatorio para evitar
     * eliminaciones masivas accidentales. Un DELETE sin WHERE eliminaría
     * TODA la tabla.</p>
     *
     * @param whereClause Condiciones WHERE
     * @throws ValidationException si whereClause es inválido o vacío
     */
    private void validateWhereClause(Map<String, Object> whereClause) {
        if (whereClause == null || whereClause.isEmpty()) {
            throw new ValidationException(
                "El campo 'whereClause' es obligatorio y no puede estar vacío. " +
                "DELETE sin WHERE eliminaría TODOS los registros de la tabla",
                ErrorCategory.DELETE_WHERE_CLAUSE_EMPTY,
                "whereClause"
            );
        }

        // Validar que no solo contenga el operador $condition sin condiciones reales
        if (whereClause.size() == 1 && whereClause.containsKey("$condition")) {
            throw new ValidationException(
                "El whereClause solo contiene el operador '$condition' sin condiciones reales",
                ErrorCategory.DELETE_WHERE_CLAUSE_MALFORMED,
                "whereClause"
            );
        }

        // Validar operadores en whereClause
        validateWhereClauseOperators(whereClause);
    }

    /**
     * Valida los operadores utilizados en el whereClause.
     *
     * @param whereClause Mapa con las condiciones
     * @throws ValidationException si se encuentra un operador inválido
     */
    private void validateWhereClauseOperators(Map<String, Object> whereClause) {
        // Operadores válidos que empiezan con $
        String[] validOperators = {"$gt", "$gte", "$lt", "$lte", "$ne", "$like", "$in", "$null", "$condition"};

        for (Map.Entry<String, Object> entry : whereClause.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // Si la clave empieza con $, verificar que sea un operador válido
            if (key.startsWith("$") && !isValidOperator(key, validOperators)) {
                throw new ValidationException(
                    String.format("Operador inválido '%s' en whereClause. " +
                        "Operadores válidos: $gt, $gte, $lt, $lte, $ne, $like, $in, $null, $condition", key),
                    ErrorCategory.DELETE_WHERE_CLAUSE_INVALID_OPERATOR,
                    "whereClause"
                );
            }

            // Si el valor es un Map (condición anidada), validar operadores internos
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedCondition = (Map<String, Object>) value;
                for (String operator : nestedCondition.keySet()) {
                    if (!isValidOperator(operator, validOperators)) {
                        throw new ValidationException(
                            String.format("Operador inválido '%s' en la condición del campo '%s'", operator, key),
                            ErrorCategory.DELETE_WHERE_CLAUSE_INVALID_OPERATOR,
                            "whereClause"
                        );
                    }
                }
            }
        }
    }

    /**
     * Verifica si un operador está en la lista de operadores válidos.
     *
     * @param operator Operador a verificar
     * @param validOperators Array de operadores válidos
     * @return true si el operador es válido
     */
    private boolean isValidOperator(String operator, String[] validOperators) {
        for (String valid : validOperators) {
            if (valid.equals(operator)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Valida el campo dbType.
     *
     * @param dbType Tipo de base de datos
     * @throws ValidationException si dbType es inválido
     */
    private void validateDbType(String dbType) {
        if (dbType == null || dbType.trim().isEmpty()) {
            throw new ValidationException(
                "El campo 'dbType' es obligatorio y no puede estar vacío",
                ErrorCategory.DELETE_DB_TYPE_EMPTY,
                "dbType"
            );
        }

        // Validar que sea uno de los tipos configurados
        // Nota: Esta validación también se hace en DatabaseFactory, pero es mejor fallar rápido
        String[] validTypes = {"db_openfinance", "db_ods"}; // Ajustar según configuración real
        boolean isValid = false;
        for (String validType : validTypes) {
            if (validType.equals(dbType)) {
                isValid = true;
                break;
            }
        }

        if (!isValid) {
            throw new ValidationException(
                String.format("El dbType '%s' no es válido. Valores permitidos: db_openfinance, db_ods", dbType),
                ErrorCategory.DELETE_DB_TYPE_INVALID,
                "dbType"
            );
        }
    }
}
