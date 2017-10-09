package org.goldenhelix.parser.sql.ora.elements;

import org.goldenhelix.parser.sql.ora.misc.ConditionParser;
import org.goldenhelix.parser.sql.ora.misc.ConditionParser.CONDITION_OP_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ElementType;
import org.goldenhelix.parser.sql.ora.misc.ElementType.EXPR_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class ConditionElement implements QueryElement_I {
	
	private String m_SqlPart;
	private QueryElement_I m_Parent;
	private ConditionParser.CONDITION_OP_TYPE m_OpType;
	
	private QueryElement_I m_LHSElem;
	private ElementType.EXPR_TYPE m_LHSType;
	
	private String m_SimpleOper;
	private String m_GrpOper;
	
	private QueryElement_I m_RHSElem;
	private ElementType.EXPR_TYPE m_RHSType;

	public ConditionElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part;
		this.m_Parent = parent;
	}

	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		//Parse the conditional expression
		ConditionParser.getInstance().parseCondition(this);
		
		//Send the visitor in
		this.sendVisitorDeep(visitor);
		
		visitor.visit(this);
	}
	
	public QueryElement_I getOtherOperand(QueryElement_I operand) {
		if(this.m_OpType == CONDITION_OP_TYPE.BINARY_EXPR) {
			return (operand == m_RHSElem) ? m_LHSElem : m_RHSElem;
		}
		
		return null;
	}
	
	public String getSimpleOperator() {
		return this.m_SimpleOper;
	}
	
	public void setSimpleOperator(String oper) {
		this.m_SimpleOper = oper;
	}
	
	public QueryElement_I getLHSElement() {
		return this.m_LHSElem;
	}
	
	public ElementType.EXPR_TYPE getLHSType() {
		return this.m_LHSType;
	}
	
	public void setLHSElement(QueryElement_I elem) {
		this.m_LHSElem = elem;
	}
	
	public void setLHSType(EXPR_TYPE type) {
		this.m_LHSType = type;
	}
	
	public QueryElement_I getRHSElement() {
		return this.m_RHSElem;
	}
	
	public ElementType.EXPR_TYPE getRHSType() {
		return this.m_RHSType;
	}
	
	public void setRHSElement(QueryElement_I elem) {
		this.m_RHSElem = elem;
	}
	
	public void setRHSType(EXPR_TYPE type) {
		this.m_RHSType = type;
	}
	
	public QueryElement_I getParent() {
		return this.m_Parent;
	}
	
	public String getSqlPart() {
		return this.m_SqlPart;
	}
	
	public void setConditionOpType(ConditionParser.CONDITION_OP_TYPE type) {
		this.m_OpType = type;
	}
	
	public ConditionParser.CONDITION_OP_TYPE getConditionOpType() {
		return this.m_OpType;
	}
	
	private void sendVisitorDeep(QueryVisitor_I visitor) throws ParseException {
		if(this.m_LHSElem != null)
			m_LHSElem.accept(visitor);
		if(this.m_RHSElem != null)
			m_RHSElem.accept(visitor);
	}
}
