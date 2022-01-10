# ![](EtruscanR-04.svg) Rasna 

Rasna is my attempt at making a working programming language.

Rasna is planned to be:
- [x] Compiled to Java Bytecode
- [ ] Compiled to LLVM
- [ ] Turing-complete
- [x] Statically typed
- [ ] As safe at runtime as it can be
- [x] Everything is an expression (everything can be assigned, see Features)
- [ ] Self-hosted (Java is used only as an initial bootstrap, once the language is mature enough I hope to rewrite it in Rasna)

## Features

### Loops

Simple programs that print numbers from 0 to 99 in an ascending order:

```julia
for (N: Int = 0 to 100) {
    print(N)
}
```

```julia
N: Int = 0
while (N < 100) {
    print(N)
    N = N + 1
}
```

Implemented:
   - [x] simulation
   - [x] compilation

### Guards

Rasna features **Guards** where an expression can be defined for a specific **Subset** of its full domain. 

For example with Variables:

```julia
i: Int | (i mod 2 == 0) = 0 //Variable i can only contain the set of even integers
...
i = 3 //runtime error
```

This can be combined with for expressions to loop over only a **specific subset** of the guard:

```julia
for (N: Int = 0 to 10 | N % 2 == 0) {
    print(N) //prints 0, 2, 4, 6, 8
}
```

Implemented:
   - [x] simulation
   - [x] compilation

### Everything is an expression

In Rasna, everything is an expression and so everything can be assigned. This means instead of ternary expressions, an if expression can be assigned to a Intiable:

```julia
i: Int = if (x > y) 1 else 2
```
If x > y then i will be assigned the value 1, else 2. If the body of the if is multiline, only the last line will be returned:

```julia
i: Int = if (x > y) {
    x = x + 1
    1
} else {
    y = y + 1
    2
}
```
This will assign i to either 1 or 2 with the side effect of incrementing either x or y

Implemented:
   - [ ] simulation
   - [x] compilation
   
This extends to loops as well, whose return type is an `Array`:

```julia
//Assigns x to an Array of Ints from 0 to 4
ns: Int[] = for (N: Int= 0 to 5) {
    N
}
print(ns) //prints [0, 1, 2, 3, 4]
```

These act as normal for loops and can contain multiple lines, as with if expressions the last line of the body is assigned:

```julia
//Assigns x to an Array of the squares of all even numbers from 0 to 99
xs: Int = for (N: Int= 0 to 100 if N mod 2 == 0) {
    N * N
}
```

Implemented:
   - [ ] simulation
   - [x] compilation
   
 ## Functions
 
 Functions so far are pretty much like rust or go:
 
 ```julia
 fn sum(a: Int, b: Int): Int {
     return a + b
 }
 
 print(sum(2, 3)) //prints 5
 ```
 
 Functions can also take guards on their parameters:
 
 ```julia
 fn isqrt(Int x | x >= 0): Int { //Attempting to call isqrt with a negative number will throw an error

     i: Int= 1
     result: Int = 1
     while (result <= x) {
         i = i + 1
         result = i * i
     }
     return i - 1
 }
 ```
 
Implemented:
   - [x] simulation
   - [x] compilation
   
 ## Structs
 
 Structs are no more than structured data:
 
 ```julia
 struct V3R {
     x: Real
     y: Real
     z: Real
 }
 ```
 
 They are initialised with a constructor syntax taking arguments in the order they were defined:
 
 ```julia
 v3: V3R = V3R(1.0, 2.0, 3.0)
 ```
 
 Alternatively for assignments only, a literal syntax can be used:
 
 ```julia
  v3: V3R = {1.0, 2.0, 3.0}
  ```
  
 This is not yet supported elsewhere due to there being no type information, 
 hopefully this can be improved in future to allow literals to be passed as function arguments.
 
 Unlike languages like c++ and Rust, structs cannot contain member functions. 
 Once defined, a struct can be used in the return types and arguments of functions just like any other type:
 
 ```julia
 fn mag3(v: V3R): Real {
     return sqrt(v.x * v.x + v.y * v.y + v.z * v.z)
 }
 ```
 
 ## Tuples

Tuples are immutable structures that contain a fixed number of values, each with their own type.

They can be useful when a method may need to return multiple values, although the types returned are not known at runtime (Yet).

They can be constructed literally like so:

```julia
t: Tuple = ("Hello", 1, false)
```

A tuple of one element should be constructed with a trailing comma, like so:

```julia
singleton: Tuple = ("Hello",)
```

Values can be accessed by their position:

```julia
print(t[0]) //will print "Hello"
```

or iterated through:

```julia
for (v: Any in t) {
    print(v)
}
```

As shown here, the return type for accessing a tuple is `Any` as the exact type is unknown at runtime, hopefully in future this will be fixed

For more see the examples and tests directory.
   
## Planned Features

- Pattern matching and type decomposition
- Custom type system
- Type subset system
- REPL

## Acknowledgements

The architecture of this compiler is heavily based on the one built by Immo Landwerth in his Building a Compiler series:
- [Youtube](https://www.youtube.com/c/ImmoLandwerth)
- [Twitter](https://twitter.com/terrajobst)
- [Building a Compiler](https://www.youtube.com/playlist?list=PLRAdsfhKI4OWNOSfS7EUu5GRAVmze1t2y)