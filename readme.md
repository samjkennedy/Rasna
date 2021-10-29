#Bixbite

Bixbite is my attempt at making a working high-level programming language.

Bixbite is planned to be:
- [x] Compiled to Java Bytecode
- [ ] Turing-complete
- [ ] Statically typed
- [ ] Everything is an expression (everything can be assigned, see Examples)
- [ ] Type Oriented
- [ ] Self-hosted (Java is used only as an initial bootstrap, once the language is mature enough I hope to rewrite it in Bixbite)

##Features

###Loops

Simple programs that print numbers from 0 to 99 in an ascending order:

```javascript
for (var N = 0 to 100) {
    print(N)
}
```

```javascript
var N = 0
while (N < 100) {
    print(N)
    N = N + 1
}
```

Implemented:
   - [x] simulation
   - [ ] compilation

### Subset Notation

Bixbite features **Subset Notation** where an expression can be defined for a specific **Subset** of its full domain. 

For example with variables:

```javascript
var i : (i mod 2 == 0) = 0 //variable i can only contain the set of even integers
...
i = 3 //runtime error
```

This can be combined with for expressions to loop over only a **specific subset** of the range:

```javascript
for (var N = 0 to 10 : N % 2 == 0) {
    print(N) //prints 0, 2, 4, 6, 8
}
```

Implemented:
   - [x] simulation
   - [ ] compilation

### Everything is an expression

In Bixbite, everything is an expression and so everything can be assigned. This means instead of ternary expressions, an if expression can be assigned to a variable:

```javascript
var i = if (x > y) 1 else 2
```
If x > y then i will be assigned the value 1, else 2. If the body of the if is multiline, only the last line will be returned:

```javascript
var i = if (x > y) {
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
   - [ ] compilation
   
This extends to loops as well, whose return type is an `Array`:

```javascript
//Assigns x to an Array of Ints from 0 to 99
var x = for (var N = 0 to 100) {
    N
}
```

These act as normal for loops and can contain multiple lines, as with if expressions the last line of the body is assigned:

```javascript
//Assigns x to an Array of the squares of all even numbers from 0 to 99
var x = for (var N = 0 to 100 : N % 2 == 0) {
    N * N
}
```

Implemented:
   - [ ] simulation
   - [ ] compilation
   
##Planned Features

- Primitive types like `Int`, `Num`, `Str`, `Bool`, and `Array`
- Custom Type system
- Function declarations and calls

##Acknowledgements

The architecture of this compiler is heavily based on the one built by Immo Landwerth in his Building a Compiler series:

    - Youtube: https://www.youtube.com/c/ImmoLandwerth
    - Twitter: https://twitter.com/terrajobst
    - Building a Compiler: https://www.youtube.com/playlist?list=PLRAdsfhKI4OWNOSfS7EUu5GRAVmze1t2y