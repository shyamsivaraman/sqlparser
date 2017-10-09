package org.goldenhelix.parser.sql.ora.misc;

import org.goldenhelix.parser.sql.ora.elements.ConditionElement;
import org.goldenhelix.parser.sql.ora.elements.ExpressionElement;
import org.goldenhelix.parser.sql.ora.elements.FunctionElement;
import org.goldenhelix.parser.sql.ora.elements.GroupedExprElement;
import org.goldenhelix.parser.sql.ora.elements.ListElement;
import org.goldenhelix.parser.sql.ora.elements.QueryElement_I;
import org.goldenhelix.parser.sql.ora.elements.SQLElement;
import org.goldenhelix.parser.sql.ora.misc.ElementType.EXPR_TYPE;

public class ConditionParser {
	
	public enum CONDITION_OP_TYPE {UNARY_NOT_EXPR, BINARY_EXPR, GROUPED_EXPR};
	
	private String[] m_SimpCompOpers = {"!=", "^=", "<>", ">=", "<=", "=", ">", "<", "in", "like"};
	private String[] m_GrpCompOpers = {"ANY", "SOME", "ALL"};
	
	private static ConditionParser m_Instance = new ConditionParser();
	
	private ConditionParser() { }
	
	public static ConditionParser getInstance() {
		return m_Instance;
	}
	
	public void parseCondition(ConditionElement elem) throws ParseException {
		String part = elem.getSqlPart();
		
		if(_isUnaryNot(part)) {
			/* Unary NOT: 
			 * SELECT * FROM employees WHERE NOT (job_id IS NULL) ORDER BY employee_id;
			 */
			elem.setConditionOpType(CONDITION_OP_TYPE.UNARY_NOT_EXPR);
			part = this._getUnaryExpression(part);
			
			ElementType.EXPR_TYPE exprType = ElementType.getInstance().evaluateExprType(part);
			QueryElement_I qElem = _classifyElement(part, exprType, elem);
			elem.setRHSElement(qElem);
			elem.setRHSType(exprType);
		} else if(_isGroupExpr(part)) {
			/*
			 * Grouped Expression:
			 * ... WHERE ... (x=y and a>b or ...) ...
			 */
			ElementType.EXPR_TYPE exprType = EXPR_TYPE.GROUP_EXPR;
			QueryElement_I lhsElem = _classifyElement(part, exprType, elem);
			elem.setConditionOpType(CONDITION_OP_TYPE.GROUPED_EXPR);
			
			elem.setLHSType(EXPR_TYPE.GROUP_EXPR);
			elem.setLHSElement(lhsElem);
		} else {
			/*
			 * Expression based on binary operators: 
			 * Arithmetic operators, In, Is, Is not, like, between 
			 */
			elem.setConditionOpType(CONDITION_OP_TYPE.BINARY_EXPR);
			
			String lhsPart = ParserHelper.getInstance().getOuterExpressionForCondition(part, part, 0, 0, m_SimpCompOpers, true);
			ElementType.EXPR_TYPE exprType = ElementType.getInstance().evaluateExprType(lhsPart);
			QueryElement_I lhsElem = _classifyElement(lhsPart, exprType, elem);
			elem.setLHSElement(lhsElem);
			elem.setLHSType(exprType);
			
			part = part.substring(lhsPart.length()+1);
			String operPart = this.getOperator(part);
			elem.setSimpleOperator(operPart);

			String rhsPart = part.substring(part.indexOf(operPart)+operPart.length());
			exprType = ElementType.getInstance().evaluateExprType(rhsPart);
			QueryElement_I rhsElem = _classifyElement(rhsPart, exprType, elem);
			elem.setRHSElement(rhsElem);
			elem.setRHSType(exprType);
		}
	}
	
	private String getOperator(String part) {
		part = part.trim();
		
		for(int i=0; i<this.m_SimpCompOpers.length; i++) {
			if(part.startsWith(this.m_SimpCompOpers[i]))
				return this.m_SimpCompOpers[i];
		}
		
		return "-";
	}
	
	private QueryElement_I _classifyElement(String part, ElementType.EXPR_TYPE exprType, QueryElement_I parent) {
		switch(exprType) {
		case QUERY_EXPR:
			SQLElement sqlElem = new SQLElement(part, parent);
			return sqlElem;
		case FUNC_EXPR:
			FunctionElement funcElem = new FunctionElement(part, parent);
			return funcElem;
		case LIST_EXPR:
			ListElement listElem = new ListElement(part, parent);
			return listElem;
		case GROUP_EXPR:
			part = _stripWrappingBrackets(part);
			GroupedExprElement gExprElem = new GroupedExprElement(part, parent);
			return gExprElem;
		case PLACEHOLDER_EXPR:
		case CONST_EXPR:
		case COL_EXPR:
			ExpressionElement expElem = new ExpressionElement(part, parent);
			expElem.setExpressionType(exprType);
			expElem.setExpressionValue(part.trim());
			return expElem;
		}
		
		return null;
	}
	
	private String _stripWrappingBrackets(String part) {
		part = part.trim();
		if(part.indexOf('(') == 0 && part.lastIndexOf(')') == part.length()-1) {
			return part.substring(1, part.length()-1);
		}
		
		return part;
	}
	
	private boolean _isUnaryNot(String part) {
		if(part.trim().startsWith("NOT "))
			return true;
		
		return false;
	}
	
	private String _getUnaryExpression(String part) {
		part = part.trim();
		part = part.substring(part.indexOf("NOT ")+4);
		
		return part;
	}
	
	private boolean _isGroupExpr(String part) {
		part = part.trim();
		return (part.indexOf('(') == 0 && part.lastIndexOf(')') == part.length()-1);
	}

}
