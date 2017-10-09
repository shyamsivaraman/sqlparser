package org.goldenhelix.parser.sql.ora.elements;

import java.util.List;

import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.misc.ParserHelper;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class OnClauseElement implements QueryElement_I {

	private String m_SqlPart;
	private QueryElement_I m_Parent;
	
	public OnClauseElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part.toLowerCase();
		this.m_Parent = parent;
	}
	
	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		List<String> subParts = ParserHelper.getInstance().generateWhereClauseParts(this.m_SqlPart);
		
		//Classify parts in elements
		QueryElement_I[] subElems = this.classifySubElements(subParts);
		
		//Send the visitor in
		this.sendVisitorDeep(subElems, visitor);
		
		visitor.visit(this);
	}

	@Override
	public String getSqlPart() {
		return this.m_SqlPart;
	}

	public QueryElement_I getParent() {
		return this.m_Parent;
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
