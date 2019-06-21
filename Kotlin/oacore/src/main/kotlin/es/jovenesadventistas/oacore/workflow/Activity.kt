package es.jovenesadventistas.oacore.workflow

class Activity(val name: String) {
    fun execute(){
        println("Actividad ejecutandose: $name")
    }
}