# Welcome to the Sigma programming language, a language for sigmas only.

This language was developed by Dean Cureton for the spring 2022 Westminster Honors Programming Languages class.

# Variables

## Types

Sigma is unique in that it uses a single type for every number where other languages would use multiple. For example, `int`, `double`, `long`, and `float` would all use the same type, `num`, in Sigma. This makes arithmetic far easier and different typing between variables less of an issue.  
Other types of variables in Sigma include `str` for string (which also includes the same functionality as `char` in other languages), `tf` for bool, and `var` for a generic variable. Finally, arrays in Sigma are dynamic and do not have a single type. For example, a single array can include both `num`s and `str`s.

## Declaring, Assigning, and Initializing Variables

Declaring a variable in Sigma is easy: begin with the keyword `var`, type the variable name, and end the line with the double exclamation point emoji (representing a semicolon in most other languages). For example, to declare the variable `x`, you would type:
```
var x‼️
```
To assign a value to `x` at declaration, use the arrow sign before typing the actual content of the variable:
```
var x <- "This is a string."‼️
```
To assign a value to `x` outside of declaration, the `var` keyword would be left out:
```
x <- 3‼️
```

## Casting Types

To convert between types in Sigma, simply use a period. As an example, consider the following block of code:
```
var x <- true‼️
log {x.num}‼️
```
This block of code would output `1`.

# Functions

## Defining Functions

Defining functions in Sigma is very similar to regular variable declaration, except with different keywords. Let's start with an example:
```
func x <- arg1 arg2 [arg3] »
    log {arg1.num + arg2.num + arg3.num}‼️
    take {nothing}‼️
«‼️
```
This is a simple function that takes in 3 arguments (one of which is optional, denoted with square brackets) and adds their number values together. As you can see, the `func` keyword denotes the creation of a function in Sigma. The arrow sign signifies assignment. Next, the arguments are listed separated by spaces, then a `»` character denotes the beginning of the main block of the function. In this case, since the function doesn't return a value, we type `take nothing`, but if it did, we would `take` whatever value we wished to return. Finally, close the function declaration with the `«` character and a double exclamation point emoji.

## Calling Functions

To call this function `x`, we would simply write:
```
x {1 2 3}‼️
x {4 5}‼️
```
This block of code would log 6, then 9.

# Comparators

We don't like equal signs in Sigma. If you type one, Sigma will throw an error. So, to check whether two things are equal in Sigma, use the `?` symbol. To check whether two variables have the same type, use the `??` symbol. To check if two numbers are within 5% of each other, use the `~` symbol (approximately equal). Use the `>` and `<` symbols for greater than and less than like in any other language. `>?` and `<?` also work as regular, but `≥` and `≤` are also supported by Sigma. In addition, adding the `!` symbol before `?`, `??`, or `~` checks if the opposite is true.

# Loops

Looping in Sigma is very simple. For loops in Sigma are quite simple, using much of the same notation as most other languages:
```
for {var i <- 0‼️ i <? 5‼️ ++i} »
    \ code
«
```
Foreach loops are implemented as such (still using the `for` keyword):
```
for {var i of array} »
    \ code
«
```
While loops are instead `when`:
```
when {2 ? 2} »
    \ code
«
```

Besides for and while loops, Sigma supports `loop` as a simpler version of `for`. `loop` takes in one number argument (if the argument is not an integer it rounds down) and loops the block inside the amount of times as the argument. In addition, inside loops, the keyword `count` is reserved to keep track of the amount of times the loop has run so far (starting at `0`). Consider the following block of code:
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
        fall {3}‼️
    «
    case {2} »
        \ code block 2
        fall {}‼️
    «
    case {3} »
        \ code block 3
        end {}‼️
    «
    nocase »
        \ code block 4
        end {}‼
    «
