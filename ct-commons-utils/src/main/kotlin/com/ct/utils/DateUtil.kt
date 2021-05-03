package com.ct.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Clase de utileria para implementar operaciones comunes y estandarizadas que tengan que ver con <code>LocalDate</code> y
 * <code>LocalDateTime</code>.
 *
 * @author Waldo Terry
 * @author Carlos de la Teja
 */
class DateUtil {
    companion object {
        private val formatterDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'")
        private val formatterDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        /**
         * Devuelve un <code>LocalDate</code> a partir de un string proporcionado, siempre que este se apegue al formato
         * <code>yyyy-MM-dd'T'hh:mm:ss.SSS'Z'</code>
         */
        fun toLocalDate(fecha: String): LocalDate {

            if (fecha.indexOf('T') > -1) {
                val fechaEditada = fecha.substring(0, fecha.indexOf('T'))
                return LocalDate.parse(fechaEditada, formatterDate)
            }
            return LocalDate.parse(fecha, formatterDate)
        }

        /**
         * Devuelve un objeto de tipo <code>LocalDateTime</code> que contiene la fecha recibida, siempre que obedezca al patron:
         * <code>yyyy-MM-dd'T'hh:mm:ss.SSS'Z'</code>
         */
        fun toLocalDateTime(fecha: String): LocalDateTime {
            if (fecha.indexOf('T') == -1) {
                val fechaEditada = fecha + "T06:00:00.000Z"
                return LocalDateTime.parse(fechaEditada, formatterDateTime)
            }
            return LocalDateTime.parse(fecha, formatterDateTime)
        }

        /**
         * Convierte un objeto <code>LocalDate</code> al formato estandar de ct para las fechas, <code>yyyy-MM-dd'T'hh:mm:ss.SSS'Z'</code>
         */
        fun toStringFormat(fecha: LocalDate) : String {
            return formatterDate.format(fecha)
        }

        /**
         * Convierte un objeto <code>LocalDateTime</code> al formato estandar de ct para las fechas, <code>yyyy-MM-dd'T'hh:mm:ss.SSS'Z'</code>
         */
        fun toStringFormat(fecha: LocalDateTime) : String {
            return formatterDateTime.format(fecha)
        }
    }
}
