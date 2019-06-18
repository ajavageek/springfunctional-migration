package ch.frankel.blog.springfu.migrationdemo

import io.r2dbc.spi.Row
import org.springframework.core.io.ClassPathResource
import org.springframework.data.r2dbc.core.R2dbcEntityOperations
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.H2Dialect
import org.springframework.fu.kofu.configuration
import org.springframework.fu.kofu.r2dbc.r2dbc
import org.springframework.fu.kofu.webApplication
import org.springframework.fu.kofu.webmvc.webMvc
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator
import org.springframework.web.servlet.function.*
import java.time.LocalDate

fun main(args: Array<String>) {
    webApplication {
        enable(dataConfig)
        enable(webConfig)
    }.run(args)
}

val dataConfig = configuration {
    beans {
        bean { R2dbcEntityTemplate(ref(), H2Dialect.INSTANCE) }
        bean {
            ConnectionFactoryInitializer().apply {
                setConnectionFactory(ref())
                setDatabasePopulator(
                    ResourceDatabasePopulator(
                        ClassPathResource("schema.sql"),
                        ClassPathResource("import.sql")
                    )
                )
            }
        }
        r2dbc()
    }
}

val webConfig = configuration {
    beans {
        bean {
            router {
                "/person".nest {
                    val handler = PersonHandler(PersonRepository(ref()))
                    GET("/{id}", handler::readOne)
                    GET("/") { handler.readAll() }
                }
            }
        }
    }
    webMvc {
        converters {
            jackson {
                indentOutput = true
            }
        }
    }
}

class PersonHandler(private val personRepository: PersonRepository) {
    fun readAll() = ServerResponse.ok().body(personRepository.findAll())
    fun readOne(request: ServerRequest) = ServerResponse.ok().body(
        personRepository.findById(request.pathVariable("id").toLong())
    )
}

class Person(val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

class PersonRepository(private val operations: R2dbcEntityOperations) {

    private val id = "ID"
    private val firstName = "FIRST_NAME"
    private val lastName = "LAST_NAME"
    private val birthdate = "BIRTHDATE"

    fun findAll() = operations
        .select(Person::class.java)
        .all()
        .collectList()
        .blockOptional()

    fun findById(id: Long) = operations
        .databaseClient
        .sql("SELECT ${this.id}, $firstName, $lastName, $birthdate FROM PERSON WHERE ${this.id}=:id")
        .bind("id", id)
        .map(toPerson)
        .first()
        .blockOptional()

    private val toPerson: (Row) -> Person = {
        Person(
            it.get(this.id, java.lang.Long::class.java)!!.toLong(),
            it.get(firstName, String::class.java).orEmpty(),
            it.get(lastName, String::class.java).orEmpty(),
            it.get(birthdate, LocalDate::class.java)
        )
    }
}
