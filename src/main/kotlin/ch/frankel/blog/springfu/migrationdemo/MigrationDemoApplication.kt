package ch.frankel.blog.springfu.migrationdemo

import org.springframework.boot.WebApplicationType.SERVLET
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.data.r2dbc.core.DatabaseClient
import org.springframework.fu.kofu.application
import org.springframework.fu.kofu.r2dbc.r2dbcH2
import org.springframework.fu.kofu.webmvc.webMvc
import org.springframework.web.servlet.function.ServerRequest
import org.springframework.web.servlet.function.ServerResponse
import java.time.LocalDate

fun main(args: Array<String>) {
    application(SERVLET) {
        listener<ApplicationReadyEvent> {
            with(ref<PersonRepository>()) {
                initialize()
                insert(Person(1L, "John", "Doe", LocalDate.of(1970, 1, 1)))
                insert(Person(2L, "Jane", "Doe", LocalDate.of(1970, 1, 1)))
                insert(Person(3L, "Goetz", "Brian", LocalDate.of(1970, 1, 1)))
            }
        }
        beans {
            bean<PersonRepository>()
            bean<PersonHandler>()
        }
        webMvc {
            router {
                "/person".nest {
                    GET("/{id}", ref<PersonHandler>()::readOne)
                    GET("/") { ref<PersonHandler>().readAll() }
                }
            }
            converters {
                jackson()
            }
        }
        r2dbcH2()
    }.run(args)
}

class PersonHandler(private val personRepository: PersonRepository) {
    fun readAll() = ServerResponse.ok().body(personRepository.findAll())
    fun readOne(request: ServerRequest) = ServerResponse.ok().body(personRepository.findById(request.pathVariable("id").toLong()))
}

class Person(val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

class PersonRepository(private val client: DatabaseClient) {

    fun findAll() = client.select()
        .from(Person::class.java)
        .fetch().all()
        .collectList()
        .blockOptional()

    fun findById(id: Long) = client
        .execute("SELECT * FROM PERSON WHERE ID=:id")
        .bind("id", id)
        .`as`(Person::class.java)
        .fetch()
        .one()
        .blockOptional()

    fun insert(person: Person) = client.insert()
        .into(Person::class.java)
        .table("PERSON")
        .using(person)
        .then()
        .block()

    fun initialize() = client.execute(
"""
CREATE TABLE IF NOT EXISTS PERSON (
      ID            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
      FIRST_NAME    VARCHAR(50) NOT NULL,
      LAST_NAME     VARCHAR(50) NOT NULL,
      BIRTHDATE     DATE
    );
""")
        .then()
        .block()
}