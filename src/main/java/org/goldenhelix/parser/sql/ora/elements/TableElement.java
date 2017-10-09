package org.goldenhelix.parser.sql.ora.elements;

import org.goldenhelix.parser.sql.ora.misc.ElementType.TABLE_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class TableElement implements QueryElement_I {

	private String m_SqlPart;
	private TABLE_TYPE m_TableType;
	private QueryElement_I m_Parent;
	private QueryElement_I m_SubElement;
	
	private String m_TableName;
	private String m_Alias = "-";
	
	public TableElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part;
		this.m_Parent = parent;
	}
	
	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		if(this.m_TableType == null)
			throw new ParseException("A Table type is not specified for this table element: " + this.m_SqlPart);
		
		switch(this.m_TableType) {
		case SIMPLE_TABLE:
			this.setTableName();
			break;
		case QUERY_TABLE:
			this.m_SubElement = createQuerySubElement();
			this.m_SubElement.accept(visitor);
			break;
		}
		
		visitor.visit(this);
	}
	
	public void setTableType(TABLE_TYPE type) {
		this.m_TableType = type;
	}
	
	public QueryElement_I getParent() {
		return this.m_Parent;
	}
	
	public String getTableName() {
		return this.m_TableName;
	}
	
	private SQLElement createQuerySubElement() {
		//TODO: Do we have aliases with double-quotes and a space in them, then this may fail
		//A query based table will be wrapped in brackets, and any alias if present will be after that
		int lastBrackIdx = this.m_SqlPart.lastIndexOf(')');
		int lastSpaceIdx = this.m_SqlPart.indexOf(" ", lastBrackIdx);
		
		String sqlPart = this.m_SqlPart;
		
		if(lastSpaceIdx != -1) {
			String alias = sqlPart.substring(lastSpaceIdx).trim();
			sqlPart = this.m_SqlPart.substring(0, lastSpaceIdx);
			
			if(alias.length() > 0)
				this.m_Alias = alias;
		}
		
		
		SQLElement sq = new SQLElement(sqlPart, this);
		return sq;
	}

	private void setTableName() {
		String parts[] = m_SqlPart.trim().split(" ");
		if(parts.length == 2) {
			this.m_Alias = parts[1];
		}
		
		this.m_TableName = parts[0];
	}
	
	public String toString() {
		return "[" + this.m_TableType + "] : " + this.m_TableName + " - " + this.m_Alias;
	}

	@Override
	public String getSqlPart() {
		return this.m_SqlPart;
	}
}
