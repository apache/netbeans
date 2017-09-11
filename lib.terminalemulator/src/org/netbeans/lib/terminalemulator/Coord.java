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
 *			"Portions Copyrighted [year] [name of copyright owner]"
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

package org.netbeans.lib.terminalemulator;

/**
 * A cartesian coordinate class, similar to Point.
 * The equivalent of 'offset' in swing.text.Document.
 * <br>
 * Rows are 0-origin, columns are 0-origin.
 * <p>
 * Why not the regular Java Point? Because ...
 * <ul>
 * <li>Point with 'x' and 'y' is not as clear.
 * <li>Point doesn't implement Comparable, which we depend on a lot.
 * </ul>
 */

public final class Coord implements Comparable<Coord> {
    public int row;
    public int col;

    /**
     * Create a Coord at the origin (top-left)
     */
    public Coord() {
	this.row = 0;
	this.col = 0;
    } 

    private Coord(int row, int col) {
	// Note this is flipped from Points(x, y) sense
	this.row = row;
	this.col = col;
    } 

    public static Coord make(int row, int col) {
	// 'row' is in absolute coordinates
	return new Coord(row, col);
    }

    public Coord(Coord coord) {
	this.row = coord.row;
	this.col = coord.col;
    } 

    Coord(BCoord coord, int bias) {
	this.row = coord.row + bias;
	this.col = coord.col;
    }

    BCoord toBCoord(int bias) {
	int new_row = row - bias;
	if (new_row < 0)
	    return new BCoord(0, 0);	// we're out of history
	else
	    return new BCoord(new_row, col);
    }

    public void copyFrom(Coord src) {
	this.row = src.row;
	this.col = src.col;
    } 

    // Overrides of Object:

    @Override
    public Object clone() {
	return new Coord(row, col);
    } 

    @Override
    public String toString() {
	return "(r=" + row + ",c=" + col + ")";	// NOI18N
    } 

    /**
     * Examples:
     * To satisfy Comparable.
     * <p>
     * <pre>
     * a &lt b	=== a.compareTo(b) &lt 0
     * a &gt= b	=== a.compareTo(b) &gt= 0
     * </pre>
     */
    @Override
    public int compareTo(Coord that) throws ClassCastException {
	// -1 or negative  -> this < o
	//  0              -> this == o
	// +1 or positive  -> this > o

	if (this.row < that.row)
	    return -1;
	else if (this.row > that.row)
	    return +1;
	else {
	    return this.col - that.col;
	}
    } 

    /* OLD public */ void clip(int rows, int cols, int bias) {
	// BE CAREFUL and clip view Coords with a view box and buffer
	// Coords with a buffer box!
	/* OLD
	if (row < 0)
	    row = 0;
	else if (row > rows)
	    row = rows;
	*/

	if (row < bias)
	    row = bias;
	else if (row > bias + rows)
	    row = bias + rows;

	if (col < 0)
	    col = 0;
	else if (col > cols)
	    col = cols;
    }
} 
