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
