package ch.frankel.blog.springfu.migrationdemo

import org.springframework.boot.*
import org.springframework.boot.autoconfigure.*
import org.springframework.data.repository.*
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import javax.persistence.*

@SpringBootApplication
class MigrationDemoApplication

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

@Entity
class Person(@Id val id: Long, val firstName: String, val lastName: String, val birthdate: LocalDate? = null)

interface PersonRepository : PagingAndSortingRepository<Person, Long>