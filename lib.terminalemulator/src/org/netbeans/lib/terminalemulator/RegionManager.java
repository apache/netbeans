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
 * "RegionManager.java"
 * RegionManager.java 1.7 01/07/10
 */

package org.netbeans.lib.terminalemulator;


public class RegionManager {
    private ActiveRegion root = new ActiveRegion(null, new Coord(), false);
    private ActiveRegion parent = null;
    private ActiveRegion region = root;

    /**
     * Eliminates all regions.
     */
    public void reset() {
	root = new ActiveRegion(null, new Coord(), false);
	parent = null;
	region = root;
    } 

    /**
     * Returns the always-present "root" ActiveRegion.
     * @return The always-present "root" ActiveRegion.
     */
    public ActiveRegion root() {
	return root;
    } 

    /**
     * Creates a new active region.
     * <p>
     * Any text at and after 'begin' will belong to this region.
     * <p>
     * Active regions can be nested.
     */
    public ActiveRegion beginRegion(Coord begin) throws RegionException {
	if (region != null) {
	    // begin new nested region
	    ActiveRegion child = new ActiveRegion(region, begin, true);
	    region.addChild(child);
	    parent = region;
	    region = child;
	    
	} else {
	    // begin new region at current level of nesting
	    region = new ActiveRegion(parent, begin, false);
	    parent.addChild(region);
	}

	return region;
    }

    /**
     * Declare the end of the current active region.
     * <p>
     * Any text before and at 'end' will belong to this region.
     */
    public ActiveRegion endRegion(Coord end) throws RegionException {
	if (region == null) {
	    throw new RegionException("endRegion(): ",		// NOI18N
				      "no current active region");// NOI18N
	} else if (region == root) {
	    throw new RegionException("endRegion(): ",		// NOI18N
				      "cannot end root region");// NOI18N
	} 

	region.setEnd(end);

        ActiveRegion endedRegion = region;

	if (region.nested) {
	    region = parent;
	    parent = region.parent;
	} else {
	    region = null;
	}

        return endedRegion;
    }

    /**
     * Eliminate the current region.
     * <p>
     * If cancelRegion is issued between a beginRegion and endRegion the
     * region that was begun is cancelled as if beginregion was never called.
     */
    public void cancelRegion() throws RegionException {
	if (region == null) {
	    throw new RegionException("cancelRegion(): ", // NOI18N
				      "no current active region"); // NOI18N
	} else if (region == root) {
	    throw new RegionException("cancelRegion(): ", // NOI18N
				      "cannot cancel root region"); // NOI18N
	}

	parent.removeChild(region);
	region = null;
    }

    /* OLD
    public ActiveRegion findRegion(Point p) {
	final Coord coord = new Coord();
	coord.row = p.y;
	coord.col = p.x;
	return findRegion(coord);
    } 
    */

    /*
     * Return the most deeply nested ActiveRegion containing 'coord'.
     * <p>
     * If no ActiveRegion is found root is returned.
     */
    public ActiveRegion findRegion(Coord acoord) {
	// What happens if we haven't closed the current/all regions?
	return root.contains(acoord);
    } 

    /**
     * Adjust coordinates when when absolute coordinates roll over.
     */
    void relocate(int from, int to) {
	int delta = to - from;
	root.relocate(delta);
    }

    /**
     * Cull any regions that are before origin.
     * <p>
     * For now we use an aggressive strategy where if any part is gone,
     * all of the region will go. This simplifies the algorithm
     * considerably and is forward compatible in the sense that if in
     * the future we decide to delete based on the region end, and 
     * adjust 'begin', that would show up as an improvement not a regression.
     * 
     */
    void cull(int origin) {
	root.cull(origin);
    } 
}
