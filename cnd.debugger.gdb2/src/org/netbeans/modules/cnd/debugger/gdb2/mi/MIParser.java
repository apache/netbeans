/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.debugger.gdb2.mi;

import org.netbeans.modules.cnd.debugger.common2.utils.StopWatch;
import org.netbeans.modules.cnd.debugger.gdb2.GdbUtils;


/**
 * Parse an MI output string into an MIRecord.
 */

public class MIParser {

    private static class MIParserException extends java.lang.Exception {
	public MIParserException(String msg) {
	    super(msg);
	} 
    }

    private static void error(String parsing, String expected, Token got)
	throws MIParserException {

	String msg = "MI parse error while parsing '" + parsing + "': " + // NOI18N
	    "Expected " + expected + " but got " + got.toString(); // NOI18N
	throw new MIParserException(msg);
    } 

    private char[] str;	// Copy of input string for efficient access.
    private int x;      // index into 'str' for next char to be parsed
    private int bx;     // index into 'str' for begin of current token
    
    private final String encoding;

    public MIParser(String encoding) {
        this.encoding = encoding;
    } 

    /**
     * Prepare for parsing.
     */

    public void setup(String data) {

	// Convert String into char array, not only for efficient access but
	// also so we can tack on a 0 "sentinel".

	int len = data.length();
	str = new char[len + 1];   // leave room for '\0'
	data.getChars(0, len, this.str, 0);
	this.str[len] = 0;
	x = 0;
    } 


    /**
     * Parse the pre-setup line into an MIRecord.
     *
     * In case of error MIRecord.isError() will return true and
     * MIRecord.error() will contain an error message.
     */
    public MIRecord parse() {
	StopWatch sw = new StopWatch("MIrecord.parse()"); // NOI18N
	sw.start();
	MIRecord record = new MIRecord();
	try {
	    parseWork(record);
	} catch (MIParserException e) {
	    record.isError = true;
	    record.error = e.getMessage();
	}
	sw.stop();
	if (Log.MI.time) {
	    // only dump anything that takes over 5 msec
	    final int threshold = 5;
	    sw.dump(threshold);
	}
	return record;
    }


    //
    // Stuff to help quickly map a character into it's class
    //

    private static final int CHAR_DIGIT = 1<<0;
    private static final int CHAR_TERM = 1<<1;

    private static final int charMap[] = makeCharMap();

    private static int[] makeCharMap() {
	int cmap[] = new int[256];
	cmap['0'] |= CHAR_DIGIT;
	cmap['1'] |= CHAR_DIGIT;
	cmap['2'] |= CHAR_DIGIT;
	cmap['3'] |= CHAR_DIGIT;
	cmap['4'] |= CHAR_DIGIT;
	cmap['5'] |= CHAR_DIGIT;
	cmap['6'] |= CHAR_DIGIT;
	cmap['7'] |= CHAR_DIGIT;
	cmap['8'] |= CHAR_DIGIT;
	cmap['9'] |= CHAR_DIGIT;

	cmap[0]   |= CHAR_TERM;
	cmap[' '] |= CHAR_TERM;
	cmap['\n'] |= CHAR_TERM;
	cmap['\r'] |= CHAR_TERM;
	cmap['\t'] |= CHAR_TERM;
	cmap[','] |= CHAR_TERM;
	cmap['='] |= CHAR_TERM;
	cmap['['] |= CHAR_TERM;
	cmap[']'] |= CHAR_TERM;
	cmap['{'] |= CHAR_TERM;
	cmap['}'] |= CHAR_TERM;

	return cmap;
    }

    private boolean charIs(char c, int what) {
	if (c >= 256)
	    return false;
	else
	    return (charMap[c] & what) == what;
    }

    // 
    // codes for token types
    //

    private enum TokenType {
        EOL,		// End Of Line
        LC,		// Left Curly {
        RC,		// Right Curly }
        LB,		// Left Brace [
        RB,		// Right Brace ]
        COMMA,		// ,
        EQ,		// =
        STR,		// "..."
        SYM,		// symbol
        NUM,		// number

        CARET,		// ^
        PLUS,		// +
        STAR,		// *
        TILDE,		// ~
        AT,		// @
        AMP;		// &
    }

