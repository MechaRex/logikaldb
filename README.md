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
1. Install FoubndationDB client and server: https://www.foundationdb.org/download/
2. Include LogikalDB as a gradle source dependency (or just clone this repository)
3. Try running some examples in the repository

## Building blocks of LogikalDB
LogikalDB basically built up by three big components:
1. FoundationDB: Responsible for giving the ACID distributed key-value foundation of our database.
2. Logikal: Built-in embedded logical programming language that is responsible for handling querying and the custom constraints.
3. LogikalDB: LogikalDB's job is to tie together the above to two component and serve a simple interface to its users.

## Basics of logic programming in LogikalDB
To understand LogikalDB you first need to understand it's built-in logical programming system(Logikal), because it's components are used for both data modelling and querying:
1. `vr(variableName: String): Variable` variable constructor:
   - `val variable = vr("variable")`: creates a logical variable called `variable`
2. `eq(firstValue: Value, secondValue: Value): Goal` constraint:
   - Logikal is dynamically typed, which means that Value is basically synonym for the Any type
   - Goal is basically a synonym for constraint in the system
   - `eq(variable, 42)`: the `variable`'s value will be always `42`, so you can think of it as immutable assignment
   - `eq(42, variable)`: the order of the values doesn't matter at all, and it has the same result as the above example
   - `eq(firstVariable, secondVariable)`: `firstVariable` will be tied to the `secondVariable`, which means that it doesn't matter which one is initialized.
      For example `eq(firstVariable,12)` will mean that the `secondVariable`'s value will be also `12`.
   - `eq(variable, BigDecimal(1024))`: logikal is dynamically typed which means that eq accepts any kind of values as input
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
1. `vr("field"): Variable` constructor: In a data modeling context vr will be the same as field which hold some value
2. `eq(vr("fieldName"), fieldValue): Goal` data builder : `eq` is responsible for pairing the field with its associated value
3. `and(firstDataBuilder: Goal, ... , lastDataBuilder: Goal): Goal` data builder combinator: and is responsible for tying together the data builders together in one data entity(record)
4. `or(firstDataBuilder: Goal, ... , lastDataBuilder: goal): Goal` data builder combinator: or makes it possible for one data builder to have more than one value(list)
5. Custom constraints: Like it was mentioned before you can also use custom constraints like `notEq` in your data model. For example putting `notEq(vr("fieldName"), fieldValue)` into our data means that field will never will be the same as the provided value, but if we try to make it equal then the data(or state) becomes invalid.

For example let's say we have the following csv we want to model:
```
Year,Make,Model
1997,Ford,E350
2000,Mercury,Cougar
```
We can model it in the following way:
```kotlin
or(
   and(eq(vr("Year"), 1997), eq(vr("Make"), "Ford"), eq(vr("Model"), "E350")),
   and(eq(vr("Year"), 2000), eq(vr("Make"), "Mercury"), eq(vr("Model"), "Cougar"))
)
```
In this example we can see that:
   - eq(vr("fieldName"), fieldValue) is responsible for creating the fields of the json object
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
2. `read(directoryPath: List<String>, key: String): Flow<Goal>`:
   - Reads out lazily the value from the database in the specified directory and key location
   - Flow is the built-in asynchronous data stream in Kotlin that we are using throughout LogikalDB to make the database reactive. You can learn more about it here: https://kotlinlang.org/docs/reference/coroutines/flow.html
   - For example: `read(listof("logikaldb","examples"), "intro")` will instantly return us a Flow and this Flow will be able to run our database operation and return the value from the database

## Querying
In LogikalDB querying is about finding data with constraints that we introduce previously in the logical programming section. We basically do the querying by adding more constraints to the data, which in turn makes the data more specialized.
Another interesting thing about LogikalDB queries is that they are based on lazy streams(flow), which means that:
- they are only run, when we want them to and
- we can alo easily compose them into bigger queries.

