package com.ct.security.backend.dto.request

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

/**
 * DTO generico para la generacion de peticiones de envio de eventos. En esta clase el eventBody es de tipo Any, lo que se traduce a un
 * LinkedHashMap en tiempo de ejecucion.
 *
 * @author Waldo Terry
 */
class EventRequest : AbstractEventRequest(){

    @JsonProperty("eventBody")
    var eventBody: Any? = null

    override fun toString(): String {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE)
    }
}