    private static class Token {
	private final TokenType type;
	private final String value;

	public Token(TokenType type) {
	    this.type = type;
            this.value = null;
	    if (Log.MI.ttrace)
		System.out.println("\t" + this.toString()); // NOI18N
	} 

	public Token(TokenType type, String value) {
	    this.type = type;
	    this.value = value;
	    if (Log.MI.ttrace)
		System.out.println("\t" + this.toString() + ": " + value); // NOI18N
	} 

        @Override
	public String toString() {
	    return type.toString() + " (" + value + ')'; //NOI18N
	} 
    } 


    private Token ungotToken = null;

    /**
     * Push the given Token back into the input stream.
     * Only one token can be pushed back.
     */

    private void ungetToken(Token t) {
	assert ungotToken == null;
	ungotToken = t;
    }


    /**
     * Get the next Token.
     */

    private Token getToken() {
	if (ungotToken != null) {
	    Token t = ungotToken;
	    ungotToken = null;
	    return t;
	}

	while (true) {
	    char c = str[x++];
	    switch (c) {
		case 0:
		    return new Token(TokenType.EOL);
		case ' ':
		case '\n':
		case '\r':
		case '\t':
		    // skip spaces
		    continue;
		case '{':
		    return new Token(TokenType.LC);
		case '}':
		    return new Token(TokenType.RC);
		case '[':
		    return new Token(TokenType.LB);
		case ']':
		    return new Token(TokenType.RB);
		case ',':
		    return new Token(TokenType.COMMA);
		case '=':
		    return new Token(TokenType.EQ);
		case '^':
		    return new Token(TokenType.CARET);
		case '+':
		    return new Token(TokenType.PLUS);
		case '*':
		    return new Token(TokenType.STAR);
		case '~':
		    return new Token(TokenType.TILDE);
		case '@':
		    return new Token(TokenType.AT);
		case '&':
		    return new Token(TokenType.AMP);
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		    x--;
		    bx = x;
		    while (charIs(str[x], CHAR_DIGIT)) {
			x++;
		    }
		    return new Token(TokenType.NUM, new String(str, bx, x-bx));
		case '"': {
		    // x is already past the '"'
		    StringBuilder string = new StringBuilder();
                    boolean escape = false;
		    while (str[x] != 0) {
			if (str[x] == '\\') {
			    escape = !escape;
			} else {
                            if (str[x] == '"' && !escape) {
                                x++;	// skip over trailing '"'
                                return new Token(TokenType.STR, string.toString());
                            }
                            escape = false;
			}
                        string.append(str[x++]);
		    }
		    }
		    break;
		default: {
		    x--;
		    bx = x;
		    while (!charIs(str[x], CHAR_TERM)) {
			x++;
		    }
		    return new Token(TokenType.SYM, new String(str, bx, x-bx));
		    }
	    }
	}
	// notreached
    }


    private MITList parseValueList(TokenType endToken, boolean topLevel)
	throws MIParserException {

	MITList list = new MITList(endToken == TokenType.RB, topLevel);
	while (true) {
	    MIValue value = parseValue(false);
	    list.add(value);
	    Token t = getToken();
	    if (t.type == endToken)
		break;
	    else if (t.type == TokenType.COMMA)
		continue;
	    else
		error("value list", ", or ]|}", t); // NOI18N
	}
	return list;
    }

    /* 
    private MITList parseResultList(TokenType endToken, boolean topLevel)
	throws MIParserException {

	MITList list = new MITList(endToken == TokenType.RB, topLevel);
	while (true) {
	    MIResult result = parseResult();
	    list.add(result);
	    Token t = getToken();
	    if (t.type == endToken)
		break;
	    else if (t.type == TokenType.COMMA)
		continue;
	    else
		error("result list", ", or ]", t); // NOI18N
	}
	return list;
    }*/

