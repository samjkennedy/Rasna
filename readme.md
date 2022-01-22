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

### Variables

Variables in Rasna are declared with the syntax:

```
[identifier]: ([Type]) = [initialiser]
```
The `[Type]` is optional if the compiler can determine the type from the initialiser, which it should always be able to.
This results in a go-style declaration syntax when omitting the type:

```rust
    //These two are equivalent
    i: Int = 0
    j := 0
```

If a variable is declared without an initialiser, a type must be provided:

```rust
    i      //Will not compile
    i: Int //Will compile
```

### Loops

Simple programs that print numbers from 0 to 99 in an ascending order:

```rust
for (N: Int = 0 to 100) {
    print(N)
}
```

```rust
N: Int = 0
while (N < 100) {
    print(N)
    N = N + 1
}
```

### Guards

Rasna features **Guards** where an expression can be defined for a specific **Subset** of its full domain. 

For example with Variables:

```rust
i: Int | (i mod 2 == 0) = 0 //Variable i can only contain the set of even integers
...
i = 3 //runtime error
```

This can be combined with for expressions to loop over only a **specific subset** of the guard:

```rust
for (N: Int = 0 to 10 | N % 2 == 0) {
    print(N) //prints 0, 2, 4, 6, 8
}
```

### Everything is an expression

In Rasna, everything is an expression and so everything can be assigned. This means instead of ternary expressions, an if expression can be assigned to a Intiable:

```rust
i: Int = if (x > y) 1 else 2
```
If x > y then i will be assigned the value 1, else 2. If the body of the if is multiline, only the last line will be returned:

```rust
i: Int = if (x > y) {
    x = x + 1
    1
} else {
    y = y + 1
    2
}
```
This will assign i to either 1 or 2 with the side effect of incrementing either x or y
   
This extends to loops as well, whose return type is `T[]`:

```rust
//Assigns x to an Array of Ints from 0 to 4
ns: Int[] = for (N: Int= 0 to 5) {
    N
}
print(ns) //prints [0, 1, 2, 3, 4]
```

These act as normal for loops and can contain multiple lines, as with if expressions the last line of the body is assigned:

```rust
//Assigns x to an Array of the squares of all even numbers from 0 to 99
xs: Int = for (N: Int= 0 to 100 if N mod 2 == 0) {
    N * N
}
```
   
 ### Functions
 
 Functions so far are pretty much like rust or go:
 
 ```rust
 fn sum(a: Int, b: Int): Int {
     return a + b
 }
 
 print(sum(2, 3)) //prints 5
 ```
 
 Functions can also take guards on their parameters:
 
 ```rust
 fn isqrt(x: Int | x >= 0): Int { //Attempting to call isqrt with a negative number will throw an error

     i: Int= 1
     result: Int = 1
     while (result <= x) {
         i = i + 1
         result = i * i
     }
     return i - 1
 }
 ```
   
 ### Structs
 
 Structs are no more than structured data:
 
 ```rust
 struct V3R {
     x: Real
     y: Real
     z: Real
 }
 ```
 
 They are initialised with a special constructor syntax taking arguments in the order they were defined:
 
 ```rust
 v3: V3R = V3R{1.0, 2.0, 3.0}
 ```
 
 Unlike languages like c++ and Rust, structs cannot contain member functions. 
 Once defined, a struct can be used in the return types and arguments of functions just like any other type:
 
 ```rust
 fn mag3(v: V3R): Real {
     return sqrt(v.x * v.x + v.y * v.y + v.z * v.z)
 }
 ```
 
 ### Uniform Function Call Syntax
 
 Like languages such as rust and D, Rasna supports [UFCS](https://en.wikipedia.org/wiki/Uniform_Function_Call_Syntax) instead of traditional OOP.
 
 Any function defined with a type as the first argument can instead be called on that type, allowing chaining of methods:
 
 ```rust
 struct Vector {
     x: Int
     y: Int
 }
 
 fn add(a: Vector, b: Vector): Vector {
     return Vector(a.x + b.x, a.y + b.y)
 }
 
 fn mul(v: Vector, s: Int): Vector {
     return Vector(v.x * s, v.y * s)
 }
 
 fn main() {
     v1: Vector = {1, 2}
     v2: Vector = {3, 4}
 
     //all the following are correct and equivalent
     v3: Vector = add(v1, v2)
     v4: Vector = v1.add(v2)
     v5: Vector = v2.add(v1)
     
     //Methods can be chained a la OOP
     v6: Vector = v2.add(v1).add(v2).mul(5)
 }
 ```
 
 ### Tuples

Tuples are immutable structures that contain a fixed number of values, each with their own type.

They can be constructed literally like so:

```rust
    t: (String, Int, Bool) = ("Hello", 1, false)
```

A tuple of one element should be constructed with a trailing comma, like so:

```rust
    singleton: (String) = ("Hello",)
```

Values can be accessed by their position:

```rust
    print(t.0) //will print "Hello"
```

### Pass by reference 

A function can accept its arguments by reference with the `ref` keyword. Any callers will also have to provide the parameter with the `ref` keyword.

```rust
fn inc(ref i: Int) {
    i++   
}

fn main() {
    i: Int = 0
    inc(ref i)
    print(i) //prints 1
}
```

When calling with UFCS, the `->` operator should be used instead of `.` to indicate a pass by reference:

```rust
    i: Int = 0
    i->inc()
    print(i) //prints 1
```

### Enums

Enums are declared similarly to structs, but the members are simple identifiers:

```rust
enum Color {
    Red
    Green
    Blue
}
```

Once declared, they can be used like any type. For now members must be qualified with their type:

```rust
    c := Color.Red
```

The variable `c` will be of the type `Color`, which compiles down to a const i32. As a result enums can be treated like Ints:

```rust
    //Prints 0, 1, 2
    for (c: Color = Color.Red to Color.Blue) {
        print(c)
    }
    
    if (Color.Red < Color.Green) { //Evaluates to true
        ...
```

## Acknowledgements

The architecture of this compiler is heavily based on the one built by Immo Landwerth in his Building a Compiler series:
- [Youtube](https://www.youtube.com/c/ImmoLandwerth)
- [Twitter](https://twitter.com/terrajobst)
- [Building a Compiler](https://www.youtube.com/playlist?list=PLRAdsfhKI4OWNOSfS7EUu5GRAVmze1t2y)