# Welcome to the Sigma programming language, a language for sigmas only.

This language was developed by Dean Cureton for the spring 2022 Westminster Honors Programming Languages class.

# Variables

## Types

Sigma is unique in that it uses a single type for every number where other languages would use multiple. For example, `int`, `double`, `long`, and `float` would all use the same type, `num`, in Sigma. This makes arithmetic far easier and different typing between variables less of an issue.  
Other types of variables in Sigma include `str` for string (which also includes the same functionality as `char` in other languages) and `tf` for bool. Finally, arrays in Sigma are dynamic and do not have a single typing. For example, a single array can include both `num`s and `str`s.

## Declaring, Assigning, and Initializing Variables

Declaring a variable in Sigma is easy: begin with the keyword corresponding with the type of variable you wish to create, type the variable name, and end the line with the double exclamation point emoji (representing a semicolon in most other languages). For example, to declare the variable `x` as a string, you would type:
```
str x‼️
```
To assign a value to `x` at declaration, use the arrow sign before typing the actual content of the variable:
```
str x <- "This is a string."‼️
```
To assign a value to `x` outside of declaration, the keyword `str` would be left out.

## Interchanging Types

To convert between types in Sigma, simply use a period. As an example, consider the following block of code:
```
tf x <- true‼️
log {x.num}‼️
```
This block of code would output `1`.

# Functions

## Defining Functions

Defining functions in Sigma is very similar to regular variable declaration, except with different keywords. Let's start with an example:
```
func x <- num arg1 num arg2 [num arg3] |
    log {arg1 + arg2 + arg3}‼️
    take {nothing}‼️
|‼️
```
This is a simple function that takes in 3 arguments (one of which is optional, denoted with square brackets) and adds their number values together. As you can see, the `func` keyword denotes the creation of a function in Sigma. The arrow sign remains the same to signify assignment. Next, list the arguments separated by spaces, then use a vertical pipe to denote the beginning of the block inside the function. In this case, since the function doesn't return a value, we type `take nothing`, but if it did, we would `take` whatever value we wished to return. Finally, close the function declaration with the double exclamation point emoji.

## Calling Functions

To call this function `x`, we would simply write:
```
x {1 2 3}‼️
x {4 5}‼️
```
This block of code would log 6, then 9.

## Built-in Functions

`log` takes in one argument, outputs the argument to the console, and appends a line break to the end.  
`logl` takes in one argument and outputs the argument to the console with no line break.
`random` takes in two number arguments and outputs a random number between the two.

# Comparators

We don't like equal signs in Sigma. If you type one, Sigma will throw an error. So, to check whether two things are equal in Sigma, use the `?` symbol. To check whether two variables have the same type, use the `??` symbol. To check if two numbers are within 5% of each other, use the `~` symbol (approximately equal). Use the `>` and `<` symbols for greater than and less than like in any other language. `>?` and `<?` also work as regular, but `≥` and `≤` are also supported by Sigma.

# Loops

Looping in Sigma is very simple. The two main loop functions are `loopnum` and `looptf` to represent the two types of loops that Sigma supports. `loopnum` takes in one number argument (if the argument is not an integer it rounds down) and loops the block inside the amount of times as the argument. In addition, inside loops, the keyword `count` is reserved to keep track of the amount of times the loop has run so far (starting at `0`). `looptf` loops a block of code as long as the conditional statement in the argument remains true. Consider the following block of code:
```
loopnum {5} |
    log {count}‼️
|
looptf {2 ? 2} |
    log {"Hello!"}‼️
|
```
This block of code would output:
```
0
1
2
3
4
Hello!
Hello!
Hello!
Hello!
Hello!
```
and the "Hello!"s would extend infinitely.

# Operators

Sigma is an infix language, and all arithmetic operators work the same as in any other language. However, since integer division isn't really a thing in Sigma since every number has the same type, integer division is represented by the symbol `//`. Consider `5.5 // 2.5`. In this case, Sigma would round both `5.5` and `2.5` down to `5` and `2`, and then perform integer division, giving `2` as a result.
The symbols `+=` and `-=` would be represented as `+<-` and `-<-`. Incrementing and decrementing would be represented the same way as any other language, using `++` and `--`.  
Boolean `and`, `or` and `not` operators are implemented in Sigma using the keywords themselves. For example, `true and fals` would output `fals`.

# Conditionals

Conditionals in Sigma are treated very similarly to most other languages. The following block of code gives an illustration:
```
if {fals} |
    log {"Block 1"}‼️
| butif {true} |
    log {"Block 2"}‼️
| but |
    log {"Block 3"}‼️
|
```
This code would output `"Block 2"`. As you can see, if/else statements are now if/but statements, and everything else works relatively the same as in other languages.

# Arrays

As noted before, all arrays in Sigma are dynamic and do not have a set type. To declare an array in Sigma, we must write:
```
arr x <- (1 true "three")‼️
```
Arrays are surrounded by parentheses and elements are separated by spaces (types are inferred). To access elements in an array, simply use parentheses (indexing starts at 0):
```
log {x(0).num + x(1).num}‼️
```
This code would output 2 (since `true` is equivalent to 1 when converted to a `num`).  
To assign values in an array, simply use the same assignment as you would for variables:
```
num x(2) <- 5‼️
```
To add or remove elements from an array, call the `add` or `remove` functions:
```
x.add {2, 3}‼️
x.remove {0}‼️
```
The `add` function takes two parameters: `content` and `index` (optional), and adds the content at whatever index signified (if `index` is empty, it adds to the end). `remove` simply removes the element at the designated index.

# Comments

To comment something out in Sigma, just use the `\` symbol. For multi-line comments, use `\.` and `.\`. 

# Keyword Overview

| Keyword            | Meaning                                        |
|--------------------|------------------------------------------------|
| `‼️`               | equivalent to a semicolon |
| `num`              | number variable                                |
| `str`              | string variable                                |
| `tf`               | boolean variable                               |
| `arr`              | array |
| `log`              | log something to the console with a line break |
| `logl`             | log something to the console with no line break |
| `func`             | function                                       |
| vertical pipe      | encloses blocks of code |
| `take`             | return value from a function                   |
| `nothing`          | null/undefined                                 |
| `true`             | boolean true                                            |
| `fals`             | boolean false                                          |
| `?`                | equals?                                        |
| `??`               | same type?                                     |
| `~`                | approximately equal (5% for `num`s)?           |
| `>=`/`>?`          | greater than or equal to?                      |
| `<=`/`<?`          | less than or equal to?                         |
| `loopnum`          | loop for some number of times                  |
| `looptf`           | loop as long as a condition holds true         |
| `count`            | the number of times a loop has run             |
| `//`               | integer division                               |
| `+<-`              | `+=`                                           |
| `-<-`              | `-=`                                           |
| `++`               | increment                                      |
| `--`               | decrement                                      |
| `and`              | boolean and                                    |
| `or`               | boolean or                                     |
| `not`              | boolean not                                    |
| `if`/`butif`/`but` | if/else statements |
| `add`              | add an element to an array |
| `remove`           | remove an element from an array |
| `\` | comment |
| `\.` `.\` | multi-line comment |
