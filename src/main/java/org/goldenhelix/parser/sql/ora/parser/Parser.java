package org.goldenhelix.parser.sql.ora.parser;

import org.goldenhelix.parser.sql.ora.elements.SQLElement;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

/**
 * 
 * @author Shyam Sivaraman
 * 
 * Creates an SQLElement root object and initiates the parsing on it. The parse is depth
 * first. This class has to be supplied with the desired Visitor type, so that the passing
 * component can listen to the parse events via the visit callback
 * 
 * The Parser object can have the same visitor handle multiple parses done via the call
 * to the parse() method
 */

public class Parser {
	
	private QueryVisitor_I m_Visitor;
	
	/**
	 * Pass on a single visitor object associated with this Parser object during its
	 * lifetime
	 * @param visitor
	 */
	
	public Parser(QueryVisitor_I visitor) {
		this.m_Visitor = visitor;
	}

	/**
	 * Pass on the SQL statement which has to be parsed
	 * @param sql
	 * @throws ParseException
	 */
	
	public void parse(String sql) throws ParseException {
		SQLElement sqlElem = new SQLElement(sql, null);
		sqlElem.accept(this.m_Visitor);
	}
}
