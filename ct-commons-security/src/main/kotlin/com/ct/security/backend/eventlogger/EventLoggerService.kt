package com.ct.security.backend.eventlogger

import com.ct.security.backend.dto.request.EventRequest
import com.ct.rest.backend.util.exception.RestException
import com.ct.security.backend.client.ServiceClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
open class EventLoggerService(private val serviceClient: ServiceClient) {

    private val log: Logger = LoggerFactory.getLogger(EventLoggerService::class.java)

    @Value("\${event.logger.url}")
    private val urlLogger: String? = null

    @Value("\${event.logger.sendEvent}")
    private val sendEvent: String? = null

    @Async
    open fun sendEvent(eventRequest: EventRequest) {
        try {
            serviceClient.getClientWithBearerToken().post(urlLogger + sendEvent, eventRequest)
            log.info("Event sent")
        } catch (e: RestException) {
            log.error("Error en mandarEvento: ${e.message}")
        }
    }
}
