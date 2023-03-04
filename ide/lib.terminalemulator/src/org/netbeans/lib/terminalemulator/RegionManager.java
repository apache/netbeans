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
