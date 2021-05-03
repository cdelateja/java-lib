package com.ct.security.backend.client

import com.ct.rest.backend.client.Client
import com.ct.security.backend.dto.Token
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import com.ct.security.backend.util.exception.SecurityException
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

/**
 * Esta clase proporciona acceso a los metodos de un cliente Rest, pero controlando el manejo de la autenticacion de estas peticiones,
 * ofreciendo opciones para mandar peticiones sin autenticacion o con autenticacion de varios tipos.
 *
 * Cada estrategia de autenticacion particular que se desee manejar se debera implementar en esta clase
 *
 * @author Waldo Terry
 * @author Carlos de la Teja
 */
@Service
class ServiceClient(private val restTemplate: RestTemplate,
                    private val cliente: Client) {

    /**
     * Logger
     */
    companion object {
        private val log = LoggerFactory.getLogger(ServiceClient::class.java)
    }

    //estas variables se deben configurar en los yml de configuracion de cada aplicacion.
    //los valores que se usen para appPassword y appName deben estar registrados en el OAuth para que  este
    //pueda entregarle un  token a este modulo.

    //nombre de la aplicacion
    private val appName: String? = "gafBack"

    //ruta del servicio de login en el oauth
    @Value("\${oauth.url}")
    private val oauthPath: String? = null

    //password asignado a la aplicacion de appName en el OAuth
    private val appPassword: String? = "gafBackThisIsSecret"

    //token guardado en memoria para el usuario de sistema.
    private var activeToken: Token? = null

    /**
     * Accesor para el token de memoria. Si no esta inicializado, se inicializa.
     */
    private fun getToken(): Token? {
        return if (Objects.nonNull(activeToken)) {
            activeToken
        }
        else {
            requestToken()
            activeToken
        }
    }

    /**
     * Arma los headers necesarios para hacer login en el OAuth (de tipo Basic) y devuelve un objeto <code>HttpHeaders</code> con el token
     * obtenido en el header <code>Authorization</code>
     */
    private fun createAuthorizationHeaders(): HttpHeaders {
        return object : HttpHeaders() {
            init {
                val auth = "iam:thisissecret"
                val encodedAuth: String = Base64.getEncoder().encodeToString(auth.toByteArray())
                val authHeader = "Basic $encodedAuth"
                set("Authorization", authHeader)
            }
        }
    }

    /**
     * Metodo que permite generar un nuevo token para asignarlo a memoria en esta clase.
     */
    @Throws(SecurityException::class)
    private fun requestToken() {
        try {
            val requestBody: MultiValueMap<String, String> = LinkedMultiValueMap()
            requestBody.add("grant_type", "client_credentials")
            log.info("Requesting token")
            val entity = HttpEntity(requestBody, createAuthorizationHeaders())
            activeToken = restTemplate.exchange(oauthPath!!, HttpMethod.POST, entity, Token::class.java).body
            log.info("Token received")
        } catch (ex: RestClientException) {
            log.error("Client exception ----------> " + ex.message)
            throw SecurityException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Error on Client[typeOfResponse]: ${ex.message}")
        }
    }

    /**
     * Metodo de soporte que agrega a la instancia asociada del cliente rest los headers basicos de comunicacion
     * (<code>Content-type</code> y <code>accepts</code>) ademas del token de seguridad para autenticar una peticion,
     * considerando que se agregara tambien el header de <code>Authorization</code> y se agregara un token con el formato de Bearer.
     *
     * El token que se agrega sera el que se incluya en la peticion activa (si esta existe) o se usara el token activo para el usuario
     * de sistema, cuyas credenciales se reciben en esta clase en las variables <code>appName</code> y <code>appPassword</code>.
     *
     * Tanto la sesion como las variables se toman gracias que este cliente debe ser integrado a un componente de Spring en los proyectos invocadores,
     * para poder valerse de los recursos de la configuracion y la propagacion de la sesion, si esta existe.
     *
     * A nivel sistema, la sesion puede no existir si la peticion se hace en pasos que no iniciaron a partir de una peticion desde un front a un servicio
     * rest. Por ejemplo, en caso que se hagan ejecuciones programadas o que se esten ejecutando cadenas de servicios que se disparan por el manejo de
     * eventos. En estos casos, puede ser necesario consumir recursos protegidos y para eso se usan las credenciales del usuario de sistema que se
     * haya registrado en el archivo de configuracion de la aplicacion invocadora.
     *
     * @retrun la instancia del <code>Client</code> en el contexto, que ha sido modificada con los headers de <code>Content-type</code>, <code>Accept</code> y
     * <code>Authorization</code> agregando para este ultimo un token de tipo Bearer.
     */
    fun getClientWithBearerToken() : Client  {
        var headers : HttpHeaders = HttpHeaders()
        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON

        val requestAttributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?

        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON

        if (Objects.nonNull(requestAttributes)) {
            val request = requestAttributes!!.request

            val authorization = request.getHeader("Authorization")
            if (Objects.nonNull(authorization)) {
                val authToken = authorization.replace("Bearer ", "")
                headers.setBearerAuth(authToken)

            }
            else {
                headers.add("Authorization", "Bearer " + getToken()!!.accessToken)
            }
        }
        else {
            headers.add("Authorization", "Bearer " + getToken()!!.accessToken)
        }

        cliente.headers = headers

        return cliente
    }

    /**
     * Metodo que devuelve una instancia del cliente rest que tiene los encabezados de <code>Content-type</code> y <code>Accepts</code>
     */
    fun getClientWithoutSecurity() : Client {
        var headers : HttpHeaders = HttpHeaders()

        headers.accept = listOf(MediaType.APPLICATION_JSON)
        headers.contentType = MediaType.APPLICATION_JSON

        cliente.headers = headers

        return cliente
    }

    /**
     * Cron que programa el refrescado automatico del token de memoria cada 10 minutos.
     */
    @Scheduled(cron = "0 0/10 * * * *")
    fun refreshToken() {
        log.info("Refreshing token")
        requestToken()
    }
}
