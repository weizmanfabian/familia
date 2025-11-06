package com.weiz.Familia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO para realizar consultas complejas con soporte para operadores avanzados.
 *
 * <p>Este DTO permite ejecutar consultas SQL con condiciones WHERE complejas que incluyen:
 * <ul>
 * <li>Operadores implícitos: Arrays se convierten automáticamente en IN, valores simples en igualdad</li>
 * <li>Operadores explícitos: $in, $like, $gt, $gte, $lt, $lte, $ne, $null, $ltInterval, $gtInterval, $lteInterval, $gteInterval</li>
 * <li>Múltiples condiciones combinadas con AND</li>
 * </ul>
 *
 * <p><strong>Ejemplos de uso:</strong>
 *
 * <p><em>Con arrays implícitos:</em>
 * <pre>{@code
 * {
 *   "query": "SELECT * FROM estados_solicitud_polizas",
 *   "whereClause": {
 *     "estado_solicitud_poliza_id": [1,2,3]
 *   },
 *   "orderClause": "id DESC",
 *   "db_type": "db_openfinance"
 * }
 * }</pre>
 *
 * <p><em>Con operadores explícitos:</em>
 * <pre>{@code
 * {
 *   "query": "SELECT * FROM users",
 *   "whereClause": {
 *     "nombre": {"$like": "%Juan%"},
 *     "edad": {"$gte": 18},
 *     "activo": {"$null": false}
 *   },
 *   "db_type": "db_openfinance"
 * }
 * }</pre>
 * <p><em>Con operadores de intervalo temporal:</em>
 * <pre>{@code
 * {
 *   "query": "SELECT * FROM solicitudes_polizas",
 *   "whereClause": {
 *     "estado_id": [1,2,3],
 *     "fecha_creacion": {"$gtInterval": 86400}
 *   },
 *   "orderClause": "fecha_creacion DESC",
 *   "limit": 100,
 *   "dbType": "db_openfinance"
 * }
 * }</pre>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplexQueryDto {

    /**
     * Consulta SQL base sin WHERE clause.
     *
     * <p>Debe ser una consulta SELECT válida. El WHERE clause se construirá
     * automáticamente basado en el campo whereClause.
     *
     * <p><strong>Ejemplo:</strong> {@code "SELECT * FROM users"}
     */
    private String query;

    /**
     * Mapa de condiciones para la cláusula WHERE.
     *
     * <p>Cada entrada del mapa representa una condición donde:
     * <ul>
     * <li><strong>Key:</strong> Nombre del campo/columna en la base de datos</li>
     * <li><strong>Value:</strong> Puede ser:
     *   <ul>
     *   <li>Valor simple (String, Number, Boolean): Se usa igualdad (=)</li>
     *   <li>Array/List: Se usa operador IN</li>
     *   <li>Map con operadores explícitos: Se usan operadores específicos</li>
     *   </ul>
     * </li>
     * </ul>
     *
     * <p><strong>Operadores soportados:</strong>
     * <ul>
     * <li>{@code $in}: Lista de valores para IN</li>
     * <li>{@code $like}: Patrón para LIKE</li>
     * <li>{@code $gt}: Mayor que (&gt;)</li>
     * <li>{@code $gte}: Mayor o igual que (&gt;=)</li>
     * <li>{@code $lt}: Menor que (&lt;)</li>
     * <li>{@code $lte}: Menor o igual que (&lt;=)</li>
     * <li>{@code $ne}: No igual (!=)</li>
     * <li>{@code $null}: IS NULL (true) o IS NOT NULL (false)</li>
     * </ul>
     *
     * <p><strong>Ejemplos:</strong>
     * <ul>
     * <li>{@code "id": 123} → {@code WHERE id = ?}</li>
     * <li>{@code "status": [1,2,3]} → {@code WHERE status IN (?,?,?)}</li>
     * <li>{@code "name": {"$like": "%Juan%"}} → {@code WHERE name LIKE ?}</li>
     * <li>{@code "age": {"$gte": 18}} → {@code WHERE age >= ?}</li>
     * </ul>
     */
    private Map<String, Object> whereClause;

    /**
     * Cláusula ORDER BY para ordenar los resultados.
     *
     * <p>Puede incluir o no la palabra clave "ORDER BY". Si no la incluye,
     * se agregará automáticamente.
     *
     * <p><strong>Ejemplos válidos:</strong>
     * <ul>
     * <li>{@code "id DESC"}</li>
     * <li>{@code "ORDER BY nombre ASC, fecha DESC"}</li>
     * <li>{@code "created_at DESC, id ASC"}</li>
     * </ul>
     */
    private String orderClause;

    /**
     * Tipo de base de datos a utilizar.
     *
     * <p>Debe corresponder a una configuración válida en DatabaseFactory.
     * Este valor determina qué conexión de base de datos se utilizará
     * para ejecutar la consulta.
     *
     * <p><strong>Ejemplos:</strong> {@code "db_openfinance"}, {@code "db_core"}, etc.
     */
    @JsonProperty("dbType")
    private String dbType;

    /**
     * Límite de registros a retornar (opcional).
     *
     * <p>Si se especifica, se agregará una cláusula LIMIT al final de la consulta.
     * Útil para paginar resultados o limitar el número de registros retornados.
     *
     * <p><strong>Ejemplo:</strong> {@code 100} generará {@code LIMIT 100}
     */
    private Integer limit;

    /**
     * Cláusula GROUP BY para agrupar resultados (opcional).
     *
     * <p>Permite agrupar registros por uno o más campos, útil para consultas
     * con funciones de agregación como COUNT, SUM, AVG, MAX, MIN.
     *
     * <p><strong>Ejemplos válidos:</strong>
     * <ul>
     * <li>{@code "estado_solicitud_poliza_id"}</li>
     * <li>{@code "sp.estado_solicitud_poliza_id, esp.nombre"}</li>
     * <li>{@code "fecha, categoria"}</li>
     * </ul>
     */
    private String groupBy;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Map<String, Object> getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(Map<String, Object> whereClause) {
        this.whereClause = whereClause;
    }

    public String getOrderClause() {
        return orderClause;
    }

    public void setOrderClause(String orderClause) {
        this.orderClause = orderClause;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }
}