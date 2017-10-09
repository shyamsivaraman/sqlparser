package org.goldenhelix.parser.sql.ora.misc;

import java.util.LinkedList;
import java.util.List;

import org.goldenhelix.parser.sql.ora.elements.JoinClauseElement.TOKEN_SELECTION;

public class ParserHelper {
	
	private static final ParserHelper m_Instance = new ParserHelper();
	
	private ParserHelper() { }
	
	public static ParserHelper getInstance() {
		return m_Instance;
	}

	/**
	 * Method used by Function Element class
	 * TODO: Merge this method and generateSubParts() to an external class
	 * @return
	 */
	
	public List<String> generateParamParts(String sqlPart) {
		List<String> pParts = new LinkedList<String>();
		
		//Unwrap the function - remove the name and brackets
		String tmpPart = sqlPart.substring(sqlPart.indexOf('(')+1);
		tmpPart = tmpPart.substring(0, tmpPart.lastIndexOf(')')).trim();
		
		//Add a comma at the end so that splitting could work when there is no comma at the end of string
		if(tmpPart.trim().lastIndexOf(',') != tmpPart.trim().length()-1)
			tmpPart += ",";

		int brackIdx = -1;
		int movingIdx = -1;
		
		while(brackIdx == -1) {
			int comIdx = tmpPart.indexOf(',', movingIdx+1);
			
			if(comIdx == -1)
				break;
			
			String pPart = tmpPart.substring(movingIdx+1, comIdx);
			if(pPart.trim().equals(""))
				break;
			
			brackIdx = pPart.indexOf('(');
			
			if(brackIdx == -1) {
				pParts.add(pPart.trim());
				movingIdx = comIdx;
			} else {
				String remPart = tmpPart.substring(movingIdx+1);
				pPart = ParserHelper.getInstance().findOuterPartWithBrackets(remPart, remPart, 0, 0, ",", true).getPart();
				pParts.add(pPart.trim());
				
				movingIdx = (movingIdx+1) + pPart.length();
				brackIdx = -1;
			}
		}
		
		return pParts;
	}
	
	/**
	 * Method used by ColumnListElement class
	 * @param sqlPart
	 * @param startIdx
	 * @param depth
	 * @return
	 */
	
	public List<String> generateSubParts(String sqlPart, int startIdx, int depth) {
		List<String> colParts = new LinkedList<String>();
		
		//Add a comma at the end so that splitting could work when there is no comma at the end of string
		if(sqlPart.trim().lastIndexOf(',') != sqlPart.trim().length()-1)
			sqlPart += ",";
		
		//TODO: Remove any starting/ending brackets
		//TODO: Remove any double/single quotes
		
		int brackIdx = -1;
		int movingIdx = -1;
		
		while(brackIdx == -1) {
			int comIdx = sqlPart.indexOf(',', movingIdx+1);
			
			if(comIdx == -1)
				break;
			
			String colPart = sqlPart.substring(movingIdx+1, comIdx);
			if(colPart.trim().equals(""))
				break;
			
			brackIdx = colPart.indexOf('(');
			
			if(brackIdx == -1) {
				colParts.add(colPart.trim());
				movingIdx = comIdx;
			} else {
				String remPart = sqlPart.substring(movingIdx+1);
				colPart = this.findOuterPartWithBrackets(remPart, remPart, 0, 0, ",", true).getPart();
				colParts.add(colPart.trim());
				
				movingIdx = (movingIdx+1) + colPart.length();
				brackIdx = -1;
			}
		}
		
		//Remove the extra comma, if present
		String lastPart = colParts.get(colParts.size()-1);
		if(lastPart != null && lastPart.lastIndexOf(',') == lastPart.length()-1)
			colParts.set(colParts.size()-1, lastPart.substring(0, lastPart.length()-1));
		
		return colParts;
	}
	
	/**
	 * Look for presence of the given token in different combinations with 'space', or without space
	 * when upaddedMatch is set to true
	 * 
	 * @param subStr
	 * @param token
	 * @param fromIdx
	 * @param doUnpaddedMatch
	 * @return
	 */
	private int getTokenIndex(String subStr, String[] tokens, int fromIdx, boolean doUnpaddedMatch) {
		int idx = -1;
		
		for(int i=0; i<tokens.length; i++) {
			idx = subStr.indexOf(" " + tokens[i] + " ", fromIdx);
			if(idx == -1)
				idx = subStr.indexOf(" " + tokens[i], fromIdx);
			if(idx == -1)
				idx = subStr.indexOf(tokens[i] + " ", fromIdx);
			if(idx == -1 && doUnpaddedMatch)
				idx = subStr.indexOf(tokens[i], fromIdx);
			
			if(idx != -1)
				break;
		}
		
		return idx;
	}
	
