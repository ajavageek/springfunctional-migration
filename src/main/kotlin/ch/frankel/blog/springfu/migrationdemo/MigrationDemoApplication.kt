package ch.frankel.blog.springfu.migrationdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.function.HandlerFunction
import org.springframework.web.servlet.function.RequestPredicates.*
import org.springframework.web.servlet.function.RouterFunctions.nest
import org.springframework.web.servlet.function.RouterFunctions.route
import org.springframework.web.servlet.function.ServerResponse
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@SpringBootApplication
class MigrationDemoApplication {

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

@Entity
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : PagingAndSortingRepository<Person, Long>