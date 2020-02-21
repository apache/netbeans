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

package org.netbeans.modules.cnd.makefile.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.StringTokenizer;

/**
 *  Read a Fortran file. Return complete Fortran statements, taking into consideration
 *  things like the source form, line length, continuation lines, and comments. This
 *  class works in conjunction with FortranParse to parse fortran files for mod and use
 *  statements.
 */
public class FortranReader {

    /** The input stream to the source file */
    private PushbackInputStream in;

    /** The statement buffer */
    private StringBuffer statement = new StringBuffer(256);

    /** Are we doing free format? */
    private boolean freeFormat;

    /** Are we doing fpp preprocessing? */
    private boolean preprocess;

    /** Use 132 character lines if set */
    private boolean longLines;

    /** Miscelaneous StringBuffer (class scope to reuse) */
    private StringBuffer sbuf = new StringBuffer();

    /** Copy of the current character read */
    private byte curc = 0;

    /** Previous curc */
    private byte lastc;

    /** Save the quote start character for multi-line quotes */
    private byte qchar;

    /** Previous lastc (used to restore lastc after ungetc()) */
    private byte prevlastc;

    /** Column number of last character read */
    private int col;

    /** Save the last column number so it can be restored during ungetc() calls */
    int lastcol = 0;

    /** The marker we use for end of file */
    private final static byte EOF = (byte) 0xff;

    /** Keep track of the line number */
    private int lineno;
    
    /** line number at the start of a statement */
    private int lnum;

    /** Keep track of what state we are in */
    private State state;

    private final State StartOfLine =      new State("StartOfLine");	// NOI18N
    private final State EndOfLine =        new State("EndOfLine");  // NOI18N
    private final State StartOfStatement = new State("StartOfStatement");   // NOI18N
    private final State EndOfStatement =   new State("EndOfStatement");	// NOI18N
    private final State InStatement =      new State("InStatement");	// NOI18N
    private final State InComment =        new State("InComment");  // NOI18N
    private final State InQuote =          new State("InQuote");  // NOI18N
    private final State GotEOF =           new State("GotEOF");  // NOI18N

    private boolean verbose = false;


    /**
     *  Create for reading, starting with current source line format
     *
     *  @param file The name of the file to read
     *  @param options	The Fortran options in effect
     *  @return	A single complete Fortran statement
     */
    public FortranReader(String file, String options, boolean verbose)
				throws FileNotFoundException {

	this.verbose = verbose;
	try {
	    in = new PushbackInputStream(new FileInputStream(file), 1);
	} catch (IllegalArgumentException ex) {
	}

	lineno = 1;
	col = 0;
	state = StartOfLine;

	setModes(file, options);
    }


    public FortranReader(String file, String options) throws FileNotFoundException {
	this(file, options, false);
    }


    /**
     *  Read and return a complete Fortran statement, taking consideration of Source
     *  Line Formats. The statement is NOT newline terminated.
     *
     *  @return	A single complete fortran statement
     */
    public String getStatement() throws UnexpectedEOFException {
	String stmnt;
	byte c;

	statement.delete(0, statement.length());
	while (true) {
	    stmnt = null;
	    if (state == StartOfLine) {
		state = readStartOfLine();
		lnum = lineno;
		if (state == StartOfStatement && statement.length() > 0) {
		    stmnt = statement.toString().trim();
		    if (stmnt.length() > 0) {
			break;
		    }
		}
	    } else if (state == EndOfLine) {
		if ((c = getc()) == '\n') {
		    state = StartOfLine;
		}
	    } else if (state == StartOfStatement || state == InStatement) {
		state = readStatement();
	    } else if (state == EndOfStatement) {
		if ((c = getc()) == ';') {
		    state = StartOfStatement;
		    stmnt = statement.toString().trim();
		}
	    } else if (state == InComment) {
		state = readUntilEOL();
	    } else if (state == InQuote) {
		state = continueQuote();
	    } else if (state == GotEOF) {	// Got an EOF
		if (curc == -1 && lastc == '\n') {
		    stmnt = statement.toString().trim();
		    if (stmnt.length() > 0) {
			break;
		    } else {
			return null;
		    }
		} else {
		    throw new UnexpectedEOFException();
		}
	    }
	}

	return stmnt;
    }


    /** Read bytes at the start of line until we determine the new state */
    private State readStartOfLine() {
	String buf;
	byte c = getc();

	if (c == '!' || (!freeFormat && isComment((char) c))) {
	    buf = getbytes(4);
	    if (buf.equalsIgnoreCase("dir$")) { // NOI18N
		trimInput();
		buf = getbytes(5);
		if (buf.equalsIgnoreCase("fixed")) {  // NOI18N
		    freeFormat = false;
		} else if (buf.length() >= 4 &&
			    buf.substring(0, 4).equalsIgnoreCase("free")) {  // NOI18N
		    freeFormat = true;
		}
	    }
	    return InComment;
	} else if (c == '\n') {
	    return state;
	}

	if (freeFormat) {
	    ungetc(c);
	    return nextState();
	} else {
	    buf = getbytes(5);
	    if (buf.length() == 5 && isContinuation(buf.charAt(4))) {
		return InStatement;
	    } else {
		return nextState();
	    }
	}
    }


