package com.weiz.Familia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO para realizar operaciones DELETE con formato JSON mejorado.
 *
 * <p>Este DTO permite eliminar registros de la base de datos utilizando
 * condiciones WHERE complejas con soporte para operadores avanzados:
 * <ul>
 * <li>Operadores de comparación: $gt, $gte, $lt, $lte, $ne</li>
 * <li>Operadores de búsqueda: $like para patrones</li>
 * <li>Operadores lógicos: $condition para OR/AND entre condiciones</li>
 * <li>Operadores IN: Arrays automáticos o $in explícito</li>
 * <li>Mayor seguridad: Formato estructurado y validado</li>
 * </ul>
 *
 * <p><strong>Ejemplo básico:</strong>
 * <pre>{@code
 * {
 *   "tableName": "originacionseguros.tipos_documento",
 *   "whereClause": {
 *     "id": 4
 *   },
 *   "dbType": "db_openfinance"
 * }
 * }</pre>
 *
 * <p><strong>Ejemplo con múltiples IDs:</strong>
 * <pre>{@code
 * {
 *   "tableName": "logs_sistema",
 *   "whereClause": {
 *     "id": [100, 101, 102, 103]
 *   },
 *   "dbType": "db_openfinance"
 * }
 * }</pre>
 * Genera: {@code DELETE FROM logs_sistema WHERE id IN (100, 101, 102, 103)}
 *
 * <p><strong>Ejemplo con operadores complejos:</strong>
 * <pre>{@code
 * {
 *   "tableName": "logs_auditoria",
 *   "whereClause": {
 *     "fecha_creacion": {"$lt": "2024-01-01T00:00:00"},
 *     "$condition": "AND",
 *     "nivel": ["DEBUG", "TRACE"]
 *   },
 *   "dbType": "db_openfinance"
 * }
 * }</pre>
 * Genera: {@code DELETE FROM logs_auditoria WHERE fecha_creacion < '2024-01-01' AND nivel IN ('DEBUG', 'TRACE')}
 *
 * <p><strong>⚠️ ADVERTENCIA DE SEGURIDAD:</strong>
 * <ul>
 * <li>SIEMPRE especificar un whereClause para evitar eliminar todos los registros</li>
 * <li>Verificar las condiciones antes de ejecutar operaciones DELETE</li>
 * <li>Considerar usar soft deletes (actualizar un campo "eliminado") en lugar de DELETE físico</li>
 * <li>Realizar backups antes de operaciones DELETE masivas</li>
 * </ul>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplexDeleteDto {

    /**
     * Nombre completo de la tabla de donde se eliminarán registros.
     *
     * <p>Debe incluir el esquema si es necesario. Este nombre se utilizará
     * directamente en la sentencia DELETE.
     *
     * <p><strong>Ejemplos:</strong>
     * <ul>
     * <li>{@code "originacionseguros.solicitudes_polizas"}</li>
     * <li>{@code "public.logs_sistema"}</li>
     * <li>{@code "temporal_imports"} (si el esquema está en el path por defecto)</li>
     * </ul>
     */
    private String tableName;

    /**
     * Mapa de condiciones para la cláusula WHERE.
     *
     * <p>Define qué registros serán eliminados. Utiliza el mismo sistema
     * de operadores complejos que ComplexQueryDto y ComplexUpdateDto:
     *
     * <p><strong>Operadores soportados:</strong>
     * <ul>
     * <li><strong>$gt:</strong> Mayor que ({@code >})</li>
     * <li><strong>$gte:</strong> Mayor o igual que ({@code >=})</li>
     * <li><strong>$lt:</strong> Menor que ({@code <})</li>
     * <li><strong>$lte:</strong> Menor o igual que ({@code <=})</li>
     * <li><strong>$ne:</strong> No igual ({@code !=})</li>
     * <li><strong>$like:</strong> Búsqueda con patrón LIKE</li>
     * <li><strong>$in:</strong> Lista de valores (también soporta arrays directos)</li>
     * <li><strong>$null:</strong> IS NULL (true) o IS NOT NULL (false)</li>
     * <li><strong>$condition:</strong> "OR" o "AND" para unir con la siguiente condición</li>
     * </ul>
     *
     * <p><strong>Ejemplos de condiciones:</strong>
     * <ul>
     * <li><strong>Por ID único:</strong>
     *   <pre>{@code {"id": 123}}</pre>
     *   Genera: {@code WHERE id = 123}
     * </li>
     * <li><strong>Por lista de IDs:</strong>
     *   <pre>{@code {"id": [1, 2, 3, 4, 5]}}</pre>
     *   Genera: {@code WHERE id IN (1, 2, 3, 4, 5)}
     * </li>
     * <li><strong>Por rango de fechas:</strong>
     *   <pre>{@code {"fecha_creacion": {"$lt": "2024-01-01T00:00:00"}}}</pre>
     *   Genera: {@code WHERE fecha_creacion < '2024-01-01 00:00:00'}
     * </li>
     * <li><strong>Por patrón:</strong>
     *   <pre>{@code {"nombre": {"$like": "%temporal%"}}}</pre>
     *   Genera: {@code WHERE nombre LIKE '%temporal%'}
     * </li>
     * <li><strong>Combinación con OR:</strong>
     *   <pre>{@code
     * {
     *   "estado": "INACTIVO",
     *   "$condition": "OR",
     *   "fecha_ultimo_acceso": {"$lt": "2023-01-01T00:00:00"}
     * }
     *   }</pre>
     *   Genera: {@code WHERE estado = 'INACTIVO' OR fecha_ultimo_acceso < '2023-01-01'}
     * </li>
     * </ul>
     *
     * <p><strong>⚠️ IMPORTANTE:</strong>
     * <ul>
     * <li>NUNCA dejar whereClause vacío o null sin validación previa</li>
     * <li>El servicio debe validar que whereClause no esté vacío</li>
     * <li>Considerar implementar soft deletes en lugar de DELETE físico</li>
     * </ul>
     */
    private Map<String, Object> whereClause;

    /**
     * Tipo de base de datos a utilizar.
     *
     * <p>Debe corresponder a una configuración válida en DatabaseFactory.
     * Este valor determina qué conexión de base de datos se utilizará
     * para ejecutar la operación DELETE.
     *
     * <p><strong>Ejemplos:</strong> {@code "db_openfinance"}, {@code "db_core"}, etc.
     */
    @JsonProperty("dbType")
    private String dbType;

    // Getters y Setters

    /**
     * Obtiene el nombre de la tabla.
     *
     * @return Nombre completo de la tabla (incluyendo esquema si aplica)
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Establece el nombre de la tabla.
     *
     * @param tableName Nombre completo de la tabla (incluyendo esquema si aplica)
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Obtiene las condiciones WHERE.
     *
     * @return Mapa con las condiciones para filtrar registros a eliminar
     */
    public Map<String, Object> getWhereClause() {
        return whereClause;
    }

    /**
     * Establece las condiciones WHERE.
     *
     * <p><strong>⚠️ IMPORTANTE:</strong> Asegurarse de que las condiciones
     * sean correctas para evitar eliminaciones no deseadas.
     *
     * @param whereClause Mapa con las condiciones para filtrar registros
     */
    public void setWhereClause(Map<String, Object> whereClause) {
        this.whereClause = whereClause;
    }

    /**
     * Obtiene el tipo de base de datos.
     *
     * @return Identificador del tipo de base de datos
     */
    public String getDbType() {
        return dbType;
    }

    /**
     * Establece el tipo de base de datos.
     *
     * @param dbType Identificador del tipo de base de datos
     */
    public void setDbType(String dbType) {
        this.dbType = dbType;
    }
}
