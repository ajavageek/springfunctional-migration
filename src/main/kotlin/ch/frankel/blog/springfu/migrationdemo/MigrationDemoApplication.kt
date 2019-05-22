package ch.frankel.blog.springfu.migrationdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.HttpMethod
import org.springframework.web.servlet.function.*
import org.springframework.web.servlet.function.RequestPredicates.*
import org.springframework.web.servlet.function.RouterFunctions.nest
import org.springframework.web.servlet.function.RouterFunctions.route
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@SpringBootApplication
class MigrationDemoApplication {

    @Bean
    fun routes(repository: PersonRepository) = router {
        val handler = PersonHandler(repository)
        "/person".nest {
            GET("/{id}", handler::readOne)
            GET("/") { handler.readAll() }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<MigrationDemoApplication>(*args)
}

class PersonHandler(private val personRepository: PersonRepository) {
    fun readAll() = ServerResponse.ok().body(personRepository.findAll())
    fun readOne(request: ServerRequest) = ServerResponse.ok().body(personRepository.findById(request.pathVariable("id").toLong()))
}

@Entity
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : PagingAndSortingRepository<Person, Long>