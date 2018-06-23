package ch.frankel.blog.springfu.migrationdemo

import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.context.annotation.*
import org.springframework.data.annotation.*
import org.springframework.data.mongodb.core.mapping.*
import org.springframework.data.mongodb.repository.*
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
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
}

fun main(args: Array<String>) {
    runApplication<MigrationDemoApplication>(*args)
}

@RestController
class PersonController(private val personRepository: PersonRepository) {

    @GetMapping("/person")
    fun readAll() = personRepository.findAll()

    @GetMapping("/person/{id}")
    fun readOne(@PathVariable id: Long) = personRepository.findById(id)
}

@Document
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : ReactiveMongoRepository<Person, Long>