«
```
In this case, the `fall` function directs the code to run the case in the argument (or just jumps to the next case if no argument is inputted). The `end` function breaks out. If `variable` was 1, block 1 and block 3 would run. If it was 2, block 2 and block 3 would run. If it was 3, only block 3 would run. Finally, if it was 4, only block 4 would run.

# Operators

Sigma is an infix language, and all arithmetic operators work the same as in any other language (including `^` for exponentials and `%` for modulus). However, since every number has the same type, integer division is represented by the symbol `//`. Consider `5.6 // 2.5`. In this case, Sigma would round both `5.6` and `2.5` down to `5` and `2`, and then perform integer division, giving `2` as a result.
The symbols `+=` and `-=` would be represented as `+<-` and `-<-`, and this same pattern applies for every other arithmetic operator. Incrementing and decrementing would be represented the same way as any other language, using `++` and `--`. However, these unary operators always become before the identifier.  
Boolean operators are implemented in Sigma using the keywords themselves. For example, `true and fals` would output `fals`. The boolean operators that Sigma supports are `and`, `or`, `not`, `nand`, `nor`, `xor`, `xnor`, and `implies`.
Every combination of arithmetic operators/comparators and variables is legal in Sigma, and the guidelines to how they are used can be found here: https://docs.google.com/spreadsheets/d/16W83w4X2BzXR-GdcyNDFXLXxx59ZKaN6VZhyUStNOJA/edit?usp=sharing.

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
var x <- (1 true "three")‼️
```
Arrays are surrounded by parentheses and elements are separated by spaces (types are inferred) because Sigma will throw an error if commas exist in a file. To access elements in an array, simply use the `get` function (indexing starts at 0):
```
log {get {x 0}.num + get {x 1}.num}‼️
```
This code would output 2 (since `true` is equivalent to 1 when converted to a `num`).  
To assign values in an array, simply use the `set` function (using the array as the first argument, the content as the second, and the index as the third):
```
set {x 2 5}‼️
```
To add or remove elements from an array, call the `add` or `remove` functions:
```
add {x 2 3}‼️
remove {x 0}‼️
```
The `add` function takes three parameters: `array`, `content` and `index` (optional), and adds the content to the array at whatever index signified (if `index` is empty, it adds to the end). `remove` takes in `array` and `index` and simply removes the element of the array at the designated index.

# Comments and other characters

To comment something out in Sigma, just use the `\` symbol. For multi-line comments, use `\.` and `.\`. For whitespace in strings, `¬` (Option+L) is tab, and `ˇ` (Shift+Option+T) is new line.

# Built-in Functions

`log{x}` takes in one argument, outputs the argument to the console, and appends a line break to the end.  
`logl{x}` takes in one argument and outputs the argument to the console with no line break.
`random{num1 num2}` takes in two number arguments and outputs a random number between the two.
`abs{num}` calculates the absolute value of a number.
`floor{num}`, `ceil{num}`, and `round{num}` truncate numbers as in other languages.
`min{num1 num2}` and `max{num1 num2}` calculate the minimum and maximum of two numbers as in other languages.
`lowercase{str}` converts a string to lowercase letters.
`uppercase{str}` converts a string to uppercase letters.
`getChar{str num}` gets the character of a string.
`substring{str num1 num2}` gets the substring of a string (inclusive for first number, exclusive for second number).
`length{x}` gets the length of a string or an array.
`get{arr num}` gets the element of an array at `num`.
`set{arr content index}` sets the element of an array to `content` at `index`.
`add{arr content index}` adds a new element to an array.
`remove{arr index}` removes an element from an array.
`contains{arr value}` returns `true` or `fals` depending on if the array contains the value.
`take{value}` returns a value in a function.
`end{}` ends a loop or change case.
`fall{ind}` skips an iteration in a loop or jumps to a new case in a `change` statement.

# Keyword Overview

| Keyword            | Meaning                                        |
|--------------------|------------------------------------------------|
| `var` | variable type |
| `num`              | number type                                |
| `str`              | string type                                |
| `tf`               | boolean type                               |
| `arr`              | array type |
| `func`             | function type                                      |
| `for` | for loop |
| `foreach` | foreach loop |
| `when` | when loop |
| `loop` | loop loop |
| `of` | of keyword in foreach loop |
| `nothing` | null |
| `and`, `or`, `not` | regular boolean operators |
| `nand`, `nor`, `xor`, `xnor`, `implies` | rare boolean operators |
| `if`, `butif`, `but` | conditional keywords |
| `change`, `case`, `nocase` | change statement keywords |
