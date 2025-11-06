package com.weiz.Familia.api.controllers;


import com.weiz.Familia.dto.ComplexQueryDto;
import com.weiz.Familia.dto.ComplexSaveDto;
import com.weiz.Familia.dto.ComplexUpdateDto;
import com.weiz.Familia.dto.ComplexDeleteDto;
import com.weiz.Familia.dto.validation.DtoValidator;
import com.weiz.Familia.dto.errors.ErrorResponse;
import com.weiz.Familia.infraestructure.services.GenericCrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/crud")
public class GenericCrudController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericCrudController.class);

    private final GenericCrudService genericCrudService;
    private final DtoValidator dtoValidator;

    @Autowired
    public GenericCrudController(GenericCrudService genericCrudService, DtoValidator dtoValidator) {
        this.genericCrudService = genericCrudService;
        this.dtoValidator = dtoValidator;
    }

    /**
     * Endpoint para ejecutar consultas SELECT complejas con operadores avanzados.
     *
     * <p>Este endpoint permite realizar consultas SQL avanzadas con condiciones WHERE dinámicas,
     * proporcionando una alternativa más potente al endpoint /getAll para casos que requieren:
     * <ul>
     * <li>Operadores IN con arrays: {@code [1,2,3]} se convierte automáticamente en {@code IN (1,2,3)}</li>
     * <li>Operadores de comparación: $gt, $gte, $lt, $lte, $ne</li>
     * <li>Búsquedas con patrones: $like para operaciones LIKE</li>
     * <li>Verificación de nulos: $null para IS NULL/IS NOT NULL</li>
     * <li>Combinación de múltiples condiciones con AND automático</li>
     * </ul>
     *
     * <p><strong>Diferencias con /getAll:</strong>
     * <ul>
     * <li><strong>/getAll:</strong> whereClause como String JSON, operadores limitados</li>
     * <li><strong>/getAllComplex:</strong> whereClause como Map, operadores avanzados, más flexibilidad</li>
     * </ul>
     *
     * <p><strong>Ejemplo de uso:</strong>
     * <pre>{@code
     * POST /api/crud/getAllComplex
     * {
     *   "query": "SELECT * FROM estados_solicitud_polizas",
     *   "whereClause": {
     *     "estado_solicitud_poliza_id": [1,2,3],
     *     "nombre": {"$like": "%activo%"}
     *   },
     *   "orderClause": "id DESC",
     *   "db_type": "db_openfinance"
     * }
     * }</pre>
     *
     * <p><strong>Respuestas:</strong>
     * <ul>
     * <li><strong>200 OK:</strong> Consulta exitosa con uno o más resultados (body contiene la lista)</li>
     * <li><strong>204 NO CONTENT:</strong> Consulta exitosa pero sin resultados (lista vacía)</li>
     * <li><strong>400 BAD REQUEST:</strong> Error de validación en los parámetros</li>
     * <li><strong>500 INTERNAL SERVER ERROR:</strong> Error en la base de datos o conexión</li>
     * </ul>
     *
     * @param body DTO con la consulta compleja a ejecutar
     * @return ResponseEntity con HTTP 200 y lista si hay resultados, 204 si no hay resultados, o error (HTTP 400/500)
     */
    @RequestMapping(value = "/getAllComplex", method = RequestMethod.POST)
    public ResponseEntity<?> getAllComplex(@RequestBody ComplexQueryDto body) throws IOException, IOException {
        // Validar el DTO antes de procesar
        dtoValidator.validateQuery(body);

        // Las excepciones son manejadas por GlobalExceptionHandler
        // que proporciona respuestas de error estructuradas y claras
        List<?> results = genericCrudService.executeComplexSelect(
            body.getQuery(),
            body.getWhereClause(),
            body.getOrderClause(),
            body.getLimit(),
            body.getGroupBy(),
            body.getDbType()
        );

        // Si no se encontraron resultados, retornar 204 No Content
        if (results == null || results.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    /**
     * Endpoint para ejecutar operaciones INSERT con formato JSON mejorado.
     *
     * <p>Este endpoint proporciona una alternativa mejorada a /save que acepta
     * valores como objeto JSON nativo en lugar de string JSON escapado, ofreciendo:
     * <ul>
     * <li><strong>Mayor legibilidad:</strong> JSON limpio sin escapar comillas</li>
     * <li><strong>Validación automática:</strong> Jackson valida tipos y estructura</li>
     * <li><strong>Mejor experiencia de desarrollo:</strong> Más fácil de escribir y mantener</li>
     * <li><strong>Menos errores:</strong> No requiere manipulación manual de strings</li>
     * </ul>
     *
     * <p><strong>Comparación con /save:</strong>
     * <table border="1">
     * <tr>
     *   <th>Aspecto</th>
     *   <th>/save (antiguo)</th>
     *   <th>/saveComplex (nuevo)</th>
     * </tr>
     * <tr>
     *   <td>Campo tabla</td>
     *   <td>"quey"</td>
     *   <td>"tableName"</td>
     * </tr>
     * <tr>
     *   <td>Campo valores</td>
     *   <td>"field" (String JSON escapado)</td>
     *   <td>"values" (Object JSON nativo)</td>
     * </tr>
     * <tr>
     *   <td>Campo db</td>
     *   <td>"db_type"</td>
     *   <td>"dbType"</td>
     * </tr>
     * </table>
     *
     * <p><strong>Ejemplo de uso:</strong>
     * <pre>{@code
     * POST /api/crud/saveComplex
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
     * <p><strong>Respuesta exitosa:</strong>
     * <pre>{@code
     * HTTP 201 Created
     * {
     *   "id": 4
     * }
     * }</pre>
     *
     * @param body DTO con los datos para la inserción
     * @return ResponseEntity con el ID generado (HTTP 201) o error (HTTP 400/500)
     */
    @RequestMapping(value = "/saveComplex", method = RequestMethod.POST)
    public ResponseEntity<?> saveComplex(@RequestBody ComplexSaveDto body) {
        try {
            // 1. Validar el DTO (incluye validación de null)
            dtoValidator.validateSave(body);

            // 2. Ejecutar la inserción compleja
            Object generatedId = genericCrudService.executeComplexInsert(
                body.getTableName(),
                body.getValues(),
                body.getDbType()
            );

            // 3. Log de éxito
            LOGGER.info("Inserción exitosa en tabla: {} con ID generado: {}", body.getTableName(), generatedId);

            // 4. Retornar el ID generado con status 201 (Created)
            return new ResponseEntity<>(generatedId, HttpStatus.CREATED);

        } catch (IllegalArgumentException e) {
            // Errores de validación de parámetros
            LOGGER.error("Error de validación en saveComplex: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Error de validación en los parámetros de entrada",
                e.getMessage(),
                "/api/crud/saveComplex"
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            // Errores generales (SQL, conexión, etc.)
            LOGGER.error("Error ejecutando saveComplex en tabla: {}", body != null ? body.getTableName() : "null", e);
            ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "DATABASE_ERROR",
                "Error al ejecutar la inserción en la base de datos",
                e.getMessage(),
                "/api/crud/saveComplex"
            );
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para ejecutar operaciones UPDATE con formato JSON mejorado y operadores complejos.
     *
     * <p>Este endpoint proporciona una alternativa mejorada a /update que acepta:
     * <ul>
     * <li><strong>Valores estructurados:</strong> Como objeto JSON nativo (no string escapado)</li>
     * <li><strong>WHERE con operadores:</strong> Soporte para $gt, $lt, $like, $in, $condition (OR/AND), etc.</li>
     * <li><strong>Validación obligatoria:</strong> Requiere whereClause para evitar actualizaciones masivas</li>
     * <li><strong>Mejor manejo de errores:</strong> Mensajes claros y específicos</li>
     * </ul>
     *
     * <p><strong>Comparación con /update:</strong>
     * <table border="1">
     * <tr>
     *   <th>Aspecto</th>
     *   <th>/update (antiguo)</th>
     *   <th>/updateComplex (nuevo)</th>
     * </tr>
     * <tr>
     *   <td>Campo tabla</td>
     *   <td>"quey"</td>
     *   <td>"tableName"</td>
     * </tr>
     * <tr>
     *   <td>Campo valores</td>
     *   <td>"field" (String JSON)</td>
     *   <td>"values" (Object JSON)</td>
     * </tr>
     * <tr>
     *   <td>WHERE clause</td>
     *   <td>"whereClause" (String JSON, limitado)</td>
     *   <td>"whereClause" (Map con operadores complejos)</td>
     * </tr>
     * </table>
     *
     * <p><strong>Ejemplo básico:</strong>
     * <pre>{@code
     * PUT /api/crud/updateComplex
     * {
     *   "tableName": "originacionseguros.tipos_documento",
     *   "values": {
     *     "nombre": "Tarjeta de Identidad Updated",
     *     "descripcion": "Documento actualizado"
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
     * PUT /api/crud/updateComplex
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
     * <p><strong>Respuestas:</strong>
     * <ul>
     * <li><strong>200 OK:</strong> Actualización exitosa, uno o más registros actualizados</li>
     * <li><strong>404 NOT FOUND:</strong> Ningún registro cumple las condiciones WHERE especificadas</li>
     * <li><strong>400 BAD REQUEST:</strong> Error de validación en los parámetros de entrada</li>
     * <li><strong>500 INTERNAL SERVER ERROR:</strong> Error en la base de datos o conexión</li>
     * </ul>
     *
     * @param body DTO con los datos para la actualización
     * @return ResponseEntity con HTTP 200 si registros actualizados, 404 si ninguno encontrado, o error (HTTP 400/500)
     */
    @RequestMapping(value = "/updateComplex", method = RequestMethod.PUT)
    public ResponseEntity<?> updateComplex(@RequestBody ComplexUpdateDto body) {
        try {
            // 1. Validar el DTO (incluye validación de null y whereClause obligatorio)
            dtoValidator.validateUpdate(body);

            // 2. Ejecutar la actualización compleja
            int affectedRows = genericCrudService.executeComplexUpdate(
                body.getTableName(),
                body.getValues(),
                body.getWhereClause(),
                body.getDbType()
            );

            // 3. Verificar si se actualizó algún registro
            if (affectedRows == 0) {
                LOGGER.warn("No se encontraron registros para actualizar en tabla: {} con WHERE: {}",
                    body.getTableName(), body.getWhereClause());
                ErrorResponse error = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "RESOURCE_NOT_FOUND",
                    "No se encontraron registros que cumplan las condiciones especificadas",
                    "La operación UPDATE no afectó ningún registro",
                    "/api/crud/updateComplex"
                );
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            }

            // 4. Log de éxito
            LOGGER.info("Actualización exitosa en tabla: {} - {} registro(s) actualizado(s)",
                body.getTableName(), affectedRows);

            // 5. Retornar éxito con status 200 (OK)
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // Errores de validación de parámetros
            LOGGER.error("Error de validación en updateComplex: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Error de validación en los parámetros de entrada",
                e.getMessage(),
                "/api/crud/updateComplex"
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            // Errores generales (SQL, conexión, etc.)
            LOGGER.error("Error ejecutando updateComplex en tabla: {}", body != null ? body.getTableName() : "null", e);
            ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "DATABASE_ERROR",
                "Error al ejecutar la actualización en la base de datos",
                e.getMessage(),
                "/api/crud/updateComplex"
            );
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint para ejecutar operaciones DELETE con operadores complejos en WHERE.
     *
     * <p>Este endpoint proporciona una alternativa mejorada a /delete con:
     * <ul>
     * <li><strong>WHERE estructurado:</strong> Condiciones como Map en lugar de String JSON</li>
     * <li><strong>Operadores avanzados:</strong> $gt, $lt, $like, $in, $condition (OR/AND), etc.</li>
     * <li><strong>Validación obligatoria:</strong> Requiere whereClause para evitar eliminaciones masivas</li>
     * <li><strong>Mayor seguridad:</strong> Prevención de SQL injection con PreparedStatements</li>
     * </ul>
     *
     * <p><strong>⚠️ ADVERTENCIAS DE SEGURIDAD:</strong>
     * <ul>
     * <li>whereClause es OBLIGATORIO - el endpoint rechaza peticiones sin condiciones</li>
     * <li>Verificar las condiciones antes de ejecutar operaciones DELETE</li>
     * <li>Considerar usar soft deletes (UPDATE con campo "eliminado") en producción</li>
     * <li>Realizar backups antes de operaciones DELETE masivas</li>
     * </ul>
     *
     * <p><strong>Ejemplo básico (eliminar por ID):</strong>
     * <pre>{@code
     * DELETE /api/crud/deleteComplex
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
     * DELETE /api/crud/deleteComplex
     * {
     *   "tableName": "logs_sistema",
     *   "whereClause": {
     *     "id": [100, 101, 102, 103]
     *   },
     *   "dbType": "db_openfinance"
     * }
     * }</pre>
     *
     * <p><strong>Ejemplo con operadores complejos:</strong>
     * <pre>{@code
     * DELETE /api/crud/deleteComplex
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
     *
     * <p><strong>Respuestas:</strong>
     * <ul>
     * <li><strong>200 OK:</strong> Eliminación exitosa, uno o más registros eliminados</li>
     * <li><strong>404 NOT FOUND:</strong> Ningún registro cumple las condiciones WHERE especificadas</li>
     * <li><strong>400 BAD REQUEST:</strong> Error de validación (whereClause vacío o parámetros inválidos)</li>
     * <li><strong>500 INTERNAL SERVER ERROR:</strong> Error en la base de datos o conexión</li>
     * </ul>
     *
     * @param body DTO con las condiciones para la eliminación
     * @return ResponseEntity con HTTP 200 si registros eliminados, 404 si ninguno encontrado, o error (HTTP 400/500)
     */
    @RequestMapping(value = "/deleteComplex", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteComplex(@RequestBody ComplexDeleteDto body) {
        try {
            // 1. Validar el DTO (incluye validación de null y whereClause obligatorio)
            dtoValidator.validateDelete(body);

            // 2. Ejecutar la eliminación compleja
            int affectedRows = genericCrudService.executeComplexDelete(
                body.getTableName(),
                body.getWhereClause(),
                body.getDbType()
            );

            // 3. Verificar si se eliminó algún registro
            if (affectedRows == 0) {
                LOGGER.warn("No se encontraron registros para eliminar en tabla: {} con WHERE: {}",
                    body.getTableName(), body.getWhereClause());
                ErrorResponse error = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "RESOURCE_NOT_FOUND",
                    "No se encontraron registros que cumplan las condiciones especificadas",
                    "La operación DELETE no afectó ningún registro",
                    "/api/crud/deleteComplex"
                );
                return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
            }

            // 4. Log de éxito
            LOGGER.info("Eliminación exitosa en tabla: {} - {} registro(s) eliminado(s)",
                body.getTableName(), affectedRows);

            // 5. Retornar éxito con status 200 (OK)
            return new ResponseEntity<>(HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // Errores de validación de parámetros
            LOGGER.error("Error de validación en deleteComplex: {}", e.getMessage());
            ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Error de validación en los parámetros de entrada",
                e.getMessage(),
                "/api/crud/deleteComplex"
            );
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            // Errores generales (SQL, conexión, etc.)
            LOGGER.error("Error ejecutando deleteComplex en tabla: {}", body != null ? body.getTableName() : "null", e);
            ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "DATABASE_ERROR",
                "Error al ejecutar la eliminación en la base de datos",
                e.getMessage(),
                "/api/crud/deleteComplex"
            );
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
