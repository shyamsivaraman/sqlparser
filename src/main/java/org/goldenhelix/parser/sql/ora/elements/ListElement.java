package org.goldenhelix.parser.sql.ora.elements;

import java.util.List;

import org.goldenhelix.parser.sql.ora.misc.ElementType;
import org.goldenhelix.parser.sql.ora.misc.ElementType.EXPR_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.misc.ParserHelper;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class ListElement implements QueryElement_I {

	private String m_SqlPart;
	private QueryElement_I m_Parent;
	
	public ListElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part;
		this.m_Parent = parent;
	}
	
	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		String part = _stripWrappingBrackets(m_SqlPart);
		
		//Extract the table names, select based tables and aliases
		List<String> subParts = ParserHelper.getInstance().generateSubParts(part, 0, 0);
		
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
			EXPR_TYPE exprType = ElementType.getInstance().evaluateExprType(part);
			QueryElement_I qe = null;
			
			switch(exprType) {
			case QUERY_EXPR:
				qe = new SQLElement(part, this);
				break;
			case FUNC_EXPR:
				qe = new FunctionElement(part, this);
				break;
			case LIST_EXPR:
				qe = new ListElement(part, this);
				break;
			case PLACEHOLDER_EXPR:
			case CONST_EXPR:
				ExpressionElement ee = new ExpressionElement(part, this);
				ee.setExpressionType(exprType);
				qe = ee;
			}
			
			qes[i++] = qe;
		}
		
		return qes;
	}
	
	private String _stripWrappingBrackets(String part) {
		part = part.trim();
		if(part.indexOf('(') == 0 && part.lastIndexOf(')') == part.length()-1) {
			return part.substring(1, part.length()-1);
		}
		
		return part;
	}
}
