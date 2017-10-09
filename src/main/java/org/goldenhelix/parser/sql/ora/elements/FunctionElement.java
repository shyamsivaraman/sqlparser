package org.goldenhelix.parser.sql.ora.elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goldenhelix.parser.sql.ora.misc.ElementType;
import org.goldenhelix.parser.sql.ora.misc.ElementType.PARAM_TYPE;
import org.goldenhelix.parser.sql.ora.misc.ParseException;
import org.goldenhelix.parser.sql.ora.misc.ParserHelper;
import org.goldenhelix.parser.sql.ora.visitor.QueryVisitor_I;

public class FunctionElement implements QueryElement_I {
	
	private String m_SqlPart;
	private String m_FuncName;
	private QueryElement_I[] m_ParamElems;
	
	private QueryElement_I m_Parent;
	
	//(?i)^[\s\(\,]*(.+?)\s*[\(]+?
	private Pattern pFuncCol = Pattern.compile("(?i)^[\\s\\(\\,]*(.+?)\\s*[\\(]+?");
	
	public FunctionElement(String part, QueryElement_I parent) {
		this.m_SqlPart = part.trim();
		this.m_Parent = parent;
	}

	@Override
	public void accept(QueryVisitor_I visitor) throws ParseException {
		this.setFuncName();
		
		List<String> params = ParserHelper.getInstance().generateParamParts(this.m_SqlPart);
		
		//Classify the parameters
		m_ParamElems = this.classifyParams(params);
		
		//Send the visitor in
		this.sendVisitorDeep(m_ParamElems, visitor);
		
		visitor.visit(this);
	}
	
	private QueryElement_I[] classifyParams(List<String> params) throws ParseException {
		int i = 0;
		QueryElement_I[] paramElems = new QueryElement_I[params.size()];
		
		for(String param : params) {
			PARAM_TYPE pType = ElementType.getInstance().evaluateParamType(param);
			
			ParamElement pe = new ParamElement(param, this);
			pe.setParamType(pType);
			
			paramElems[i++] = pe;
		}
		
		return paramElems;
	}

	private void setFuncName() throws ParseException {
		Matcher m = pFuncCol.matcher(this.m_SqlPart);
		if(m.find()) {
			String name = m.group(1);
			if(name != null && !name.trim().equals(""))
				this.m_FuncName = name;
		}
		
		if(this.m_FuncName == null)
			throw new ParseException("Incorrect function input: " + this.m_FuncName);
	}
	
	private void sendVisitorDeep(QueryElement_I[] elems, QueryVisitor_I visitor) throws ParseException {
		for(QueryElement_I q : elems) {
			q.accept(visitor);
		}
	}
	
	public String toString() {
		return "[FUNCTION]: " + this.m_FuncName;
	}

	@Override
	public String getSqlPart() {
		return this.m_SqlPart;
	}
}
