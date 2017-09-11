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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.java.source.pretty;

import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class CharBuffer {
    char[] chars;
    int used;
    int col;
    int maxcol;
    static final int UNLIMITED = 999999;
    int rightMargin = 72;
    int tabSize = 8;
    boolean expandTabs = true;
    int leftMargin = 0;
    int hardRightMargin = UNLIMITED;
    int lastBlankLines = 0;
    int lastObserved;
    
    List<TrimBufferObserver>    trimObservers = new ArrayList<>();
    
    public final int length() { return used; }
    public void setLength(int l) { if (l < used) used = l < 0 ? 0 : l; }
    public CharBuffer() { chars = new char[10]; }
    public CharBuffer(int rm, int ts, boolean et) {
	this();
	rightMargin = rm;
        tabSize = ts;
        expandTabs = et;
    }
    public final boolean hasMargin() {
        return hardRightMargin != UNLIMITED;
    }
    public int harden() {
	int ret = hardRightMargin;
	hardRightMargin = rightMargin;
	return ret;
    }
    public void restore(int n) {
	hardRightMargin = n;
    }
    private final void needRoom(int n) {
        if (chars.length <= used+n) {
            char[] nc = new char[(used+n)*2];
            System.arraycopy(chars, 0, nc, 0, used);
            chars = nc;
        }
    }
    private static RuntimeException err = new IndexOutOfBoundsException();
    private void columnOverflowCheck() {
        if ((col > hardRightMargin || maxcol > hardRightMargin) &&
                hardRightMargin != UNLIMITED) throw err;
    }
    void toCol(int n) {
        if (!expandTabs)
            while((col + tabSize - (col % tabSize)) <= n)
                append('\t');
        while (col < n) append(' ');
    }
    void toLeftMargin() {
	toCol(leftMargin);
    }
    void toColExactly(int n) {
        if (n < 0) n = 0;
        if (n < col) nlTerm();
        toCol(n);
    }
    void notRightOf(int n) {
        if (n > col) needSpace();
        else toColExactly(n);
    }
    void ctlChar() {
        switch (chars[used-1]) {
          case '\n':
            if (hardRightMargin != UNLIMITED) throw err;
            if (col > maxcol) maxcol = col;
            col = 0;
            break;
          case '\t':
            col += tabSize - (col % tabSize);
            break;
          case '\b':
            if (col > maxcol) maxcol = col;
            col--;
            break;
        }
        columnOverflowCheck();
    }
    private void append0(char c) {
        chars[used++] = c;
        if (c > ' ') lastBlankLines = 0;
        if (c < ' ') ctlChar();
        else if (++col > hardRightMargin) columnOverflowCheck();
    }
    public final void append(char c) { needRoom(1); append0(c); }
    public final void append(char[] b) {
        if (b != null) append(b, 0, b.length);
    }
    public final void append(char[] b, int off, int len) {
        if (b != null) {
            needRoom(len);
            while (--len >= 0) append0(b[off++]);
        }
    }
    public void append(String s) {
        int len = s.length();
        needRoom(len);
        for (int i = 0; i < len; i++) append0(s.charAt(i));
    }
    public void append(CharBuffer cb) { append(cb.chars, 0, cb.used); }
    public void appendUtf8(byte[] src, int i, int len) {
	    int limit = i + len;
	    while (i < limit) {
		int b = src[i++] & 0xFF;
		if (b >= 0xE0) {
		    b = (b & 0x0F) << 12;
		    b = b | (src[i++] & 0x3F) << 6;
		    b = b | (src[i++] & 0x3F);
		} else if (b >= 0xC0) {
		    b = (b & 0x1F) << 6;
		    b = b | (src[i++] & 0x3F);
		}
		append((char) b);
	    }
    }
    public char[] toCharArray() {
        char[] nm = new char[used];
        System.arraycopy(chars, 0, nm, 0, used);
        return nm;
    }
    public void copyClear(CharBuffer cb) {
        char[] t = chars;
        chars = cb.chars;
        used = cb.used;
        cb.chars = t;
        cb.used = 0;
    }
    public void appendClear(CharBuffer cb) {
        if (used == 0) copyClear(cb);
        else { append(cb); cb.used = 0; }
    }
    public void clear() { used = 0; col = 0; maxcol = 0; }
    public int width() { return col > maxcol ? col : maxcol; }
    public String toString() { return new String(chars, 0, used); }
    public void writeTo(Writer w) throws IOException {
	w.write(chars, 0, used);
    }
    public String substring(int off, int end) {
        return new String(chars, off, end-off);
    }
    public void to(Writer w)
            throws IOException { to(w, 0, used); }
    public void to(Writer w, int st, int len)
            throws IOException { w.write(chars, st, len); }
    public boolean equals(Object o) {
        if (o instanceof String) {
            String s = (String)o;
            if (s.length() != used) return false;
            for (int i = used; --i >= 0; )
                if (chars[i] != s.charAt(i)) return false;
            return true;
        }
        return o == this;
    }
    public boolean equals(char[] o) {
        if (o.length != used) return false;
        for (int i = used; --i >= 0; )
            if (chars[i] != o[i]) return false;
        return true;
    }
    public void trim() {
        int st = 0;
        int end = used;
        while (st < end && chars[st] <= ' ') st++;
        while (st < end && chars[end-1] <= ' ') end--;
        if (st >= end) {
            trimTo(0);
        } else {
            int x = end-st;
            if (st > 0) System.arraycopy(chars, st, chars, 0, used);
            trimTo(x);
        }
    }
    public boolean endsWith(String s) {
        int len = s.length();
        if (len > used) return false;
        for (int i = used; --len >= 0; --i)
            if (chars[i] != s.charAt(len)) return false;
        return true;
    }
    public boolean startsWith(String s) {
        int len = s.length();
        if (len > used) return false;
        while (--len >= 0)
            if (chars[len] != s.charAt(len)) return false;
        return true;
    }
    public void needSpace() {
	int t = used;
	if(t>0 && chars[t-1]>' ')
	    append(' ');
    }
    
    /**
     * In addition to trimming the buffer, the method notifies TrimBufferObservers.
     * @param newUsed the new buffer limit
     */
    private void trimTo(int newUsed) {
        used = newUsed;
        for (TrimBufferObserver o : trimObservers) {
            o.trimmed(used);
        }
    }
    
    public void addTrimObserver(TrimBufferObserver o) {
        trimObservers.add(o);
    }
    public void nlTerm() {
	if(hasMargin())
	    needSpace();
	else {
	    int t = used;
	    if (t <= 0) return;
	    while (t > 0 && chars[t-1] <= ' ') t--; // NOI18N
            trimTo(t);
	    append('\n'); // NOI18N
	}
    }
    public void toLineStart() {
	if(hasMargin())
	    needSpace();
	else {
	    int t = used;
	    if (t <= 0) return;
	    while (t > 0 && chars[t-1] <= ' ' && chars[t-1] != '\n') t--; // NOI18N
            trimTo(t);
            col = 0;
	}
    }
    
    public void eatAwayChars(int count) {
        if (used <= 0) return;
        int nCol = 0;
        while (count > nCol && nCol <= used && chars[used-nCol] != '\n') nCol++; // NOI18N
        col = nCol;
        trimTo(used - nCol);
    }
    
    public void _blanklines(int n) {
        int numBlankLines = n = Math.max(lastBlankLines, n);
	if(hasMargin())
	    needSpace();
	else {
	    nlTerm();
            while(n-- > 0)
	        append('\n');
	}
        lastBlankLines = numBlankLines;
    }
    
    /**
     * Ensures there's exactly `n' blank lines between previous contents and end of the buffer.
     * The implementation attempts not to touch preceding completed lines unless it really needs to strip
     * some lines from the end. If the buffer ends with all-whitespace line but without line terminator,
     * this line is truncated to empty so that the buffer always ends with a newline character.
     * <p/>
     * This complicated algorithm helps to preserve boundaries of guarded blocks (see BlockSequences); with simpler
     * implementation, end of a guarded block might get truncated as it ended with whitespaces followed by a newline
     * and such line was trimmed by the previous (simpler) impl.
     * 
     * @param n desired number of blank lines
     */
    public void blanklines(int n) {
        int numBlankLines = n = Math.max(lastBlankLines, n);
	if(hasMargin())
	    needSpace();
	else {
	    int t = used;
	    if (t <= 0) {
                while(n-- > 0)
                    append('\n'); // NOI18N
                lastBlankLines = numBlankLines;
                return;
            }
            
            final int l = n + 1;
            int c = 0;
            int lastNl = -1;
            // ring buffer nli cycles through last n newlines, so newline at pointer is the -(n + 1)th newline found.
            int[] nlpos = new int[l];
            int nli = 0;
	    while (t > 0 && chars[t-1] <= ' ') { // NOI18N
                if (chars[t-1] == '\n') { // NOI18N
                    if (lastNl == -1) lastNl = t;
                    c++;
                    nlpos[nli] = t;
                    nli = (nli + 1) % l;
                }
                t--;
            }
            if (c == l) {
                this.used = lastNl - 1;
                // does some processing for '\n', e.g. updates col
                append('\n'); // NOI18N
            } else if (c > l) {
                int p = 0;
                if (n > 0) {
                    // discard some newlines, use the ring buffer
                    p = nli;
                }
                this.used = nlpos[p] - 1;
                // let the other vars adjust
                append('\n'); // NOI18N
            } else {
                // must generate some more newlines, trim the last line if unterminated
                if (lastNl > -1) {
                    this.used = lastNl;
                }
                while (c < l) {
                    c++;
                    append('\n'); // NOI18N
                }
            }
	}
        lastBlankLines = numBlankLines;
    }
    public boolean isWhitespaceLine() {
        if (col > 0) {
            int pos = used - 1;

            while (pos >= 0) {
                if (chars[pos] == '\n') return true; // NOI18N

                if (!Character.isWhitespace(chars[pos])) {
                    return false;
                }
                pos--;
            }
        }

        return true;
    }
    public int getCol() {
        return col;
    }
    
    interface TrimNotify {
        public void trimmed(int len);
    }
}