    /* This implementation supports the output like this:
        result: variable = value
        value: tuple (, tuple)*
    */
    private MITList parseResultList(TokenType endToken, boolean topLevel)
	throws MIParserException {

	MITList list = new MITList(endToken == TokenType.RB, topLevel);
        String name = null;
	while (true) {
            Token t = getToken();
            if (t.type == TokenType.SYM) {
                name = t.value;
                
                t = getToken();
                
                if (t.type != TokenType.EQ) {
                    error("result", "=", t); // NOI18N
                }
            } else if (t.type == TokenType.LC) {
                ungetToken(t);
            } else {
                error("result list", "variable or {", t); // NOI18N
            }
            
            if (name == null) {
                error("result list", "variable", t); // NOI18N
            }
            MIValue value = parseValue("file".equals(name) || "fullname".equals(name)); //NOI18N
            
	    MIResult result = new MIResult(name, value);
	    list.add(result);
            
	    t = getToken();
	    if (t.type == endToken)
		break;
	    else if (t.type == TokenType.COMMA)
		continue;
	    else
		error("result list", ", or ]", t); // NOI18N
	}
	return list;
    }

    private MIValue parseValue(boolean decode) throws MIParserException {
	Token t = getToken();

	if (t.type == TokenType.STR) {
            String value = t.value;
            if (decode) {
                value = GdbUtils.gdbToUserEncoding(value, encoding);
            }
	    return new MIConst(value);
	} else if (t.type == TokenType.LC) {
	    return parseTList(TokenType.RC);

	} else if (t.type == TokenType.LB) {
	    return parseTList(TokenType.RB);
	}

	error("value", "c-string or { or [", t); // NOI18N
	return null;
    }

    /*
    private MIResult parseResult() throws MIParserException {
	Token tsym = getToken();
	if (tsym.type != TokenType.SYM)
	    error("result", "variable", tsym); // NOI18N

	Token teq = getToken();
	if (teq.type != TokenType.EQ)
	    error("result", "=", teq); // NOI18N

	MIValue value = parseValue("file".equals(tsym.value) || "fullname".equals(tsym.value)); //NOI18N

	return new MIResult(tsym.value, value);
    }*/


    private MITList parseTList(TokenType endToken) throws MIParserException {
	boolean topLevel = (endToken == TokenType.EOL);
	Token t = getToken();

	if (t.type == endToken) {
	    // empty list
	    return new MITList(endToken == TokenType.RB, topLevel);
	}

	switch (t.type) {
	    case SYM:
		// list of results
		ungetToken(t);
		return parseResultList(endToken, topLevel);

	    case STR:
	    case LC:
	    case LB:
		// list of values
		ungetToken(t);
		return parseValueList(endToken, topLevel);

	    default:
		break;
	}

	error("tlist", "]|} or variable or c-string or [ or {", t); // NOI18N
	return null;
    }

    private MIRecord parseWork(MIRecord record) throws MIParserException {

	Token t = getToken();
	if (t.type == TokenType.NUM) {
            try {
                record.token = java.lang.Integer.parseInt(t.value);
            } catch (NumberFormatException nfe) {
                throw new MIParserException("Unable to parse token: " + t.value); //NOI18N
            }
	    t = getToken();
	}

	switch (t.type) {
	    case EOL:
		record.results = new MITList(false, true);
		return record;
	    case CARET:
		record.type = '^';
		record.isStream = false;
		break;
	    case PLUS:
		record.type = '+';
		record.isStream = false;
		break;
	    case STAR:
		record.type = '*';
		record.isStream = false;
		break;
	    case EQ:
		record.type = '=';
		record.isStream = false;
		break;

	    case TILDE:
		record.type = '~';
		record.isStream = true;
		break;
	    case AT:
		record.type = '@';
		record.isStream = true;
		break;
	    case AMP:
		record.type = '&';
		record.isStream = true;
		break;
	}


	if (record.isStream) {
	    t = getToken();
	    if (t.type != TokenType.STR)
		error("stream-record", "c-string", t); // NOI18N
	    record.stream = t.value;

	} else {
	    t = getToken();
	    if (t.type != TokenType.SYM)
		error("non-stream-record", "SYM", t); // NOI18N
	    record.cls = t.value;

	    t = getToken();
	    if (t.type == TokenType.EOL) {
		// empty results
		record.results = new MITList(false, true);

	    } else if (t.type == TokenType.COMMA) {
		// normal results
		record.results = parseTList(TokenType.EOL);

	    } else {
		// something else
		// create a dummy 'results' so toString() doesn't bomb.
		record.results = new MITList(false, true);
		error("results", ", or EOL", t); // NOI18N
	    }
	}

	return record;
    }
}
