package org.goldenhelix.parser.sql.ora.parser;

import org.goldenhelix.parser.sql.ora.elements.SQLElement;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class Parser {
	
	private QueryVisitor_I m_Visitor;
	
	public Parser(QueryVisitor_I visitor) {
		this.m_Visitor = visitor;
	}

	public void parse(String sql) throws ParseException {
		SQLElement sqlElem = new SQLElement(sql, null);
		sqlElem.accept(this.m_Visitor);
	}
}
