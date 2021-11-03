![](logo.svg)
# Lazuli

Lazuli is my attempt at making a working high-level programming language.

Lazuli is planned to be:
- [x] Compiled to Java Bytecode
- [ ] Turing-complete
- [x] Statically typed
- [ ] Everything is an expression (everything can be assigned, see Features)
- [ ] Type Oriented/Based in set theory (rather than classes there are Types that can be subsetted)
- [ ] Self-hosted (Java is used only as an initial bootstrap, once the language is mature enough I hope to rewrite it in Lazuli)

## Features

### Loops

Simple programs that print numbers from 0 to 99 in an ascending order:

```javascript
for (Int N = 0 to 100) {
    print(N)
}
```

```javascript
Int N = 0
while (N < 100) {
    print(N)
    N = N + 1
}
```

Implemented:
   - [x] simulation
   - [x] compilation

### Guards

Lazuli features **Guards** where an expression can be defined for a specific **Subset** of its full domain. 

For example with Variables:

```javascript
Int i | (i mod 2 == 0) = 0 //Variable i can only contain the set of even integers
...
i = 3 //runtime error
```

This can be combined with for expressions to loop over only a **specific subset** of the guard:

```javascript
for (Int N = 0 to 10 | N % 2 == 0) {
    print(N) //prints 0, 2, 4, 6, 8
}
```

Implemented:
   - [x] simulation
   - [x] compilation

### Everything is an expression

In Lazuli, everything is an expression and so everything can be assigned. This means instead of ternary expressions, an if expression can be assigned to a Intiable:

```javascript
Int i = if (x > y) 1 else 2
```
If x > y then i will be assigned the value 1, else 2. If the body of the if is multiline, only the last line will be returned:

```javascript
Int i = if (x > y) {
    x = x + 1
    1
} else {
    y = y + 1
    2
}
```
This will assign i to either 1 or 2 with the side effect of incrementing either x or y

Implemented:
   - [x] simulation
   - [x] compilation
   
This extends to loops as well, whose return type is an `Array`:

```javascript
//Assigns x to an Array of Ints from 0 to 4
IntArray ns = for (Int N = 0 to 5) {
    N
}
print(ns) //prints [0, 1, 2, 3, 4]
```

These act as normal for loops and can contain multiple lines, as with if expressions the last line of the body is assigned:

```javascript
//Assigns x to an Array of the squares of all even numbers from 0 to 99
IntArray xs = for (Int N = 0 to 100 if N mod 2 == 0) {
    N * N
}
```

Implemented:
   - [ ] simulation
   - [ ] compilation
   
 ## Functions
 
 Functions so far are pretty much like any other C-like language:
 
 ```javascript
 Int sum(Int a, Int b) {
     return a + b
 }
 
 print(sum(2, 3)) //prints 5
 ```
 
 Functions can also take guards on their parameters:
 
 ```javascript
 Int isqrt(Int x | x >= 0) { //Attempting to call isqrt with a negative number will throw an error

     Int i = 1
     Int result = 1
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
   
For more see the examples and tests directory.
   
## Planned Features

- Primitive types like `Int`, `Real` `Num`, `Bool` and `Str` 
    - [x] Int 
    - [x] Bool 
    - [ ] Real 
    - [ ] Num
    - [ ] Str
    
- Custom Type system
- Type subset system

## Acknowledgements

The architecture of this compiler is heavily based on the one built by Immo Landwerth in his Building a Compiler series:
- [Youtube](https://www.youtube.com/c/ImmoLandwerth)
- [Twitter](https://twitter.com/terrajobst)
- [Building a Compiler](https://www.youtube.com/playlist?list=PLRAdsfhKI4OWNOSfS7EUu5GRAVmze1t2y)