    /**
     *  Read bytes until we can determine a new state. This method is only valid in
     *  the state is StartOfLine. If called in an invalide state it returns the current
     *  state without reading any bytes.
     */
    private State nextState() {
	byte c;

	if (state == StartOfLine) {
	    while ((c = getc()) != EOF) {
		if (col > 132 || (!freeFormat && !longLines && col > 72)) {
		    return readUntilEOL();
		} else if (isSpace((char) c)) {
		    continue;
		} else if (c == '!') {
		    return readUntilEOL();
		} else if (c == ';') {
		    ungetc(c);
		    return EndOfStatement;
		} else if (c == '\n') {
		    ungetc(c);
		    return EndOfLine;
		} else if (c == '\'' || c == '"') {
		    return readQuote(c, true);
		} else {
		    ungetc(c);
		    return StartOfStatement;
		}
	    }

	    return GotEOF;
	} else {
	    return state;
	}
    }


    /** Read until we reach the end of a statement */
    private State readStatement() {
	byte c;

	while ((c = getc()) != EOF) {
	    if (freeFormat && col > 132 ||
			(!freeFormat && (col > 72 || (longLines && col > 132)))) {
		return readUntilEOL();
	    } else if (c == '\n') {
		ungetc(c);
		return EndOfLine;
	    } else if (c == '\'' || c == '"') {
		return readQuote(c, true);
	    } else if (freeFormat && c == '&') {
		ungetc(c);
		return EndOfLine;
	    } else if (c == ';') {
		ungetc(c);
		return EndOfStatement;
	    } else if (c != ' ') {
		statement.append((char) c);
	    }
	}

	return GotEOF;
    }


    /** Read a quote. Be sure to ignore embedded quotes */
    private State readQuote(byte qchar, boolean printQchar) {
	byte c;

	this.qchar = qchar;
	if (printQchar) {
	    statement.append((char) qchar);
	}
	while ((c = getc()) != EOF) {
	    if ((c == qchar && peekc() != qchar && lastc != qchar)) {
		// The end of the quote was found
		statement.append((char) c);
		return InStatement;
	    } else if (freeFormat && c == '&' && peekc() == '\n') {
		// The quote is being continued
		return InQuote;
	    } else if (c == '\n') {
		// The quote is being continued
		ungetc(c);
		return InQuote;
	    } else {
		statement.append((char) c);
	    }
	}

	return GotEOF;
    }


    /** Read more of the quote */
    private State continueQuote() {
	byte c;

	if (freeFormat) {
	    if (curc == '&' && peekc() == '\n') {
		getc();	    // read the '\n'
		sbuf.delete(0, sbuf.length());
		while ((c = getc()) != EOF) {
		    if (c == ' ' || c == '\t') {
			sbuf.append(c);
		    } else if (c == '&') {
			return readQuote(qchar, false);
		    } else if (c == '!') {
			readUntilEOL();
		    } else if (c == '\n') {
			statement.append(sbuf);
			sbuf.delete(0, sbuf.length());
		    } else {
			statement.append(sbuf);
			ungetc(c);
			return readQuote(qchar, false);
		    }
		}
		return GotEOF;
	    } else if (curc == '\n') {
		return readQuote(qchar, false);
	    } else {
		// Error condition
		readUntilEOL();
		return EndOfLine;
	    }
	} else {
	    getc();	    // read the '\n'
	    while (true) {
		sbuf.delete(0, sbuf.length());
		while ((c = getc()) == ' ' || c == '\t') {
		    sbuf.append(c);
		}
		if ((col == 1 && (c == 'C' || c == '*')) || (col < 6 && c == '!')) {
		    readUntilEOL();
		} else if (c == EOF) {
		    return GotEOF;
		} else if (c == '\n') {
		    continue;
		} else if (col == 6 && isContinuation((char) c)) {
		    return readQuote(qchar, false);
		} else if (col > 6) {
		    statement.append(sbuf);
		    statement.append((char) c);
		    return readQuote(qchar, false);
		}
	    }
	}

    }


    /**
     *  Read input until the desired state is reached.
     *
     *  @return Either EndOfLine or GotEOF
     */
    private State readUntilEOL() {
	byte c;

	while ((c = getc()) != EOF) {
	    if (c == '\n') {
		ungetc(c);
		return EndOfLine;
	    }
	}
	return GotEOF;
    }


    /** Trim spaces and tabs from the input */
    private void trimInput() {
	byte c;

	do {
	    c = getc();
	} while (c == ' ' || c == '\t');
	ungetc(c);
    }


