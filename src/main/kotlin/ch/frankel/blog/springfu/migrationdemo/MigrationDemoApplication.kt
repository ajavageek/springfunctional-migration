package ch.frankel.blog.springfu.migrationdemo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.web.servlet.function.*
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@SpringBootApplication
class MigrationDemoApplication

fun beans() = beans {
    bean {
        router {
            "/person".nest {
                val handler = PersonHandler(ref())
                GET("/{id}", handler::readOne)
                GET("/") { handler.readAll() }
            }
        }
    }
}

fun main(args: Array<String>) {
    runApplication<MigrationDemoApplication>(*args) {
        addInitializers(beans())
    }
}

class PersonHandler(private val personRepository: PersonRepository) {
    fun readAll() = ServerResponse.ok().body(personRepository.findAll())
    fun readOne(request: ServerRequest) = ServerResponse.ok().body(personRepository.findById(request.pathVariable("id").toLong()))
}

@Entity
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : PagingAndSortingRepository<Person, Long>