	private int[] getFirstTokenIndex(String subStr, String[] tokens, int fromIdx, boolean doUnpaddedMatch) {
		int idx = -1;
		int prevIdx = subStr.length();
		int tokenLen = 0;
		
		for(int i=0; i<tokens.length; i++) {
			idx = subStr.indexOf(" " + tokens[i] + " ", fromIdx);
			if(idx == -1)
				idx = subStr.indexOf(" " + tokens[i], fromIdx);
			if(idx == -1)
				idx = subStr.indexOf(tokens[i] + " ", fromIdx);
			if(idx == -1 && doUnpaddedMatch)
				idx = subStr.indexOf(tokens[i], fromIdx);
			
			if(idx != -1 && idx < prevIdx) {
				prevIdx = idx;
				tokenLen = tokens[i].length();
			}
		}
		
		return (prevIdx == subStr.length()) ? new int[] {-1,0} : new int[]{prevIdx, tokenLen};
	}

	public SQLPart findOuterPart(String sql, String subStr, int startIdx, int depth, String token, boolean doUnpaddedMatch) {
		int fromIdx = -1;
		SQLPart part = null;
		String tmpStr  = null;
		
		//Based on the depth we are, get the index of the token this function is using as a delimiter ('from', 'comma', ...)
		if(depth == 0) {
			fromIdx = this.getTokenIndex(subStr, new String[] {token}, fromIdx+1, doUnpaddedMatch);
		} else {
			for(int i=0; i<=depth; i++) {
				fromIdx = this.getTokenIndex(subStr, new String[] {token}, fromIdx+1, doUnpaddedMatch);
			}
		}

		//Return the string as it is if its not having a delimiter
		if(fromIdx == -1)
			return new SQLPart(subStr, startIdx);
		
		//Else, cut out the substring to work upon (to check the well-formedness of brackets, select-from)
		tmpStr = subStr.substring(startIdx, fromIdx);
		
		//select-from well-formedness check
		int selectIdx = tmpStr.indexOf("select ");
		//If this select was not found at the start of the input, then its wrong
		if(selectIdx != 0) {
			if(selectIdx == -1) selectIdx = tmpStr.indexOf("(select ");
			if(selectIdx == -1) selectIdx = tmpStr.indexOf(" select ");
		}
		
		if(selectIdx == -1) {
			//return new SQLPart(subStr.substring(0, sql.indexOf(token)).trim(), startIdx);
			return new SQLPart(tmpStr, startIdx);
		}
		
		int openSfCount = 0;
		boolean switchToFrom = false;
		
		while(selectIdx != -1) {
			if(!switchToFrom) {
				openSfCount++;
				
				int tmpIdx = -1;
				tmpIdx = tmpStr.indexOf(" select ", selectIdx+8+1);
				if(tmpIdx == -1)
					tmpIdx = tmpStr.indexOf("(select ", selectIdx+8+1);
				selectIdx = tmpIdx;
			}
			
			if(selectIdx == -1 && !switchToFrom) {
				switchToFrom = true;
				selectIdx = tmpStr.indexOf(" from ");
			}
			
			if(switchToFrom && selectIdx != -1) {
				openSfCount--;
				selectIdx = tmpStr.indexOf(" from ", selectIdx+1);
			}
		}
		
		//If brackets are still irregular, expand more into the original string
		if(openSfCount > 0) {
			part = this.findOuterPart(sql, subStr, startIdx, ++depth, token, false);
		} else {
			part = new SQLPart(tmpStr, startIdx);
		}
		
		return part;
	}
	
	/**
	 * Expected value of token = 'where'
	 * @param sql
	 * @param subStr
	 * @param startIdx
	 * @param depth
	 * @param token
	 * @param doUnpaddedMatch
	 * @return
	 */
	
