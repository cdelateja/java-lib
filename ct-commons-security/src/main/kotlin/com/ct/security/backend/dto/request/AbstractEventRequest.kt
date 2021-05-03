package com.ct.security.backend.dto.request

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * DTO abstracto para el envio y recepcion de eventos que permite, si el usuario lo desea, se pueda cambiar la definicion de la variable
 * eventBody por un tipo concreto del proyecto invocador, en lugar de usar la definicion  default de tipo <code>Any</code>, ya que procesar JSON como mapas puede no ser
 * util en el proyecto invocador.
 *
 * @author Waldo Terry
 */
abstract class AbstractEventRequest {

    @JsonProperty("username")
    lateinit var username: String

    @JsonProperty("correlationId")
    var correlationId: String = ""

    @JsonProperty("eventType")
    var eventType: EventType = EventType.FULL_STORE

    @JsonProperty("eventName")
    lateinit var eventName: String

    @JsonProperty("applicationName")
    lateinit var applicationName: String

    @JsonProperty("coreName")
    lateinit var coreName: String
}
