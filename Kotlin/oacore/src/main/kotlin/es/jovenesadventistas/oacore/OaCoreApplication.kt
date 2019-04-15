package es.jovenesadventistas.oacore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OaCoreApplication

fun main(args: Array<String>) {
	runApplication<OaCoreApplication>(*args)
}
