# Oracle SQL Parser (Non-validating)

A non-validating parser for Oracle SQL. The SQL input is parsed left to right by extracting the outermost constructs as parts (select clause, from list, where clause, etc.) and then recursively breaking each of them into a constant, table, column or a placeholder.

>> The logic is not as per the norm of lexing and parsing the input, but is a feed-forward type logic which keeps consuming a given input string until a certain set of conditions is satisfied.

## Parser Components
The parser is built with the 'Visitor Pattern' as reference, with target object of end-use being the visitor implementation. End use in this case is considered as listening to only the required parsing events while the input string is being parsed.
* Element objects - 
