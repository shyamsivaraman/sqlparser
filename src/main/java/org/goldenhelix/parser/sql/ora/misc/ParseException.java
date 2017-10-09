package org.goldenhelix.parser.sql.ora.misc;

public class ParseException extends Exception {

	private static final long serialVersionUID = 6239217498624653878L;

	public ParseException(String msg) {
		super(msg);
	}
	
	public ParseException(String msg, Throwable t) {
		super(msg, t);
	}
}
