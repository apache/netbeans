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

/*
 * "Line.java"
 * Line.java 1.10 01/07/10
 */

package org.netbeans.lib.terminalemulator;

class Line {
    public int glyph_glyph;
    public int glyph_rendition;	// Background color for the whole line
    				// This is independent of per-character
				// rendition.
    private char buf[];
    private int attr[];

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
	Attr = null;
	glyph_glyph = 0;
	glyph_rendition = 0;
	wrapped = false;
	about_to_wrap = false;
    } 


    public int capacity() {
	return Capacity;
    } 
    public int length() {
	return Length;
    } 

    public boolean hasAttributes() {
	return attr != null;
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


    public Char [] charArray() {
	return buf;
    }
    public int [] attrArray() {
	return attr;
    }


    public StringBuffer stringBuffer() {
	// only used for word finding
	// Grrr, why don't we have: new StringBuffer(buf, 0, length);
	StringBuffer sb = new StringBuffer(length);
	return sb.append(buf, 0, length);
    } 

    /*
     * Ensure that our capacity is min_capacity rounded up to 8.
     */
    private void ensureCapacity(int min_capacity) {

	if (min_capacity <= capacity)
	    return;	// nothing to do

	/* OLD

	// constant increments

	int new_capacity = min_capacity;

	// round up to 8
	new_capacity &= ~0x7;
	new_capacity += 8;
	*/

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

    /**
     * Insert character and attribute at 'column' and shift everything 
     * past 'column' right.
     */
    public void insertCharAt(char c, int column, int a) {
	int new_length = length + 1;
	ensureCapacity(new_length);

	System.arraycopy(buf, column, buf, column + 1, length - column);
	buf[column] = c;

	if (haveAttributes(a)) {
	    System.arraycopy(attr, column, attr, column + 1, length - column);
	    attr[column] = a;
	}

	length = new_length;
    }

    /*
     * Generic addition and modification.
     * Line will grow to accomodate column.
     */
    public void setCharAt(char c, int column, int a) {
	if (column >= length) {
	    ensureCapacity(column+1);
	    length = column+1;
	}
	buf[column] = c;
	if (haveAttributes(a)) {
	    attr[column] = a;
	}
    } 

    public void deleteCharAt(int column) {
	if (column < 0 || column >= length)
	    return;
	System.arraycopy(buf, column+1, buf, column, length-column-1);
	buf[length-1] = 0;
	if (attr != null) {
	    System.arraycopy(attr, column+1, attr, column, length-column-1);
	    attr[length-1] = 0;
	}
	// SHOULD move this up
	length--;
    }

    public void clearToEndFrom(int col) {
	ensureCapacity(col+1);

	// Grrr, why is there a System.arrayCopy() but no System.arrayClear()?
	for (int cx = col; cx < length; cx++)
	    buf[cx] = 0;
	if (attr != null) {
	    for (int cx = col; cx < length; cx++)
		attr[cx] = 0;
	}
	length = col;
    } 


    /*
     * Used for selections
     * If the ecol is past the actual line length a "\n" is appended.
     */
    public String text(int bcol, int ecol) {
	// System.out.println("Line.text(bcol " + bcol + " ecol " + ecol + ")");
	// System.out.println("\tlength " + length);

	String newline = "";

	// this only happens for "empty" lines below the cursor.
	if (length == 0)
	    return "";

	if (ecol >= length) {
	    // The -1 snuffs out the newline.
	    ecol = length-1;
	    newline = "\n";

	    if (bcol >= length)
		bcol = length-1;
	}
	return new String(buf, bcol, ecol-bcol+1) + newline;
    }

    public void setCharacterAttribute(int bcol, int ecol,
				      int value, boolean on) {
	// HACK: value is the ANSI code, haveAttributes takes out own
	// compact encoding, but it only checks for 0 so it's OK.
	if (!haveAttributes(value))
	    return;

	if (on) {
	    for (int c = bcol; c <= ecol; c++)
		attr[c] = Attr.setAttribute(attr[c], value);
	} else {
	    for (int c = bcol; c <= ecol; c++)
		attr[c] = Attr.unsetAttribute(attr[c], value);
	}
    }
}
