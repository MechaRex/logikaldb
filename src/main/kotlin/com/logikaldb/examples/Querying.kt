package com.logikaldb.examples

import com.logikaldb.Constraint.and
import com.logikaldb.Constraint.eq
import com.logikaldb.Constraint.or
import com.logikaldb.Constraint.vr
import com.logikaldb.LogikalDB
import com.logikaldb.StdLib.notEq
import com.logikaldb.and
import com.logikaldb.selectBy
import com.logikaldb.selectFlowBy
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

private fun main() {
    runBlocking {
        val logikalDB = LogikalDB()

        val firstName = vr("firstName")
        val lastName = vr("lastName")
        val department = vr("department")
        val salary = vr("salary")

        // Write employee data to the database
        logikalDB.write(
            listOf("example", "query"), "employee",
            or(
                and(eq(firstName, "John"), eq(lastName, "Smith"), eq(department, "HR"), eq(salary, BigDecimal(10000))),
                and(eq(firstName, "Yolanda"), eq(lastName, "Gates"), eq(department, "IT"), eq(salary, BigDecimal(25000))),
                and(eq(firstName, "Bill"), eq(lastName, "Smith"), eq(department, "HR"), eq(salary, BigDecimal(15000))),
                and(eq(firstName, "Gustav"), eq(lastName, "Musk"), eq(department, "IT"), eq(salary, BigDecimal(20000))),
                and(eq(firstName, "Charlie"), eq(lastName, "Fisher"), eq(department, "SE"), eq(salary, BigDecimal(30000))),
            )
        )

        // Query the employee data with lastName==Smith constraint
        logikalDB.read(listOf("example", "query"), "employee")
            .and(eq(lastName, "Smith"))
            .selectBy(logikalDB)
            .forEach { println("Result with lastName==Smith query: $it") }

        // Query the employee data with lastName!=Smith and department==IT constraint
        logikalDB.read(listOf("example", "query"), "employee")
            .and(notEq(lastName, "Smith"), eq(department, "IT"))
            .selectBy(logikalDB)
            .forEach { println("Result with lastName!=Smith and department==IT query: $it") }

        // Same query as before but using selectFlow instead
        logikalDB.read(listOf("example", "query"), "employee")
            .and(notEq(lastName, "Smith"), eq(department, "IT"))
            .selectFlowBy(logikalDB)
            .collect { println("Flow Result with lastName!=Smith and department==IT query: $it") }
    }
}
