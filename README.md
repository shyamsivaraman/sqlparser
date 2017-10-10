# Oracle SQL Parser (Non-validating)

A non-validating parser for Oracle SQL. The SQL input is parsed left to right by extracting the outermost constructs as parts (select clause, from list, where clause, etc.) and then recursively breaking each of them into a constant, table, column or a placeholder.

>> The logic is not as per the norm of lexing and parsing the input, but is a feed-forward type logic which keeps consuming a given input string until a certain set of conditions are satisfied.

## Parser Components
The parser is built with the 'Visitor Pattern' as reference, with target object of end-use being the visitor implementation. End use in this case is considered as listening to only the required parsing events while the input string is being parsed.
* Element objects - Object representing the nodes constructed while parsing the SQL
* Parser - Constructs the Element objects and forwards the visitor.
* Logic
>>Feed-forward parsing logic extracts the outermost construct of a desired type from the input.
>>The extracted part is then broken down into sub-parts using an appropriate tokenizing keyword.
>>Each broken sub-part is run through a classification regex and then converted to a an element.

## Pending
* Support for update, insert and delete statements
* Parsing logic for WITH, BETWEEN, IS ANY, EXISTS
* Code restructuring
