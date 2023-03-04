/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * "Line.java"
 * Line.java 1.12 01/07/24
 */

package org.netbeans.lib.terminalemulator;

final class Line {
    public int glyph_glyph;
    public int glyph_rendition;	// Background color for the whole line
    				// This is independent of per-character
				// rendition.

    private char buf[];		// actual characters
    private int attr[];		// attributes (allocated on demand)
    
    // SHOULD use shorts?
    private int capacity;	// == buf.length == attr.length
    private int length;		// how much of buf and attr is filled
    
    
    public Line() {
        reset();
    }
    
    public void reset() {
        length = 0;
        capacity = 32;
        buf = new char[capacity];
        attr = null;
        glyphId = 0;
        backgroundColor = 0;
        wrapped = false;
        about_to_wrap = false;
    }

    public void reset(Term term, int end, int eraseAttr) {
        reset();
        clearToEndFrom(term, 0, end, eraseAttr);
    }
    
    public int capacity() {
        return capacity;
    }
    
    /**
     * Number of characters in the line.
     * charArray()[length()] is where the cursor or newline would be.
     *
     */
    public int length() {
        return length;
    }
    
    public boolean hasAttributes() {
        return attr != null;
    }
    
    private int glyphId;
    public void setGlyphId(int glyphId) {
        this.glyphId = glyphId;
    }
    public int getGlyphId() {
        return glyphId;
    }
    
    private int backgroundColor;// Background color for the whole line
    // This is independent of per-character
    // rendition.
    public void setBackgroundColor(int bgColor) {
        backgroundColor = bgColor;
    }
    public int getBackgroundColor() {
        return backgroundColor;
    }
    
    public void setWrapped(boolean wrapped) {
        this.wrapped = wrapped;
    }
    public boolean isWrapped() {
        return wrapped;
    }
    // SHOULD collapse wrapped with about_to_wrap into a bitfield
    private boolean wrapped;
    
    
    
    public boolean setAboutToWrap(boolean about_to_wrap) {
        boolean old_about_to_wrap = about_to_wrap;
        this.about_to_wrap = about_to_wrap;
        return old_about_to_wrap;
    }
    public boolean isAboutToWrap() {
        return about_to_wrap;
    }
    // Perhaps SHOULD have a state: normal, about-to-wrap, wrapped.
    private boolean about_to_wrap;
    
    
    /**
     * Return true if we've already seen attributes for this line
     * or 'a' is the first one, in which case we allocate the 'attr' array.
     */
    private boolean haveAttributes(int a) {
        if (attr == null && a != 0) {
            attr = new int[capacity];
        }
        return attr != null;
    }
    
    
    public void accumulateInto(int bcol, int ecol, StringBuffer buf) {
        buf.append(this.buf, bcol, ecol-bcol+1);
    }
    
    public char charAt(int col) {
        return buf[col];
    }
    
    private void charAtPut(int col, char c) {
        buf[col] = c;
    }
    
    @Override
    public String toString() {
        assert false;
        return new String(buf);
    }
    
    public char[] getChars(char[] array) {
        System.arraycopy(buf, 0, array, 0, length);
        return array;
    }
    
    public int [] attrArray() {
        return attr;
    }
    
    
    public byte width(MyFontMetrics metrics, int at) {
        if (at >= capacity)
            return 1;
        return (byte) metrics.wcwidth(charAt(at));
    }
    
    /*
     * Convert a cell column to a buffer column.
     */
    public int cellToBuf(MyFontMetrics metrics, int target_col) {
        if (metrics.isMultiCell()) {
            int bc = 0;
            int vc = 0;
            for(;;) {
                int w = width(metrics, bc);
                if (vc + w - 1 >= target_col)
                    break;
                vc += w;
                bc++;
            }
            return bc;
        } else {
            return target_col;
        }
    }
    
    /*
     * Convert a buffer column to a cell column.
     */
    public int bufToCell(MyFontMetrics metrics, int target_col) {
        if (metrics.isMultiCell()) {
            int vc = 0;
            for (int bc = 0; bc < target_col; bc++) {
                vc += width(metrics, bc);
            }
            return vc;
        } else {
            return target_col;
        }
    }
    
    
    
    public StringBuffer stringBuffer() {
        // only used for word finding
        // Grrr, why don't we have: new StringBuffer(buf, 0, length);

        // This gets passed on to WordDelineator which is part of the public API
        @SuppressWarnings("StringBufferMayBeStringBuilder")

        StringBuffer sb = new StringBuffer(length);
        return sb.append(buf, 0, length);
    }
    
    /*
     * Ensure that we have at least 'min_capacity'.
     */
    private void ensureCapacity(Term term, int min_capacity) {
        
        term.noteColumn(this, min_capacity);
        
        if (min_capacity <= capacity)
            return;	// nothing to do
        
        // doubling
        int new_capacity = (length+1) * 2;
        if (new_capacity < 0)
            new_capacity = Integer.MAX_VALUE;
        else if (min_capacity > new_capacity)
            new_capacity = min_capacity;
        
        
        char new_buf[] = new char[new_capacity];
        System.arraycopy(buf, 0, new_buf, 0, length);
        buf = new_buf;
        
        if (attr != null) {
            int new_attr[] = new int[new_capacity];
            System.arraycopy(attr, 0, new_attr, 0, length);
            attr = new_attr;
        }
        
        capacity = new_capacity;
    }

    private void fillGap(int col, int eraseAttr) {
        // fill any newly opened gap (between length and col) with SP
        for (int cx = length; cx < col; cx++)
            charAtPut(cx, ' ');
        if (haveAttributes(eraseAttr)) {
            for (int cx = length; cx < col; cx++)
                attr[col] = eraseAttr;
        }
    }
    
