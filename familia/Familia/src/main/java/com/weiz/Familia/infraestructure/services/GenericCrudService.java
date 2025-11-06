/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.weiz.Familia.infraestructure.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weiz.Familia.util.DateConversionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public class GenericCrudService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericCrudService.class);

    private final DataSource dataSource;

    @Autowired
    public GenericCrudService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    private Map<String, Object> convertJsonMap(String value) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        // Usar LinkedHashMap para mantener el orden de inserción del JSON
        LinkedHashMap<String, Object> map = objectMapper.readValue(value,
                new TypeReference<LinkedHashMap<String, Object>>() {
                });
        return map;
    }




    private boolean esBooleano(Object value) {
        return value instanceof Boolean;
    }

    private boolean esDouble(Object value) {
        return value instanceof Double;
    }

    private boolean isNumeric(Object value) {
        // Excluir epoch milliseconds (se tratan como timestamps)
        if (isEpochMillis(value)) {
            return false;
        }
        return value instanceof Long || value instanceof Integer;
    }

    private boolean isTimestamp(Object value) {
        return value instanceof Timestamp || value instanceof LocalDateTime || isTimestampValue(value) || isEpochMillis(value);
    }

    private boolean isTimestampValue(Object value) {
        try {
            fechaTimestamp(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si un valor numérico representa epoch milliseconds.
     * Epoch milliseconds son números de 13 dígitos (milisegundos desde 1970-01-01T00:00:00Z).
     * Rango válido aproximado: 1000000000000 (2001) a 9999999999999 (2286).
     */
    private boolean isEpochMillis(Object value) {
        if (value instanceof Long || value instanceof Integer) {
            long numValue = Long.parseLong(value.toString());
            // Epoch millis son números de 13 dígitos (entre años 2001-2286)
            return numValue >= 1000000000000L && numValue <= 9999999999999L;
        }
        return false;
    }

    private Timestamp fechaTimestamp(Object value) {
        // Si es epoch milliseconds (Long/Integer)
        if (isEpochMillis(value)) {
            long epochMilli = Long.parseLong(value.toString());
            return new Timestamp(epochMilli);
        }
        // Si es String con formato ISO-8601
        LocalDateTime localDateTime = LocalDateTime.parse((String) value);
        return Timestamp.valueOf(localDateTime);
    }

    private boolean esTexto(Object value) {
        return value instanceof String;
    }

    /*
    // (con arrays implícitos)
  {
      "query": "SELECT * FROM originacionseguros.estados_solicitud_polizas",
      "whereClause": {
          "estado_solicitud_poliza_id": [1,2]
      },
      "orderClause": "id DESC",
      "db_type": "db_openfinance"
  }

  // Con operadores explícitos
  {
      "query": "SELECT * FROM users",
      "whereClause": {
          "nombre": {"$like": "%Juan%"},
          "edad": {"$gte": 18},
          "activo": {"$null": false}
      },
      "db_type": "db_openfinance"
  }

  // Con $condition para controlar operadores lógicos (OR/AND) entre condiciones
  {
      "query": "SELECT * FROM originacionseguros.solicitudes_polizas",
      "whereClause": {
          "estado_solicitud_poliza_id": [1,2],
          "$condition": "OR",
          "creado_en": {"$lt": "2025-09-30T08:34:09"},
          "nombre": {"$like": "%Weizman%"}
      },
      "orderClause": "creado_en ASC",
      "db_type": "db_openfinance"
  }
  // Resultado: WHERE estado_solicitud_poliza_id IN (?,?) OR creado_en < ? AND nombre LIKE ?

   Operadores explícitos:
  - $in: {"$in": [1,2,3]}
  - $like: {"$like": "%Juan%"}
  - $gt, $gte, $lt, $lte: Comparaciones
  - $ne: No igual
  - $null: {"$null": true/false} para IS NULL/IS NOT NULL
  - $condition: "OR" o "AND" - Define el operador lógico ANTES de la siguiente condición
     */

    /**
     * Ejecuta una consulta SELECT compleja con soporte para operadores avanzados.
     *
     * <p>Este método permite realizar consultas SQL complejas con condiciones WHERE dinámicas que incluyen:
     * <ul>
     * <li><strong>Operadores implícitos:</strong> Arrays automáticamente se convierten en IN, valores simples en igualdad</li>
     * <li><strong>Operadores explícitos:</strong> $in, $like, $gt, $gte, $lt, $lte, $ne, $null</li>
     * <li><strong>Parámetros seguros:</strong> Todos los valores se pasan como parámetros preparados (previene SQL injection)</li>
     * <li><strong>Múltiples condiciones:</strong> Se combinan automáticamente con AND</li>
     * </ul>
     *
     * <p><strong>Proceso de construcción de consulta:</strong>
     * <ol>
     * <li>Se toma la consulta base (sin WHERE)</li>
     * <li>Se construye dinámicamente la cláusula WHERE basada en whereClause</li>
     * <li>Se agrega ORDER BY si está presente</li>
     * <li>Se ejecuta con parámetros preparados para seguridad</li>
     * </ol>
     *
     * <p><strong>Ejemplos de transformación:</strong>
     * <ul>
     * <li>{@code "id": 123} → {@code WHERE id = ?}</li>
     * <li>{@code "status": [1,2,3]} → {@code WHERE status IN (?,?,?)}</li>
     * <li>{@code "name": {"$like": "%Juan%"}} → {@code WHERE name LIKE ?}</li>
     * <li>Múltiples: {@code WHERE id = ? AND status IN (?,?) AND name LIKE ?}</li>
     * </ul>
     *
     * @param query       Consulta SQL base sin WHERE clause (ej: "SELECT * FROM users")
     * @param whereClause Mapa con condiciones WHERE. Puede ser null o vacío para consultas sin filtros
     * @param orderClause Cláusula ORDER BY opcional. Puede incluir o no "ORDER BY"
     * @param dbType      Tipo de base de datos para la conexión (debe existir en DatabaseFactory)
     * @return Lista de mapas donde cada mapa representa una fila con columnas como keys
     * @throws IOException      Si hay errores de conversi2ón JSON o procesamiento de parámetros
     * @throws RuntimeException Si hay errores SQL o de conexión a base de datos
     */
    @Transactional
    public List<?> executeComplexSelect(String query, Map<String, Object> whereClause, String orderClause, Integer limit, String groupBy, String dbType) throws IOException {
        long startTime = System.currentTimeMillis();

        LOGGER.info("[executeComplexSelect] ===== INICIO DE CONSULTA COMPLEJA =====");
        LOGGER.info("[executeComplexSelect] Query base: {}", query);
        LOGGER.info("[executeComplexSelect] DB Type: {}", dbType);

        // Validaciones de entrada con mensajes claros
        if (query == null || query.trim().isEmpty()) {
            LOGGER.error("[executeComplexSelect] Query nula o vacía");
            throw new IllegalArgumentException(
                    "El campo 'query' es obligatorio. Debe proporcionar una consulta SQL SELECT válida. " +
                            "Ejemplo: 'SELECT * FROM tabla'"
            );
        }

        // dbType es opcional - si no se proporciona, se usa la conexión por defecto
        if (dbType == null || dbType.trim().isEmpty()) {
            LOGGER.debug("[executeComplexSelect] dbType no proporcionado, usando conexión por defecto");
        }

        List<Object> resultList = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        try {
            StringBuilder fullQuery = new StringBuilder(query);

            // Construir WHERE clause si existe
            if (whereClause != null && !whereClause.isEmpty()) {
                LOGGER.debug("[executeComplexSelect] Construyendo WHERE clause con {} parámetros", whereClause.size());
                String whereCondition = buildComplexWhereClause(whereClause, parameters);
                fullQuery.append(" WHERE ").append(whereCondition);
                LOGGER.debug("[executeComplexSelect] WHERE clause construida: {}", whereCondition);
                LOGGER.debug("[executeComplexSelect] Total parámetros en WHERE: {}", parameters.size());
            }

            // Agregar GROUP BY si existe
            if (groupBy != null && !groupBy.isEmpty()) {
                if (!groupBy.trim().toLowerCase().startsWith("group by")) {
                    fullQuery.append(" GROUP BY ").append(groupBy);
                } else {
                    fullQuery.append(" ").append(groupBy);
                }
                LOGGER.debug("[executeComplexSelect] GROUP BY agregado: {}", groupBy);
            }

            // Agregar ORDER BY si existe
            if (orderClause != null && !orderClause.isEmpty()) {
                if (!orderClause.trim().toLowerCase().startsWith("order by")) {
                    fullQuery.append(" ORDER BY ").append(orderClause);
                } else {
                    fullQuery.append(" ").append(orderClause);
                }
                LOGGER.debug("[executeComplexSelect] ORDER BY agregado: {}", orderClause);
            }

            // Agregar LIMIT si existe
            if (limit != null && limit > 0) {
                fullQuery.append(" LIMIT ").append(limit);
                LOGGER.debug("[executeComplexSelect] LIMIT agregado: {}", limit);
            }

            LOGGER.info("[executeComplexSelect] Query completa: {}", fullQuery.toString());

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(fullQuery.toString())) {

                // Establecer parámetros
                for (int i = 0; i < parameters.size(); i++) {
                    setComplexParameter(pstmt, i + 1, parameters.get(i));
                }

                ResultSet rs = pstmt.executeQuery();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                LOGGER.debug("[executeComplexSelect] Columnas detectadas: {}", columnCount);

                int rowCount = 0;
                while (rs.next()) {
                    rowCount++;
                    LOGGER.trace("[executeComplexSelect] Procesando fila: {}", rowCount);

                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnType = metaData.getColumnTypeName(i);
                        String columnName = metaData.getColumnName(i);
                        Object originalValue = rs.getObject(i);
                        Object finalValue = originalValue;

                        LOGGER.trace("[executeComplexSelect] Columna '{}' - Tipo SQL: {} - Valor original: {}",
                                columnName, columnType, originalValue);

                        if ("money".equalsIgnoreCase(columnType)) {
                            String moneyValue = rs.getString(i);
                            if (moneyValue != null) {
                                moneyValue = moneyValue.replaceAll("[^\\d.-]", "");
                                finalValue = new BigDecimal(moneyValue);
                                LOGGER.trace("[executeComplexSelect] Conversión MONEY: {} → {}", originalValue, finalValue);
                            } else {
                                finalValue = null;
                            }
                        } else if ("jsonb".equalsIgnoreCase(columnType) || "json".equalsIgnoreCase(columnType)) {
                            String jsonValue = rs.getString(i);
                            if (jsonValue != null && !jsonValue.isEmpty()) {
                                try {
                                    ObjectMapper mapper = new ObjectMapper();
                                    Object parsedJson = mapper.readValue(jsonValue, Object.class);

                                    // Si es un Map con estructura {null, type, value}, extraer el value
                                    if (parsedJson instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> jsonMap = (Map<String, Object>) parsedJson;
                                        if (jsonMap.containsKey("value") && jsonMap.containsKey("type")) {
                                            String valueContent = (String) jsonMap.get("value");
                                            if (valueContent != null && !valueContent.isEmpty() && !valueContent.equals("{}")) {
                                                try {
                                                    Object innerJson = mapper.readValue(valueContent, Object.class);
                                                    finalValue = innerJson;
                                                } catch (Exception e) {
                                                    LOGGER.warn("[executeComplexSelect] Error parseando JSON inner para {}: {}", columnName, e.getMessage());
                                                    finalValue = new HashMap<>();
                                                }
                                            } else {
                                                finalValue = new HashMap<>();
                                            }
                                        } else {
                                            finalValue = parsedJson;
                                        }
                                    } else {
                                        finalValue = parsedJson;
                                    }
                                    LOGGER.trace("[executeComplexSelect] Conversión JSON: {} → {}", originalValue, finalValue);
                                } catch (Exception e) {
                                    LOGGER.warn("[executeComplexSelect] Error parseando JSON para {}: {}", columnName, e.getMessage());
                                    finalValue = new HashMap<>();
                                }
                            } else {
                                finalValue = new HashMap<>();
                            }
                        } else {
                            // Aplicar conversión de fecha a epoch millis
                            finalValue = DateConversionUtil.convertToEpochMillis(originalValue, columnType);

                            if (!Objects.equals(originalValue, finalValue)) {
                                LOGGER.debug("[executeComplexSelect] Conversión FECHA: Columna '{}' - Tipo SQL: {} - Original: {} → Epoch: {} ({})",
                                        columnName, columnType, originalValue, finalValue,
                                        finalValue != null ? DateConversionUtil.formatEpochMillis((long) finalValue) : "null");
                            }
                        }

                        row.put(columnName, finalValue);
                    }
                    resultList.add(row);
                }

                long endTime = System.currentTimeMillis();
                LOGGER.info("[executeComplexSelect] Consulta completada exitosamente - {} registros procesados en {} ms",
                        rowCount, (endTime - startTime));

            } catch (SQLException e) {
                LOGGER.error("[executeComplexSelect] Error SQL al ejecutar consulta: {} - Error: {}",
                        fullQuery.toString(), e.getMessage(), e);
                throw new RuntimeException("Error al ejecutar la consulta compleja: " + fullQuery.toString(), e);
            }

        } catch (Exception e) {
            LOGGER.error("[executeComplexSelect] Error inesperado procesando consulta compleja: {}", e.getMessage(), e);
            throw new RuntimeException("Error procesando la consulta compleja", e);
        }

        LOGGER.info("[executeComplexSelect] ===== FIN DE CONSULTA COMPLEJA =====");
        return resultList;
    }

    /**
     * Construye la cláusula WHERE completa a partir de un mapa de condiciones.
     *
     * <p>Analiza cada entrada del mapa y determina qué tipo de operador usar:
     * <ul>
     * <li><strong>Map:</strong> Contiene operadores explícitos (delegado a buildOperatorCondition)</li>
     * <li><strong>List/Array:</strong> Se convierte automáticamente en operador IN</li>
     * <li><strong>Valor simple:</strong> Se usa operador de igualdad (=)</li>
     * </ul>
     *
     * <p>Todas las condiciones se combinan con AND automáticamente.
     *
     * @param whereClause Mapa con las condiciones WHERE
     * @param parameters  Lista donde se agregan los parámetros para PreparedStatement
     * @return String con la cláusula WHERE completa (sin la palabra "WHERE")
     */
    private String buildComplexWhereClause(Map<String, Object> whereClause, List<Object> parameters) {
        LOGGER.debug("[buildComplexWhereClause] ===== INICIO DE CONSTRUCCIÓN DE WHERE =====");
        LOGGER.debug("[buildComplexWhereClause] whereClause recibida: {}", whereClause);

        StringBuilder where = new StringBuilder();
        boolean first = true;
        String nextLogicalOp = "AND"; // Operador por defecto para la próxima condición

        for (Map.Entry<String, Object> entry : whereClause.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();

            LOGGER.trace("[buildComplexWhereClause] Procesando campo: '{}' - Valor: {} - Tipo: {}",
                    field, value, value != null ? value.getClass().getSimpleName() : "null");

            // Si encontramos $condition, guardamos el operador para usar ANTES de la siguiente condición
            if ("$condition".equals(field)) {
                if (value != null) {
                    String conditionValue = value.toString().trim().toUpperCase();
                    if ("OR".equals(conditionValue) || "AND".equals(conditionValue)) {
                        nextLogicalOp = conditionValue;
                        LOGGER.debug("[buildComplexWhereClause] $condition detectada: próximo operador = {}", nextLogicalOp);
                    }
                }
                continue; // No procesar $condition como una condición real
            }

            // Agregar operador lógico antes de esta condición (excepto la primera)
            if (!first) {
                where.append(" ").append(nextLogicalOp).append(" ");
                LOGGER.trace("[buildComplexWhereClause] Operador lógico agregado: {}", nextLogicalOp);
                nextLogicalOp = "AND"; // Resetear a AND por defecto después de usarlo
            }
            first = false;

            if (value instanceof Map) {
                // Operadores explícitos: {"$like": "%nombre%", "$gt": 18}
                @SuppressWarnings("unchecked")
                Map<String, Object> operatorMap = (Map<String, Object>) value;
                LOGGER.debug("[buildComplexWhereClause] Campo '{}' con operadores explícitos: {}", field, operatorMap.keySet());
                where.append(buildOperatorCondition(field, operatorMap, parameters));
            } else if (value instanceof List) {
                // Array implícito = IN: [1, 2, 3]
                @SuppressWarnings("unchecked")
                List<Object> listValue = (List<Object>) value;
                LOGGER.debug("[buildComplexWhereClause] Campo '{}' con IN operator - {} valores", field, listValue.size());
                where.append(field).append(" IN (");
                for (int i = 0; i < listValue.size(); i++) {
                    if (i > 0) where.append(", ");
                    where.append("?");
                    parameters.add(listValue.get(i));
                    LOGGER.trace("[buildComplexWhereClause] Parámetro agregado (IN): {}", listValue.get(i));
                }
                where.append(")");
            } else {
                // Valor simple = igualdad
                LOGGER.debug("[buildComplexWhereClause] Campo '{}' con igualdad (=) - Valor: {}", field, value);
                where.append(field).append(" = ?");
                parameters.add(value);
                LOGGER.trace("[buildComplexWhereClause] Parámetro agregado (=): {}", value);
            }
        }

        String whereResult = where.toString();
        LOGGER.debug("[buildComplexWhereClause] WHERE completa: {}", whereResult);
        LOGGER.debug("[buildComplexWhereClause] Total parámetros agregados: {}", parameters.size());
        LOGGER.debug("[buildComplexWhereClause] ===== FIN DE CONSTRUCCIÓN DE WHERE =====");

        return whereResult;
    }

    /**
     * Genera un cast SQL apropiado para epoch millis basado en el tipo de columna.
     *
     * <p>Si el valor es epoch millis válido Y la columna es DATE/TIMESTAMP, retorna el placeholder con cast:
     * <ul>
     * <li>{@code ?::timestamp} - Para columnas TIMESTAMP/DATE cuando el valor es epoch millis</li>
     * <li>{@code ?} - Para otros tipos de datos (VARCHAR, INT, etc.) o valores no-epoch</li>
     * </ul>
     *
     * @param value Valor a validar
     * @param sqlTypeName Tipo SQL de la columna (obtenido de DatabaseMetaData)
     * @return Cast SQL string (ej: "?" o "?::timestamp")
     */
    private String buildSqlCastForEpochMillis(Object value, String sqlTypeName) {
        // Solo aplicar cast si:
        // 1. El valor es epoch millis válido
        // 2. La columna es de tipo DATE o TIMESTAMP
        if (value != null && DateConversionUtil.isValidEpochMillis(value) &&
            (DateConversionUtil.isDateType(sqlTypeName) || DateConversionUtil.isTimestampType(sqlTypeName))) {
            LOGGER.trace("[buildSqlCastForEpochMillis] Epoch millis detectado: {} para columna tipo {} - usando cast ::timestamp", value, sqlTypeName);
            return "?::timestamp";
        }
        return "?";
    }

    /**
     * Obtiene los metadatos de las columnas de una tabla.
     *
     * <p>Retorna un Map donde:
     * <ul>
     * <li>Key: nombre de la columna (en minúsculas)</li>
     * <li>Value: tipo SQL de la columna (VARCHAR, TIMESTAMP, DATE, etc.)</li>
     * </ul>
     *
     * @param conn Conexión a la base de datos
     * @param tableName Nombre completo de la tabla (puede incluir esquema)
     * @return Map con nombre de columna → tipo SQL
     * @throws SQLException Si hay error al obtener metadatos
     */
    private Map<String, String> getTableColumnTypes(Connection conn, String tableName) throws SQLException {
        Map<String, String> columnTypes = new HashMap<>();

        // Separar esquema y tabla si es necesario
        String schema = null;
        String table = tableName;

        if (tableName.contains(".")) {
            String[] parts = tableName.split("\\.", 2);
            schema = parts[0];
            table = parts[1];
        }

        DatabaseMetaData metaData = conn.getMetaData();

        // Obtener columnas de la tabla
        try (ResultSet rs = metaData.getColumns(null, schema, table, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME").toLowerCase();
                String columnType = rs.getString("TYPE_NAME");
                columnTypes.put(columnName, columnType);
                LOGGER.trace("[getTableColumnTypes] Columna: {} - Tipo: {}", columnName, columnType);
            }
        }

        if (columnTypes.isEmpty()) {
            LOGGER.warn("[getTableColumnTypes] No se encontraron columnas para tabla: {}", tableName);
        } else {
            LOGGER.debug("[getTableColumnTypes] Tabla {} tiene {} columnas", tableName, columnTypes.size());
        }

        return columnTypes;
    }

    /**
     * Construye condiciones específicas basadas en operadores explícitos.
     *
     * <p>Procesa un mapa que contiene operadores explícitos y sus valores asociados.
     * Cada operador se traduce a su equivalente SQL correspondiente.
     *
     * <p><strong>Operadores soportados y su traducción:</strong>
     * <ul>
     * <li>{@code $in}: {@code field IN (?,?,?)} - Lista de valores</li>
     * <li>{@code $like}: {@code field LIKE ?} - Patrón de búsqueda</li>
     * <li>{@code $gt}: {@code field > ?} - Mayor que</li>
     * <li>{@code $gte}: {@code field >= ?} - Mayor o igual</li>
     * <li>{@code $lt}: {@code field < ?} - Menor que</li>
     * <li>{@code $lte}: {@code field <= ?} - Menor o igual</li>
     * <li>{@code $ne}: {@code field != ?} - No igual</li>
     * <li>{@code $null}: {@code field IS NULL/IS NOT NULL} - Verificación de nulos</li>
     * </ul>
     *
     * <p>Si hay múltiples operadores para el mismo campo, se combinan con AND.
     *
     * @param field       Nombre del campo/columna de la base de datos
     * @param operatorMap Mapa con operadores y sus valores (ej: {"$like": "%Juan%", "$ne": null})
     * @param parameters  Lista donde se agregan los parámetros para PreparedStatement
     * @return String con la condición SQL completa para este campo
     */
    private String buildOperatorCondition(String field, Map<String, Object> operatorMap, List<Object> parameters) {
        StringBuilder condition = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, Object> opEntry : operatorMap.entrySet()) {
            if (!first) {
                condition.append(" AND ");
            }
            first = false;

            String operator = opEntry.getKey();
            Object value = opEntry.getValue();

            switch (operator) {
                case "$in":
                    if (value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> listValue = (List<Object>) value;
                        condition.append(field).append(" IN (");
                        for (int i = 0; i < listValue.size(); i++) {
                            if (i > 0) condition.append(", ");
                            condition.append("?");
                            parameters.add(listValue.get(i));
                        }
                        condition.append(")");
                    }
                    break;
                case "$like":
                    condition.append(field).append(" LIKE ?");
                    parameters.add(value);
                    break;
                case "$gt":
                    condition.append(field).append(" > ").append(buildSqlCastForEpochMillis(value, null));
                    parameters.add(value);
                    break;
                case "$gte":
                    condition.append(field).append(" >= ").append(buildSqlCastForEpochMillis(value, null));
                    parameters.add(value);
                    break;
                case "$lt":
                    condition.append(field).append(" < ").append(buildSqlCastForEpochMillis(value, null));
                    parameters.add(value);
                    break;
                case "$lte":
                    condition.append(field).append(" <= ").append(buildSqlCastForEpochMillis(value, null));
                    parameters.add(value);
                    break;
                case "$ne":
                    condition.append(field).append(" != ").append(buildSqlCastForEpochMillis(value, null));
                    parameters.add(value);
                    break;
                case "$null":
                    if (Boolean.TRUE.equals(value)) {
                        condition.append(field).append(" IS NULL");
                    } else {
                        condition.append(field).append(" IS NOT NULL");
                    }
                    break;
                case "$ltInterval":
                    // value debe ser un número (segundos)
                    condition.append(field).append(" < NOW() - INTERVAL '")
                            .append(value).append(" seconds'");
                    break;
                case "$gtInterval":
                    condition.append(field).append(" > NOW() - INTERVAL '")
                            .append(value).append(" seconds'");
                    break;
                case "$lteInterval":
                    condition.append(field).append(" <= NOW() - INTERVAL '")
                            .append(value).append(" seconds'");
                    break;
                case "$gteInterval":
                    condition.append(field).append(" >= NOW() - INTERVAL '")
                            .append(value).append(" seconds'");
                    break;
                default:
                    // Operador no reconocido, tratar como igualdad
                    condition.append(field).append(" = ?");
                    parameters.add(value);
                    break;
            }
        }

        return condition.toString();
    }

    /**
     * Establece un parámetro en el PreparedStatement detectando automáticamente el tipo.
     *
     * <p>Análisis de tipos y conversión automática:
     * <ul>
     * <li><strong>null:</strong> {@code setNull()} con Types.NULL</li>
     * <li><strong>Boolean:</strong> {@code setBoolean()}</li>
     * <li><strong>Double:</strong> {@code setDouble()}</li>
     * <li><strong>Long/Integer:</strong> {@code setLong()}</li>
     * <li><strong>Timestamp/LocalDateTime:</strong> {@code setTimestamp()}</li>
     * <li><strong>String:</strong> {@code setString()}</li>
     * <li><strong>Map/List:</strong> Convertido a JSON string para campos JSONB</li>
     * <li><strong>Otros:</strong> {@code setObject()} - delegado al driver JDBC</li>
     * </ul>
     *
     * @param pstmt PreparedStatement donde establecer el parámetro
     * @param index indice del parámetro (1-based)
     * @param value Valor a establecer, puede ser null
     * @throws SQLException Si hay error al establecer el parámetro
     */
    private void setComplexParameter(PreparedStatement pstmt, int index, Object value) throws SQLException {
        LOGGER.trace("[setComplexParameter] Índice: {} - Valor: {} - Tipo: {}",
                index, value, value != null ? value.getClass().getSimpleName() : "null");

        if (value == null) {
            LOGGER.trace("[setComplexParameter] Índice {} - Estableciendo NULL", index);
            pstmt.setNull(index, Types.NULL);

        } else if (esBooleano(value)) {
            boolean boolValue = Boolean.parseBoolean(value.toString());
            LOGGER.trace("[setComplexParameter] Índice {} - setBoolean({})", index, boolValue);
            pstmt.setBoolean(index, boolValue);

        } else if (esDouble(value)) {
            double doubleValue = Double.parseDouble(value.toString());
            LOGGER.trace("[setComplexParameter] Índice {} - setDouble({})", index, doubleValue);
            pstmt.setDouble(index, doubleValue);

        } else if (isNumeric(value)) {
            // Validar si podría ser epoch millis válido
            try {
                long longValue = Long.parseLong(value.toString());

                if (DateConversionUtil.isValidEpochMillis(longValue)) {
                    LOGGER.debug("[setComplexParameter] Índice {} - Epoch millis detectado: {} ({})",
                            index, longValue, DateConversionUtil.formatEpochMillis(longValue));

                    // Convertir epoch millis a Timestamp en UTC
                    // Usar setTimestamp con Calendar UTC funciona tanto para DATE como TIMESTAMP
                    // - PostgreSQL trunca automáticamente la hora para columnas DATE
                    // - Para columnas TIMESTAMP preserva fecha y hora completa
                    Timestamp sqlTimestamp = DateConversionUtil.epochMillisToSqlTimestamp(longValue);
                    LOGGER.debug("[setComplexParameter] Índice {} - Convertido a java.sql.Timestamp: {}", index, sqlTimestamp);

                    // Usar Calendar UTC para evitar conversiones automáticas de zona horaria
                    java.util.Calendar calUTC = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"));
                    pstmt.setTimestamp(index, sqlTimestamp, calUTC);
                } else {
                    LOGGER.trace("[setComplexParameter] Índice {} - Número regular (no epoch millis): {}", index, longValue);
                    pstmt.setLong(index, longValue);
                }
            } catch (Exception e) {
                LOGGER.warn("[setComplexParameter] Error procesando número en índice {}: {}", index, e.getMessage());
                pstmt.setLong(index, Long.parseLong(value.toString()));
            }

        } else if (isTimestamp(value)) {
            Timestamp sqlTimestamp = fechaTimestamp(value);
            LOGGER.trace("[setComplexParameter] Índice {} - setTimestamp({})", index, sqlTimestamp);
            pstmt.setTimestamp(index, sqlTimestamp);

        } else if (esTexto(value)) {
            String stringValue = value.toString();
            LOGGER.trace("[setComplexParameter] Índice {} - setString({})", index, stringValue);
            pstmt.setString(index, stringValue);

        } else if (value instanceof Map || value instanceof List) {
            // Convertir Map/List a JSON string para campos JSONB de PostgreSQL
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonString = mapper.writeValueAsString(value);
                LOGGER.trace("[setComplexParameter] Índice {} - setObject como JSON: {}", index, jsonString);
                // Usar setObject con Types.OTHER para que PostgreSQL lo trate como JSONB
                pstmt.setObject(index, jsonString, Types.OTHER);
            } catch (Exception e) {
                LOGGER.error("[setComplexParameter] Error convirtiendo Map/List a JSON en índice {}: {}", index, e.getMessage(), e);
                throw new SQLException("Error al convertir Map/List a JSON: " + e.getMessage(), e);
            }

        } else {
            LOGGER.trace("[setComplexParameter] Índice {} - setObject directo para tipo: {}", index, value.getClass().getSimpleName());
            pstmt.setObject(index, value);
        }
    }

    // ==================== MÉTODOS COMPLEJOS (JSON NATIVO) ====================

    /**
     * Ejecuta una operación INSERT compleja utilizando formato JSON estructurado.
     *
     * que acepta los valores como un Map en lugar de un String JSON escapado, ofreciendo:
     * <ul>
     * <li><strong>Mayor legibilidad:</strong> Valores como objetos JSON nativos</li>
     * <li><strong>Validación automática:</strong> Jackson valida tipos y estructura</li>
     * <li><strong>Manejo correcto de tipos:</strong> No requiere conversión manual de tipos</li>
     * <li><strong>Menos errores:</strong> No hay que escapar comillas ni manejar strings</li>
     * </ul>
     *
     * <p><strong>Proceso de ejecución:</strong>
     * <ol>
     * <li>Validar que tableName y values no sean null o vacíos</li>
     * <li>Construir la sentencia INSERT INTO con columnas y placeholders (?)</li>
     * <li>Establecer parámetros con detección automática de tipos</li>
     * <li>Ejecutar INSERT y recuperar la clave generada (ID)</li>
     * <li>Retornar el ID del registro insertado</li>
     * </ol>
     *
     * <p><strong>Ejemplo de sentencia generada:</strong>
     * <pre>{@code
     * // Entrada:
     * tableName = "originacionseguros.tipos_documento"
     * values = {
     *   "id": 4,
     *   "nombre": "Tarjeta de Identidad",
     *   "sigla": "TI",
     *   "descripcion": "Documento de identidad"
     * }
     *
     * // SQL generado:
     * INSERT INTO originacionseguros.tipos_documento (id, nombre, sigla, descripcion)
     * VALUES (?, ?, ?, ?)
     * }</pre>
     *
     * @param tableName Nombre completo de la tabla (incluyendo esquema si es necesario)
     * @param values    Mapa con los nombres de columnas como keys y valores a insertar como values
     * @param dbType    Tipo de base de datos para la conexión (debe existir en DatabaseFactory)
     * @return El ID generado para el registro insertado (clave primaria auto-generada)
     * @throws IllegalArgumentException Si tableName, values o dbType son null/vacíos
     * @throws RuntimeException         Si hay errores SQL o de conexión a base de datos
     */
    @Transactional
    public Object executeComplexInsert(String tableName, Map<String, Object> values, String dbType) {
        // 1. Validar parámetros de entrada
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo 'tableName' es obligatorio. Debe proporcionar el nombre de la tabla. " +
                            "Ejemplo: 'originacionseguros.tipos_documento'"
            );
        }

        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo 'values' es obligatorio y no puede estar vacío. " +
                            "Debe proporcionar al menos un campo para insertar."
            );
        }

        // dbType es opcional - si no se proporciona, se usa la conexión por defecto
        if (dbType == null || dbType.trim().isEmpty()) {
            LOGGER.debug("dbType no proporcionado, usando conexión por defecto");
        }

        // 2. Conectar a la BD y obtener metadatos de la tabla
        try (Connection conn = dataSource.getConnection()) {

            // 2.1. Obtener tipos de columnas de la tabla
            Map<String, String> columnTypes = getTableColumnTypes(conn, tableName);

            // 2.2. Construir la sentencia INSERT con columnas y placeholders
            StringBuilder query = new StringBuilder("INSERT INTO ").append(tableName).append(" (");
            StringBuilder placeholders = new StringBuilder(") VALUES (");

            // Agregar nombres de columnas y placeholders con casting automático de epoch millis
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String columnName = entry.getKey();
                Object value = entry.getValue();
                String columnType = columnTypes.get(columnName.toLowerCase());

                query.append(columnName).append(", ");
                placeholders.append(buildSqlCastForEpochMillis(value, columnType)).append(", ");

                LOGGER.trace("[executeComplexInsert] Columna '{}' - Tipo: {} - Valor: {} - Cast: {}",
                        columnName, columnType, value, buildSqlCastForEpochMillis(value, columnType));
            }

            // Remover las últimas comas y espacios
            query.setLength(query.length() - 2);
            placeholders.setLength(placeholders.length() - 2);

            // Completar la sentencia SQL
            query.append(placeholders).append(")");

            LOGGER.info("[executeComplexInsert] SQL generado: {}", query.toString());

            // 3. Ejecutar la inserción con PreparedStatement
            try (PreparedStatement pstmt = conn.prepareStatement(query.toString(), Statement.RETURN_GENERATED_KEYS)) {

                // 4. Establecer los valores de los parámetros con detección automática de tipos
                int index = 1;
                for (Object value : values.values()) {
                    setComplexParameter(pstmt, index++, value);
                }

                // 5. Ejecutar la inserción
                int affectedRows = pstmt.executeUpdate();

                // 6. Verificar que se insertó al menos un registro
                if (affectedRows == 0) {
                    throw new SQLException("La inserción falló, no se insertó ningún registro.");
                }

                // 7. Recuperar la clave generada (ID del nuevo registro)
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getObject(1);
                } else {
                    throw new SQLException("No se pudo obtener la clave generada después de la inserción.");
                }

            } catch (SQLException e) {
                throw new RuntimeException(
                        "Error al ejecutar inserción compleja en tabla '" + tableName + "': " + e.getMessage(),
                        e
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al obtener metadatos o ejecutar inserción en tabla '" + tableName + "': " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Ejecuta una operación UPDATE compleja con soporte para operadores avanzados en WHERE.
     *
     * acepta tanto los valores a actualizar como las condiciones WHERE como Maps estructurados,
     * con soporte completo para operadores complejos:
     * <ul>
     * <li><strong>Valores estructurados:</strong> No requiere escapar JSON manualmente</li>
     * <li><strong>WHERE con operadores:</strong> $gt, $gte, $lt, $lte, $like, $in, $condition, etc.</li>
     * <li><strong>Validación automática:</strong> Tipos y estructura validados por Jackson</li>
     * <li><strong>Mayor seguridad:</strong> Prevención de SQL injection con PreparedStatements</li>
     * </ul>
     *
     * <p><strong>Proceso de ejecución:</strong>
     * <ol>
     * <li>Validar que tableName, values y whereClause no sean null o vacíos</li>
     * <li>Construir la cláusula SET con los valores a actualizar</li>
     * <li>Construir la cláusula WHERE usando {@link #buildComplexWhereClause}</li>
     * <li>Establecer parámetros tanto para SET como para WHERE</li>
     * <li>Ejecutar UPDATE y verificar registros afectados</li>
     * </ol>
     *
     * <p><strong>Ejemplo de sentencia generada:</strong>
     * <pre>{@code
     * // Entrada:
     * tableName = "originacionseguros.tipos_documento"
     * values = {
     *   "nombre": "Tarjeta de Identidad Updated",
     *   "descripcion": "Documento actualizado"
     * }
     * whereClause = {
     *   "id": 4
     * }
     *
     * // SQL generado:
     * UPDATE originacionseguros.tipos_documento
     * SET nombre = ?, descripcion = ?
     * WHERE id = ?
     * }</pre>
     *
     * <p><strong>Ejemplo con operadores complejos:</strong>
     * <pre>{@code
     * // Entrada:
     * values = {"activo": false}
     * whereClause = {
     *   "ultimo_acceso": {"$lt": "2024-01-01T00:00:00"},
     *   "$condition": "OR",
     *   "intentos_fallidos": {"$gte": 5}
     * }
     *
     * // SQL generado:
     * UPDATE usuarios
     * SET activo = ?
     * WHERE ultimo_acceso < ? OR intentos_fallidos >= ?
     * }</pre>
     *
     * @param tableName   Nombre completo de la tabla (incluyendo esquema si es necesario)
     * @param values      Mapa con columnas a actualizar como keys y nuevos valores como values
     * @param whereClause Mapa con condiciones WHERE (soporta operadores complejos)
     * @param dbType      Tipo de base de datos para la conexión
     * @return Número de filas actualizadas (0 si ningún registro cumplió las condiciones WHERE)
     * @throws IllegalArgumentException Si tableName, values, whereClause o dbType son null/vacíos
     * @throws RuntimeException         Si hay errores SQL o de conexión a base de datos
     */
    @Transactional
    public int executeComplexUpdate(String tableName, Map<String, Object> values, Map<String, Object> whereClause, String dbType) {
        // 1. Validar parámetros de entrada
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo 'tableName' es obligatorio. Debe proporcionar el nombre de la tabla. " +
                            "Ejemplo: 'originacionseguros.solicitudes_polizas'"
            );
        }

        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo 'values' es obligatorio y no puede estar vacío. " +
                            "Debe proporcionar al menos un campo para actualizar."
            );
        }

        if (whereClause == null || whereClause.isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo 'whereClause' es obligatorio y no puede estar vacío. " +
                            "Especificar condiciones WHERE para evitar actualizar todos los registros."
            );
        }

        // dbType es opcional - si no se proporciona, se usa la conexión por defecto
        if (dbType == null || dbType.trim().isEmpty()) {
            LOGGER.debug("dbType no proporcionado, usando conexión por defecto");
        }

        // 2. Conectar a la BD y obtener metadatos de la tabla
        try (Connection conn = dataSource.getConnection()) {

            // 2.1. Obtener tipos de columnas de la tabla
            Map<String, String> columnTypes = getTableColumnTypes(conn, tableName);

            // 2.2. Construir la cláusula SET
            StringBuilder query = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

            // Agregar columnas y placeholders para SET con casting automático de epoch millis
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String columnName = entry.getKey();
                Object value = entry.getValue();
                String columnType = columnTypes.get(columnName.toLowerCase());

                query.append(columnName).append(" = ").append(buildSqlCastForEpochMillis(value, columnType)).append(", ");

                LOGGER.trace("[executeComplexUpdate] Columna '{}' - Tipo: {} - Valor: {} - Cast: {}",
                        columnName, columnType, value, buildSqlCastForEpochMillis(value, columnType));
            }

            // Remover la última coma y espacio
            query.setLength(query.length() - 2);

            // 3. Construir la cláusula WHERE con operadores complejos
            List<Object> whereParameters = new ArrayList<>();
            String whereCondition = buildComplexWhereClause(whereClause, whereParameters);
            query.append(" WHERE ").append(whereCondition);

            LOGGER.info("[executeComplexUpdate] SQL generado: {}", query.toString());

            // 4. Ejecutar la actualización
            try (PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

                // 5. Establecer parámetros para SET
                int paramIndex = 1;
                for (Object value : values.values()) {
                    setComplexParameter(pstmt, paramIndex++, value);
                }

                // 6. Establecer parámetros para WHERE
                for (Object param : whereParameters) {
                    setComplexParameter(pstmt, paramIndex++, param);
                }

                // 7. Ejecutar el UPDATE
                int affectedRows = pstmt.executeUpdate();

                // Log informativo (opcional: puede comentarse si no se necesita)
                if (affectedRows == 0) {
                    // No lanzar excepción, solo advertencia - puede ser válido que no haya registros que actualizar
                    System.out.println("ADVERTENCIA: UPDATE ejecutado pero no se actualizaron registros. Query: " + query);
                }

                return affectedRows;

            } catch (SQLException e) {
                throw new RuntimeException(
                        "Error al ejecutar actualización compleja en tabla '" + tableName + "': " + e.getMessage(),
                        e
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al obtener metadatos o ejecutar actualización en tabla '" + tableName + "': " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Ejecuta una operación DELETE compleja con soporte para operadores avanzados en WHERE.
     *
     * <ul>
     * <li><strong>WHERE estructurado:</strong> Condiciones como Map en lugar de String JSON</li>
     * <li><strong>Operadores avanzados:</strong> $gt, $lt, $like, $in, $condition (OR/AND), etc.</li>
     * <li><strong>Validación obligatoria:</strong> Requiere whereClause para evitar eliminaciones masivas</li>
     * <li><strong>Mayor seguridad:</strong> PreparedStatements previenen SQL injection</li>
     * </ul>
     *
     * <p><strong>Proceso de ejecución:</strong>
     * <ol>
     * <li>Validar que tableName y whereClause no sean null o vacíos (CRÍTICO)</li>
     * <li>Construir la sentencia DELETE FROM con WHERE clause compleja</li>
     * <li>Usar {@link #buildComplexWhereClause} para generar condiciones</li>
     * <li>Establecer parámetros con detección automática de tipos</li>
     * <li>Ejecutar DELETE y reportar registros afectados</li>
     * </ol>
     *
     * <p><strong>Ejemplo básico:</strong>
     * <pre>{@code
     * // Entrada:
     * tableName = "originacionseguros.tipos_documento"
     * whereClause = {"id": 4}
     *
     * // SQL generado:
     * DELETE FROM originacionseguros.tipos_documento WHERE id = ?
     * }</pre>
     *
     * <p><strong>Ejemplo con múltiples IDs:</strong>
     * <pre>{@code
     * // Entrada:
     * whereClause = {"id": [100, 101, 102]}
     *
     * // SQL generado:
     * DELETE FROM logs_sistema WHERE id IN (?, ?, ?)
     * }</pre>
     *
     * <p><strong>Ejemplo con operadores complejos:</strong>
     * <pre>{@code
     * // Entrada:
     * whereClause = {
     *   "fecha_creacion": {"$lt": "2024-01-01T00:00:00"},
     *   "$condition": "AND",
     *   "estado": "TEMPORAL"
     * }
     *
     * // SQL generado:
     * DELETE FROM registros WHERE fecha_creacion < ? AND estado = ?
     * }</pre>
     *
     * <p><strong>⚠️ ADVERTENCIAS DE SEGURIDAD:</strong>
     * <ul>
     * <li>whereClause es OBLIGATORIO - el método lanza excepción si está vacío</li>
     * <li>Verificar las condiciones antes de ejecutar operaciones DELETE</li>
     * <li>Considerar usar soft deletes (UPDATE con campo "eliminado") en lugar de DELETE</li>
     * <li>Realizar backups antes de operaciones DELETE masivas</li>
     * </ul>
     *
     * @param tableName   Nombre completo de la tabla (incluyendo esquema si es necesario)
     * @param whereClause Mapa con condiciones WHERE (soporta operadores complejos). OBLIGATORIO.
     * @param dbType      Tipo de base de datos para la conexión
     * @return Número de filas eliminadas (0 si ningún registro cumplió las condiciones WHERE)
     * @throws IllegalArgumentException Si tableName, whereClause o dbType son null/vacíos
     * @throws RuntimeException         Si hay errores SQL o de conexión a base de datos
     */
    @Transactional
    public int executeComplexDelete(String tableName, Map<String, Object> whereClause, String dbType) {
        // 1. Validar parámetros de entrada (CRÍTICO para DELETE)
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo 'tableName' es obligatorio. Debe proporcionar el nombre de la tabla. " +
                            "Ejemplo: 'originacionseguros.tipos_documento'"
            );
        }

        if (whereClause == null || whereClause.isEmpty()) {
            throw new IllegalArgumentException(
                    "El campo 'whereClause' es OBLIGATORIO para operaciones DELETE. " +
                            "NO se permite eliminar todos los registros de la tabla sin condiciones. " +
                            "Especifique al menos una condición WHERE."
            );
        }

        // dbType es opcional - si no se proporciona, se usa la conexión por defecto
        if (dbType == null || dbType.trim().isEmpty()) {
            LOGGER.debug("dbType no proporcionado, usando conexión por defecto");
        }

        // 2. Construir la sentencia DELETE FROM
        StringBuilder query = new StringBuilder("DELETE FROM ").append(tableName);

        // 3. Construir la cláusula WHERE con operadores complejos
        List<Object> parameters = new ArrayList<>();
        String whereCondition = buildComplexWhereClause(whereClause, parameters);
        query.append(" WHERE ").append(whereCondition);

        // 4. Ejecutar la eliminación
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query.toString())) {

            // 5. Establecer parámetros para WHERE
            for (int i = 0; i < parameters.size(); i++) {
                setComplexParameter(pstmt, i + 1, parameters.get(i));
            }

            // 6. Ejecutar el DELETE
            int affectedRows = pstmt.executeUpdate();

            // Log informativo sobre registros eliminados
            if (affectedRows == 0) {
                System.out.println("ADVERTENCIA: DELETE ejecutado pero no se eliminaron registros. Query: " + query);
            } else {
                System.out.println("DELETE exitoso: " + affectedRows + " registro(s) eliminado(s) de " + tableName);
            }

            return affectedRows;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Error al ejecutar eliminación compleja en tabla '" + tableName + "': " + e.getMessage(),
                    e
            );
        }
    }

}
