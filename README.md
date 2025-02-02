## Formula Syntax
- Write the desired formula in `input.txt`.
- Supported connectives:
  - `AND`
  - `OR`
  - `->` (implication)
  - `<->` (biconditional)
  - `!` (negation)
- Connectives require square brackets `[]` for order of operations:
  - Example: `[[![A]] AND [B]] -> [C]`
- If a formula contains only `AND`, brackets `[]` are optional:
  - Example: `A AND B AND C`
- **NB:** Don't use `(`, `)` outside of functions and predicates :
  - Example: `[a=b] OR [R(d)]`

## Running the Program
Navigate to the project folder and run the following command:
```sh
java -cp bin Main
```

### If the command does not work:
On Linux, compile the project and create an executable by running:
```sh
javac -d bin **/*.java
```
Then, execute the program using:
```sh
java -cp bin Main
```

## Notes
- Ensure `input.txt` is in the correct format before running the program.
- Java must be installed on your system to compile and run the program.
- The `bin` directory should exist to store compiled files.

