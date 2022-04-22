# Welcome to the Sigma programming language, a language for sigmas only.

This language was developed by Dean Cureton for the spring 2022 Westminster Honors Programming Languages class.

# Variables

## Types

Sigma is unique in that it uses a single type for every number where other languages would use multiple. For example, `int`, `double`, `long`, and `float` would all use the same type, `num`, in Sigma. This makes arithmetic far easier and typing between variables less of an issue.  
Other types of variables in Sigma include `str` for string, `tf` for bool, `arr` for array, and `var` for a generic variable. (However, these actual type names are rarely used since types are inferred for variable declarations.) Finally, arrays in Sigma are dynamic and do not have a single type. For example, a single array can include both `num`s and `str`s and will not have a fixed size.

## Declaring, Assigning, and Initializing Variables

Declaring a variable in Sigma is easy:
```
var x‼️
```
Begin with the keyword `var`, type the variable name, and end the line with the double exclamation point emoji (representing a semicolon in most other languages). To initialize a variable, do this:
```
var x <- "This is a string."‼️
```
Begin with the keyword `var`, type the variable name, use the `<-` assignment symbol, type the content of the variable, and end the line with the double exclamation point emoji.

To assign a value to `x` outside of declaration, the `var` keyword would be left out:
```
x <- 3‼️
```

## Casting Types

To convert between types in Sigma, simply call their types as a function (see "Calling Functions" below). As an example, consider the following block of code:
```
var x <- true‼️
log { num {x} }‼️
```
This block of code would output `1`.

# Functions

## Defining Functions

Defining functions in Sigma is very similar to regular variable declaration, except with different keywords. Let's start with an example:
```
func x <- arg1, arg2, arg3 »
    log { { arg1 + arg2 + arg3 } }‼️
«‼️
```
This is a simple function that takes in 3 arguments and prints their values added together. As you can see, the `func` keyword denotes the creation of a function in Sigma. The arrow sign signifies assignment. Next, the arguments are listed separated by commas. Then, a `»` character denotes the beginning of the main block of the function. Finally, close the function declaration with the `«` character and a double exclamation point emoji.

To return a value from a function, place the value as the last statement in its body. For example:
```
func toString <- arg »
    str { arg }‼️
«
```
This function would return the value of `arg` converted to a string.

## Calling Functions

To call the function `x` defined above, we would simply write:
```
x {1, 2, 3}‼️
x {4, 5, 6}‼️
```
This block of code would log 6, then 15.

# Comparators

We don't like equal signs in Sigma. If you type one, Sigma will throw an error. So, to check whether two things are equal in Sigma, use the `?` symbol. To check whether two variables have the same type, use the `??` symbol. To check if two numbers are within 5% of each other, use the `~` symbol (approximately equal). Use the `>` and `<` symbols for greater than and less than like in any other language. `>?` and `<?` also work as regular, but `≥` and `≤` are also supported by Sigma. In addition, adding the `!` symbol before `?`, `??`, or `~` checks if the opposite is true.

# Loops

Looping in Sigma is very simple. For loops in Sigma are quite simple, using much of the same notation as most other languages:
```
for {var i <- 0‼️ i <? 5‼️ ++i} »
    \ code
«
```
Foreach loops are implemented as such:
```
foreach {var i of array} »
    \ code
«
```
While loops are instead `when`:
```
when {2 ? 2} »
    \ code
«
```

Besides for and while loops, Sigma supports `loop` as a simpler version of `for`. `loop` takes in one number argument (if the argument is not an integer it rounds down) and loops the block inside the amount of times as the argument. In addition, inside all types of loops, the keyword `count` is reserved to keep track of the amount of times the loop has run so far (starting at `0`). Consider the following block of code:
```
loop {5} »
    log {count}‼️
«
```
This block of code would output:
```
0
1
2
3
4
```

Switch cases are also supported in Sigma using the `change` keyword. Use it like this:
```
change {variable} »
    case {1} »
        \ code block 1
    «
    case {2} »
        \ code block 2
    «
    case {3} »
        \ code block 3
    «
    nocase »
        \ code block 4
    «
«
```

# Operators