The following example show these features:
```kotlin
val firstQueryConstraint = and(eq(vr("firstField"), "firstValue"), eq(vr("secondField"), "secondValue"))
val secondQueryConstraintFirstPart = or(eq(vr("thirdField"), "thirdValueA"), eq(vr("thirdField"), "thirdValueB"))
val secondQueryConstraintSecondPart = notEq(vr("fourthField"), "fourthValue")
val secondQueryConstraint = and(secondQueryConstraintFirstPart, secondQueryConstraintSecondPart)

logikalDB.read(listOf("logikaldb"), "key") // getting the data
   .map{ and(it, firstQueryConstraint) } // makes data more specialized
   .map{ and(it, secondQueryConstraint) } // makes data even more specialized
   .map{ logikalDB.run(it) } // running the query
   .flattenMerge().collect { println("$it") } // we run the whole flow and print out the query result(s)
```
In this example at first we read out the data, but remember that it only returns instantly a flow, so nothing really happens. 
The flow and everything attached to it is only run when we call the `collect` terminal operation at the end. Until we call `collect`, we are basically just building up our database query.

Another interesting thing about the LogikalDB queries is that we can also store them in tha database as LogikalDB has an unified data model.

## Using Stdlib
LogikalDB by design has a small extensible core, which also means that it has a standard library which provides extra features that are missing from this small core.
The standard library only uses the publicly available extension points of LogikalDB, so there is nothing special in them that you can't achieve yourself with LogikalDB. 
The only speciality is that the standard library is automatically included in LogikalDB.

For now the standard library provides the following functionality:
1. `notEq(firstValue: Value, secondValue: Value): Goal`
2. `cmp(firstValue: Value, secondValue: Value, compareValue: Int): Goal`
3. `inSet(variable: Variable, values: Set<Value>): Goal`

Standard library is under work so let us know what kind of functionality you would like to see in it.

## Constraint system
LogikalDB is built on top of an embedded logical programming language called Logikal.
It's important to note, because programming languages are just tools to describe how the state of a program must change step by step.
So a program in any kind of programming language can be thought as just a state stream, where we start with some initial state, which we modify until we reach a final state where we can get the answer we want.
So the type information of a program can be imagined as: `input: State -> processing:(State -> State) -> output: State`

In LogikalDB we use the same concept for querying, because querying is basically just about finding the one or more state where are answers are hiding.
You can thin of querying the following way: `data: State -> querying:(State -> Flow<State?>) -> result: Flow<State?>`
First we get some initial state, which for example can be the data returned from the database and
in the query phase is where the uncertainty comes in place, because in case of a query it can happen that the query fails, or it returns multiple results, which is why we are using `Flow<State?>` type.

In this concept the constraints job is to modify the state of our program, which is the query itself.
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

The first step in creating your own constraints is defining a new `ConstraintLibrary` which will contain your constraints:
```kotlin
public object MyLibrary : ConstraintLibrary {
   override fun exportConstraints(): ConstraintRegistry {
      return registerConstraints()
   }
}
```

The next step is to create the constraint itself inside the library:
```kotlin
public fun isHelloWorld(myInput: Value): Goal {
        return Constraint.create(::isHelloWorld, myInput) { state ->
            val valueOfMyInput = state.valueOf(myInput)
            if (valueOfMyInput == "Hello world!") {
                state
            } else {
                null
            }
        }
    }
```

After that the next step is to register it in our library's `exportConstraints` function:
```kotlin
override fun exportConstraints(): ConstraintRegistry {
   return registerConstraints(::isHelloWorld)
}
```

Finally the last step is to register the library itself into LogikalDB:
```kotlin
val logikalDB = LogikalDB(MyLibrary)
```

After all that setup we can just use our constraint like we would use any other constraint in LogikalDB:
```kotlin
val result = and(
   or(eq(vr("A"), "Foo bar!"), eq(vr("A"), "Hello world!")),
   isHelloWorld(vr("A"))
)
```

You can find more examples about custom constraints in the `StdLib`, which is also just a `ConstraintLibrary`, which has some custom constraints defined in it.
So there is no magic in defining your own constraints.