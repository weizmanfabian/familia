package com.weiz.Familia.infraestructure.services;


import com.weiz.Familia.dto.errors.HelpContent;
import com.weiz.Familia.util.constans.ErrorCategory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Repositorio centralizado de mensajes de ayuda para errores de validación.
 *
 * <p>Contiene todos los mensajes predefinidos, ejemplos y enlaces a documentación
 * que se muestran en el campo {@code help} de {@link ErrorResponse}
 * cuando ocurre un error de tipo {@code VALIDATION_ERROR}.</p>
 *
 * <p>Los mensajes están organizados por {@link ErrorCategory} y proporcionan:</p>
 * <ul>
 * <li>Descripción clara del problema</li>
 * <li>Ejemplos prácticos de uso correcto</li>
 * <li>Enlaces a documentación (cuando aplique)</li>
 * </ul>
 *
 * <p><strong>Uso:</strong></p>
 * <pre>{@code
 * @Autowired
 * private HelpMessageRepository helpRepo;
 *
 * HelpContent help = helpRepo.getHelp(ErrorCategory.DELETE_WHERE_CLAUSE_EMPTY);
 * ErrorResponse error = new ErrorResponse(...);
 * error.setHelp(help);
 * }</pre>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@Component
public class HelpMessageRepository {

    private final Map<ErrorCategory, HelpContent> helpMessages;

    public HelpMessageRepository() {
        this.helpMessages = new HashMap<>();
        initializeDeleteMessages();
        initializeUpdateMessages();
        initializeQueryMessages();
        initializeSaveMessages();
        initializeGenericMessages();
    }

    /**
     * Obtiene el contenido de ayuda para una categoría de error específica.
     *
     * @param category Categoría del error
     * @return Contenido de ayuda con ejemplos, o null si no hay ayuda definida
     */
    public HelpContent getHelp(ErrorCategory category) {
        return helpMessages.get(category);
    }

    // ==================== ComplexDeleteDto ====================

