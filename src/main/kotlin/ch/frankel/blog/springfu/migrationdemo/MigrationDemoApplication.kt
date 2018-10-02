package ch.frankel.blog.springfu.migrationdemo

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.kofu.application
import org.springframework.boot.kofu.mongo.embedded
import org.springframework.boot.kofu.mongo.mongodb
import org.springframework.boot.kofu.ref
import org.springframework.boot.kofu.web.jackson
import org.springframework.boot.kofu.web.server
import org.springframework.context.support.beans
import org.springframework.data.annotation.*
import org.springframework.data.mongodb.core.*
import org.springframework.data.mongodb.core.mapping.*
import org.springframework.web.reactive.function.server.*
import java.time.Duration
import java.time.LocalDate

val beans = beans {
    bean<PersonHandler>()
    bean<PersonRepository>()
}

val app = application {
    import(beans)
    listener<ApplicationReadyEvent> {
        ref<PersonRepository>().insert(
                arrayListOf(Person(1, "John", "Doe", LocalDate.of(1970, 1, 1)),
                        Person(2, "Jane", "Doe", LocalDate.of(1970, 1, 1)),
                        Person(3, "Brian", "Goetz"))
        ).blockLast(Duration.ofSeconds(2))
    }
    server {
        import(::routes)
        codecs {
            jackson()
        }
    }
    mongodb {
        embedded()
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
    app.run(args)
}

@Document
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

class PersonRepository(private val mongo: ReactiveMongoOperations) {
    fun findAll() = mongo.findAll<Person>()
    fun findById(id: Long) = mongo.findById<Person>(id)
    fun insert(persons: List<Person>) = mongo.insert(persons, Person::class)
}