Sigma is an infix language, and all arithmetic operators work the same as in any other language (including `^` for exponentials and `%` for modulus). However, since every number has the same type, integer division is represented by the symbol `//`. Consider `5.6 // 2.5`. In this case, Sigma return `2` as a result.
The symbols `+=` and `-=` would be represented as `+<-` and `-<-`, and this same pattern applies for every other arithmetic operator. Incrementing and decrementing would be represented the same way as any other language, using `++` and `--`. However, these unary operators always become before the identifier.  
Boolean operators are implemented in Sigma using the keywords themselves. For example, `true and fals` would output `fals`. The boolean operators that Sigma supports are `and`, `or`, `not`, `nand`, `nor`, `xor`, and `xnor`.
Every combination of arithmetic operators/comparators and variables (excluding closures) is legal in Sigma, and the guidelines to how they are used can be found here: https://docs.google.com/spreadsheets/d/16W83w4X2BzXR-GdcyNDFXLXxx59ZKaN6VZhyUStNOJA/edit?usp=sharing.

# Conditionals

Conditionals in Sigma are treated very similarly to most other languages. The following block of code gives an illustration:
```
if {fals} »
    log {"Block 1"}‼️
« butif {true} »
    log {"Block 2"}‼️
« but »
    log {"Block 3"}‼️
«
```
This code would output `"Block 2"`. As you can see, if/else statements are now if/but statements, and everything else works relatively the same as in other languages.

# Arrays

As noted before, all arrays in Sigma are dynamic and do not have a set type. To declare an array in Sigma, we must write:
```
var x <- (1, true, "three")‼️
```
Arrays are surrounded by parentheses and elements are separated by commas (types are inferred). To access elements in an array, simply use the `get` function (indexing starts at 0):
```
log {{ num {get {x 0}} + num{get {x 1}} }}‼️
```
This code would output 2 (since `true` is equivalent to 1 when converted to a `num`).  
To assign values in an array, simply use the `set` function (using the array as the first argument, the content as the second, and the index as the third):
```
set {x, 2, 5}‼️
```
To add or remove elements from an array, call the `add` or `remove` functions:
```
add {x, 2, 3}‼️
remove {x, 0}‼️
```
The `add` function takes three parameters: `array`, `content` and `index`, and adds the content to the array at whatever index signified (if `index` is empty, it adds to the end). `remove` takes in `array` and `index` and simply removes the element of the array at the designated index.

# Comments and other characters

To comment something out in Sigma, just use the `\` symbol. For multi-line comments, use `\.` and `.\`. For whitespace in strings, `¬` (Option+L) is tab, and `ˇ` (Shift+Option+T) is new line.

# Built-in Functions

`log{x}` takes in one argument, outputs the argument to the console, and appends a line break to the end.
`random{num1 num2}` takes in two number arguments and outputs a random number between the two.
`abs{num}` calculates the absolute value of a number.
`floor{num}`, `ceil{num}`, and `round{num}` truncate numbers as in other languages.
`sqrt{num}` returns the square root of a number.
`min{num1, num2}` and `max{num1, num2}` calculate the minimum and maximum of two numbers as in other languages.
`lowercase{str}` converts a string to lowercase letters.
`uppercase{str}` converts a string to uppercase letters.
`getchar{str, num}` gets the character of a string.
`substring{str, num1, num2}` gets the substring of a string (inclusive for first number, exclusive for second number).
`length{x}` gets the length of a string or an array.
`get{arr, num}` gets the element of an array at `num`.
`set{arr, content, index}` sets the element of an array to `content` at `index`.
`add{arr, content, index}` adds a new element to an array.
`remove{arr, index}` removes an element from an array.
`contains{arr, value}` returns `true` or `fals` depending on if the array contains the value.
`num{value}`, `str{value}`, `tf{value}` and `arr{value}` returns the conversion of `value` to each type respectively.

# Keyword Overview

| Keyword            | Meaning                                        |
|--------------------|------------------------------------------------|
| `var` | variable type |
| `num`              | number type                                |
| `str`              | string type                                |
| `tf`               | boolean type                               |
| `true` and `fals` | true and false booleans |
| `arr`              | array type |
| `nothing` | null type |
| `func`             | function type                                      |
| `for` | for loop |
| `foreach` | foreach loop |
| `of` | of keyword in foreach loop |
| `when` | while loop |
| `loop` | set number loop |
| `count` | number of times a loop has run |
| `change` | switch statement |
| `case` | case in a switch statement |
| `nocase` | default in a switch statement |
| `and`, `or`, `not` | regular boolean operators |
| `nand`, `nor`, `xor`, `xnor` | rare boolean operators |
| `if`, `butif`, `but` | conditional keywords |
