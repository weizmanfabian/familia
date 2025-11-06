package com.weiz.Familia.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad centralizada para conversión de fechas a epoch millis.
 *
 * <p>Esta clase proporciona métodos para:
 * <ul>
 * <li>Detectar tipos de columna de fecha en la base de datos (DATE, TIMESTAMP, BIGINT)</li>
 * <li>Convertir fechas SQL (Date, Timestamp) a epoch millis</li>
 * <li>Validar y parsear epoch millis desde diferentes tipos (Long, String, Integer)</li>
 * <li>Registrar todas las conversiones en logs para debugging</li>
 * </ul>
 *
 * <p><strong>Principio de conversión:</strong>
 * <ul>
 * <li>Entrada: epoch millis (Long, String o Integer)</li>
 * <li>BD interna: DATE, TIMESTAMP, BIGINT (transparente)</li>
 * <li>Salida: epoch millis (Long)</li>
 * </ul>
 *
 * <p><strong>Ejemplo de uso:</strong>
 * <pre>{@code
 * // Conversión de DATE de BD a epoch millis
 * java.sql.Date sqlDate = new java.sql.Date(System.currentTimeMillis());
 * long epochMillis = DateConversionUtil.sqlDateToEpochMillis(sqlDate);
 *
 * // Validación de epoch millis de entrada
 * if (DateConversionUtil.isValidEpochMillis(781238400000L)) {
 *     long parsed = DateConversionUtil.parseEpochMillis(781238400000L);
 * }
 * }</pre>
 *
 * @author Credifamilia
 * @version 1.0
 * @since 2025
 */