	public SQLPart findOuterPartForTables(String sql, String subStr, int startIdx, int depth, String token, boolean doUnpaddedMatch) {
		int whereIdx = -1;
		SQLPart part = null;
		String tmpStr  = null;
		
		//Based on the depth we are, get the index of the token this function is using as a delimiter ('where', 'comma', ...)
		if(depth == 0) {
			whereIdx = this.getTokenIndex(subStr, new String[] {token}, whereIdx+1, doUnpaddedMatch);
		} else {
			for(int i=0; i<=depth; i++) {
				whereIdx = this.getTokenIndex(subStr, new String[] {token}, whereIdx+1, doUnpaddedMatch);
			}
		}

		//Return the string as it is if its not having a delimiter
		if(whereIdx == -1)
			return new SQLPart(subStr, startIdx);
		
		//Else, cut out the substring to work upon (to check the well-formedness of brackets, select-from)
		tmpStr = subStr.substring(startIdx, whereIdx);
		
		//Check presence of select based tables
		int selectIdx = tmpStr.indexOf("(");
		
		if(selectIdx == -1)
			return new SQLPart(subStr.substring(0, sql.indexOf(token)).trim(), startIdx);
		
		int openSfCount = 0;
		boolean switchToFrom = false;
		
		while(selectIdx != -1) {
			if(!switchToFrom) {
				openSfCount++;
				selectIdx = tmpStr.indexOf("(", selectIdx+1);
			}
			
			if(selectIdx == -1 && !switchToFrom) {
				switchToFrom = true;
				selectIdx = tmpStr.indexOf(")");
			}
			
			if(switchToFrom && selectIdx != -1) {
				openSfCount--;
				selectIdx = tmpStr.indexOf(")", selectIdx+1);
			}
		}
		
		//If brackets are still irregular, expand more into the original string
		if(openSfCount > 0) {
			part = this.findOuterPartForTables(sql, subStr, startIdx, ++depth, token, false);
		} else {
			part = new SQLPart(tmpStr, startIdx);
		}
		
		return part;
	}
	
	/**
	 * Expected value of token = group by/ order by/ end of line
	 * @param sql
	 * @param subStr
	 * @param startIdx
	 * @param depth
	 * @param token
	 * @param doUnpaddedMatch
	 * @return
	 */
	
	public SQLPart findOuterPartForWhereClause(String sql, String subStr, int startIdx, int depth, String[] tokens, boolean doUnpaddedMatch) {
		int whereIdx = -1;
		SQLPart part = null;
		String tmpStr  = null;
		
		//Based on the depth we are, get the index of the token this function is using as a delimiter ('where', 'comma', ...)
		if(depth == 0) {
			whereIdx = this.getTokenIndex(subStr, tokens, whereIdx+1, doUnpaddedMatch);
		} else {
			for(int i=0; i<=depth; i++) {
				whereIdx = this.getTokenIndex(subStr, tokens, whereIdx+1, doUnpaddedMatch);
			}
		}

		//Return the string as it is if its not having a delimiter
		if(whereIdx == -1)
			return new SQLPart(subStr, startIdx);
		
		//Else, cut out the substring to work upon (to check the well-formedness of brackets, select-from)
		tmpStr = subStr.substring(startIdx, whereIdx);
		
		//Check presence of nesting
		int selectIdx = tmpStr.indexOf("(");
		
		if(selectIdx == -1)
			return new SQLPart(subStr.substring(0, whereIdx).trim(), startIdx);
		
		int openSfCount = 0;
		boolean switchToFrom = false;
		
		while(selectIdx != -1) {
			if(!switchToFrom) {
				openSfCount++;
				selectIdx = tmpStr.indexOf("(", selectIdx+7+1);
			}
			
			if(selectIdx == -1 && !switchToFrom) {
				switchToFrom = true;
				selectIdx = tmpStr.indexOf(")");
			}
			
			if(switchToFrom && selectIdx != -1) {
				openSfCount--;
				selectIdx = tmpStr.indexOf(")", selectIdx + 1);
			}
		}
		
		//If brackets are still irregular, expand more into the original string
		if(openSfCount > 0) {
			part = this.findOuterPartForWhereClause(sql, subStr, startIdx, ++depth, tokens, false);
		} else {
			part = new SQLPart(tmpStr, startIdx);
		}
		
		return part;
	}
	