    private void initializeDeleteMessages() {
        // DELETE_WHERE_CLAUSE_EMPTY
        helpMessages.put(ErrorCategory.DELETE_WHERE_CLAUSE_EMPTY,
                HelpContent.builder()
                        .field("whereClause")
                        .description("El whereClause no puede estar vacío. DELETE sin WHERE eliminaría TODOS los registros de la tabla. Siempre especifica al menos una condición.")
                        .addExample(
                                "Eliminar por ID único",
                                "{\n" +
                                        "  \"tableName\": \"originacionseguros.tipos_documento\",\n" +
                                        "  \"whereClause\": {\"id\": 4},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Eliminar por lista de IDs",
                                "{\n" +
                                        "  \"tableName\": \"logs_sistema\",\n" +
                                        "  \"whereClause\": {\"id\": [100, 101, 102, 103]},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Eliminar con rango de fechas",
                                "{\n" +
                                        "  \"tableName\": \"logs_auditoria\",\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"fecha_creacion\": {\"$lt\": \"2024-01-01T00:00:00\"}\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .documentation("/docs/complex-delete#whereclause")
                        .build()
        );

        // DELETE_WHERE_CLAUSE_INVALID_OPERATOR
        helpMessages.put(ErrorCategory.DELETE_WHERE_CLAUSE_INVALID_OPERATOR,
                HelpContent.builder()
                        .field("whereClause")
                        .description("Operador inválido en whereClause. Operadores válidos: $gt, $gte, $lt, $lte, $ne, $like, $in, $null, $ltInterval, $gtInterval, $lteInterval, $gteInterval. Para igualdad simple, usa el valor directo sin operador.")
                        .addExample(
                                "Igualdad simple (sin operador)",
                                "{\n" +
                                        "  \"whereClause\": {\"estado\": \"ACTIVO\"}\n" +
                                        "}"
                        )
                        .addExample(
                                "Mayor que ($gt)",
                                "{\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"fecha_creacion\": {\"$gt\": \"2024-01-01T00:00:00\"}\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .addExample(
                                "Búsqueda con LIKE",
                                "{\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"nombre\": {\"$like\": \"%temporal%\"}\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .addExample(
                                "Lista con IN",
                                "{\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"estado\": {\"$in\": [\"PENDIENTE\", \"EN_PROCESO\"]}\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .documentation("/docs/complex-delete#operators")
                        .build()
        );

        // DELETE_WHERE_CLAUSE_MALFORMED
        helpMessages.put(ErrorCategory.DELETE_WHERE_CLAUSE_MALFORMED,
                HelpContent.builder()
                        .field("whereClause")
                        .description("La estructura JSON del whereClause está mal formada. Verifica llaves, comas y comillas.")
                        .addExample(
                                "Estructura correcta básica",
                                "{\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"id\": 123\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .addExample(
                                "Múltiples condiciones con AND (por defecto)",
                                "{\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"estado\": \"INACTIVO\",\n" +
                                        "    \"fecha_creacion\": {\"$lt\": \"2024-01-01T00:00:00\"}\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .addExample(
                                "Múltiples condiciones con OR",
                                "{\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"estado\": \"INACTIVO\",\n" +
                                        "    \"$condition\": \"OR\",\n" +
                                        "    \"fecha_ultimo_acceso\": {\"$lt\": \"2023-01-01T00:00:00\"}\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .build()
        );

        // DELETE_TABLE_NAME_EMPTY
        helpMessages.put(ErrorCategory.DELETE_TABLE_NAME_EMPTY,
                HelpContent.builder()
                        .field("tableName")
                        .description("El campo tableName es obligatorio. Debe incluir el esquema si la tabla no está en el esquema por defecto.")
                        .addExample(
                                "Con esquema explícito",
                                "{\n" +
                                        "  \"tableName\": \"originacionseguros.tipos_documento\",\n" +
                                        "  \"whereClause\": {\"id\": 4},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Esquema public (explícito)",
                                "{\n" +
                                        "  \"tableName\": \"public.logs_sistema\",\n" +
                                        "  \"whereClause\": {\"id\": 100},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Sin esquema (usa esquema por defecto)",
                                "{\n" +
                                        "  \"tableName\": \"temporal_imports\",\n" +
                                        "  \"whereClause\": {\"id\": 1},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // DELETE_TABLE_NAME_INVALID
        helpMessages.put(ErrorCategory.DELETE_TABLE_NAME_INVALID,
                HelpContent.builder()
                        .field("tableName")
                        .description("El formato del tableName es inválido. Usa solo letras, números, guiones bajos y punto para separar esquema.tabla.")
                        .addExample(
                                "Formato válido con esquema",
                                "\"tableName\": \"originacionseguros.tipos_documento\""
                        )
                        .addExample(
                                "Formato válido sin esquema",
                                "\"tableName\": \"usuarios_sistema\""
                        )
                        .build()
        );

        // DELETE_DB_TYPE_EMPTY
        helpMessages.put(ErrorCategory.DELETE_DB_TYPE_EMPTY,
                HelpContent.builder()
                        .field("dbType")
                        .description("El campo dbType es obligatorio. Debe corresponder a una configuración válida en DatabaseFactory.")
                        .addExample(
                                "Con dbType db_openfinance",
                                "{\n" +
                                        "  \"tableName\": \"originacionseguros.tipos_documento\",\n" +
                                        "  \"whereClause\": {\"id\": 4},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Con dbType db_ods",
                                "{\n" +
                                        "  \"tableName\": \"clientes\",\n" +
                                        "  \"whereClause\": {\"id\": 123},\n" +
                                        "  \"dbType\": \"db_ods\"\n" +
                                        "}"
                        )
                        .build()
        );

        // DELETE_DB_TYPE_INVALID
        helpMessages.put(ErrorCategory.DELETE_DB_TYPE_INVALID,
                HelpContent.builder()
                        .field("dbType")
                        .description("El dbType especificado no existe en la configuración. Valores válidos: db_openfinance, db_ods")
                        .addExample(
                                "dbType válido: db_openfinance",
                                "{\n" +
                                        "  \"tableName\": \"logs_sistema\",\n" +
                                        "  \"whereClause\": {\"id\": 100},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );
    }

    // ==================== ComplexUpdateDto ====================

    private void initializeUpdateMessages() {
        // UPDATE_WHERE_CLAUSE_EMPTY
        helpMessages.put(ErrorCategory.UPDATE_WHERE_CLAUSE_EMPTY,
                HelpContent.builder()
                        .field("whereClause")
                        .description("El whereClause no puede estar vacío. UPDATE sin WHERE modificaría TODOS los registros de la tabla.")
                        .addExample(
                                "Actualizar por ID único",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"updates\": {\"estado\": \"INACTIVO\"},\n" +
                                        "  \"whereClause\": {\"id\": 123},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // UPDATE_VALUES_EMPTY
        helpMessages.put(ErrorCategory.UPDATE_VALUES_EMPTY,
                HelpContent.builder()
                        .field("values")
                        .description("El campo 'values' (valores a actualizar) no puede estar vacío. Debe contener al menos un campo a modificar.")
                        .addExample(
                                "Actualizar un campo",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\"estado\": \"INACTIVO\"},\n" +
                                        "  \"whereClause\": {\"id\": 123},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Actualizar múltiples campos",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\n" +
                                        "    \"estado\": \"INACTIVO\",\n" +
                                        "    \"fecha_modificacion\": \"2025-01-15T10:30:00\"\n" +
                                        "  },\n" +
                                        "  \"whereClause\": {\"id\": 123},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // UPDATE_WHERE_CLAUSE_INVALID_OPERATOR
        helpMessages.put(ErrorCategory.UPDATE_WHERE_CLAUSE_INVALID_OPERATOR,
                HelpContent.builder()
                        .field("whereClause")
                        .description("Operador inválido en whereClause. Operadores válidos: $gt, $gte, $lt, $lte, $ne, $like, $in, $null, $ltInterval, $gtInterval, $lteInterval, $gteInterval")
                        .addExample(
                                "Actualizar con condición mayor que",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\"activo\": false},\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"ultimo_acceso\": {\"$lt\": \"2024-01-01T00:00:00\"}\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Actualizar con condición OR",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\"activo\": false},\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"estado\": \"INACTIVO\",\n" +
                                        "    \"$condition\": \"OR\",\n" +
                                        "    \"intentos_fallidos\": {\"$gte\": 5}\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // UPDATE_WHERE_CLAUSE_MALFORMED
        helpMessages.put(ErrorCategory.UPDATE_WHERE_CLAUSE_MALFORMED,
                HelpContent.builder()
                        .field("whereClause")
                        .description("La estructura JSON del whereClause está mal formada. Verifica llaves, comas y comillas.")
                        .addExample(
                                "Estructura correcta",
                                "{\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"id\": 123,\n" +
                                        "    \"estado\": \"ACTIVO\"\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .build()
        );

        // UPDATE_VALUES_INVALID
        helpMessages.put(ErrorCategory.UPDATE_VALUES_INVALID,
                HelpContent.builder()
                        .field("values")
                        .description("Los valores a actualizar tienen formato inválido. Deben ser pares clave-valor válidos.")
                        .addExample(
                                "Valores válidos con diferentes tipos",
                                "{\n" +
                                        "  \"values\": {\n" +
                                        "    \"nombre\": \"Juan Pérez\",\n" +
                                        "    \"edad\": 30,\n" +
                                        "    \"activo\": true,\n" +
                                        "    \"fecha_modificacion\": \"2025-01-15T10:30:00\",\n" +
                                        "    \"observaciones\": null\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .build()
        );

        // UPDATE_TABLE_NAME_EMPTY
        helpMessages.put(ErrorCategory.UPDATE_TABLE_NAME_EMPTY,
                HelpContent.builder()
                        .field("tableName")
                        .description("El campo tableName es obligatorio para operaciones UPDATE.")
                        .addExample(
                                "UPDATE con tableName",
                                "{\n" +
                                        "  \"tableName\": \"originacionseguros.solicitudes\",\n" +
                                        "  \"values\": {\"estado\": \"APROBADO\"},\n" +
                                        "  \"whereClause\": {\"id\": 100},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // UPDATE_TABLE_NAME_INVALID
        helpMessages.put(ErrorCategory.UPDATE_TABLE_NAME_INVALID,
                HelpContent.builder()
                        .field("tableName")
                        .description("El formato del tableName es inválido. Usa solo letras, números, guiones bajos y punto para separar esquema.tabla.")
                        .addExample(
                                "Formato válido",
                                "\"tableName\": \"originacionseguros.solicitudes_polizas\""
                        )
                        .build()
        );

        // UPDATE_DB_TYPE_EMPTY
        helpMessages.put(ErrorCategory.UPDATE_DB_TYPE_EMPTY,
                HelpContent.builder()
                        .field("dbType")
                        .description("El campo dbType es obligatorio. Debe corresponder a una configuración válida en DatabaseFactory.")
                        .addExample(
                                "UPDATE con dbType",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\"estado\": \"INACTIVO\"},\n" +
                                        "  \"whereClause\": {\"id\": 123},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // UPDATE_DB_TYPE_INVALID
        helpMessages.put(ErrorCategory.UPDATE_DB_TYPE_INVALID,
                HelpContent.builder()
                        .field("dbType")
                        .description("El dbType especificado no existe en la configuración. Valores válidos: db_openfinance, db_ods")
                        .addExample(
                                "dbType válido",
                                "\"dbType\": \"db_openfinance\""
                        )
                        .build()
        );
    }

    // ==================== ComplexQueryDto ====================

    private void initializeQueryMessages() {
        // QUERY_WHERE_CLAUSE_INVALID_OPERATOR
        helpMessages.put(ErrorCategory.QUERY_WHERE_CLAUSE_INVALID_OPERATOR,
                HelpContent.builder()
                        .field("whereClause")
                        .description("Operador inválido en whereClause. Operadores válidos: $gt, $gte, $lt, $lte, $ne, $like, $in, $null, $ltInterval, $gtInterval, $lteInterval, $gteInterval")
                        .addExample(
                                "Query con operador LIKE",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM users\",\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"nombre\": {\"$like\": \"%Juan%\"}\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Query con operador mayor o igual",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM users\",\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"edad\": {\"$gte\": 18}\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Query con lista IN",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM estados_solicitud_polizas\",\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"estado_solicitud_poliza_id\": [1, 2, 3]\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Query con intervalo temporal (últimos 30 segundos)",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM solicitudes_polizas\",\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"estado_id\": [3, 4, 5, 6],\n" +
                                        "    \"fecha_envio_negozia\": {\"$ltInterval\": 30}\n" +
                                        "  },\n" +
                                        "  \"limit\": 100,\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Query con intervalo temporal (últimas 24 horas)",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM logs_sistema\",\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"fecha_creacion\": {\"$gtInterval\": 86400}\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // QUERY_WHERE_CLAUSE_MALFORMED
        helpMessages.put(ErrorCategory.QUERY_WHERE_CLAUSE_MALFORMED,
                HelpContent.builder()
                        .field("whereClause")
                        .description("La estructura JSON del whereClause está mal formada. Verifica llaves, comas y comillas.")
                        .addExample(
                                "Estructura correcta con múltiples condiciones",
                                "{\n" +
                                        "  \"whereClause\": {\n" +
                                        "    \"nombre\": {\"$like\": \"%Juan%\"},\n" +
                                        "    \"edad\": {\"$gte\": 18},\n" +
                                        "    \"activo\": true\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .build()
        );

        // QUERY_SELECT_FIELDS_INVALID
        helpMessages.put(ErrorCategory.QUERY_SELECT_FIELDS_INVALID,
                HelpContent.builder()
                        .field("query")
                        .description("La consulta SQL tiene formato inválido. Debe ser una sentencia SELECT válida.")
                        .addExample(
                                "SELECT simple",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM usuarios\",\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "SELECT con columnas específicas",
                                "{\n" +
                                        "  \"query\": \"SELECT id, nombre, email FROM usuarios\",\n" +
                                        "  \"whereClause\": {\"activo\": true},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // QUERY_TABLE_NAME_EMPTY
        helpMessages.put(ErrorCategory.QUERY_TABLE_NAME_EMPTY,
                HelpContent.builder()
                        .field("query")
                        .description("El campo 'query' es obligatorio. Debe contener una sentencia SELECT válida.")
                        .addExample(
                                "Query básico",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM originacionseguros.tipos_documento\",\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Query con WHERE y ORDER BY",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM usuarios\",\n" +
                                        "  \"whereClause\": {\"activo\": true},\n" +
                                        "  \"orderClause\": \"id DESC\",\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // QUERY_TABLE_NAME_INVALID
        helpMessages.put(ErrorCategory.QUERY_TABLE_NAME_INVALID,
                HelpContent.builder()
                        .field("query")
                        .description("La sentencia SELECT tiene formato inválido o contiene caracteres no permitidos.")
                        .addExample(
                                "SELECT válido",
                                "\"query\": \"SELECT * FROM originacionseguros.solicitudes_polizas\""
                        )
                        .build()
        );

        // QUERY_DB_TYPE_EMPTY
        helpMessages.put(ErrorCategory.QUERY_DB_TYPE_EMPTY,
                HelpContent.builder()
                        .field("dbType")
                        .description("El campo dbType es obligatorio. Debe corresponder a una configuración válida en DatabaseFactory.")
                        .addExample(
                                "Query con dbType",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM usuarios\",\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // QUERY_DB_TYPE_INVALID
        helpMessages.put(ErrorCategory.QUERY_DB_TYPE_INVALID,
                HelpContent.builder()
                        .field("dbType")
                        .description("El dbType especificado no existe en la configuración. Valores válidos: db_openfinance, db_ods")
                        .addExample(
                                "dbType válido",
                                "\"dbType\": \"db_openfinance\""
                        )
                        .build()
        );

        // QUERY_ORDER_BY_INVALID
        helpMessages.put(ErrorCategory.QUERY_ORDER_BY_INVALID,
                HelpContent.builder()
                        .field("orderClause")
                        .description("La cláusula ORDER BY tiene formato inválido. Usa: 'campo ASC/DESC' o 'campo1 ASC, campo2 DESC'")
                        .addExample(
                                "ORDER BY simple",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM usuarios\",\n" +
                                        "  \"orderClause\": \"id DESC\",\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "ORDER BY múltiple",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM usuarios\",\n" +
                                        "  \"orderClause\": \"nombre ASC, fecha_creacion DESC\",\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // QUERY_PAGINATION_INVALID
        helpMessages.put(ErrorCategory.QUERY_PAGINATION_INVALID,
                HelpContent.builder()
                        .field("pagination")
                        .description("Los valores de paginación (limit/offset) son inválidos. Deben ser números positivos.")
                        .addExample(
                                "Paginación válida",
                                "{\n" +
                                        "  \"query\": \"SELECT * FROM usuarios LIMIT 10 OFFSET 20\",\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );
    }

    // ==================== ComplexSaveDto ====================

    private void initializeSaveMessages() {
        // SAVE_DATA_EMPTY
        helpMessages.put(ErrorCategory.SAVE_DATA_EMPTY,
                HelpContent.builder()
                        .field("values")
                        .description("El campo 'values' no puede estar vacío. Debe contener los valores a insertar.")
                        .addExample(
                                "Insertar un registro completo",
                                "{\n" +
                                        "  \"tableName\": \"originacionseguros.tipos_documento\",\n" +
                                        "  \"values\": {\n" +
                                        "    \"id\": 4,\n" +
                                        "    \"nombre\": \"Tarjeta de Identidad\",\n" +
                                        "    \"sigla\": \"TI\",\n" +
                                        "    \"descripcion\": \"Documento de identidad para menores de edad\"\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Insertar con valores mixtos",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\n" +
                                        "    \"nombre\": \"Juan Pérez\",\n" +
                                        "    \"edad\": 30,\n" +
                                        "    \"activo\": true,\n" +
                                        "    \"fecha_registro\": \"2025-01-15T10:30:00\",\n" +
                                        "    \"observaciones\": null\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // SAVE_DATA_INVALID
        helpMessages.put(ErrorCategory.SAVE_DATA_INVALID,
                HelpContent.builder()
                        .field("values")
                        .description("Los valores a insertar tienen formato inválido. Deben ser pares clave-valor con tipos de datos válidos.")
                        .addExample(
                                "Valores con diferentes tipos de datos",
                                "{\n" +
                                        "  \"values\": {\n" +
                                        "    \"nombre\": \"Juan Pérez\",\n" +
                                        "    \"edad\": 30,\n" +
                                        "    \"salario\": 5000.50,\n" +
                                        "    \"activo\": true,\n" +
                                        "    \"fecha_nacimiento\": \"1995-01-15T00:00:00\",\n" +
                                        "    \"departamento\": null\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .build()
        );

        // SAVE_TABLE_NAME_EMPTY
        helpMessages.put(ErrorCategory.SAVE_TABLE_NAME_EMPTY,
                HelpContent.builder()
                        .field("tableName")
                        .description("El campo tableName es obligatorio. Debe especificar la tabla donde insertar los datos.")
                        .addExample(
                                "INSERT con tableName completo",
                                "{\n" +
                                        "  \"tableName\": \"originacionseguros.tipos_documento\",\n" +
                                        "  \"values\": {\n" +
                                        "    \"nombre\": \"Cédula de Ciudadanía\",\n" +
                                        "    \"sigla\": \"CC\"\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // SAVE_TABLE_NAME_INVALID
        helpMessages.put(ErrorCategory.SAVE_TABLE_NAME_INVALID,
                HelpContent.builder()
                        .field("tableName")
                        .description("El formato del tableName es inválido. Usa solo letras, números, guiones bajos y punto para separar esquema.tabla.")
                        .addExample(
                                "Formato válido con esquema",
                                "\"tableName\": \"originacionseguros.tipos_documento\""
                        )
                        .addExample(
                                "Formato válido sin esquema",
                                "\"tableName\": \"clientes\""
                        )
                        .build()
        );

        // SAVE_DB_TYPE_EMPTY
        helpMessages.put(ErrorCategory.SAVE_DB_TYPE_EMPTY,
                HelpContent.builder()
                        .field("dbType")
                        .description("El campo dbType es obligatorio. Debe corresponder a una configuración válida en DatabaseFactory.")
                        .addExample(
                                "INSERT con dbType",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\n" +
                                        "    \"nombre\": \"María García\",\n" +
                                        "    \"email\": \"maria@example.com\"\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // SAVE_DB_TYPE_INVALID
        helpMessages.put(ErrorCategory.SAVE_DB_TYPE_INVALID,
                HelpContent.builder()
                        .field("dbType")
                        .description("El dbType especificado no existe en la configuración. Valores válidos: db_openfinance, db_ods")
                        .addExample(
                                "dbType válido",
                                "\"dbType\": \"db_openfinance\""
                        )
                        .build()
        );
    }

    // ==================== Genéricos ====================

    private void initializeGenericMessages() {
        // INVALID_JSON_STRUCTURE
        helpMessages.put(ErrorCategory.INVALID_JSON_STRUCTURE,
                HelpContent.builder()
                        .field("body")
                        .description("El JSON enviado en el body no se pudo parsear. Verifica la sintaxis JSON (llaves, comas, comillas).")
                        .addExample(
                                "Estructura JSON válida para DELETE",
                                "{\n" +
                                        "  \"tableName\": \"originacionseguros.tipos_documento\",\n" +
                                        "  \"whereClause\": {\"id\": 4},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Estructura JSON válida para UPDATE",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\"estado\": \"INACTIVO\"},\n" +
                                        "  \"whereClause\": {\"id\": 123},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Estructura JSON válida para INSERT",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\n" +
                                        "    \"nombre\": \"Juan Pérez\",\n" +
                                        "    \"email\": \"juan@example.com\"\n" +
                                        "  },\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // MISSING_REQUIRED_FIELD
        helpMessages.put(ErrorCategory.MISSING_REQUIRED_FIELD,
                HelpContent.builder()
                        .field("body")
                        .description("Falta un campo requerido en la petición. Los campos obligatorios varían según la operación.")
                        .addExample(
                                "Campos obligatorios para DELETE",
                                "{\n" +
                                        "  \"tableName\": \"<requerido>\",\n" +
                                        "  \"whereClause\": \"<requerido>\",\n" +
                                        "  \"dbType\": \"<requerido>\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Campos obligatorios para UPDATE",
                                "{\n" +
                                        "  \"tableName\": \"<requerido>\",\n" +
                                        "  \"values\": \"<requerido>\",\n" +
                                        "  \"whereClause\": \"<requerido>\",\n" +
                                        "  \"dbType\": \"<requerido>\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Campos obligatorios para INSERT",
                                "{\n" +
                                        "  \"tableName\": \"<requerido>\",\n" +
                                        "  \"values\": \"<requerido>\",\n" +
                                        "  \"dbType\": \"<requerido>\"\n" +
                                        "}"
                        )
                        .addExample(
                                "Campos obligatorios para QUERY",
                                "{\n" +
                                        "  \"query\": \"<requerido>\",\n" +
                                        "  \"dbType\": \"<requerido>\"\n" +
                                        "}"
                        )
                        .build()
        );

        // INVALID_DATA_TYPE
        helpMessages.put(ErrorCategory.INVALID_DATA_TYPE,
                HelpContent.builder()
                        .field("body")
                        .description("Un campo tiene un tipo de dato incorrecto. Verifica que los tipos coincidan con lo esperado.")
                        .addExample(
                                "Tipos de datos correctos",
                                "{\n" +
                                        "  \"tableName\": \"usuarios\",\n" +
                                        "  \"values\": {\n" +
                                        "    \"nombre\": \"texto como string\",\n" +
                                        "    \"edad\": 30,\n" +
                                        "    \"salario\": 5000.50,\n" +
                                        "    \"activo\": true,\n" +
                                        "    \"fecha\": \"2025-01-15T10:30:00\",\n" +
                                        "    \"notas\": null\n" +
                                        "  },\n" +
                                        "  \"whereClause\": {\"id\": 123},\n" +
                                        "  \"dbType\": \"db_openfinance\"\n" +
                                        "}"
                        )
                        .build()
        );

        // VALUE_OUT_OF_RANGE
        helpMessages.put(ErrorCategory.VALUE_OUT_OF_RANGE,
                HelpContent.builder()
                        .field("body")
                        .description("Un valor está fuera del rango permitido o tiene un formato incorrecto.")
                        .addExample(
                                "Valores dentro de rangos válidos",
                                "{\n" +
                                        "  \"values\": {\n" +
                                        "    \"edad\": 30,\n" +
                                        "    \"cantidad\": 100,\n" +
                                        "    \"porcentaje\": 0.25\n" +
                                        "  }\n" +
                                        "}"
                        )
                        .build()
        );
    }
}