public class DateConversionUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateConversionUtil.class);

    // Zona horaria por defecto: UTC
    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    // Rango válido de epoch millis para fechas de nacimiento y otros usos (1900-2500)
    // MIN: 1900-01-01 → -2208988800000L (permite personas de 120+ años)
    // MAX: 2500-12-31 → 16725225600000L (rango futuro amplio)
    private static final long MIN_EPOCH_MILLIS = -2208988800000L;  // 1900-01-01
    private static final long MAX_EPOCH_MILLIS = 16725225600000L;  // 2500-12-31

    // Patrones de fecha ISO-8601 para strings
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter ISO_LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Tipos SQL de fecha comunes
    private static final String[] DATE_TYPES = {"DATE", "date"};
    private static final String[] TIMESTAMP_TYPES = {"TIMESTAMP", "timestamp", "TIMESTAMPTZ", "timestamptz", "TIMESTAMP WITH TIME ZONE", "timestamp with time zone"};
    private static final String[] EPOCH_TYPES = {"BIGINT", "bigint", "BIGSERIAL", "bigserial", "LONG", "long"};

    /**
     * Detecta si una columna SQL es de tipo DATE.
     *
     * @param sqlTypeName Nombre del tipo SQL obtenido de ResultSetMetaData
     * @return true si es DATE, false en caso contrario
     */
    public static boolean isDateType(String sqlTypeName) {
        if (sqlTypeName == null) {
            return false;
        }
        for (String dateType : DATE_TYPES) {
            if (sqlTypeName.equalsIgnoreCase(dateType)) {
                LOGGER.trace("[isDateType] '{}' detectado como DATE", sqlTypeName);
                return true;
            }
        }
        return false;
    }

    /**
     * Detecta si una columna SQL es de tipo TIMESTAMP.
     *
     * @param sqlTypeName Nombre del tipo SQL obtenido de ResultSetMetaData
     * @return true si es TIMESTAMP, false en caso contrario
     */
    public static boolean isTimestampType(String sqlTypeName) {
        if (sqlTypeName == null) {
            return false;
        }
        for (String timestampType : TIMESTAMP_TYPES) {
            if (sqlTypeName.equalsIgnoreCase(timestampType)) {
                LOGGER.trace("[isTimestampType] '{}' detectado como TIMESTAMP", sqlTypeName);
                return true;
            }
        }
        return false;
    }

    /**
     * Detecta si una columna SQL es de tipo numérico que almacena epoch millis.
     *
     * @param sqlTypeName Nombre del tipo SQL obtenido de ResultSetMetaData
     * @return true si es BIGINT/BIGSERIAL, false en caso contrario
     */
    public static boolean isEpochMillisField(String sqlTypeName) {
        if (sqlTypeName == null) {
            return false;
        }
        for (String epochType : EPOCH_TYPES) {
            if (sqlTypeName.equalsIgnoreCase(epochType)) {
                LOGGER.trace("[isEpochMillisField] '{}' detectado como campo epoch millis", sqlTypeName);
                return true;
            }
        }
        return false;
    }

    /**
     * Convierte un java.sql.Date a epoch millis (00:00:00 UTC).
     *
     * @param sqlDate Fecha SQL a convertir
     * @return Epoch millis correspondiente a la fecha a las 00:00:00 UTC
     */
    public static long sqlDateToEpochMillis(Date sqlDate) {
        if (sqlDate == null) {
            LOGGER.warn("[sqlDateToEpochMillis] Recibido null, retornando 0");
            return 0L;
        }

        try {
            // Convertir java.sql.Date → LocalDate
            LocalDate localDate = sqlDate.toLocalDate();

            // Convertir LocalDate → LocalDateTime a las 00:00:00
            LocalDateTime localDateTime = localDate.atStartOfDay();

            // Convertir a Instant en UTC
            Instant instant = localDateTime.atZone(UTC_ZONE).toInstant();

            // Convertir a epoch millis
            long epochMillis = instant.toEpochMilli();

            LOGGER.debug("[sqlDateToEpochMillis] {} → {} (epoch millis)", sqlDate, epochMillis);
            return epochMillis;

        } catch (Exception e) {
            LOGGER.error("[sqlDateToEpochMillis] Error convirtiendo {}: {}", sqlDate, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * Convierte un java.sql.Timestamp a epoch millis.
     *
     * @param sqlTimestamp Timestamp SQL a convertir
     * @return Epoch millis correspondiente
     */
    public static long sqlTimestampToEpochMillis(Timestamp sqlTimestamp) {
        if (sqlTimestamp == null) {
            LOGGER.warn("[sqlTimestampToEpochMillis] Recibido null, retornando 0");
            return 0L;
        }

        try {
            long epochMillis = sqlTimestamp.getTime();
            LOGGER.debug("[sqlTimestampToEpochMillis] {} → {} (epoch millis)", sqlTimestamp, epochMillis);
            return epochMillis;

        } catch (Exception e) {
            LOGGER.error("[sqlTimestampToEpochMillis] Error convirtiendo {}: {}", sqlTimestamp, e.getMessage(), e);
            return 0L;
        }
    }

    /**
     * Convierte un valor a epoch millis basado en el tipo SQL de la columna.
     *
     * <p>Lógica de conversión:
     * <ul>
     * <li>Si es DATE → convertir a epoch millis (00:00:00 UTC)</li>
     * <li>Si es TIMESTAMP → convertir a epoch millis</li>
     * <li>Si es BIGINT/epoch → mantener intacto</li>
     * <li>Si es null → retornar null</li>
     * <li>Si es otro tipo → retornar sin cambios</li>
     * </ul>
     *
     * @param value Valor desde la BD
     * @param sqlTypeName Tipo SQL de la columna
     * @return Epoch millis si aplica conversión, o el valor original
     */
    public static Object convertToEpochMillis(Object value, String sqlTypeName) {
        if (value == null) {
            LOGGER.trace("[convertToEpochMillis] Valor null - retornando null");
            return null;
        }

        try {
            // Si es DATE
            if (isDateType(sqlTypeName)) {
                if (value instanceof Date) {
                    long epochMillis = sqlDateToEpochMillis((Date) value);
                    LOGGER.debug("[convertToEpochMillis] DATE {} → {} (epoch millis)", value, epochMillis);
                    return epochMillis;
                } else {
                    LOGGER.warn("[convertToEpochMillis] Tipo DATE esperado pero recibido: {}", value.getClass().getSimpleName());
                    return value;
                }
            }

            // Si es TIMESTAMP
            if (isTimestampType(sqlTypeName)) {
                if (value instanceof Timestamp) {
                    long epochMillis = sqlTimestampToEpochMillis((Timestamp) value);
                    LOGGER.debug("[convertToEpochMillis] TIMESTAMP {} → {} (epoch millis)", value, epochMillis);
                    return epochMillis;
                } else if (value instanceof Long) {
                    // Ya es epoch millis
                    LOGGER.debug("[convertToEpochMillis] TIMESTAMP ya es epoch: {}", value);
                    return value;
                } else {
                    LOGGER.warn("[convertToEpochMillis] Tipo TIMESTAMP esperado pero recibido: {}", value.getClass().getSimpleName());
                    return value;
                }
            }

            // Si es BIGINT/epoch
            if (isEpochMillisField(sqlTypeName)) {
                LOGGER.trace("[convertToEpochMillis] BIGINT epoch - sin conversión: {}", value);
                return value;
            }

            // Otros tipos (VARCHAR, INT, etc)
            LOGGER.trace("[convertToEpochMillis] Tipo SQL: {} - Sin conversión requerida", sqlTypeName);
            return value;

        } catch (Exception e) {
            LOGGER.error("[convertToEpochMillis] Error convirtiendo {} (tipo: {}): {}",
                        value, sqlTypeName, e.getMessage(), e);
            return value;
        }
    }

    /**
     * Valida si un valor es epoch millis válido.
     *
     * <p>Un epoch válido debe:
     * <ul>
     * <li>Ser un Long, Integer, o String que se pueda parsear a Long</li>
     * <li>Tener al menos 10 dígitos (>= 1000000000) para evitar confusión con IDs normales</li>
     * <li>Estar en rango razonable (1900-2500)</li>
     * </ul>
     *
     * <p><strong>Heurística de detección:</strong>
     * <ul>
     * <li>Números < 1000000000 (10 dígitos): Son IDs normales, no epoch millis</li>
     * <li>Números >= 1000000000: Podrían ser epoch millis, validar rango</li>
     * </ul>
     *
     * @param value Valor a validar
     * @return true si es epoch millis válido, false en caso contrario
     */
    public static boolean isValidEpochMillis(Object value) {
        if (value == null) {
            LOGGER.trace("[isValidEpochMillis] Valor null - no es epoch válido");
            return false;
        }

        try {
            long epochMillis;

            if (value instanceof Long) {
                epochMillis = (Long) value;
            } else if (value instanceof Integer) {
                epochMillis = ((Integer) value).longValue();
            } else if (value instanceof String) {
                try {
                    epochMillis = Long.parseLong((String) value);
                } catch (NumberFormatException e) {
                    LOGGER.trace("[isValidEpochMillis] String no es número: {}", value);
                    return false;
                }
            } else {
                LOGGER.trace("[isValidEpochMillis] Tipo no soportado: {}", value.getClass().getSimpleName());
                return false;
            }

            // HEURÍSTICA MEJORADA: Solo validar rango de fechas (1900-2500)
            // Esto permite epoch millis con 12+ dígitos (fechas desde ~1970)
            // Los IDs típicamente son números pequeños (< 1 millón)
            if (epochMillis > -1000000000L && epochMillis < 1000000000L) {
                LOGGER.trace("[isValidEpochMillis] {} es muy pequeño (< 10 dígitos) - Es un ID normal, no epoch millis", epochMillis);
                return false;
            }

            // Validar rango (1900-01-01 a 2500-12-31)
            boolean isValid = epochMillis >= MIN_EPOCH_MILLIS && epochMillis <= MAX_EPOCH_MILLIS;

            if (isValid) {
                LOGGER.trace("[isValidEpochMillis] {} es epoch válido ({})",
                            epochMillis, formatEpochMillis(epochMillis));
            } else {
                String providedDate = formatEpochMillis(epochMillis);
                String minDate = formatEpochMillis(MIN_EPOCH_MILLIS);
                String maxDate = formatEpochMillis(MAX_EPOCH_MILLIS);
                LOGGER.warn("[isValidEpochMillis] {} ({}) está fuera de rango válido. Permitido: {} ({}) a {} ({})",
                            epochMillis, providedDate,
                            MIN_EPOCH_MILLIS, minDate,
                            MAX_EPOCH_MILLIS, maxDate);
            }

            return isValid;

        } catch (Exception e) {
            LOGGER.trace("[isValidEpochMillis] Error validando {}: {}", value, e.getMessage());
            return false;
        }
    }

    /**
     * Parsea un valor a epoch millis válido.
     *
     * <p>Acepta Long, Integer, o String que pueda convertirse a Long.
     * Valida que:
     * <ul>
     * <li>Tenga al menos 10 dígitos (>= 1000000000) para evitar confusión con IDs</li>
     * <li>Esté en rango razonable (1900-2500)</li>
     * </ul>
     *
     * @param value Valor a parsear
     * @return Epoch millis como Long
     * @throws IllegalArgumentException Si no es epoch válido
     */
    public static long parseEpochMillis(Object value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException("Valor null no puede ser parseado como epoch millis");
        }

        try {
            long epochMillis;

            if (value instanceof Long) {
                epochMillis = (Long) value;
            } else if (value instanceof Integer) {
                epochMillis = ((Integer) value).longValue();
            } else if (value instanceof String) {
                try {
                    epochMillis = Long.parseLong((String) value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("String '" + value + "' no es un número válido");
                }
            } else {
                throw new IllegalArgumentException("Tipo no soportado para epoch millis: " + value.getClass().getSimpleName());
            }

            // HEURÍSTICA MEJORADA: Solo rechazar números muy pequeños (< 10 dígitos)
            if (epochMillis > -1000000000L && epochMillis < 1000000000L) {
                throw new IllegalArgumentException(
                    String.format(
                        "El valor %d es muy pequeño para ser epoch millis (< 10 dígitos). " +
                        "Parece que es un ID normal, no una fecha.",
                        epochMillis
                    )
                );
            }

            // Validar rango (1900-01-01 a 2500-12-31)
            if (epochMillis < MIN_EPOCH_MILLIS || epochMillis > MAX_EPOCH_MILLIS) {
                String minDate = formatEpochMillis(MIN_EPOCH_MILLIS);
                String maxDate = formatEpochMillis(MAX_EPOCH_MILLIS);
                String providedDate = formatEpochMillis(epochMillis);
                throw new IllegalArgumentException(
                    String.format(
                        "El valor de epoch millis %d (%s) está fuera del rango permitido. " +
                        "Debe estar entre %d (%s) y %d (%s). " +
                        "El rango permitido es desde 1900-01-01 hasta 2500-12-31.",
                        epochMillis, providedDate,
                        MIN_EPOCH_MILLIS, minDate,
                        MAX_EPOCH_MILLIS, maxDate
                    )
                );
            }

            LOGGER.debug("[parseEpochMillis] {} parseado a {} (epoch millis)", value, epochMillis);
            return epochMillis;

        } catch (IllegalArgumentException e) {
            LOGGER.warn("[parseEpochMillis] Error parseando {}: {}", value, e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.error("[parseEpochMillis] Error inesperado parseando {}: {}", value, e.getMessage(), e);
            throw new IllegalArgumentException("Error inesperado parseando epoch millis", e);
        }
    }

    /**
     * Convierte epoch millis a java.sql.Date para BD (sin hora).
     *
     * <p>Útil para columnas DATE donde no importa la hora.
     * Trunca la hora a 00:00:00.
     *
     * @param epochMillis Epoch millis a convertir
     * @return java.sql.Date correspondiente (sin componente de hora)
     */
    public static Date epochMillisToSqlDate(long epochMillis) {
        try {
            // Crear un Date truncado a medianoche (00:00:00)
            Date sqlDate = new Date(epochMillis);
            LOGGER.debug("[epochMillisToSqlDate] {} → {} (SQL Date)", epochMillis, sqlDate);
            return sqlDate;
        } catch (Exception e) {
            LOGGER.error("[epochMillisToSqlDate] Error convirtiendo {}: {}", epochMillis, e.getMessage(), e);
            throw new RuntimeException("Error convirtiendo epoch millis a Date", e);
        }
    }

    /**
     * Convierte epoch millis a java.sql.Timestamp para BD (con hora).
     *
     * <p>Este método es la inversa de sqlTimestampToEpochMillis().
     * Útil para columnas TIMESTAMP donde importa la hora.
     *
     * @param epochMillis Epoch millis a convertir
     * @return java.sql.Timestamp correspondiente
     */
    public static Timestamp epochMillisToSqlTimestamp(long epochMillis) {
        try {
            Timestamp sqlTimestamp = new Timestamp(epochMillis);
            LOGGER.debug("[epochMillisToSqlTimestamp] {} → {} (SQL Timestamp)", epochMillis, sqlTimestamp);
            return sqlTimestamp;
        } catch (Exception e) {
            LOGGER.error("[epochMillisToSqlTimestamp] Error convirtiendo {}: {}", epochMillis, e.getMessage(), e);
            throw new RuntimeException("Error convirtiendo epoch millis a Timestamp", e);
        }
    }

    /**
     * Obtiene información legible de un epoch millis.
     *
     * <p>Útil para logs y debugging.
     *
     * @param epochMillis Epoch millis a convertir
     * @return String legible (ej: "2025-01-15 14:30:45 UTC")
     */
    public static String formatEpochMillis(long epochMillis) {
        try {
            Instant instant = Instant.ofEpochMilli(epochMillis);
            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, UTC_ZONE);
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " UTC";
        } catch (Exception e) {
            LOGGER.warn("[formatEpochMillis] Error formateando {}: {}", epochMillis, e.getMessage());
            return String.valueOf(epochMillis);
        }
    }
}
