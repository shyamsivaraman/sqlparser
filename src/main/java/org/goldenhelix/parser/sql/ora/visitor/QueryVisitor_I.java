package org.goldenhelix.parser.sql.ora.visitor;

import org.goldenhelix.parser.sql.ora.elements.QueryElement_I;

public interface QueryVisitor_I {

	public void visit(QueryElement_I element);
}
