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
 *
 */

/*
 * "LineDiscipline.java"
 * LineDiscipline.java 1.8 01/07/10
 */

package org.netbeans.lib.terminalemulator;

/**
 * Do the sort of stuff pty's normally do:
 * <ul>
 * <li> echoing
 * <li> CR/NL mappings
 * <li> BS processing
 * <li> Line buffering.
 * </ul>
 * <p>
 * Currently the settings are hardcoded to simulate a pty setup for running
 * shells.
 * <p>
 * This class is not complete by any means and is merely an example of
 * a TermStream. Things that it might do:
 * <ul>
 * <li> TAB processing
 * <li> conversion of control characters to "signals".
 * </ul>
 */

public class LineDiscipline extends TermStream {

    private static final char bs_sequence[] = {(char)8, (char)32, (char)8};

    // input line main buffer
    private final StringBuffer line = new StringBuffer();

    // auto-growing buffer for sending lines accumulated in 'line'.
    private int send_buf_sz = 2;
    private char send_buf[] = new char[send_buf_sz];
    char [] send_buf(int n) {
	if (n >= send_buf_sz) {
	    send_buf_sz = n+1;
	    send_buf = new char[send_buf_sz];
	}
	return send_buf;
    }

    // buffer for processing incoming characters
    private int put_capacity = 16;
    private int put_length = 0;
    private char put_buf[] = new char[put_capacity];

    @Override
    public void flush() {
	toDTE.flush();
    }

    @Override
    public void putChar(char c) {
	// Even though we dealing with one character, as the processing on it
	// may get more complicated we will want to use the code factored in 
	// processChar()

	// reset buffer
	put_length = 0;

	// fill it
	processChar(c);

	// flush it
	toDTE.putChars(put_buf, 0, put_length);
    }

    @Override
    public void putChars(char buf[], int offset, int count) {

	// reset buffer
	put_length = 0;

	// fill it
	for (int bx = 0; bx < count; bx++)
	    processChar(buf[offset+bx]);

	// flush it
	toDTE.putChars(put_buf, 0, put_length);
    }

    private void processChar(char c) {
	// Any actual mapping and processing gets done here
	appendChar(c);

	// Map NL to NLCR *stty onlcr)
	if (c == 10)
	    appendChar((char) 13);
    }

    private void appendChar(char c) {

	// Play StringBuffer

	if (put_length >= put_capacity) {
	    int new_capacity = put_capacity * 2;
	    if (new_capacity < 0)
		new_capacity = Integer.MAX_VALUE;
	    char new_buf[] = new char[new_capacity];
	    System.arraycopy(put_buf, 0, new_buf, 0, put_length);
	    put_buf = new_buf;
	    put_capacity = new_capacity;
	}

	put_buf[put_length++] = c;
    }




    @Override
    @SuppressWarnings({"AssignmentToMethodParameter", "ValueOfIncrementOrDecrementUsed"})
    public void sendChar(char c) {
	// keystroke -> world (DCE)

	// map CR to NL (stty icrnl)
	if (c == 13) {
	    toDTE.putChar(c);	// echo
	    toDTE.flush();

	    c = (char) 10;
	    toDTE.putChar(c);	// echo the newline too
	    toDTE.flush();

	    line.append(c);

	    int nchars = line.length();
	    char [] tmp = send_buf(nchars);
	    line.getChars(0, nchars, tmp, 0);
	    toDCE.sendChars(tmp, 0, nchars);
	    line.delete(0, 99999);		// clear the line

	} else if (c == 10) {
            toDTE.putChar((char) 13);	// echo carriage return too
	    toDTE.flush();

	    toDTE.putChar(c);	// echo
	    toDTE.flush();

	    line.append(c);

	    int nchars = line.length();
	    char [] tmp = send_buf(nchars);
	    line.getChars(0, nchars, tmp, 0);
	    toDCE.sendChars(tmp, 0, nchars);
	    line.delete(0, 99999);		// clear the line

        } else if (c == 8) {
	    // BS
	    int nchars = line.length();

	    if (nchars == 0)
		return;		// nothing left to BS over

	    char erased_char;		// The char we're going to erase
	    try {
		erased_char = line.charAt(nchars-1);
	    } catch (Exception x) {
		return;		// apparently the 'nchars == 0' test failed above ;-)
	    } 
	    int cwidth = getTerm().charWidth(erased_char);

	    // remove from line buffer
	    line.delete(nchars-1, nchars);

	    // HACK 
	    // If you play a bit with DtTerm on Solaris in a non-latin locale 
	    // you'll see that when you BS over a multi-cell character it
	    // doesn't erase the whole character. The character is erased but the
	    // cursor moves back only one column. So you usually need to BS twice
	    // to get rid of it. If you "fix" Term to do something more reasonable 
	    // you'll find out that as you backspace you'll run over the cursor. 
	    // that's because the kernel linebuffer accounting assumes the above setup.
	    // I"m not sure how all of this came about but we have to mimic similar
	    // acounting and we do it by padding the buffer (only) with a bunch of spaces.
	    // 
	    // NOTE: There are two strong invariants you have to keep in mind:
	    // - Solaris, and I assume other unixes, stick to the BS-SP-BS echo
	    //   even though they seem to know about character widths.
	    // - BS from Term's point of view is _strictly_ a cursor motion operation!
	    //   The fact that it erases things has to do with the line discipline
	    //   (kernels or this class 'ere)
	    //
	    // Now I know non-unix people will want BS to behave sanely in non-unix
	    // environments, so perhapws we SHOULD have a property to control whether
	    // things get erased the unix way or some other way.

	    while(--cwidth > 0 ) {
		line.append(' ');
	    }

	    // erase character on screen
	    toDTE.putChars(bs_sequence, 0, 3);
	    toDTE.flush();

	} else {
	    toDTE.putChar(c);	// echo
	    toDTE.flush();
	    line.append(c);
	}
    }

    @Override
    public void sendChars(char c[], int offset, int count) {
	for (int cx = 0; cx < count; cx++)
	    sendChar(c[offset+cx]);
    }
}