    /**
     * Insert character and attribute at 'column' and shift everything
     * past 'column' right.
     */
    public void insertCharAt(Term term, char c, int column, int a) {
        int new_length = length + 1;
        
        if (column >= length) {
            new_length = column+1;
            ensureCapacity(term, new_length);
            fillGap(column, 0);                 // SHOULD be eraseAttr?
        } else {
            ensureCapacity(term, new_length);
            System.arraycopy(buf, column, buf, column + 1, length - column);
            if (haveAttributes(a))
                System.arraycopy(attr, column, attr, column + 1, length - column);
        }
        
        term.checkForMultiCell(c);
        
        charAtPut(column, c);
        if (haveAttributes(a))
            attr[column] = a;
        
        length = new_length;
    }
    
    /*
     * Generic addition and modification.
     * Line will grow to accomodate column.
     */
    public void setCharAt(Term term, char c, int column, int a, int eraseAttr) {
        if (column >= length) {
            ensureCapacity(term, column+1);
            fillGap(column, eraseAttr);
            length = column+1;
        }
        term.checkForMultiCell(c);
        if (column < 0) {
            throw new IllegalArgumentException(String.format("column=%d; buf_length=%d; buf_capacity=%d", column, length, capacity));   //NOI18N
        }
        charAtPut(column, c);
        if (haveAttributes(a)) {
            attr[column] = a;
        }
    }
    
    public void deleteCharAt(int column) {
        if (column < 0 || column >= length)
            return;
        System.arraycopy(buf, column+1, buf, column, length-column-1);
        charAtPut(length-1, (char) 0);
        if (attr != null) {
            System.arraycopy(attr, column+1, attr, column, length-column-1);
            attr[length-1] = 0;
        }
        // SHOULD move this up
        length--;
    }
    
    public void clearToEndFrom(Term term, int from, int end, int eraseAttr) {
        ensureCapacity(term, end+1);
        fillGap(end, eraseAttr);

        // Grrr, why is there a System.arrayCopy() but no System.arrayClear()?
        for (int cx = from; cx <= end; cx++)
            charAtPut(cx, ' ');
        if (haveAttributes(eraseAttr)) {
            for (int cx = from; cx <= end; cx++)
                attr[cx] = eraseAttr;
        }
        length = end+1;
    }

    public void clearTo(Term term, int col, int eraseAttr) {
        ensureCapacity(term, col+1);
        fillGap(col, eraseAttr);

        for (int cx = 0; cx <= col; cx++)
            charAtPut(cx, ' ');
        if (haveAttributes(eraseAttr)) {
            for (int cx = 0; cx <= col; cx++)
                attr[cx] = eraseAttr;
        }
        if (length < col+1)
            length = col+1;
    }

    public void clearFromTo(Term term, int from, int to, int eraseAttr) {
        if (from > to)
            return;
        ensureCapacity(term, to+1);
        fillGap(to, eraseAttr);

        for (int cx = from; cx <= to; cx++)
            charAtPut(cx, ' ');
        if (haveAttributes(eraseAttr)) {
            for (int cx = from; cx <= to; cx++)
                attr[cx] = eraseAttr;
        }
        if (length < to+1)
            length = to+1;
    }
    
    /*
     * Used for selections
     * If the ecol is past the actual line length a "\n" is appended.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    public String text(int bcol, int ecol) {
        /* DEBUG
        System.out.println("Line.text(bcol " + bcol + " ecol " + ecol + ")");	// NOI18N
        System.out.println("\tlength " + length);	// NOI18N
         */
        
        
        String newline = "";	// NOI18N
        
        // This only happens for 'empty' lines below the cursor.
        // Actually it also happens for 'empty' lines in the middle.
        // See issue 31951 for example.
        // So in order to get newlines in selected text we will also get
        // newlines from the 'empty' lines below the cursor.
        // This is in fact what xterm does.
        // DtTerm doesn't allow selection of the 'empty' lines below the
        // cursor, but that is issue 21577 and is not easy to solve.
        
        if (length == 0)
	    return "\n"; // NOI18N
        
        if (ecol >= length) {
            // The -1 snuffs out the newline.
            ecol = length-1;
            newline = "\n";	// NOI18N
            
            if (bcol >= length)
                bcol = length-1;
        }
        int count = ecol - bcol + 1;
        if (bcol < 0 ||  count < 0) {
            throw new IllegalArgumentException(String.format("offset=%d; count=%d; buf_length=%d; buf_capacity=%d", bcol, count, length, capacity));    //NOI18N
        }
        return new String(buf, bcol, count) + newline;
    }
    
    public void setCharacterAttribute(int bcol, int ecol,
            int value, boolean on) {
        // HACK: value is the ANSI code, haveAttributes takes out own
        // compact encoding, but it only checks for 0 so it's OK.
        if (!haveAttributes(value))
            return;
        
	try {
	    if (on) {
		for (int c = bcol; c <= ecol; c++)
		    attr[c] = Attr.setAttribute(attr[c], value);
	    } else {
		for (int c = bcol; c <= ecol; c++)
		    attr[c] = Attr.unsetAttribute(attr[c], value);
	    }
	} catch (ArrayIndexOutOfBoundsException x) {
	    System.out.printf("bcol %d  ecol %d  capacity %d  length %d buf %d  attr %d", // NOI18N
		    bcol, ecol, capacity, length, buf.length, attr.length);
	    throw x;
	}
    }
}
