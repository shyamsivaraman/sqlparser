package org.goldenhelix.parser.sql.ora.elements;

import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public interface QueryElement_I {

	public void accept(QueryVisitor_I visitor) throws ParseException;
	
	public String getSqlPart();
}
