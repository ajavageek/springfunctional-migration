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
    fun routes(repository: PersonRepository) = nest(
            path("/person"),
                route(
                        GET("/{id}"),
                        HandlerFunction { ServerResponse.ok().body(repository.findById(it.pathVariable("id").toLong())) })
                .andRoute(
                        method(HttpMethod.GET),
                        HandlerFunction { ServerResponse.ok().body(repository.findAll()) })
    )
}

fun main(args: Array<String>) {
    runApplication<MigrationDemoApplication>(*args)
}

@Document
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : ReactiveMongoRepository<Person, Long>