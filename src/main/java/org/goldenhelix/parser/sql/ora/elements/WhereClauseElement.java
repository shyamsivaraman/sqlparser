package org.goldenhelix.parser.sql.ora.elements;

import java.util.List;

import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.misc.ParserHelper;
import org.goldenhelix.parser.sql.ora.misc.SQLPart;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class WhereClauseElement implements QueryElement_I {

	private SQLPart m_SqlPart;
	private SQLElement m_Parent;
	
	public WhereClauseElement(String part, SQLElement parent) {
		this.m_SqlPart = new SQLPart(part.toLowerCase(), 0);
		this.m_Parent = parent;
	}

	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		//Get the outermost part - lookahead by 'group by/ order by' or 'end of string'
		m_SqlPart = ParserHelper.getInstance().findOuterPartForWhereClause(m_SqlPart.getPart(), m_SqlPart.getPart(), 0, 0, new String[]{"group by","order by"}, false);
		
		//Extract the table names, select based tables and aliases
		List<String> subParts = ParserHelper.getInstance().generateWhereClauseParts(m_SqlPart.getPart());
		
		//Classify parts in elements
		QueryElement_I[] subElems = this.classifySubElements(subParts);
		
		//Send the visitor in
		this.sendVisitorDeep(subElems, visitor);
		
		visitor.visit(this);
	}
	
	public QueryElement_I getParent() {
		return this.m_Parent;
	}
	
	public String getSqlPart() {
		return this.m_SqlPart.getPart();
	}
	
	private void sendVisitorDeep(QueryElement_I[] elems, QueryVisitor_I visitor) throws ParseException {
		for(QueryElement_I q : elems) {
			q.accept(visitor);
		}
	}
	
	private QueryElement_I[] classifySubElements(List<String> parts) throws ParseException {
		int i=0;
		QueryElement_I[] qes = new QueryElement_I[parts.size()];
		
		for(String part : parts) {
			ConditionElement te = new ConditionElement(part, this);
			qes[i++] = te;
		}
		
		return qes;
	}
}
