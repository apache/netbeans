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
 * "ActiveTerm.java"
 * ActiveTerm.java 1.9 01/07/30
 */

package org.netbeans.lib.terminalemulator;

import java.awt.*;
import java.awt.event.*;

public class ActiveTerm extends StreamTerm {

    private ActiveTermListener at_listener;

    private final RegionManager rm;

    private Coord last_begin = null;
    private Coord last_end = null;

    /** Remembers the default mouse pointer icon used by Term */
    private final Cursor regularCursor;

    /** The mouse pointer icon to use why flying over hyperlinks */
    private final Cursor pointerCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

    /** Remember what cursor is currently active */
    private Cursor currentCursor;

    public ActiveTerm() {
	super();

        regularCursor = getScreen().getCursor();

	setCursorVisible(false);

	rm = regionManager();

	getScreen().addMouseListener(new MouseAdapter() {
            @Override
	    public void mouseClicked(MouseEvent e) {
		Point p = mapToBufRowCol(e.getPoint());
		BCoord c = new BCoord(p.y, p.x);
		Coord ac = new Coord(c, firsta());
		ActiveRegion region = rm.findRegion(ac);
		if (region != null) {
		    if (region.isSelectable())
			setSelectionExtent(region.getExtent());
		    if (at_listener != null)
			at_listener.action(region, e);
		}
	    }
	} );

	getScreen().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
	    public void mouseMoved(MouseEvent e) {
		Point p = mapToBufRowCol(e.getPoint());
		BCoord c = new BCoord(p.y, p.x);
		Coord ac = new Coord(c, firsta());

		ActiveRegion region = rm.findRegion(ac);
                if (region.isLink())
                    setCursorEfficiently(pointerCursor);
                else
                    setCursorEfficiently(regularCursor);

		ActiveRegion hl_region = findRegionToHilite(region);
		if (hl_region == null)
		    hilite(null, null);
		else 
		    hilite(hl_region.begin, hl_region.end);
	    }
	} );
    } 

    private void setCursorEfficiently(Cursor cursor) {
        if (cursor == currentCursor)
            return;
        currentCursor = cursor;
        getScreen().setCursor(cursor);
    }

    private ActiveRegion findRegionToHilite(ActiveRegion region) {
	if (region == null)
	    return null;
	else if (region.isFeedbackEnabled())
	    return region;
	else if (region.isFeedbackViaParent())
	    return findRegionToHilite(region.parent());
	else
	    return null;
    }

    public void setActionListener(ActiveTermListener listener) {
	this.at_listener = listener;
    } 

    private void hilite_help(Coord begin, Coord end, boolean on) {
	if (begin == null && end == null)
	    return;	// nothing to do
	setCharacterAttribute(begin, end, 9, on);
    }

    public void hilite(Coord begin, Coord end) {
	if (end != null && end.row == 1 && end.col == 0)
	    end = getCursorCoord();
	hilite_help(last_begin, last_end, false);
	last_begin = (begin == null)? null: (Coord) begin.clone();
	last_end = (end == null)? null: (Coord) end.clone();
        hilite_help(begin, end, true);        
    }

    public void hilite(ActiveRegion region) {
	hilite(region.begin, region.end);
    } 

    public ActiveRegion beginRegion(boolean hyperlink) {
	ActiveRegion region;
	try {
	    region = rm.beginRegion(getCursorCoord());
	} catch (RegionException x) {
            return new ActiveRegion(null, new Coord(), false);
	} 
        region.setParentAttrs(attrSave());
	if (hyperlink) {
	    setAttribute(34);		// fg -> blue
	    setAttribute(4);		// underline
	}
	return region;
    }

    public void endRegion() {
	Coord cursor = getCursorCoord();
	Coord bcursor = backup(cursor);

	// This only happens if we begin and end a region w/o any output
	// in between
	if (bcursor == null) {
	    bcursor = cursor;
        }

        ActiveRegion endedRegion;
	try {
	    endedRegion = rm.endRegion(bcursor);
	} catch (RegionException x) {
            return;
	}
	// OLD setAttribute(0);		// reset
        attrRestore(endedRegion.getParentAttrs());
    } 

    public ActiveRegion findRegion(Coord coord) {
	return rm.findRegion(coord);
    }

    @SuppressWarnings("empty-statement")
    public void cancelRegion() {
	try {
	    rm.cancelRegion();
	} catch (RegionException x) {
	    ;
	} 
    }
    
    @Override
    public void clear() {
        nullLasts();
        super.clear ();
    }

    @Override
    public void clearHistoryNoRefresh() {
        nullLasts();
	super.clearHistoryNoRefresh ();
    }

    /**
     * Create a hyperlink.
     * @param url
     * @param text
     */
    @Override
    protected void hyperlink(String url, String text) {
        // default implementation just dumps out the text
        ActiveRegion ar = beginRegion(true);
        ar.setLink(true);
        ar.setUserObject(url);
        for (char c : text.toCharArray())
            ops().op_char(c);
        endRegion();
    }
    
    private void nullLasts() {
        last_begin = null;
        last_end = null;        
    }
}
