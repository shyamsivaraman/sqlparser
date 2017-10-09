package org.goldenhelix.parser.sql.ora.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ElementType {

	private static final ElementType m_Et = new ElementType();

	/* Type enumerations */
	
	/* SQL Statement types */
	public enum SQL_TYPE {SELECT_STMT, UPDATE_STMT, DELETE_STMT, UNKNOWN};
	/* Types of columns allowed in the SELECT clause */
	public enum COL_TYPE {SIMPLE_COLUMN, FUNC_COLUMN, QUERY_COLUMN, EXPR_COLUMN, CONST_COLUMN, STAR_COLUMN};
	/* Types of params allowed inside a function */
	public enum PARAM_TYPE {CONST_PARAM, FUNC_PARAM, COL_PARAM, PLACEHOLDER_PARAM, STAR_PARAM};
	/* Types of table constructs allowed inside a FROM clause */
	public enum TABLE_TYPE {SIMPLE_TABLE, QUERY_TABLE, JOIN_CLAUSE}
	/* Types of expressions allowed in the RHS/LHS of a given condition in the WHERE clause */
	public enum EXPR_TYPE {CONST_EXPR, COL_EXPR, PLACEHOLDER_EXPR, FUNC_EXPR, QUERY_EXPR, GROUP_EXPR, LIST_EXPR};
	/* Type of table allowed inside the FROM clause */
	public enum TABLE_LIST_TYPE {SIMPLE_TABLE_LIST, JOIN_TABLE_LIST};
	
	/* Evaluation Patterns */
	//v0.1 - (?i)^(?:(?:\s|\()*)(select)(.*) - G1=Type, G2=Rest of query
	//v0.2 - (?i)^(?:(?:\s|\()*)(?:(select)+\s(?:distinct)*)(.*)
	private Pattern pGetType = Pattern.compile("(?i)^(?:(?:\\s|\\()*)(?:(select)+\\s(?:distinct)*)(.*)");
	
	//(?i)^[\s\(\,]*([\w\.\-\_\d]+)(?:as|\s)*([\w\.\-\_\d]*?)[\s\,\)]*$ - G1=Column name, G2 = Alias
	private Pattern pSimpleCol = Pattern.compile("(?i)^[\\s\\(\\,]*([\\w\\.\\-\\_\\d]+)(?:as|\\s)*([\\w\\.\\-\\_\\d]*?)[\\s\\,\\)]*$");
	
	//(?i)^[\s\(\,]*(.+?)\s*[\(]+?
	private Pattern pFuncCol = Pattern.compile("(?i)^[\\s\\(\\,]*(.+?)\\s*[\\(]+?");
	
	//(?i)^\(+?(?:[\'\w\d\-\_\s\?\(\)\*]+?),(?:(?:[\s\'\w\d\-\_\?\(\)\,\*]+?)+?)\)+$
	private Pattern pListExpr = Pattern.compile("(?i)^\\(+?(?:[\\'\\w\\d\\-\\_\\s\\?\\(\\)\\*]+?),(?:(?:[\\s\\'\\w\\d\\-\\_\\?\\(\\)\\,\\*]+?)+?)\\)+$");
	
	//.*([\s]+?join[\s]+?).*?(?:(?:[\s]+?on[\s]+?)|(?:(?:(?!on).)*))
	private Pattern pJoinClause = Pattern.compile(".*([\\s]+?join[\\s]+?).*?(?:(?:[\\s]+?on[\\s]+?)|(?:(?:(?!on).)*))");

	private ElementType() { }
	
	public static ElementType getInstance() {
		return m_Et;
	}
	
	public TABLE_LIST_TYPE evaluateFromClauseType(String input) throws ParseException {
		
		return null;
	}
	
	public EXPR_TYPE evaluateExprType(String input) throws ParseException {
		input = input.trim();
		
		if(testQueryExpr(input)) return EXPR_TYPE.QUERY_EXPR;
		if(testFunctionExpr(input)) return EXPR_TYPE.FUNC_EXPR;
		if(testConstExpr(input)) return EXPR_TYPE.CONST_EXPR;
		if(testListExpr(input)) return EXPR_TYPE.LIST_EXPR;
		if(testPlaceholderExpr(input)) return EXPR_TYPE.PLACEHOLDER_EXPR;
		if(testColExpr(input)) return EXPR_TYPE.COL_EXPR;
		
		throw new ParseException("Unknown expression type input: " + input);
	}
	
	private boolean testListExpr(String input) {
		Matcher m = pListExpr.matcher(input);
		return m.matches();
	}
	
	private boolean testQueryExpr(String input) {
		return this.testQueryTable(input);
	}
	
	private boolean testFunctionExpr(String input) {
		return this.testFunctionParam(input);
	}
	
	private boolean testConstExpr(String input) {
		return this.testConstParam(input);
	}
	
	private boolean testPlaceholderExpr(String input) {
		return this.testPlaceholderParam(input);
	}
	
	private boolean testColExpr(String input) {
		return true;
	}
	
	public TABLE_TYPE evaluateJoinTableType(String input) throws ParseException {
		input = input.trim();
		
		if(testQueryTable(input)) return TABLE_TYPE.QUERY_TABLE;
		if(testSimpleTable(input)) return TABLE_TYPE.SIMPLE_TABLE;
		
		throw new ParseException("Unknown table type input: " + input);
	}
	
	public TABLE_TYPE evaluateTableType(String input) throws ParseException {
		input = input.trim();
		
		if(testJoinClause(input)) return TABLE_TYPE.JOIN_CLAUSE;
		if(testQueryTable(input)) return TABLE_TYPE.QUERY_TABLE;
		if(testSimpleTable(input)) return TABLE_TYPE.SIMPLE_TABLE;
		
		throw new ParseException("Unknown table type input: " + input);
	}
	
	private boolean testSimpleTable(String input) {
		return true;
	}
	
	private boolean testJoinClause(String input) {
		Matcher m = pJoinClause.matcher(input);
		return m.matches();
	}

	private boolean testQueryTable(String input) {
		String type = null;
		Matcher m = pGetType.matcher(input);
		
		if(!m.find()) {
			return false;
		}
		
		type = m.group(1);
		
		if(type == null) {
			return false;
		} else if(type.trim().equalsIgnoreCase("SELECT")) {
			return true;
		}
		
		return false;
	}

	public PARAM_TYPE evaluateParamType(String input) throws ParseException {
		input = input.trim();
		
		if(testPlaceholderParam(input)) return PARAM_TYPE.PLACEHOLDER_PARAM;
		if(testStarParam(input)) return PARAM_TYPE.STAR_PARAM;
		if(testConstParam(input)) return PARAM_TYPE.CONST_PARAM;
		if(testFunctionParam(input)) return PARAM_TYPE.FUNC_PARAM;
		if(testColParam(input)) return PARAM_TYPE.COL_PARAM;
		
		throw new ParseException("Unknown parameter type input: " + input);
	}
	
	private boolean testStarParam(String input) {
		return (input.length() == 1 && input.matches("\\*"));
	}

	private boolean testConstParam(String input) {
		input = input.trim();
		if(input.indexOf('\'') == 0 && input.lastIndexOf('\'') == input.length()-1) {
			return true;
		}
		
		return input.matches("\\d+?");
	}

	private boolean testPlaceholderParam(String input) {
		return (input.length() == 1 && input.matches("\\?"));
	}

	private boolean testColParam(String input) {
		return true;
	}

	private boolean testFunctionParam(String input) {
		Matcher m = pFuncCol.matcher(input);
		return m.find();
	}

	public COL_TYPE evaluateColumnType(String input) throws ParseException {
		if(testQueryColumn(input)) return COL_TYPE.QUERY_COLUMN;
		if(testSimpleColumn(input)) return COL_TYPE.SIMPLE_COLUMN;
		if(testFunctionColumn(input)) return COL_TYPE.FUNC_COLUMN;
		if(testStarParam(input)) return COL_TYPE.STAR_COLUMN;
		if(testExprColumn(input)) return COL_TYPE.EXPR_COLUMN;
		
		
		throw new ParseException("Unknown column type input: " + input);
	}
	
	public SQL_TYPE evaluateSQLType(String input) throws ParseException{
		String type = null;
		Matcher m = pGetType.matcher(input);
		
		if(!m.find()) {
			throw new ParseException("Incorrect sql input for SQL Type matching");
		}
		
		type = m.group(1);
		
		if(type == null) {
			throw new ParseException("Incorrect sql statement type: " + type);
		} else if(type.trim().equalsIgnoreCase("SELECT")) {
			return SQL_TYPE.SELECT_STMT;
		} else if(type.trim().equalsIgnoreCase("UPDATE")){
			return SQL_TYPE.UPDATE_STMT;
		} else if(type.trim().equalsIgnoreCase("DELETE")) {
			return SQL_TYPE.DELETE_STMT;
		} else {
			throw new ParseException("Incorrect sql statement type: " + type);
		}
	}
	
	private boolean testSimpleColumn(String input) {
		Matcher m = pSimpleCol.matcher(input);
		return m.find();
	}
	
	private boolean testFunctionColumn(String input) {
		Matcher m = pFuncCol.matcher(input);
		return m.find();
	}
	
	private boolean testExprColumn(String input) {
		return false;
	}

	private boolean testQueryColumn(String input) {
		try {
			this.evaluateSQLType(input);
		} catch (ParseException e) {
			return false;
		}
		
		return true;
	}

	public static void main(String args[]) {
		boolean b = ElementType.getInstance().testSimpleColumn("user.col");
		System.out.println(b);
	}
}
