package org.goldenhelix.parser.sql.ora.elements;

import java.util.List;

import org.goldenhelix.parser.sql.ora.misc.ElementType;
import org.goldenhelix.parser.sql.ora.misc.ElementType.COL_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.misc.ParserHelper;
import org.goldenhelix.parser.sql.ora.misc.SQLPart;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class ColumnsListElement implements QueryElement_I {

	private SQLPart m_SqlPart;
	private SQLElement m_Parent;
	
	public ColumnsListElement(String part, SQLElement parent) {
		this.m_SqlPart = new SQLPart(part.trim().toLowerCase(), 0);
		this.m_Parent = parent;
	}
	
	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		//Get the outermost part
		m_SqlPart = ParserHelper.getInstance().findOuterPart(m_SqlPart.getPart(), m_SqlPart.getPart(), 0, 0, "from", false);
		
		//Extract the columns, query_based_columns, functions from the columns section
		List<String> subParts = ParserHelper.getInstance().generateSubParts(m_SqlPart.getPart(), 0, 0);
		
		//Classify parts into appropriate element classes
		QueryElement_I[] subElems = this.classifySubElements(subParts);
		
		//Send the visitor in
		this.sendVisitorDeep(subElems, visitor);
		
		visitor.visit(this);
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
			COL_TYPE colType = ElementType.getInstance().evaluateColumnType(part);

			ColumnElement ce = new ColumnElement(part, this);
			ce.setColType(colType);
			
			qes[i++] = ce;
		}
		
		return qes;
	}
	
	public QueryElement_I getParent() {
		return this.m_Parent;
	}
	
	//TODO: Ensure that the SQL part of all elements are NOT trimmed
	public String getSqlPart() {
		return this.m_SqlPart.getPart();
	}
}
