package com.weiz.Familia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO para realizar operaciones UPDATE con formato JSON mejorado.
 *
 * <p>Este DTO permite actualizar registros en la base de datos utilizando un formato
 * JSON estructurado, combinando:
 * <ul>
 * <li>Valores a actualizar: Como objeto JSON nativo (no string escapado)</li>
 * <li>Condiciones WHERE: Con soporte para operadores complejos ($condition, $gt, $like, etc.)</li>
 * <li>Mayor legibilidad: Formato JSON limpio y fácil de leer</li>
 * <li>Validación automática: Jackson valida la estructura</li>
 * </ul>
 *
 * <p><strong>Ejemplo básico:</strong>
 * <pre>{@code
 * {
 *   "tableName": "originacionseguros.tipos_documento",
 *   "values": {
 *     "nombre": "Tarjeta de Identidad Updated",
 *     "descripcion": "Documento de identidad para menores de edad Updated"
 *   },
 *   "whereClause": {
 *     "id": 4
 *   },
 *   "dbType": "db_openfinance"
 * }
 * }</pre>
 *
 * <p><strong>Ejemplo con operadores complejos:</strong>
 * <pre>{@code
 * {
 *   "tableName": "usuarios",
 *   "values": {
 *     "activo": false,
 *     "fecha_desactivacion": "2025-09-30T10:00:00"
 *   },
 *   "whereClause": {
 *     "ultimo_acceso": {"$lt": "2024-01-01T00:00:00"},
 *     "$condition": "OR",
 *     "intentos_fallidos": {"$gte": 5}
 *   },
 *   "dbType": "db_openfinance"
 * }
 * }</pre>
 *
 * <p>Esto generará:
 * <pre>{@code
 * UPDATE usuarios
 * SET activo = false, fecha_desactivacion = '2025-09-30T10:00:00'
 * WHERE ultimo_acceso < '2024-01-01T00:00:00' OR intentos_fallidos >= 5
 * }</pre>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplexUpdateDto {

    /**
     * Nombre completo de la tabla donde se actualizarán registros.
     *
     * <p>Debe incluir el esquema si es necesario. Este nombre se utilizará
     * directamente en la sentencia UPDATE.
     *
     * <p><strong>Ejemplos:</strong>
     * <ul>
     * <li>{@code "originacionseguros.solicitudes_polizas"}</li>
     * <li>{@code "public.clientes"}</li>
     * <li>{@code "productos"} (si el esquema está en el path por defecto)</li>
     * </ul>
     */
    private String tableName;

    /**
     * Mapa de valores a actualizar (cláusula SET).
     *
     * <p>Cada entrada del mapa representa una columna y su nuevo valor:
     * <ul>
     * <li><strong>Key:</strong> Nombre de la columna a actualizar</li>
     * <li><strong>Value:</strong> Nuevo valor, puede ser:
     *   <ul>
     *   <li>String: Texto o fechas en formato ISO-8601</li>
     *   <li>Number: Integer, Long, Double, BigDecimal</li>
     *   <li>Boolean: true/false</li>
     *   <li>null: Para establecer NULL en la columna</li>
     *   </ul>
     * </li>
     * </ul>
     *
     * <p><strong>Notas importantes:</strong>
     * <ul>
     * <li>Solo incluir columnas que se desean actualizar</li>
     * <li>Las fechas deben enviarse en formato ISO-8601: "yyyy-MM-dd'T'HH:mm:ss"</li>
     * <li>Los valores null establecerán NULL en SQL</li>
     * <li>No incluir columnas de clave primaria a menos que sea necesario</li>
     * </ul>
     *
     * <p><strong>Ejemplo:</strong>
     * <pre>{@code
     * {
     *   "estado": "ACTIVO",
     *   "ultima_modificacion": "2025-09-30T15:30:00",
     *   "intentos": 0,
     *   "observaciones": null
     * }
     * }</pre>
     */
    private Map<String, Object> values;

    /**
     * Mapa de condiciones para la cláusula WHERE.
     *
     * <p>Define qué registros serán actualizados. Utiliza el mismo sistema
     * de operadores complejos que ComplexQueryDto:
     * <ul>
     * <li><strong>Operadores de comparación:</strong> $gt, $gte, $lt, $lte, $ne</li>
     * <li><strong>Operadores de búsqueda:</strong> $like para patrones</li>
     * <li><strong>Operadores de lista:</strong> Arrays para IN, $in explícito</li>
     * <li><strong>Operadores lógicos:</strong> $condition para OR/AND entre condiciones</li>
     * <li><strong>Operadores de nulos:</strong> $null para IS NULL/IS NOT NULL</li>
     * </ul>
     *
     * <p><strong>Ejemplos de condiciones:</strong>
     * <ul>
     * <li>{@code "id": 4} → {@code WHERE id = ?}</li>
     * <li>{@code "estado": ["ACTIVO", "PENDIENTE"]} → {@code WHERE estado IN (?, ?)}</li>
     * <li>{@code "fecha": {"$gte": "2025-01-01T00:00:00"}} → {@code WHERE fecha >= ?}</li>
     * <li>{@code "nombre": {"$like": "%Juan%"}} → {@code WHERE nombre LIKE ?}</li>
     * </ul>
     *
     * <p><strong>Ejemplo con operador OR:</strong>
     * <pre>{@code
     * {
     *   "estado": "PENDIENTE",
     *   "$condition": "OR",
     *   "intentos": {"$gte": 3}
     * }
     * }</pre>
     * Genera: {@code WHERE estado = 'PENDIENTE' OR intentos >= 3}
     *
     * <p><strong>IMPORTANTE:</strong> Siempre especificar un whereClause para evitar
     * actualizar todos los registros de la tabla accidentalmente.
     */
    private Map<String, Object> whereClause;

    /**
     * Tipo de base de datos a utilizar.
     *
     * <p>Debe corresponder a una configuración válida en DatabaseFactory.
     * Este valor determina qué conexión de base de datos se utilizará
     * para ejecutar la operación UPDATE.
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
     * Obtiene el mapa de valores a actualizar.
     *
     * @return Mapa con los nombres de columnas y sus nuevos valores
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Establece los valores a actualizar.
     *
     * @param values Mapa con los nombres de columnas y sus nuevos valores
     */
    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Obtiene las condiciones WHERE.
     *
     * @return Mapa con las condiciones para filtrar registros a actualizar
     */
    public Map<String, Object> getWhereClause() {
        return whereClause;
    }

    /**
     * Establece las condiciones WHERE.
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
