package com.logikaldb.examples

import com.logikaldb.Constraint.and
import com.logikaldb.Constraint.eq
import com.logikaldb.Constraint.or
import com.logikaldb.Constraint.vr
import com.logikaldb.LogikalDB
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.map
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
            listOf("example", "join"), "employee",
            or(
                and(eq(firstName, "John"), eq(lastName, "Smith"), eq(department, "HR"), eq(salary, BigDecimal(10000))),
                and(eq(firstName, "Yolanda"), eq(lastName, "Gates"), eq(department, "IT"), eq(salary, BigDecimal(25000))),
                and(eq(firstName, "Bill"), eq(lastName, "Smith"), eq(department, "HR"), eq(salary, BigDecimal(15000))),
                and(eq(firstName, "Gustav"), eq(lastName, "Musk"), eq(department, "IT"), eq(salary, BigDecimal(20000))),
                and(eq(firstName, "Charlie"), eq(lastName, "Fisher"), eq(department, "SE"), eq(salary, BigDecimal(30000))),
            )
        )

        val manager = vr("manager")
        val managerEmail = vr("managerEmail")

        // Write manager data to the database
        logikalDB.write(
            listOf("example", "join"), "manager",
            or(
                and(eq(department, "HR"), eq(manager, "Chris Harris"), eq(managerEmail, "charris@company.com")),
                and(eq(department, "IT"), eq(manager, "Olivia Jones"), eq(managerEmail, "ojones@company.com")),
                and(eq(department, "SE"), eq(manager, "Bill Milles"), eq(managerEmail, "bmilles@company.com"))
            )
        )

        // Read out the data (flow) from the database
        val employees = logikalDB.read(listOf("example", "join"), "employee")
        val managers = logikalDB.read(listOf("example", "join"), "manager")

        // Join the returned data from the database based on the department field
        val joinedResult = employees.map { employee ->
            managers.map { manager ->
                and(employee, manager)
            }
        }.flattenMerge().filterNotNull()

        // Run the query and print out the result
        joinedResult
            .map { logikalDB.run(it) }
            .flattenMerge().filterNotNull()
            .collect { println("Result by joining two tables: $it") }
    }
}
