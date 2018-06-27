package ch.frankel.blog.springfu.migrationdemo

import org.springframework.beans.factory.annotation.*
import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.context.support.*
import org.springframework.data.annotation.*
import org.springframework.data.mongodb.core.mapping.*
import org.springframework.data.mongodb.repository.*
import org.springframework.web.reactive.function.server.*
import org.springframework.web.server.adapter.*
import java.time.Duration
import java.time.LocalDate

@SpringBootApplication
class MigrationDemoApplication {

    @Autowired
    fun register(ctx: GenericApplicationContext) = beans().initialize(ctx)
}

fun beans() = beans {
    bean {
        CommandLineRunner {
            ref<PersonRepository>().insert(
                    arrayListOf(Person(1, "John", "Doe", LocalDate.of(1970, 1, 1)),
                                Person(2, "Jane", "Doe", LocalDate.of(1970, 1, 1)),
                                Person(3, "Brian", "Goetz"))
            ).blockLast(Duration.ofSeconds(2))
        }
    }
    bean {
        routes(PersonHandler(ref()))
    }
}

fun routes(handler: PersonHandler) = router {
    "/person".nest {
        GET("/{id}", handler::readOne)
        GET("/", handler::readAll)
    }
}

class PersonHandler(private val personRepository: PersonRepository) {
    fun readAll(request: ServerRequest) = ServerResponse.ok().body(personRepository.findAll())
    fun readOne(request: ServerRequest) = ServerResponse.ok().body(personRepository.findById(request.pathVariable("id").toLong()))
}

fun main(args: Array<String>) {
    runApplication<MigrationDemoApplication>(*args)
}

@Document
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : ReactiveMongoRepository<Person, Long>