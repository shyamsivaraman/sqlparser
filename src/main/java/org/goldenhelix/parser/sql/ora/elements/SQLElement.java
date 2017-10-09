package org.goldenhelix.parser.sql.ora.elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goldenhelix.parser.sql.ora.misc.ElementType;
import org.goldenhelix.parser.sql.ora.misc.ElementType.SQL_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class SQLElement implements QueryElement_I {

	private String m_Sql;
	private SQL_TYPE m_Type;
	private QueryElement_I m_Parent;
	private Pattern pGetType = Pattern.compile("(?i)^(?:(?:\\s|\\()*)(select)(.*)");
	
	//TODO: Unions
	
	public SQLElement(String sql, QueryElement_I parent) {
		this.m_Sql = sql.trim().toLowerCase();
		this.m_Parent = parent;
	}
	
	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		m_Sql = unwrapBracks(m_Sql);
		Matcher m = generateSubElements(pGetType, m_Sql);
		m.find();
		
		m_Type = ElementType.getInstance().evaluateSQLType(this.m_Sql);

		switch(m_Type) {
			case SELECT_STMT:
				this.parseSelectStatement(m.group(2), visitor);
				break;
			case UPDATE_STMT:
			case DELETE_STMT:
		}
		//Determine clause formation - where, join etc.
		
		//Pass the visitor to them by calling accept on them
		
		visitor.visit(this);
	}
	
	private void parseSelectStatement(String part, QueryVisitor_I visitor) throws ParseException {
		ColumnsListElement cols = new ColumnsListElement(part, this);
		cols.accept(visitor);
		
		int colSectionIdx = this.m_Sql.indexOf(cols.getSqlPart());
		String fromSectionStart = this.m_Sql.substring(colSectionIdx + cols.getSqlPart().length());
		
		int fromIdx = fromSectionStart.indexOf("from");
		fromSectionStart = fromSectionStart.substring(fromIdx + "from".length());
		
		TablesListElement tables = new TablesListElement(fromSectionStart, this);
		tables.accept(visitor);

		int fromSectionIdx = fromSectionStart.indexOf(tables.getSqlPart());
		String whereSectionStart = fromSectionStart.substring(fromSectionIdx + tables.getSqlPart().length());
		
		int whereIdx = whereSectionStart.indexOf("where");
		
		if(whereIdx != -1) {
			whereSectionStart = whereSectionStart.substring(whereIdx + "where".length());
			WhereClauseElement whereClause = new WhereClauseElement(whereSectionStart, this);
			whereClause.accept(visitor);
		}
	}
	
	private Matcher generateSubElements(Pattern p, String str) {
		return p.matcher(str);
	}

	public String toString() {
		return "[" + this.m_Type + "]: " + this.m_Sql;
	}
	
	private String unwrapBracks(String part) {
		part = part.trim();
		
		if(part.startsWith("(") && part.endsWith(")")) {
			part = part.substring(1, part.length()-1);
			unwrapBracks(part);
		}
		
		return part;
	}

	@Override
	public String getSqlPart() {
		return this.m_Sql;
	}
}
