ct-Commons
---

## Uso

### Para el Cliente Rest

Esta funcionalidad esta en el jar ct-commons-rest en términos básicos. El cliente rest de esta libreria
está en la clase `Client` y permite ejecutar cualquier operación de comunicación REST: `get, post, put o delete`.

El cliente rest básico se se usa a través de los métodos `getClientWithBearerToken` o `getClientWithoutSecurity` de la
clase `ServiceClient` en el jar ct-commons-security y permiten obtener una instancia del cliente con o 
sin token de autenticación tipo Bearer, segun el método que se haya usado. Si fuera necesario agregar estrategias de autenticación adicionales, 
el lugar adecuado sería la clase `ServiceClient`.

Para poder usar los clientes rest: 

1. Agregar dependencias necesarias al pom:
```
<dependency>
    <groupId>com.ct</groupId>
    <artifactId>ct-commons-rest</artifactId>
    <version>0.0.12</version>
</dependency>
<dependency>
    <groupId>com.ct</groupId>
    <artifactId>ct-commons-security</artifactId>
    <version>0.0.12</version>
</dependency>
```
2. Es necesario declarar un `RestTemplate` en la aplicación del proyecto de la siguiente manera:

```
@Primary
@Bean
fun getCustomRestTemplate(): RestTemplate? {
  return RestTemplate()
}  
```
La anterior es la configuración más básica de un `RestTemplate` posible, pero se puede utilizar cualquier configuración
deseada, siempre y cuando haya un bean activo que pueda devolver un `RestTemplate` en el contexto.

3. Se tiene que modificar la configuración de la aplicación de SpringBoot cambiando la anotación `@SpringBootApplication` por:

```
@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = ["com.<rutas locales del proyecto a escanear>", "com.ct.rest.backend", "com.ct.security.backend"])
```
Esto debido a que se necesita modificar el comportamiento default de la anotación `@ComponentScan` para poder indicar
que se escaneen los paquetes de esta librería (es decir, las rutas `com.ct`). Al tener que sobreescribir un
comportamiento default, no se puede continuar usando la anotación @SpringbootApplication y se tiene que reemplazar por los 3
elementos que la componen.

4. Habiendo realizado la configuración anterior, se puede usar el `ServiceClient` para implementar clientes en 
cualquier proyecto con un contexto de Spring, por ejemplo:

```
@Service
class IngestorClient(private val client: ServiceClient)
``` 
Define un servicio que podrá implementar un cliente rest, y se usaría así en sus métodos:

```
val jsonResponseSrc = client.getClientWithBearerToken().post(url + path, requestDTO)
```
Donde el `url` y `path` son cadenas con los valores referidos y `requestDTO` es cualquier DTO que se
haya construido para manejar requests. Los métodos del cliente automáticamente convierten los objetos 
provistos a formato JSON.

Los métodos `get`, `put` y `delete` se invocan de la misma manera, solo ajustando la firma para indicar si se
enviara un cuerpo de petición o no en un DTO.

Es importante mencionar que para que se pueda utilizar el cliente, se deben definir en la configuracion de la aplicacion
las siguientes propiedades:

``` 
oauth:
  url: <url del servidor de OAuth2 que maneja los tokens>
```

**Cliente de envío de eventos**

Una vez hecha la configuración del cliente Rest, si se desea se puede usar el cliente de envío de eventos que ya viene empacado con la librería
en la clase de servicio `com.ct.security.backend.eventlogger.EventLoggerService`. Si se escanea esta clase
en la aplicación, se agrega al contexto el servicio `EventLogger` que ya se conecta a una instancia del manejador de 
eventos ct para enviar eventos.

Para poder usar el servicio se deben agregar las siguientes propiedades al yml e configuración del proyecto (el application.yml, no el bootstrap. Si el application.yml de la aplicación
se toma de la nube, se debe actualizar ese), además de haber configurado correctamente el cliente Rest como se mencionó más arriba en esta guía:

```
event:
  logger:
    url: <url donde esté publicada la instancia del event-logger-api>/events
    sendEvent: /create
```
Para uso del cliente de eventos de esta librería también se incluye un DTO `EventRequest` que ya define la estructura
estándar, acordada por ct, para los eventos Kafka; con la salvedad de que el cuerpo del evento (su `eventBody`) es
de tipo `Any` y por lo tanto puede recibir cualquier DTO que el proyecto invocador decida depositar ahí. De lo anterior que
se pueda determinar que **el paso del username y el correlationalId, así como la asignación del cuerpo del evento, son responsabilidad
del proyecto invocador**. El `EventLoggerService` se limita a enviar el evento recibido al event logger provisto y, siendo
que el método de envío es asíncrono, no se espera una respuesta. 

Es importante mencionar que por default, el tipo de evento que se define en el DTO `EventRequest` es `FULL_STORE`, pero se puede cambiar
con los valores definidos en el enum `EventType` según lo requiera el proyecto invocador.

