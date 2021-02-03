package com.logikaldb.examples

import com.logikaldb.Constraint.and
import com.logikaldb.Constraint.eq
import com.logikaldb.Constraint.or
import com.logikaldb.Constraint.vr
import com.logikaldb.LogikalDB
import com.logikaldb.and
import com.logikaldb.select
import kotlinx.coroutines.runBlocking

private fun main() {
    runBlocking {
        val logikalDB = LogikalDB()

        val dataset = or(
            and(eq(vr("name"), "Bulbasaur"), eq(vr("type"), "Grass")),
            and(eq(vr("name"), "Charmander"), eq(vr("type"), "Fire")),
            and(eq(vr("name"), "Squirtle"), eq(vr("type"), "Water")),
            and(eq(vr("name"), "Vulpix"), eq(vr("type"), "Fire"))
        )
        val query = eq(vr("type"), "Fire")

        // Write the dataset to the database
        logikalDB.write(listOf("example", "quick"), "pokemon", dataset)

        // Query the pokemon, which type is fire and finally print out the results
        logikalDB.read(listOf("example", "quick"), "pokemon")
            .and(query)
            .select(logikalDB)
            .forEach { println("Result: $it") }
    }
}
