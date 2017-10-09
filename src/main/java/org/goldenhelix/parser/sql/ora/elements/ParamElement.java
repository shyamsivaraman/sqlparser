package org.goldenhelix.parser.sql.ora.elements;

import org.goldenhelix.parser.sql.ora.misc.ElementType;
import org.goldenhelix.parser.sql.ora.misc.ElementType.COL_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ElementType.PARAM_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class ParamElement implements QueryElement_I {

	private String m_SqlPart;
	private PARAM_TYPE m_ParamType;
	
	private QueryElement_I m_Parent;
	
	private String m_ParamValue;
	private QueryElement_I m_SubElement;
	
	public ParamElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part;
		this.m_Parent = parent;
	}
	
	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		if(this.m_ParamType == null)
			throw new ParseException("A param type is not specified for this element");

		switch(this.m_ParamType) {
		case CONST_PARAM:
		case PLACEHOLDER_PARAM:
		case STAR_PARAM:
			this.m_ParamValue = this.m_SqlPart.trim();
			break;
		case FUNC_PARAM:
			this.m_SubElement = createFuncSubElement();
			this.m_SubElement.accept(visitor);
			break;
		case COL_PARAM:
			this.m_SubElement = createColSubElement();
			this.m_SubElement.accept(visitor);
			break;
		}
		
		visitor.visit(this);
	}
	
	public void setParamType(PARAM_TYPE type) {
		this.m_ParamType = type;
	}
	
	public PARAM_TYPE getParamType() {
		return this.m_ParamType;
	}

	public QueryElement_I getParent() {
		return this.m_Parent;
	}
	
	public String getParamValue() {
		return this.m_ParamValue;
	}
	
	public QueryElement_I getSubElement() {
		return this.m_SubElement;
	}
	
	private QueryElement_I createColSubElement() throws ParseException {
		ColumnElement ce = new ColumnElement(m_SqlPart, this);
		COL_TYPE colType = ElementType.getInstance().evaluateColumnType(m_SqlPart);
		ce.setColType(colType);
		
		return ce;
	}
	
	private FunctionElement createFuncSubElement() {
		FunctionElement fe = new FunctionElement(this.m_SqlPart, this);
		return fe;
	}
	
	public String toString() {
		if(this.m_ParamType == PARAM_TYPE.FUNC_PARAM)
			return "[" + this.m_ParamType + "] : " + this.m_ParamValue;
		return "";
	}

	@Override
	public String getSqlPart() {
		return this.m_SqlPart;
	}
}
