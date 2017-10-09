package org.goldenhelix.parser.sql.ora.elements;

import java.util.List;

import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.misc.ParserHelper;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class JoinClauseElement implements QueryElement_I {
	
	public enum TOKEN_SELECTION {JOIN_KEYWORD, ON_KEYWORD};

	private String m_SqlPart;
	private QueryElement_I m_Parent;
	
	public JoinClauseElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part;
		this.m_Parent = parent;
	}
	
	/**
	 * Generate parts of the join clause:
	 * <table|simple_query|query_with_join>
	 * <INNER|CROSS|NATURAL INNER|FULL OUTER|LEFT OUTER| RIGHT OUTER|' '>JOIN
	 * <table|simple_query|query_with_join>
	 * <ON|USING>
	 * <conditions|column_list>
	 */
	
	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		//Get parts tokenized by join keyword
		List<String> subPartsByJoin = ParserHelper.getInstance().generateJoinClauseElements(m_SqlPart, TOKEN_SELECTION.JOIN_KEYWORD);
		
		QueryElement_I[] subElems = this.classifySubElements(subPartsByJoin);
		
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
	
	private QueryElement_I[] classifySubElements(List<String> subPartsByJoin) {
		int count = 0;
		JoinTableElement preJte = null;
		QueryElement_I[] qes = new QueryElement_I[subPartsByJoin.size()];
		
		for(String sqlPart : subPartsByJoin) {
			JoinTableElement jte = new JoinTableElement(sqlPart, this);
			
			if(preJte != null)
				preJte.setNextJoinTable(jte);
			
			qes[count++] = jte;
			preJte = jte;
		}
		
		return qes;
	}
}
