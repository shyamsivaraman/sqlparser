package org.goldenhelix.parser.sql.ora.visitor;

import org.goldenhelix.parser.sql.ora.elements.ConditionElement;
import org.goldenhelix.parser.sql.ora.elements.ExpressionElement;
import org.goldenhelix.parser.sql.ora.elements.ListElement;
import org.goldenhelix.parser.sql.ora.elements.ParamElement;
import org.goldenhelix.parser.sql.ora.elements.QueryElement_I;
import org.goldenhelix.parser.sql.ora.misc.ElementType.EXPR_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ElementType.PARAM_TYPE;

public class ParsingVisitor implements QueryVisitor_I {

	private int count = 0;
	
	@Override
	public void visit(QueryElement_I element) {
		if(element instanceof ParamElement) {
			ParamElement p = (ParamElement) element;
			if(p.getParamType() == PARAM_TYPE.PLACEHOLDER_PARAM) {
				QueryElement_I parent = p.getParent();
				System.out.println("Function param: " + (++count) + ": " + p.getParamType() + 
						" [Parent: " + parent.getClass().getCanonicalName() + "]");
			}
		} else if(element instanceof ExpressionElement) {
			ExpressionElement e = (ExpressionElement) element;
			String colName = "";
			if(e.getExpressionType() == EXPR_TYPE.PLACEHOLDER_EXPR) {
				QueryElement_I parent = e.getParent();
				if(parent instanceof ConditionElement) {
					colName = this.getColumnName(parent, e);
				} else if(parent instanceof ListElement) {
					ListElement le = (ListElement)parent;
					colName = this.getColumnName(le.getParent(), le);
				}
				
				System.out.println("Expr param: " + (++count) + ": " + e.getExpressionType() + 
						" [Parent: " + parent.getClass().getCanonicalName() + "], [Col Name: " + colName + "]");
			}
		}
	}
	
	private String getColumnName(QueryElement_I parent, QueryElement_I e) {
		String colName = null;
		ConditionElement ce = (ConditionElement)parent;
		QueryElement_I otherOperand = ce.getOtherOperand(e);
		if(otherOperand instanceof ExpressionElement) {
			ExpressionElement ee = (ExpressionElement)otherOperand;
			if(ee.getExpressionType() == EXPR_TYPE.COL_EXPR) {
				colName = ee.getExpressionVaue();
			}
		}
		
		return colName;
	}
}
