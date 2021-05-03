package com.ct.rest.backend.util.json

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ct.rest.backend.util.exception.RestException
import org.springframework.http.HttpStatus
import java.io.IOException

/**
 * Utility class that defines JSON read and write methods most commonly used.
 *
 * @author Carlos de la Teja
 * @author Waldo Terry
 */
class JsonUtil {
    companion object {
        private val mapper = ObjectMapper()

        /**
         * Transforms any object to a JSON String.
         *
         * @param json Source object to marshal into a JSON.
         *
         * @return a String containing the marshalled Object.
         *
         * @throws RestException if an error occurs during the reading or the casting.
         */
        @Throws(RestException::class)
        fun getJsonString(json: Any): String {
            return try {
                mapper.writeValueAsString(json)
            } catch (e: JsonProcessingException) {
                throw RestException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error getting json string: " + e.cause?.message)
            }
        }

        /**
         * Reads a provided JSON String and returns the corresponding <code>JsonNode</code>
         *
         * @param jsonString The source string to read.
         *
         * @return a <code>JsonNode</code> corresponding to the read String.
         *
         * @throws RestException if an error occurs during the reading or the casting.
         */
        @Throws(RestException::class)
        fun getJsonNode(jsonString: String): JsonNode {
            return try {
                mapper.readValue(jsonString, JsonNode::class.java)
            } catch (e: JsonProcessingException) {
                throw RestException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error getting JsonNode: " + e.cause?.message)
            }
        }

        /**
         * Attempts to read a single object of a given type from the provided <code>JsonNode</code>.
         *
         * @param sonBody source Json object
         * @param clazz Class to which the read information will be cast.
         *
         * @return A single <code>JsonNode</code> object that represents the given source object. Since it only tries to read
         * a single object, if the given json contains a list of objects this method will not read correctly.
         *
         * @throws RestException if an error occurs during the reading or the casting.
         */
        @Throws(RestException::class)
        fun <T> getSingleObjectFromJsonNode(sonBody: JsonNode, clazz: Class<T>?): T {
            return try {
                mapper.treeToValue(sonBody, clazz)
            } catch (e: IOException) {
                throw RestException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.message)
            }
        }

        /**
         * Transforms any given object into a <code>JsonNode</code>.
         *
         * @param dto Source object to read.
         *
         * @return a <code>JsonNode</code> that represents the given object.
         *
         * @throws RestException if an error occurs during the reading or the casting.
         */
        @Throws(RestException::class)
        fun getJsonNodeFromObject(dto: Any): JsonNode {
            return try {
                mapper.readValue(getJsonString(dto), JsonNode::class.java)
            } catch (e: JsonProcessingException) {
                throw RestException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error getting JsonNode: " + e.cause?.message)
            }
        }

        /**
         * Method that allows for the serialization of a Map of values into a Json document.
         *
         * @param params The map to serialize into JSON. The keys will be the Json fields and the values the Json values. Individual
         * value marshaling will be handled as corresponds to the value type.
         *
         * @return the resulting Json in a <code>String</code> format.
         * @throws RestException If an error occurs during serialization.
         */
        @Throws(RestException::class)
        fun mapToJson(params: Map<String, Any>) : String {
            return try{
                listToJson(params.keys.toTypedArray(), params.values.toTypedArray())
            }
            catch(e: Exception) {
                throw RestException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error getting JsonNode: " + e.cause?.message)
            }
        }
        /**
         * Marshals a list of values to a JSON and encloses the result in curly braces to complete a valid JSON document.
         *
         * @param names The names of the provided fields, in the order provided.
         * @param params the values for the named fields, in the same order as the names are listed.
         *
         * @return a String containing a JSON that represents the given list of fields and values.
         */
        private fun listToJson(names: Array<String>, params: Array<Any>): String {
            val json = StringBuffer("{")
            json.append(getJson(names, params))
            json.append("}")
            return json.toString()
        }

        /**
         * Utility method that actually transforms a list of names and values into a JSON. Implemented like this because jackson
         * kotlin support does not include the serilization of Map<String, Any>, sadly.
         */
        private fun getJson(names: Array<String>, params: Array<Any>): String {
            val jsonValue = StringBuffer()
            val var5 = names.size
            for ((x, var6) in (0 until var5).withIndex()) {
                val name = names[var6]
                if (params[x] is Collection<*>) {
                    val valor = if (params[x] is Map<*, *>) {
                        mapToJson(params[x] as Map<String, Any>)
                    } else {
                        getJsonString(params[x])
                    }

                    jsonValue.append("\"$name\":$valor,")
                }
                else {
                    jsonValue.append("\"" + name + "\":\"" + params[x] + "\",")
                }
            }
            if (jsonValue.isNotEmpty()) {
                jsonValue.deleteCharAt(jsonValue.length - 1)
            }
            return jsonValue.toString()
        }

    }
}
