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

final class BCoord implements Comparable<BCoord> {
    public int row;
    public int col;

    /**
     * Create a BCoord at the origin (top-left)
     */
    public BCoord() {
	this.row = 0;
	this.col = 0;
    } 

    public BCoord(int row, int col) {
	// Note this is flipped from Points(x, y) sense
	this.row = row;
	this.col = col;
    } 

    public BCoord(BCoord coord) {
	this.row = coord.row;
	this.col = coord.col;
    } 

    public void copyFrom(BCoord src) {
	this.row = src.row;
	this.col = src.col;
    } 

    // Overrides of Object:

    @Override
    public Object clone() {
	return new BCoord(row, col);
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
    public int compareTo(BCoord that) throws ClassCastException {
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

    public void clip(int rows, int cols) {
	// BE CAREFUL and clip view BCoords with a view box and buffer
	// BCoords with a buffer box!
	if (row < 0)
	    row = 0;
	else if (row > rows)
	    row = rows;
	if (col < 0)
	    col = 0;
	else if (col > cols)
	    col = cols;
    }
} 
