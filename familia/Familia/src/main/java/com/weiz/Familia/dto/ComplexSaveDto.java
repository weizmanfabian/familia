package com.weiz.Familia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO para realizar operaciones INSERT con formato JSON mejorado.
 *
 * <p>Este DTO permite insertar registros en la base de datos utilizando un formato
 * JSON estructurado en lugar de strings escapados, proporcionando:
 * <ul>
 * <li>Mayor legibilidad: Los valores se envían como objetos JSON nativos</li>
 * <li>Validación automática: Jackson valida la estructura JSON</li>
 * <li>Manejo de tipos: Los tipos de datos se preservan correctamente</li>
 * <li>Menos errores: No requiere escapar comillas ni manipular strings</li>
 * </ul>
 *
 * <p><strong>Ejemplo de uso:</strong>
 * <pre>{@code
 * {
 *   "tableName": "originacionseguros.tipos_documento",
 *   "values": {
 *     "id": 4,
 *     "nombre": "Tarjeta de Identidad",
 *     "sigla": "TI",
 *     "descripcion": "Documento de identidad para menores de edad"
 *   },
 *   "dbType": "db_openfinance"
 * }
 * }</pre>
 *
 * <p><strong>Comparación con el formato anterior:</strong>
 * <table border="1">
 * <tr>
 *   <th>Formato Antiguo</th>
 *   <th>Formato Nuevo (Complex)</th>
 * </tr>
 * <tr>
 *   <td><code>"field": "{\"id\":4,\"nombre\":\"TI\"}"</code></td>
 *   <td><code>"values": {"id": 4, "nombre": "TI"}</code></td>
 * </tr>
 * </table>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComplexSaveDto {

    /**
     * Nombre completo de la tabla donde se insertará el registro.
     *
     * <p>Debe incluir el esquema si es necesario. Este nombre se utilizará
     * directamente en la sentencia INSERT INTO.
     *
     * <p><strong>Ejemplos:</strong>
     * <ul>
     * <li>{@code "originacionseguros.tipos_documento"}</li>
     * <li>{@code "public.users"}</li>
     * <li>{@code "clientes"} (si el esquema está en el path por defecto)</li>
     * </ul>
     */
    private String tableName;

    /**
     * Mapa de valores a insertar en el registro.
     *
     * <p>Cada entrada del mapa representa una columna y su valor:
     * <ul>
     * <li><strong>Key:</strong> Nombre de la columna en la base de datos</li>
     * <li><strong>Value:</strong> Valor a insertar, puede ser:
     *   <ul>
     *   <li>String: Texto o fechas en formato ISO-8601</li>
     *   <li>Number: Integer, Long, Double, BigDecimal</li>
     *   <li>Boolean: true/false</li>
     *   <li>null: Para valores nulos</li>
     *   </ul>
     * </li>
     * </ul>
     *
     * <p><strong>Notas importantes:</strong>
     * <ul>
     * <li>No es necesario incluir campos con valores auto-generados (ej: ID con autoincrement)</li>
     * <li>Las fechas deben enviarse en formato ISO-8601: "yyyy-MM-dd'T'HH:mm:ss"</li>
     * <li>Los valores null se manejarán correctamente como NULL en SQL</li>
     * </ul>
     *
     * <p><strong>Ejemplo:</strong>
     * <pre>{@code
     * {
     *   "nombre": "Juan Pérez",
     *   "edad": 30,
     *   "activo": true,
     *   "fecha_nacimiento": "1995-01-15T00:00:00",
     *   "notas": null
     * }
     * }</pre>
     */
    private Map<String, Object> values;

    /**
     * Tipo de base de datos a utilizar.
     *
     * <p>Debe corresponder a una configuración válida en DatabaseFactory.
     * Este valor determina qué conexión de base de datos se utilizará
     * para ejecutar la operación INSERT.
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
     * Obtiene el mapa de valores a insertar.
     *
     * @return Mapa con los nombres de columnas y sus valores
     */
    public Map<String, Object> getValues() {
        return values;
    }

    /**
     * Establece los valores a insertar.
     *
     * @param values Mapa con los nombres de columnas y sus valores
     */
    public void setValues(Map<String, Object> values) {
        this.values = values;
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
