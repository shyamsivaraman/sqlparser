package org.goldenhelix.parser.sql.ora.elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goldenhelix.parser.sql.ora.misc.ElementType;
import org.goldenhelix.parser.sql.ora.misc.ElementType.SQL_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

/**
 * Models the SQL statement. Creates intermediate objects representing the Column List, Table List
 * and the Where Clause if present. For each of the three objects created, the visitor is sent in
 * so that visit events can be raised after the specified element's parsing completes
 * 
 * TODO: Union statement support
 * 
 * @author Shyam Sivaraman
 *
 */

public class SQLElement implements QueryElement_I {
	
	private String m_Sql;
	private ElementType.SQL_TYPE m_Type;
	private QueryElement_I m_Parent;
	private Pattern pGetType = Pattern.compile("(?i)^(?:(?:\\s|\\()*)(select)(.*)");
	
	public SQLElement(String sql, QueryElement_I parent) {
		this.m_Sql = sql.trim().toLowerCase();
		this.m_Parent = parent;
	}
	
	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		m_Sql = unwrapBracks(m_Sql);
		
		Matcher m = generateSubElements(pGetType, m_Sql);
		if(!m.find())
			throw new ParseException("Matcher failed - Incorrect SQL statement");
		
		m_Type = ElementType.getInstance().evaluateSQLType(this.m_Sql);

		switch(m_Type) {
			case SELECT_STMT:
				this.parseSelectStatement(m.group(2), visitor);
				break;
			case UPDATE_STMT:
			case DELETE_STMT:
			case UNKNOWN:
		}
		
		visitor.visit(this);
	}
	
	/**
	 * Create the ColumnsListElement (Having the list of columns specified and their types) => Send visitor in =>
	 * Create the TablesListElement (Having the list of tables/ join conditions) => Send visitor in =>
	 * Create the WhereClauseElement (Having the where clause items) => Send visitor in =>
	 * Call visit on SQLElement (marking the visit end) => End parsing
	 * 
	 * @param part
	 * @param visitor
	 * @throws ParseException
	 */
	
	private void parseSelectStatement(String part, QueryVisitor_I visitor) throws ParseException {
		//Create the column list object, and send the visitor in
		ColumnsListElement cols = new ColumnsListElement(part, this);
		cols.accept(visitor);
		
		//Extract the FROM part from the main statement
		int colSectionIdx = this.m_Sql.indexOf(cols.getSqlPart());
		String fromSectionStart = this.m_Sql.substring(colSectionIdx + cols.getSqlPart().length());
		int fromIdx = fromSectionStart.indexOf("from");
		fromSectionStart = fromSectionStart.substring(fromIdx + "from".length());
		
		//Create the tables list object and send visitor in
		TablesListElement tables = new TablesListElement(fromSectionStart, this);
		tables.accept(visitor);

		//Extract the WHERE clause
		int fromSectionIdx = fromSectionStart.indexOf(tables.getSqlPart());
		String whereSectionStart = fromSectionStart.substring(fromSectionIdx + tables.getSqlPart().length());
		int whereIdx = whereSectionStart.indexOf("where");
		
		//If WHERE clause present, the create the object and send visitor in
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
	
	/**
	 * Trim any unwanted brackets wrapping the SQL statements
	 * @param part
	 * @return
	 */
	
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
