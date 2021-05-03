package com.ct.rest.backend.client

import com.ct.rest.backend.util.exception.RestException
import com.ct.rest.backend.util.json.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.IOException
import java.util.*

@Service
class Client(private val restTemplate: RestTemplate) {

    var headers: HttpHeaders? = null

    companion object {
        private val log = LoggerFactory.getLogger(Client::class.java)
    }

    enum class RequestType {
        GET, POST, PUT, DELETE
    }

    @Throws(RestException::class)
    fun post(url: String, jsonBody: Any): String {
        return request(RequestType.POST, url, JsonUtil.getJsonString(jsonBody))
    }

    @Throws(RestException::class)
    fun get(url: String): String {
        return request(RequestType.GET, url, null)
    }

    @Throws(RestException::class)
    fun get(url: String, jsonBody: Any): String {
        return request(RequestType.GET, url, JsonUtil.getJsonString(jsonBody))
    }

    @Throws(RestException::class)
    fun put(url: String, jsonBody: Any): String {
        return request(RequestType.PUT, url, JsonUtil.getJsonString(jsonBody))
    }

    fun delete(url: String, jsonBody: Any): String {
        return request(RequestType.DELETE, url, JsonUtil.getJsonString(jsonBody))
    }

    @Throws(RestException::class)
    fun request(requestType: RequestType, url: String, jsonBody: String?): String {
        return getRequest(typeOfResponse(requestType, url, jsonBody))
    }

    @Throws(RestException::class)
    fun getRequest(response: ResponseEntity<String>): String {
        val jsonOutput = response.body
        validateResponse(response)
        return try {
            log.info("--------------> Output : $jsonOutput")
            jsonOutput!!
        } catch (e: IOException) {
            throw RestException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.message)
        }
    }

    @Throws(RestException::class)
    fun validateResponse(response: ResponseEntity<String>) {
        val status = response.statusCode.value()
        log.info("--------------> Status : $status")
        when (status) {
            HttpStatus.BAD_REQUEST.value() ->
                throw RestException(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.reasonPhrase)
            HttpStatus.UNAUTHORIZED.value() ->
                throw RestException(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.reasonPhrase)
            HttpStatus.INTERNAL_SERVER_ERROR.value() ->
                throw RestException(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase)
        }
    }

    @Throws(RestException::class)
    fun typeOfResponse(requestType: RequestType, url: String, jsonBody: String?): ResponseEntity<String> {
        val headers: HttpHeaders = headers!!
        val entity = HttpEntity(jsonBody, headers)
        try {
            log.info("--------------> Petition[$requestType]: $url")
            log.info("--------------> JsonSend: $jsonBody")
            return when (requestType) {
                RequestType.GET -> restTemplate.exchange(url, HttpMethod.GET, entity, String::class.java)
                RequestType.POST -> restTemplate.exchange(url, HttpMethod.POST, entity, String::class.java)
                RequestType.PUT -> restTemplate.exchange(url, HttpMethod.PUT, entity, String::class.java)
                RequestType.DELETE -> restTemplate.exchange(url, HttpMethod.DELETE, entity, String::class.java)
            }
        } catch (ex: RestClientException) {
            log.error("Client exception ----------> " + ex.message)
            throw RestException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Error on Client[typeOfResponse]: ${ex.message}")
        }
    }
}