**Objetos Genéricos de respuesta Rest**

En la librería ct-commons-rest se incluyen dos objetos de respuesta para servicios Rest que obedecen al estándar manejado por 
ct, listado abajo para referencia:

```
{
	"responseStatus": 200,
	"responseError": "OK!",
	"result": <ANY JSON OBJECT>
}
```
Donde `responseStatus` es un código numérico para la respuesta, `responseError` es el mensaje asociado con ese código y
`result` es el cuerpo de la respuesta como tal, donde cabe cualquier objeto JSON.

Este esquema se adoptó considerando que habrá situaciones donde se tengan que manejar errores de negocio muy particulares, cuyas
circunstancias y/o mensajes no puedan ser correctamente representados por los errores estándar del protocolo HTTP; después de todo, 
una vez que un cliente recibe un `httpStatus` diferente de 200 en los headers, la ejecución se detiene debido a la respuesta errónea
y se vuelve imposible manejar algún mensaje particular, diferente de los mensajes default del protocolo HTTP.

Las clases de respuesta auxiliares de la librería están en el paquete `com.ct.rest.backend.dto` y son: 

- La interfaz `ResponseObj`: Esta interfaz define las propiedades `responseError` y `responseStatus`, pero espera que el implementador 
defina su propio `result`. Sirve para identificar DTOs en el proyecto invocador como objetos de respuesta de servicios Rest y ofrece 2
métodos de validación de la respuesta: `validateResponseOrElse` que checa que la respuesta sea válida (que su `responseStatus` sea 200)
y si no lo es, genera una `RestException` con la información de error contenida en la respuesta y `responseValid` que es un chequeo simple
de la validez de la respuesta. 

Se agregó también el método estático `validateJsonNodeResponse` que permite confirmar que una respuesta en formato JsonNode 
obedece al formato estándar de ct (con responseStatus y responseError por lo menos) y si es así, si esta respuesta es correcta (response status de 200)

- El DTO `Response`: Es una implementación default de la interfaz `ResponseObj` que define un `result` de tipo `Any`, ideal para usarse en 
aquellos casos donde no hay restricciones en el tipo de objeto al que se debe deserializar una respuesta.

**Utilería para procesamiento de JSON**

La librería `ct-commons-rest` incluye la clase `com.ct.rest.backend.util.json.JsonUtil` que ofrece varios métodos para la 
serialización y deserialización de objetos a formato JSON, que se implementaron con la versión 2.11.3 de Jackson. Para más información,
referirse a la documentación de la clase.

### Utilerías de uso general

La librería `ct-commons-utils` pretende agrupar métodos o fragmentos de código de uso general y repetitivo, a fin
de reducir las líneas de código de *boiler plating* como se le llama en inglés. La idea de esta librería es que
no se agregue aquí lógica propia al negocio de los proyectos donde se implementa (eso, aunque sí está bien que vaya en un
util, debería definirse en los proyectos del negocio), si no que se vaya complementando con fragmentos de código útiles 
para la implementación de servicios.

Actualmente sólo contiene la clase `DateUtil` que contiene varios métodos de uso frecuente para formatear fechas a formato String
y viceversa, usando el formato estándar pactado por ct para fechas con hora y fechas sin hora. Es importante mencionar que
esta clase usa los objetos de fecha de Java 8/11. Si fuera necesario, podrían agregarse métodos para manejar `java.util.Date` en un 
futuro.

Para más información, referirse a la documentación de la clase.
## Histórico de versiones
*Ver. 1.0.0*

Se corrige un bug de la utileria de manejo de fechas que provocaba un error si se intentaba formatear
a String un objeto LocalDate.

Se agrega README completo y algo de documentación al código.

*Ver. 1.0.1*

Se exponen metodos PUT y DELETE en el cliente rest.

*Ver. 1.1.1*

Se marcan como obligatorias las propiedades `username`, `eventName`, `applicationName` y `coreName`. Tratar de accederlas sin
inicializarlas con un valor no nulo antes, producirá un error de inicialización. Esto porque son datos obligatorios para el correcto
envío de un evento.

Se arregla bug en DateUtil que provocaba error al parsear un LocalDate a partir de un String con el formato correcto.

*Ver 1.1.2* 

Se agrega un metodo estatico a la interfaz ResponseObj que permite validar si una respuesta
en formato JsonNode es una respuesta estandar ct correcta (o de lo contrario se arroja la
excepcion contenida en esa respuesta como una RestException)

*Ver 1.1.3*
Se hace que los metodos de parseo de fecha por String de DateUtil puedan manejar formatos con hora o sin ella sin importar si 
se llama el metodo de LocalDate o LocalDateTime. Se sigue teniendo que manejar el fomrato de fecha estandar de ct.