	private SQLPart findOuterPartWithBrackets(String sql, String subStr, int startIdx, int depth, String token, boolean doUnpaddedMatch) {
		int fromIdx = -1;
		SQLPart part = null;
		String tmpStr  = null;
		
		//Based on the depth we are, get the index of the token this function is using as a delimiter ('from', 'comma', ...)
		if(depth == 0) {
			fromIdx = this.getTokenIndex(subStr, new String[] {token}, fromIdx+1, doUnpaddedMatch);
		} else {
			for(int i=0; i<=depth; i++) {
				fromIdx = this.getTokenIndex(subStr, new String[] {token}, fromIdx+1, doUnpaddedMatch);
			}
		}

		//Return the string as it is if its not having a delimiter
		if(fromIdx == -1)
			return new SQLPart(subStr, startIdx);
		
		//Else, cut out the substring to work upon (to check the well-formedness of brackets, select-from)
		tmpStr = subStr.substring(startIdx, fromIdx);
		
		//Bracket well-formedness check
		int brackIdx = tmpStr.indexOf('(');
		if(brackIdx == -1)
			return new SQLPart(subStr.substring(0, sql.indexOf(token)).trim(), startIdx);
		
		int openCount = 0;
		boolean switchBracks = false;

		//Match the bracket counts
		while(brackIdx != -1) {
			if(!switchBracks) {
				openCount++;
				brackIdx = tmpStr.indexOf('(', brackIdx+1);
			}
			
			if(brackIdx == -1 && !switchBracks) {
				switchBracks = true;
				brackIdx = tmpStr.indexOf(')');
			}
			
			if(switchBracks && brackIdx != -1) {
				openCount--;
				brackIdx = tmpStr.indexOf(')', brackIdx+1);
			}
		}
		
		//If brackets are still irregular, expand more into the original string
		if(openCount > 0) {
			part = this.findOuterPartWithBrackets(sql, subStr, startIdx, ++depth, token, false);
		} else {
			part = new SQLPart(tmpStr, startIdx);
		}
		
		return part;
	}
	
	public String getOuterExpressionForCondition(String sql, String subStr, int startIdx, int depth, String[] tokens, boolean doUnpaddedMatch) {
		int opIdx = -1;
		String part = null;
		String tmpStr  = null;
		
		//Based on the depth we are, get the index of the token this function is using as a delimiter ('where', 'comma', ...)
		if(depth == 0) {
			opIdx = this.getFirstTokenIndex(subStr, tokens, opIdx+1, doUnpaddedMatch)[0];
		} else {
			for(int i=0; i<=depth; i++) {
				opIdx = this.getFirstTokenIndex(subStr, tokens, opIdx+1, doUnpaddedMatch)[0];
			}
		}

		//Return the string as it is if its not having a delimiter
		if(opIdx == -1)
			return subStr;
		
		//Else, cut out the substring to work upon (to check the well-formedness of brackets, select-from)
		tmpStr = subStr.substring(startIdx, opIdx);
		
		//Check presence of nesting
		int selectIdx = tmpStr.indexOf("(");
		
		if(selectIdx == -1)
			return subStr.substring(0, opIdx).trim();
		
		int openSfCount = 0;
		boolean switchToFrom = false;
		
		while(selectIdx != -1) {
			if(!switchToFrom) {
				openSfCount++;
				selectIdx = tmpStr.indexOf("(", selectIdx+1);
			}
			
			if(selectIdx == -1 && !switchToFrom) {
				switchToFrom = true;
				selectIdx = tmpStr.indexOf(")");
			}
			
			if(switchToFrom && selectIdx != -1) {
				openSfCount--;
				selectIdx = tmpStr.indexOf(")", selectIdx + 1);
			}
		}
		
		//If brackets are still irregular, expand more into the original string
		if(openSfCount > 0) {
			part = this.getOuterExpressionForCondition(sql, subStr, startIdx, ++depth, tokens, false);
		} else {
			part = tmpStr;
		}
		
		return part;
	}
	
	public List<String> generateJoinClauseElements(String sqlPart, TOKEN_SELECTION tokenType) {
		List<String> partList = new LinkedList<String>();
		int exprIdx = 0;
		int exprStartIdx = 0;
		
		String[] joinTokens = {"natural inner join", "full outer join", "left outer join", 
				"right outer join", "cross join", "natural join", "inner join", "join"
		};
		String[] joinConditionTokens = {"on", "using"};
		String[][] tokenSlots = {joinTokens, joinConditionTokens};
		
		int tokenSelector = (tokenType == TOKEN_SELECTION.JOIN_KEYWORD) ? 0 : 1;
		
		while(exprIdx < sqlPart.length()) {
			exprIdx = this.getNextExpressionIdx(sqlPart, exprIdx, tokenSlots[tokenSelector], 0);
			String tmpStr = sqlPart.substring(exprStartIdx, exprIdx);
			partList.add(tmpStr);
			
			int tokenLen = getFirstTokenIndex(sqlPart.substring(exprIdx), tokenSlots[tokenSelector], 0, false)[1];
			exprIdx = exprIdx + tokenLen + 1;
			
			exprStartIdx = exprIdx;
		}
		
		return partList;
	}
	
