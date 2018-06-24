package ch.frankel.blog.springfu.migrationdemo

import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.context.annotation.*
import org.springframework.data.annotation.*
import org.springframework.data.mongodb.core.mapping.*
import org.springframework.data.mongodb.repository.*
import org.springframework.http.*
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.RequestPredicates.*
import org.springframework.web.reactive.function.server.RouterFunctions.*
import java.time.Duration
import java.time.LocalDate

@SpringBootApplication
class MigrationDemoApplication {

    @Bean
    fun initialize(repository: PersonRepository) = CommandLineRunner {
        repository.insert(
                arrayListOf(Person(1, "John", "Doe", LocalDate.of(1970, 1, 1)),
                            Person(2, "Jane", "Doe", LocalDate.of(1970, 1, 1)),
                            Person(3, "Brian", "Goetz"))
        ).blockLast(Duration.ofSeconds(2))
    }

    @Bean
    fun routes(repository: PersonRepository): RouterFunction<ServerResponse> {
        val handler = PersonHandler(repository)
        return nest(
            path("/person"),
                route(
                        GET("/{id}"),
                        HandlerFunction { handler.readOne(it) })
                .andRoute(
                        method(HttpMethod.GET),
                        HandlerFunction { handler.readAll(it) })
    )}
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