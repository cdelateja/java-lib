package com.ct.rest.backend.dto

import com.ct.rest.backend.util.exception.RestException
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

/**
 * General use DTO to store and manage rest service responses generated using the ct standard response of responseStatus, responseError and
 * result.
 *
 * This is an abstract representation of a response, that does not implement a concrete result (in order to let
 * clients define the specific type of result they wish to impelment.
 *
 * @author Waldo Terry
 * @author Carlos de la Teja
 */
open class Response() : ResponseObj {

    override var responseStatus: Int = 200
    override var responseError: String = "OK"
    var result: Any? = null

    /**
     * Builds a <code>Response</code> object that will contain the provided exception status code and message.
     */
    constructor(e: RestException) : this() {
        setException(e)
    }

    /**
     * Builds a <code>Response</code> object that will contain the provided response status and response body (result).
     */
    constructor(responseStatus: Int, result: Any?) : this() {
        this.responseStatus = responseStatus
        this.responseError = "OK"
        this.result = result
    }

    /**
     * Sets the provided exception status code and message to  this response object.
     */
    private fun setException(e: RestException) {
        responseStatus = e.status!!
        responseError = e.message!!
    }

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE)
    }

}
