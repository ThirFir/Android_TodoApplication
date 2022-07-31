package com.example.TodoManager

import java.lang.IllegalArgumentException

abstract class Pizza() {
    abstract fun make()
}

class KoreanCheesePizza : Pizza() {
    override fun make() {

    }
}

class KoreanPotatoPizza : Pizza() {
    override fun make() {

    }
}

abstract class PizzaStore {

    fun orderPizza(type : String) : Pizza {

        val pizza : Pizza = createPizza(type)


        // pizza.make()

        return pizza
    }

    abstract fun createPizza(type : String) : Pizza
}

class KoreanPizzaStore : PizzaStore() {
    override fun createPizza(type : String): Pizza {
        return when(type) {
            "cheese" -> KoreanCheesePizza()
            "potato" -> KoreanPotatoPizza()
            else -> throw IllegalArgumentException()
        }
    }

}

interface Units {
    fun models()
}

abstract class TerranUnit() : Units {
    abstract override fun models()
}

class Marine() : TerranUnit() {
    init{
        println("marine constructed")
    }
    override fun models() {

    }
}
class Firebat() : TerranUnit() {
    override fun models() {

    }
}

abstract class SpawnBuilding {
    fun spawnUnit(type : String) : Units {
        val unit = createUnit(type)

        unit.models()

        return unit
    }

    protected abstract fun createUnit(type : String) : Units
}
class Bunker() : SpawnBuilding() {
    override fun createUnit(type : String): Units {
        return when(type) {
            "marine" -> Marine()
            "firebat" -> Firebat()
            else -> throw IllegalArgumentException()
        }
    }
}


fun main() {
    val bunker : SpawnBuilding = Bunker()
    bunker.spawnUnit("marine")
}