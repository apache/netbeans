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
 * "Extent.java"
 * Extent.java 1.5 01/07/26
 */

package org.netbeans.lib.terminalemulator;

public class Extent {
    public Coord begin;
    public Coord end;

    public Extent(Coord begin, Coord end) {
	this.begin = (Coord) begin.clone();
	this.end = (Coord) end.clone();
    } 

    /**
     * Override Object.toString
     */
    public String toString() {
	return "Extent[" + begin + " " + end + "]";	// NOI18N
    } 

    /**
     * Ensure that 'begin' is before 'end'.
     */
    public Extent order() {
	if (begin.compareTo(end) > 0) {
	    Coord tmp = begin;
	    begin = end;
	    end = tmp;
	}
	return this;
    }

    /*
     * Return true if selection intersects the given row/column
     */
    public boolean intersects(int arow, int col) {
	if (begin.row > arow)
	    return false;
	else if (end.row < arow)
	    return false;
	else if (begin.row == end.row)
	    return col >= begin.col && col <= end.col;
	else if (arow == begin.row)
	    return col >= begin.col;
	else if (arow == end.row)
	    return col <= end.col;
	else
	    return true;
    }
}
