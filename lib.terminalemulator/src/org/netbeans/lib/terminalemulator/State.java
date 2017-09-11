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