    private boolean isComment(char c) {
	return "cC!dD*".indexOf(c) != -1; // NOI18N
    }

    private boolean isSpace(char c) {
	return " \t".indexOf(c) != -1; // NOI18N
    }

    private boolean isContinuation(char c) {
	return "0 ".indexOf(c) == -1; // NOI18N
    }


    /**
     *  Get a specific number of bytes.
     *
     *  @param num  The number of bytes to put in the buffer
     *  @return A String with up to num characters
     */
    private String getbytes(int num) {

	if (num < 0) {
	    return null;
	}

	sbuf.delete(0, sbuf.length());
	for (int i = 0; i < num; i++) {
	    byte c = getc();

	    if (c == '\n') {
		ungetc(c);
		break;
	    }
	    sbuf.append((char) c);
	}

	return sbuf.toString();
    }


    /** Get a single character */
    private byte getc() {

	if (curc == EOF) {
	    // Don't bother reading any more
	    return EOF;
	}

	try {
	    prevlastc = lastc;	    // lets me restore lastc after ungetc(byte)
	    lastc = curc;
	    curc = (byte) (in.read() & 0xff);

	    if (curc == '\n') {
		lastcol = col;
		lineno++;
		col = 0;
	    } else if (curc == -1) {
		return EOF;
	    } else {
		col++;
	    }
	    return curc;
	} catch (IOException ex) {
	    return EOF;
	}
    }


    /** Peek at the next character without reading it */
    private byte peekc() {
	byte nextc = getc();
	ungetc(nextc);
	return nextc;
    }


    /** Unread the character */
    private void ungetc(byte b) {
    
	if (b != EOF) {
	    if (b == '\n') {
		lineno--;	// otherwise it will get incremented a 2nd time next getc()
		col = lastcol;
	    } else {
		col--;
	    }
	    try {
		in.unread(b);
		curc = lastc;
		lastc = prevlastc;
		prevlastc = 0;
	    } catch (IOException ex) {
		println("*** Unread too many bytes ***");   // NOI18N
	    }
	}
    }


    /**
     *  Set the initial line format from the names and options. Also check a few
     *  other options that we need to know about.
     */
    private void setModes(String file, String options) {
	String ext;
	int pos;

	// First, check the file extension. This controls several options.
	pos = file.lastIndexOf('.');
	if (pos >= 0) {
	    ext = file.substring(pos + 1);
	    if (ext.equals("f90") || ext.equals("f95")) {   // NOI18N
		freeFormat = true;
	    } else if (ext.equals("F90") || ext.equals("F95")) {    // NOI18N
		freeFormat = true;
		preprocess = true;
	    } else if (ext.equals(".f") || ext.equals("for")) {	// NOI18N
		freeFormat = false;
	    } else if (ext.equals("F")) {   // NOI18N
		freeFormat = false;
		preprocess = true;
	    }
	} else {
	    //throw new IllegalArgumentException(NbBundle.getMessage(FortranReader.class,
			    //"InvalidFortranFileName", file));	// NOI18N
	}

	// Next, check the options string. Some of these options override ones set by
	// the file extension (which is why we did the extension processing first).
	if (options != null && options.length() > 0) {
	    StringTokenizer st = new StringTokenizer(options);

	    while (st.hasMoreTokens()) {
		String opt = st.nextToken();

		if (opt.equals("-free")) {  // NOI18N
		    freeFormat = true;
		} else if (opt.equals("-fixed")) {  // NOI18N
		    freeFormat = false;
		} else if (opt.equals("-e")) {	// NOI18N
		    longLines = true;
		} else if (opt.equals("-fpp")) {    // NOI18N
		    preprocess = true;
		}
	    }
	}
    }


    /** An simple enumeration class */
    public static class State {
	private final String name;

	public State(String name) {
	    this.name = name;
	}

        @Override
	public String toString() {
	    return name;
	}
    }


    // The following code is all debug code. It doesn't need any i18n translation
    // nor does it get tested.
    private void println(String msg) {

	if (verbose) {
	    System.out.println(msg);
	}
    }


    public static void main(String[] args) {
	FortranReader fr = null;
	boolean verbose = false;
	String file = null;

	for (int i = 0; i < args.length; i++) {
	    if (args[i].equals("-v")) {	// NOI18N
		verbose = true;
	    } else {
		file = args[i];
	    }
	}

	if (file == null) {
	    file = "fwutil.f"; // NOI18N
	}

	try {
	    fr = new FortranReader(file, null);
	    fr.verbose = verbose;
	    String line;

	    int i = 0;
	    while ((line = fr.getStatement()) != null) {
		int lineno = fr.lnum - 1;
		System.out.println(lineno + ": " + line);   // NOI18N
	    }
	} catch (UnexpectedEOFException unex) {
	    System.err.println("Unexpected EOF exception"); // NOI18N
	} catch (FileNotFoundException fnfex) {
	    System.err.println("File not found exception"); // NOI18N
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
}
