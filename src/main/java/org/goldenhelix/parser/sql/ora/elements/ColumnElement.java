package org.goldenhelix.parser.sql.ora.elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goldenhelix.parser.sql.ora.misc.ElementType.COL_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class ColumnElement implements QueryElement_I {
	
	private String m_SqlPart;
	private COL_TYPE m_ColType;
	
	private QueryElement_I m_Parent;
	private QueryElement_I m_SubElement;
	
	private String m_TableName;
	private String m_ColName;
	private String m_Alias;
	
	//(?i)^[\s\(\,]*([\w\.\-\_\d]+)\s*(as)*\s*([\w\.\-\_\d]*?)[\s\,\)]*$ - G1=Column name, G2=as (optional), G3 = Alias
	private Pattern pSimpleCol = Pattern.compile("(?i)^[\\s\\(\\,]*([\\w\\.\\-\\_\\d]+)\\s*(as)*\\s*([\\w\\.\\-\\_\\d]*?)[\\s\\,\\)]*$");
	
	public ColumnElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part;
		this.m_Parent = parent;
	}

	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		if(this.m_ColType == null)
			throw new ParseException("A column type is not specified for this element");
		
		switch(this.m_ColType) {
			case SIMPLE_COLUMN:
				setLocalValues();
				break;
			case FUNC_COLUMN:
				this.m_SubElement = createFuncSubElement();
				this.m_SubElement.accept(visitor);
				break;
			case QUERY_COLUMN:
				this.m_SubElement = createQuerySubElement();
				//TODO: Fix this when using sql2
				this.m_SubElement.accept(visitor);
				break;
			case EXPR_COLUMN:
				this.m_SubElement = createExprSubElement();
				this.m_SubElement.accept(visitor);
				break;
		}
		
		visitor.visit(this);
	}
	
	public void setColType(COL_TYPE type) {
		this.m_ColType = type;
	}
	
	public COL_TYPE getColType() {
		return this.m_ColType;
	}
	
	/**************** Private methods section ****************/
	
	private ExpressionElement createExprSubElement() {
		ExpressionElement exp = new ExpressionElement(this.m_SqlPart, this);
		return exp;
	}

	private SQLElement createQuerySubElement() {
		//TODO: Do we have aliases with double-quotes and a space in them, then this may fail
		//A query based table will be wrapped in brackets, and any alias if present will be after that
		
		//(select c.col5, select nvl(b,'-') from t2 where colx = 2) as inq2
		String alias = null;
		int lastBrackIdx = this.m_SqlPart.lastIndexOf(')');
		int lastSpaceIdx = this.m_SqlPart.indexOf(" ", lastBrackIdx);
		
		String sqlPart = this.m_SqlPart;
		
		if(lastSpaceIdx != -1) {
			alias = sqlPart.substring(lastSpaceIdx).trim();
			sqlPart = this.m_SqlPart.substring(0, lastSpaceIdx);
			
			int asIdx = alias.indexOf("as ");
			if(asIdx != -1) {
				alias = alias.substring(asIdx + "as ".length());
			}
			
			if(alias.length() > 0)
				this.m_Alias = alias;
		}
				
		SQLElement sq = new SQLElement(sqlPart, this);
		return sq;
	}

	private FunctionElement createFuncSubElement() {
		FunctionElement fe = new FunctionElement(this.m_SqlPart, this);
		return fe;
	}

	private void setLocalValues() {
		 Matcher m = pSimpleCol.matcher(m_SqlPart);
		 while(m.find()) {
			 String tabCol = m.group(1);
			 
			 if(tabCol.indexOf('.') != -1) {
				 String[] tokens = tabCol.split("\\.");
				 this.m_TableName = tokens[0];
				 this.m_ColName = tokens[1];
			 } else {
				 this.m_ColName = tabCol;
			 }
			 
			 if(m.group(3) != null && !m.group(3).trim().equals(""))
				 this.m_Alias = m.group(3);
		 }
	}
	
	public String toString() {
		if(this.m_ColType == COL_TYPE.SIMPLE_COLUMN)
			return "[" +this.m_ColType + "] : " + ((this.m_TableName == null)?"-":this.m_TableName) + "." +
				this.m_ColName + " alias (" + ((this.m_Alias == null)?"-":this.m_Alias) + ")";
		return "";
	}

	@Override
	public String getSqlPart() {
		return this.m_SqlPart;
	}
}
