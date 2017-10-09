package org.goldenhelix.parser.sql.ora.elements;

import org.goldenhelix.parser.sql.ora.misc.ElementType.EXPR_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class ExpressionElement implements QueryElement_I {
	
	private String m_SqlPart;
	private EXPR_TYPE m_ExprType;
	
	private QueryElement_I m_Parent;

	//Value may be a constant, placeholder or column name (aliased/ non-aliased)
	private String m_ExprValue;
	
	public ExpressionElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part;
		this.m_Parent = parent;
	}

	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		visitor.visit(this);
	}
	
	public EXPR_TYPE getExpressionType() {
		return this.m_ExprType;
	}
	
	public void setExpressionType(EXPR_TYPE type) {
		this.m_ExprType = type;
	}
	
	public String getExpressionVaue() {
		return this.m_ExprValue;
	}
	
	public void setExpressionValue(String val) {
		this.m_ExprValue = val;
	}

	@Override
	public String getSqlPart() {
		return this.m_SqlPart;
	}
	
	public QueryElement_I getParent() {
		return this.m_Parent;
	}
}
