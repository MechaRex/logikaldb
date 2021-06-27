# LogikalDB
Foundational reactive logical database

## Features
* Simple and extensible core architecture 
* Reactive system based on Kotlin Flow
* Logical programming system based on microKanren
* Powerful constraint system
* Unified data and query model
* Easily composable queries
* Built on top of FoundationDB

## Getting started
1. Install [FoubndationDB client and server](https://www.foundationdb.org/download/)
2. Try out the [examples](https://github.com/MechaRex/logikaldb-examples)

## Quick example
```kotlin
val logikalDB = LogikalDB()

val pokemonName = vr("name", String::class.java)
val pokemonType = vr("type", String::class.java)

val dataset = or(
   and(eq(pokemonName, "Bulbasaur"), eq(pokemonType, "Grass")),
   and(eq(pokemonName, "Charmander"), eq(pokemonType, "Fire")),
   and(eq(pokemonName, "Squirtle"), eq(pokemonType, "Water")),
   and(eq(pokemonName, "Vulpix"), eq(pokemonType, "Fire"))
)
val query = eq(pokemonType, "Fire")

// Write the dataset to the database
logikalDB.write(listOf("example", "quick"), "pokemon", dataset)

// Query the pokemon, which type is fire and finally print out the name of the found pokemons
logikalDB.read(listOf("example", "quick"), "pokemon")
   .and(query)
   .select(pokemonName)
   .forEach { println("Result: $it") }

// Result: VariableMap(variableValueMap={Variable(variableName=name, variableType=class java.lang.String)=Vulpix})
// Result: VariableMap(variableValueMap={Variable(variableName=name, variableType=class java.lang.String)=Charmander})
```
This quick example shows the basic principles behind LogikalDB, which is to use basic logical operators to describe and query our dataset.\
You can also see that LogikalDB is a folder path based key-value database. For example in this case the folder path is `/example/quick` and the key is `pokemon`.\
Please look into the [examples](https://github.com/MechaRex/logikaldb-examples) for more complex examples.

## Building blocks of LogikalDB
LogikalDB basically built up by three big components:
1. FoundationDB: Responsible for giving the ACID distributed key-value foundation of our database.
2. Logikal: Built-in embedded logical programming language that is responsible for handling querying and the custom constraints.
3. LogikalDB: LogikalDB's job is to tie together the above to two component and serve a simple interface to its users.

## Basics of logic programming in LogikalDB
To understand LogikalDB you first need to understand it's built-in logical programming system(Logikal), because it's components are used for both data modelling and querying:
1. `vr(variableName: VariableName, variableType: Class<T>): Variable<T>` variable constructor:
   - Logikal is optionally typed which means that T can be a concrete value or it can also be a dynamic type like `Value` which is a type alisa for Any?
   - `val variable = vr("variable", String::class.java)`: creates a logical string variable called `variable`
2. `eq(variable: Variable<T>, value: T): Goal` constraint:
   - Logikal is optionally typed, so it supports both statically and dynamically created equality constraints 
   - Goal is basically a synonym for constraint in the system
   - `eq(variable, 42)`: the `variable`'s value will be always `42`, so you can think of it as immutable assignment
   - `eq(firstVariable, secondVarliable)`: `firstVariable` will be tied to the `secondVariable`, which means that it doesn't matter which one is initialized.
      For example `eq(firstVariable, 12)` will mean that the `secondVariable`'s value will be also `12`.
   - `eq(secondVarliable, firstVariable)`: the order of the values doesn't matter at all, and it has the same result as the above example
   - `eq(variable, BigDecimal(1024))`: logikal can work with any kind of type in a type safe manner
3. and(firstConstraint: Goal, ... , lastConstraint: Goal) constraint combinator:
   - `and(eq(firstVariable, 42), eq(secondVariable, 128))`: `firstVariable`'s value will be `42` and the `secondVariable`'s value will be `128` at the same time
   - `and(eq(firstVariable, 42), eq(firstVariable, 128))`: in this case we want that the `firstVariable`'s value to be `42` and `128` at the same time, but this is **not allowed**, so `firstVariable` **won't be defined at all**
4. or(firstConstraint: Goal, ... , lastConstraint: Goal) constraint combinator:
   - `or(eq(firstVariable, 42), eq(firstVariable, 128))`: `firstVariable`'s value will be `42` at the **first time**, and it will be `128` at the **second time**.
   - `or(eq(firstVariable, 42), eq(secondVariable, 128))`: in this case we have different variables, but they are still separated time wise, which means that:
   - **First time** the `firstVariable`'s value will be `42`, but the `secondVariable` won't be defined
   - **Second time** the `secondVariable`'s value will be `128`, but the `firstVariable` won1t be defined

Based on this you can notice that time is essential in Logikal. Time is essential because Logikal is basically an embedded logical programming language, which means that it has stackframes.
So when we talked about "first time" or "second time", we basically meant to say "first stackframe" or "second stackframe" instead. In Logikal stackframe is called state, and you can learn more about it in the later sections of the readme.

Another powerful feature of Logikal is that we can extend its system with our own custom constraints, and they will behave as any other built-in constraint.
For example the `notEq` constraint in the standard library is defined as custom constraint, but you can still use it in both data modelling and querying. You can learn more about custom constraints in the later part of the readme.

## Data modeling
The data modelling is based on the previously mentioned logical components, but in this case you can think of constraints(or Goals) as data builders for simplicity’s sake:
1. `vr(variableName, variableType): Variable` constructor: In a data modeling context vr will be the same as field which hold some type of value
2. `eq(variable, value): Goal` data builder : `eq` is responsible for pairing the field with it's associated value
3. `and(firstDataBuilder: Goal, ... , lastDataBuilder: Goal): Goal` data builder combinator: and is responsible for tying together the data builders together in one data entity(record)
4. `or(firstDataBuilder: Goal, ... , lastDataBuilder: goal): Goal` data builder combinator: or makes it possible for one data builder to have more than one value(list)
5. Custom constraints: Like it was mentioned before you can also use custom constraints like `notEq` in your data model. For example putting `notEq(variable, value)` into our data means that field will never will be the same as the provided value, but if we try to make it equal then the data(or state) becomes invalid.

For example let's say we have the following csv we want to model:
```
Year,Make,Model
1997,Ford,E350
2000,Mercury,Cougar
```
We can model it in the following way:
```kotlin
val year = vr("Year", Integer::class.java)
val make = vr("Make", String::class.java)
val model = vr("Model", String::class.java)

or(
   and(eq(year, 1997), eq(make, "Ford"), eq(model, "E350")),
   and(eq(year, 2000), eq(make, "Mercury"), eq(model, "Cougar"))
)
```
In this example we can see that:
   - eq(variable, value) is responsible for creating the fields of the json object
   - and( fields ) is responsible for tying together the fields of a row together
   - or( rows ) is responsible for creating the rows by making it possible by making a field(or column) have multiple values

If you have dabbled with functional programming then you can recognize that this is really close in a concept to [algebraic data type](https://en.wikipedia.org/wiki/Algebraic_data_type), 
because in our case `and` behaves as the product type and `or` behaves as the sum type.  

## Database operations
LogikalDB is built on FoundationDB, which means that it supports the directory path based key-value operations that FoundationDB offers.
When you are working with LogikalDB you should think that you are working in a filesystem where:
- the value is that same as a file,
- the key is the file name and
- the directory path is the path to the directory where you are storing your files(or values)

LogikalDB supports the following operations for now:
1. `write(directoryPath: List<String>, key: String, value: Goal): Unit`: 
   - Creates a value in the specified directory under the specified key
   - For example: `write(listof("logikaldb","examples"), "intro", eq(vr("example"), "Hello World!"))` creates a `example="Hello World!"` value under the `intro` key in the `logikaldb/examples` directory
2. `read(directoryPath: List<String>, key: String): Query`:
   - Reads out lazily the value from the database in the specified directory and key location
   - Query is a lazy query builder that you can refine with more constraints. Internally Query uses [Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html) to make Query lazy and also a reactive stream.
   - For example: `read(listof("logikaldb","examples"), "intro")` will instantly return a Query that will give back the results in `/logikaldb/examples/intro` when executed.

## Querying
In LogikalDB querying is about finding data with constraints that we introduce previously in the logical programming section. 
We basically do the querying by adding more constraints to the data, which in turn makes the data more specialized.
Another interesting thing about LogikalDB queries is that they are based on lazy streams(flow), which means that:
- they are only run, when we want them to and
- we can alo easily compose them into bigger queries.

The following example show these features:
```kotlin
val firstField = vr("firstField", String::class.java)
val secondField = vr("secondField", String::class.java)
val thirdField = vr("thirdField", String::class.java)
val fourthField = vr("fourthField", String::class.java)

val firstQueryConstraint = and(eq(firstField, "firstValue"), eq(secondField, "secondValue"))
val secondQueryConstraintFirstPart = or(eq(thirdField, "thirdValueA"), eq(thirdField, "thirdValueB"))
val secondQueryConstraintSecondPart = notEq(fourthField, "fourthValue")
val secondQueryConstraint = and(secondQueryConstraintFirstPart, secondQueryConstraintSecondPart)

logikalDB.read(listOf("logikaldb"), "key") // getting the data from the db
   .and(firstQueryConstraint, secondQueryConstraint) // query the data based on the two query constraints
   .select() // run the query and return every variable's value
   .forEach { println("Result: $it") } // print out the results
```
In this example at first we read out the data, but remember that it only returns instantly a Query, so nothing really happens. 
The Query is only run when we call the `select` terminal operation at the end. 
Until we call `select`, we are basically just building up our database query.

Another option is using `selectBy` which will return instantly a flow of variable values. 
It will be executed when a terminal operation is run on the flow.

## Joining
Joining in LogikalDB is basically about combining multiple queries based on a join constraint.
```kotlin
// Read out the data (flow) from the database
val employees = logikalDB.read(listOf("example", "join"), "employee")
val departments = logikalDB.read(listOf("example", "join"), "department")

// This is the constraint used to join the tables based on the department name
val joinGoal = eq(empDepartment, depDepartmentName)

// Run the join query and print out the results
employees.join(joinGoal, departments)
   .select()
   .forEach { println("Result by joining two tables: $it") }
```
In this example you can see that we are joining together the employees and departments tables based on the join constraint.

## Using Stdlib
LogikalDB by design has a small extensible core, which also means that it has a standard library which provides extra features that are missing from this small core.
The standard library only uses the publicly available extension points of LogikalDB, so there is nothing special in them that you can't achieve yourself with LogikalDB. 
The only speciality is that the standard library is automatically included in LogikalDB.

For now the standard library provides the following functionality:
1. `notEq` and `notEqDynamic`
2. `cmp` and `cmpDynamic`
3. `inSet`

Standard library is under work so let us know what kind of functionality you would like to see in it.

## Constraint system
LogikalDB is built on top of an embedded logical programming language called Logikal.
It's important to note, because programming languages are just tools to describe how the state of a program must change step by step.
So a program in any kind of programming language can be thought as just a state stream, where we start with some initial state, which we modify until we reach a final state where we can get the answer we want.
So the type information of a program can be imagined as: `input: State -> processing:(State -> State) -> output: State`

In LogikalDB we use the same concept for querying, because querying is basically just about finding one or more state where the answers are hiding.
You can thin of querying the following way: `data: State -> querying:(State -> Flow<State?>) -> result: Flow<State?>`
First we get some initial state, which for example can be the data returned from the database and
in the query phase is where the uncertainty comes in place, because in case of a query it can happen that the query fails, or it returns multiple results, which is why we are using `Flow<State?>` type.

In this concept the constraint's job is to modify the state of our program, which is the query itself.
We have multiple built-in simple operations of constraints to modify the state:
1. eq() used for adding a new value to the state
2. and() used for grouping values together in one state
3. or() used for creating separate state for every value
4. custom constraints are used to modify a state one by one

Custom constraints can be thought as a kind of general filter functions, because they have the following type: `State -> State?`
They are general, because it doesn't matter where they are defined, because they will run when every constrained variable in the custom constraint has a value.
So they kind of sit outside the normal flow of a query and only gets involved when they have every necessary information available for them.

## Create custom constraints
Custom constraints are the main extension point of LogikalDB as it let you plug in your custom code into the hearth of the system.
This is why the custom constraint system is a very powerful and advanced feature, so use it wisely.
This introduction is at the end of the readme, because you need to be familiar how the constraint system works to be able to understand how you can extend it.

First you need to create your custom constraint:
```kotlin
public fun isHelloWorld(myVariable: Variable<String>): Goal {
        return Constraint.create(myVariable) { state ->
            val valueOfMyVariable = state.valueOf(myVariable)
            if (valueOfMyVariable == "Hello world!") {
                state
            } else {
                null
            }
        }
    }
```

After that you can just easily use that constraint however you like:
```kotlin
val myVariable = vr("A", String::class.java)

val result = and(
   or(eq(myVariable, "Foo bar!"), eq(myVariable, "Hello world!")),
   isHelloWorld(myVariable)
)
```

You can find more examples about custom constraints in the `StdLib`, which is also just a `ConstraintLibrary`, which has some custom constraints defined in it.
So there is no magic in defining your own constraints.