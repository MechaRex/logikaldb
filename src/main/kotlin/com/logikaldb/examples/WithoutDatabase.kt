package com.logikaldb.examples

import com.logikaldb.Constraint
import com.logikaldb.Constraint.and
import com.logikaldb.LogikalDB
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.runBlocking

private fun main() {
    runBlocking {
        val logikalDB = LogikalDB()

        val dataset = Constraint.or(
            and(Constraint.eq(Constraint.vr("name"), "Bulbasaur"), Constraint.eq(Constraint.vr("type"), "Grass")),
            and(Constraint.eq(Constraint.vr("name"), "Charmander"), Constraint.eq(Constraint.vr("type"), "Fire")),
            and(Constraint.eq(Constraint.vr("name"), "Squirtle"), Constraint.eq(Constraint.vr("type"), "Water")),
            and(Constraint.eq(Constraint.vr("name"), "Vulpix"), Constraint.eq(Constraint.vr("type"), "Fire"))
        )
        val query = Constraint.eq(Constraint.vr("type"), "Fire")

        // Query tha dataset without using the database
        logikalDB
            .run(and(dataset, query))
            .filterNotNull()
            .collect { println("Result w/o db: ${it.valuesOf()}") }
    }
}
