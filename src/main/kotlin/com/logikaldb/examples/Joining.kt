package com.logikaldb.examples

import com.logikaldb.Constraint.and
import com.logikaldb.Constraint.eq
import com.logikaldb.Constraint.or
import com.logikaldb.Constraint.vr
import com.logikaldb.LogikalDB
import com.logikaldb.join
import com.logikaldb.select
import kotlinx.coroutines.runBlocking
import java.math.BigDecimal

private fun main() {
    runBlocking {
        val logikalDB = LogikalDB()

        val empFirstName = vr("employee.firstName")
        val empLastName = vr("employee.lastName")
        val empDepartment = vr("employee.department")
        val empSalary = vr("employee.salary")

        // Write employee data to the database
        logikalDB.write(
            listOf("example", "join"), "employee",
            or(
                and(eq(empFirstName, "John"), eq(empLastName, "Smith"), eq(empDepartment, "HR"), eq(empSalary, BigDecimal(10000))),
                and(eq(empFirstName, "Yolanda"), eq(empLastName, "Gates"), eq(empDepartment, "IT"), eq(empSalary, BigDecimal(25000))),
                and(eq(empFirstName, "Bill"), eq(empLastName, "Smith"), eq(empDepartment, "HR"), eq(empSalary, BigDecimal(15000))),
                and(eq(empFirstName, "Gustav"), eq(empLastName, "Musk"), eq(empDepartment, "IT"), eq(empSalary, BigDecimal(20000))),
                and(eq(empFirstName, "Charlie"), eq(empLastName, "Fisher"), eq(empDepartment, "SE"), eq(empSalary, BigDecimal(30000))),
            )
        )

        val depDepartmentName = vr("employee.departmentName")
        val depManager = vr("department.manager")
        val depManagerEmail = vr("department.managerEmail")

        // Write department data to the database
        logikalDB.write(
            listOf("example", "join"), "department",
            or(
                and(eq(depDepartmentName, "HR"), eq(depManager, "Chris Harris"), eq(depManagerEmail, "charris@company.com")),
                and(eq(depDepartmentName, "IT"), eq(depManager, "Olivia Jones"), eq(depManagerEmail, "ojones@company.com")),
                and(eq(depDepartmentName, "SE"), eq(depManager, "Bill Milles"), eq(depManagerEmail, "bmilles@company.com"))
            )
        )

        // Read out the data (flow) from the database
        val employees = logikalDB.read(listOf("example", "join"), "employee")
        val departments = logikalDB.read(listOf("example", "join"), "department")

        // This is the constraint used to join the tables based on the department name
        val joinGoal = eq(empDepartment, depDepartmentName)

        // Run the join query and print out the results
        employees.join(joinGoal, departments)
            .select(logikalDB)
            .forEach { println("Result by joining two tables: $it") }
    }
}
