package org.goldenhelix.parser.sql.ora.misc;

public class SQLPart {

	private String m_Part;
	private int m_StartIdx;
	
	public SQLPart(String part, int startIdx) {
		this.m_Part = part;
		this.m_StartIdx = startIdx;
	}
	
	public String getPart() {
		return this.m_Part;
	}
	
	public int getStartIdx() {
		return this.m_StartIdx;
	}
	
	public String toString() {
		return this.m_Part + " : " + this.m_StartIdx;
	}
}
