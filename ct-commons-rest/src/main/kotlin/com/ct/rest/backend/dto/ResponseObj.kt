package com.ct.rest.backend.dto

import com.fasterxml.jackson.databind.JsonNode
import com.ct.rest.backend.util.exception.RestException
import java.util.*

/**
 * Common use interface that marks DTOs as Response objects that can be used to handle rest service responses that adhere to the
 * standard ct response pattern.

 * @author Waldo Terry
 */
interface ResponseObj {

    companion object{
        /**
         * Permite validar un JSON recibido en formato nodo para empatarlo con el formato de respuestas estandar de servicios
         * rest de ct.
         *
         * Si no obedece a este patron, arrojara un error generico indicando que el JSON recibido no es una respuesta estandar
         * de servicios ct.
         */
        fun validateJsonNodeResponse(jsonObjectSource: JsonNode) : JsonNode {
            val responseStatus  = jsonObjectSource.get("responseStatus").asInt()
            val responseError = jsonObjectSource.get("responseError").asText()

            if (Objects.isNull(responseStatus) || Objects.isNull(responseError)) {
                throw RestException(400, "Respuesta en formato no estandar. No se encontraron los campos responseStatus y/o responseError")
            }

            if (responseStatus != 200) {
                throw RestException(responseStatus, responseError)
            }

            return jsonObjectSource
        }
    }

    var responseStatus: Int
    var responseError: String

    /**
     * Checks whether or not this Response is valid. If it is NOT, the method then throws a <code>RestException</code>
     * built with this Response's status code and message.
     *
     * If a message is not provided, a stock "Internal Server Error" will be used.
     *
     * @return <code>true</code> if the response is valid (responseStatus == 200)
     * @throws RestException with the parameters of the response read (that is to say, its <code>responseStatus</code>
     * for the exception code and <code>responseError</code> for the exception message.
     */
    fun validateResponseOrElse() : Boolean {
        if (responseStatus != 200) {
            throw RestException(responseStatus, responseError)
        }
        else {
            return true
        }
    }

    /**
     * Checks whether or not this Response is valid.
     */
    fun responseValid() : Boolean {
        return responseStatus == 200
    }
}
