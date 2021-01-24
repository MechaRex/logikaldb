package com.logikaldb.examples

import com.logikaldb.Constraint.and
import com.logikaldb.Constraint.eq
import com.logikaldb.Constraint.or
import com.logikaldb.Constraint.vr
import com.logikaldb.LogikalDB
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.runBlocking

private fun main() {
    runBlocking {
        val data = or(
            and(eq(vr("name"), "Bulbasaur"), eq(vr("type"), "Grass")),
            and(eq(vr("name"), "Charmander"), eq(vr("type"), "Fire")),
            and(eq(vr("name"), "Squirtle"), eq(vr("type"), "Water")),
            and(eq(vr("name"), "Vulpix"), eq(vr("type"), "Fire"))
        )

        val query = and(
            data,
            eq(vr("type"), "Fire")
        )

        LogikalDB().run(query)
            .filterNotNull()
            .collect { println("Result: $it") }
    }
}