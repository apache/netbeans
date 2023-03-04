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
 * "State.java"
 * State.java 1.11 01/07/26
 */

package org.netbeans.lib.terminalemulator;

class State {
    public int rows;

    // Index of Line visible on top of the canvas (0-origin)
    public int firstx;
    public int firsty;

    // Cursor is in "cell" coordinates
    public BCoord cursor = new BCoord();

    public void adjust(int amount) {
	firstx += amount;
	if (firstx < 0)
	    firstx = 0;

	cursor.row += amount;
	if (cursor.row < 0)
	    cursor.row = 0;
    } 

    // Current attribute as defined by class Attr
    public int attr;

    // If 'true' characters replace what's under cursor (default)
    // If 'false' act as an insert operation.
    public boolean overstrike = true;


    /*
     * Cursor saving and restoration.
     * Saved values are not adjusted!
     */
    public void saveCursor() {
	saved_cursor = (BCoord) cursor.clone();
    }
    public void restoreCursor() {
	if (saved_cursor != null) {
	    cursor = saved_cursor;
	    saved_cursor = null;
	}
    }
    private BCoord saved_cursor = null;

    private final int g[] = new int[] {10, 10, 10, 10};
    private int gl = 0;         // index into 'g'

    public void setG(int gx, int fx) {
        assert gx >= 0 && gx <= 3;
        assert fx >= 0 && fx <= 9;
        g[gx] = fx;
    }

    public void selectGL(int gx) {
        assert gx >= 0 && gx <= 3;
        gl = gx;
    }


    /*
     * Calculate the font.
     */
    public int font() {
        return g[gl];
    }
} 
