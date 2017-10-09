package org.goldenhelix.parser.sql.ora.elements;

import java.util.List;

import org.goldenhelix.parser.sql.ora.elements.JoinClauseElement.TOKEN_SELECTION;
import org.goldenhelix.parser.sql.ora.misc.ElementType;
import org.goldenhelix.parser.sql.ora.misc.ElementType.TABLE_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.misc.ParserHelper;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class JoinTableElement implements QueryElement_I {

	private String m_SqlPart;
	private QueryElement_I m_Parent;
	
	private QueryElement_I m_CurrentTableElem;
	private QueryElement_I m_ConditionElement;		//Condition (IN/ USING) clause
	private JoinTableElement m_NextJoinTable;
	
	public JoinTableElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part.toLowerCase();
		this.m_Parent = parent;
	}
	
	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		//Get parts by "on" or "using" keyword
		List<String> subPartsByJoin = ParserHelper.getInstance().generateJoinClauseElements(m_SqlPart, TOKEN_SELECTION.ON_KEYWORD);
		
		//Classify first part (simple table/ query)
		this.m_CurrentTableElem = this.classifyTableElement(subPartsByJoin.get(0));
		
		//Classify the second condition part if present (in condition/ using list)
		if(subPartsByJoin.size() == 2)
			this.m_ConditionElement = this.classifyConditionElement(subPartsByJoin.get(1));
		
		//Send visitor to first & second part
		this.m_CurrentTableElem.accept(visitor);
		if(this.m_ConditionElement != null)
			this.m_ConditionElement.accept(visitor);
		
		visitor.visit(this);
	}

	@Override
	public String getSqlPart() {
		return this.m_SqlPart;
	}
	
	public QueryElement_I getParent() {
		return this.m_Parent;
	}

	public void setNextJoinTable(JoinTableElement joinTableElem) {
		this.m_NextJoinTable = joinTableElem;
	}
	
	private QueryElement_I classifyTableElement(String part) throws ParseException {
		TABLE_TYPE tableType = ElementType.getInstance().evaluateJoinTableType(part);
		
		TableElement te = new TableElement(part, this);
		te.setTableType(tableType);
		
		return te;
	}

	private QueryElement_I classifyConditionElement(String part) {
		OnClauseElement oce = new OnClauseElement(part, this);
		return oce;
	}
}