	/**
	 * select * from table where
	 * (1+2*3) > 8 OR|AND
	 * tab.col > 5 or|and alias.col > 4 or|and 4 >= col or|and 5 = 2 or|and
	 * tab.col is not null or|and tab.col is null or|and
	 * func1('A') = col or|and
	 * t.col like '%x%' or|and
	 * t.col in ('A', 5, ?) or|and t.col in (select col from tab) or|and
	 * (t.col1, t.col2) in (?,?), ('A', 5)
	 * 
	 * @param sqlPart
	 * @return
	 */
	
	public List<String> generateWhereClauseParts(String sqlPart) {
		List<String> partList = new LinkedList<String>();
		int exprIdx = 0;
		int exprStartIdx = 0;
		
		while(exprIdx < sqlPart.length()) {
			exprIdx = this.getNextExpressionIdx(sqlPart, exprIdx, new String[]{"and", "or"}, 0);
			
			String tmpStr = sqlPart.substring(exprStartIdx, exprIdx);
			partList.add(tmpStr);
			
			exprIdx = skipXJunction(sqlPart.substring(exprIdx)) + exprIdx;
			exprStartIdx = exprIdx;
		}
		
		return partList;
	}
	
	/**
	 * Skip the Conjunction and Disjunction part
	 * @param sqlPart
	 * @return
	 */
	private int skipXJunction(String sqlPart) {
		int jIdx = sqlPart.indexOf(" or ");
		if(jIdx != -1 && jIdx == 0) {
			return 4;
		}
		
		jIdx = sqlPart.indexOf(" and ");
		if(jIdx != -1 && jIdx == 0) {
			return 5;
		}
		
		return 0;
	}
	
	//(1+2*3) > 8 or tab.col1 > 5 and 4 >= col and 5 = 2 and func1('a') = col and t.col like '%x%' and t.col in ('a', 5, ?) or t.col5 in (select col from tab where a = 10) and (t.col1 > 4 and t.col5 = 10)
	public int getNextExpressionIdx(String sqlPart, int startIdx, String[] tokens, int depth) {
		int tokenIdx = startIdx;
		int exprIdx = -1;
		String tmpStr  = null;
		
		//String[] tokens = {"and", "or"};
		
		//Based on the depth we are, get the index of the token this function is using as a delimiter ('from', 'comma', ...)
		if(depth == 0) {
			tokenIdx = this.getFirstTokenIndex(sqlPart, tokens, tokenIdx, false)[0];
		} else {
			for(int i=0; i<=depth; i++) {
				tokenIdx = this.getFirstTokenIndex(sqlPart, tokens, tokenIdx+3, false)[0];
			}
		}

		//If no tokens found, then return the full string length as the expression
		if(tokenIdx == -1)
			return sqlPart.length();
		
		//Else, cut out the substring to work upon (to check the well-formedness of brackets, select-from)
		tmpStr = sqlPart.substring(startIdx, tokenIdx);
		
		//Bracket well-formedness check
		int brackIdx = tmpStr.indexOf('(');
		if(brackIdx == -1)
			return tokenIdx;
		
		int openCount = 0;
		boolean switchBracks = false;

		//Match the bracket counts
		while(brackIdx != -1) {
			if(!switchBracks) {
				openCount++;
				brackIdx = tmpStr.indexOf('(', brackIdx+1);
			}
			
			if(brackIdx == -1 && !switchBracks) {
				switchBracks = true;
				brackIdx = tmpStr.indexOf(')');
			}
			
			if(switchBracks && brackIdx != -1) {
				openCount--;
				brackIdx = tmpStr.indexOf(')', brackIdx+1);
			}
		}
		
		//If brackets are still irregular, expand more into the original string
		if(openCount > 0) {
			exprIdx = this.getNextExpressionIdx(sqlPart, startIdx, tokens, ++depth);
		} else {
			exprIdx = tokenIdx;
		}
		
		return exprIdx;
	}
}