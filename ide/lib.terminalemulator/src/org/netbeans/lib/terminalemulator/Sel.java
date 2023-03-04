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
 * "Sel.java"
 * Sel.java 1.22 01/07/26
 */

package org.netbeans.lib.terminalemulator;

import java.awt.*;
import java.awt.datatransfer.*;

/**
 * Selection expert:
 * <ul>
 * <li> Tracks gestures
 * <li> Retrieves actual selected text
 * <li> Paints the selection into a given Graphic
 * </ul>
 *
 * The following actions are understood and dealt with:
 * <dl>
 * <dt> track()
 * <dd> in general extends a selection.
 *   It is usually connecetd to a mouse drag event.
 * <dt> track()
 * <dd> will initiate a character-sized selection if no selection exists.
 * <dt> select_word()
 * <dd> selects a "word" after which track() extends the selection by words.
 *   It is usually connected to a left double-click.
 * <dt> slect_line()
 * <dd> selects a line after which track() extend the selection by lines.
 *   It is usually connected to a left triple-click.
 * <dt> done()
 * <dd> stops tracking. The current selection is stuffed into the clipboard.
 *   It is usually connected to a left release.
 * <dt> extend()
 * <dd> only extends an existing selection. (Doesn't start one like track)
 *   It is usually connected to a mouse right-press.
 * <dt> cancel() nulls the selection.
 * </dl>
 * <p>
 * A Selection has two components that need tracking, the visual feedback
 * and the selected text. As the history scrolls out the visual feedback
 * will be eliminated, but we (SHOULD but don't yet) retain the text so
 * that copy actions still retreieve it properly. DtTerm actually
 * suffers from this.
 * <p>
 * The selection has an origin and an extent. In general the origin and
 * extent are not ordered (that is extent might be _before_ origin). they
 * get ordered as needed.
 */

class Sel implements ClipboardOwner {
    // What granularity of selection are we working with
    private static final int SEL_NONE = 0;
    private static final int SEL_CHAR = 1;
    private static final int SEL_WORD = 2;
    private static final int SEL_LINE = 3;

    // See intersection() for explanation.
    public static final int INT_NONE = 0;
    public static final int INT_ABOVE = 1;
    public static final int INT_ON = 2;
    public static final int INT_STRADDLES = 3;
    public static final int INT_BELOW = 4;

    private int sel_tracking;
    private int old_sel_tracking;

    // origin and extent are kept in absolute RowCol coordinates
    private Coord sel_origin;
    /* TMP private */ public  Coord sel_extent;

    // The word that was initially selected by select_word
    // Used by 'track()'.
    private Extent initial_word;

    private Term term;
    private State state;

    // properties:
    private Color color = new Color(204, 204, 255);  // swing color
    void setColor(Color color) { this.color = color; } 
    Color getColor() { return color; } 
    
    private Color xor_color = Color.white;
    void setXORColor(Color color) { xor_color = color; } 
    Color getXORColor() { return xor_color; }

    Sel(Term term, State state) {
	this.term = term;
	this.state = state;
    } 

    /**
     * Adjust the selection against 'afirstline'.
     * <p>
     * As the selection reaches the top of the history buffer it will get
     * trimmed until eventually all of it will go away.
     *
     * This form doesn't work if the selection is "split" by insertion of
     * lines. Maybe we SHOULD have two arguments, adjust origin and adjust
     * extent?
     */
    void adjust(int afirstline, int amount, int alastline) {

	if (sel_origin == null)
	    return;

	/* DEBUG
	System.out.println("Sel.adjust origin " + sel_origin.row + " " + sel_origin.col);	// NOI18N
	System.out.println("Sel.adjust extent " + sel_extent.row + " " + sel_extent.col);	// NOI18N
	System.out.println("Sel.adjust afirstline " + afirstline + "  amount " + amount + "  alastline " + alastline);	// NOI18N
	*/

	if (sel_origin.compareTo(sel_extent) >= 0) {
	    // extent before origin
	    sel_extent.row += amount;
	    if (sel_extent.row < afirstline)
		sel_extent.row = afirstline;

	    sel_origin.row += amount;
	    if (sel_origin.row >= alastline) {
		sel_origin.row = alastline-1;
		sel_origin.col = term.buf().totalCols();
	    }
	    if (sel_origin.row < afirstline || sel_extent.row > alastline) {
		// it has completely vanished
		sel_extent = null;
		sel_origin = null;
	    }

	} else {
	    // origin before extent
	    sel_origin.row += amount;
	    if (sel_origin.row < afirstline)
		sel_origin.row = afirstline;

	    sel_extent.row += amount;
	    if (sel_extent.row >= alastline) {
		sel_extent.row = alastline-1;
		sel_extent.col = term.buf().totalCols();
	    }
	    if (sel_extent.row < afirstline || sel_origin.row > alastline) {
		// it has completely vanished
		sel_origin = null;
		sel_extent = null;
	    }
	}

	term.fireSelectionExtentChanged();
    }

    void relocate(int from, int to) {
	if (sel_origin == null)
	    return;
	int delta = to - from;
	sel_origin.row += delta;
	sel_extent.row += delta;
    }

    Extent getExtent() {
	if (sel_origin == null)
	    return null;
	Extent x = new Extent(sel_origin, sel_extent);
	x.order();
	return x;
    }

    void setExtent(Extent extent) {
	cancel(false);
	extent.order();
	sel_origin = (Coord) extent.begin.clone();
	sel_extent = (Coord) extent.end.clone();
	done(/* OLD false */);		// so it makes it into clipboard
    }

    public void select_word(Extent range) {
	sel_origin = new Coord(range.begin);
	sel_extent = new Coord(range.end);
	sel_tracking = Sel.SEL_WORD;
	old_sel_tracking = Sel.SEL_NONE;
	initial_word = range;
    }

    public void select_line(Coord coord) {

	// LATER coord.clip(term.buf.nlines, term.buf.totalCols(), term.firsta);

	sel_origin = Coord.make(coord.row, 0);
	sel_extent = Coord.make(coord.row, term.buf().totalCols());
	sel_tracking = Sel.SEL_LINE;
	old_sel_tracking = Sel.SEL_NONE;
    }
    
    public void select_line(Extent range) {
	sel_origin = new Coord(range.begin);
	sel_extent = new Coord(range.end);
	sel_tracking = Sel.SEL_LINE;
	old_sel_tracking = Sel.SEL_NONE;
    }

    private boolean extend_work(Coord p, int tracking) {
	/*
	 * return true if a screen refresh is needed
	 */
	if (tracking == Sel.SEL_NONE) {
	    return false;

	} else if (tracking == Sel.SEL_CHAR) {
	    sel_extent = p;

	} else if (tracking == Sel.SEL_WORD) {
	    BExtent Bnew_range = term.buf().find_word(term.getWordDelineator(), p.toBCoord(term.firsta()));
	    Extent new_range = Bnew_range.toExtent(term.firsta());
	    if (p.compareTo(initial_word.begin) < 0) {
		sel_origin = new Coord(new_range.begin);
		sel_extent = initial_word.end;
	    } else if (p.compareTo(initial_word.end) > 0) {
		sel_origin = initial_word.begin;
		sel_extent = new Coord(new_range.end);
	    } else {
		sel_origin = initial_word.begin;
		sel_extent = initial_word.end;
	    }

	} else if (tracking == Sel.SEL_LINE) {
	    if (p.compareTo(sel_origin) > 0) {
		sel_origin = Coord.make(sel_origin.row, 0);
		sel_extent = Coord.make(p.row, term.buf().totalCols());
	    } else {
		sel_origin = Coord.make(sel_origin.row, term.buf().totalCols());
		sel_extent = Coord.make(p.row, 0);
	    }
	}
	return true;
    }

    public void track(Coord p) {
	if (sel_tracking == Sel.SEL_NONE) {
	    // initiate a selection
	    sel_origin = p;
	    sel_extent = p;
	    sel_tracking = Sel.SEL_CHAR;
	    old_sel_tracking = Sel.SEL_NONE;
	}
	extend_work(p, sel_tracking);
    }

    public boolean extend(Coord p) {
	// return true if a screen refresh is needed
	if (sel_origin == null)
	    return false;
	else
	    return extend_work(p, old_sel_tracking);
    }

    /*
     * Variation on cancel which doesn't update the Selection.
     * Used by lostOwnership() in addition to plain cancel().
     */
    private boolean cancelHelp(boolean and_fire) {
	if (sel_origin == null)
	    return false;
	old_sel_tracking = Sel.SEL_NONE;
	sel_tracking = Sel.SEL_NONE;
	sel_origin = null;
	sel_extent = null;
	initial_word = null;

	if (and_fire)
	    term.fireSelectionExtentChanged();

	return true;
    }

    public boolean cancel(boolean and_fire) {
	if (!cancelHelp(and_fire))
	    return false;
	term.copyToSelection();
	return true;
    }

    public void done(/* OLD boolean force_copy */) {
	// don't track anymore
	// but extend will still work
	old_sel_tracking = sel_tracking;
	sel_tracking = Sel.SEL_NONE;

	term.copyToSelection();

	term.fireSelectionExtentChanged();
    }

    public void lostOwnership(Clipboard cb, Transferable c) {
	/*
	 * Part of the ClipboardOwner interface.
	 * The string created in sel_done should be retained until
	 * this function is called.
	 */
	/* DEBUG
	System.out.println("lostOwnership()");	// NOI18N
	*/
	if (cancelHelp(true))
	    term.repaint(false);
    } 

    public String getSelection() {

	Extent x = getExtent();
	if (x == null)
	    return null;

        if (x.begin != null && x.end != null) {
            final StringBuffer text = new StringBuffer();
            term.visitLines(x.begin, x.end, true, new LineVisitor() {
                public boolean visit(Line l, int row, int bcol, int ecol) {
                    text.append(l.text(bcol, ecol));
                    return true;
                }
            } );
            return text.toString();
        }

	return ""; //NOI18N
    }

    /*
     * Helps decide what to do with the selection when a line is 
     * added, removed or cleared.
     */
    int intersection(int line) {
	/* DEBUG
	if (sel_origin == null) {
	    System.out.println("Sel.intersection(" + line + ") no selection");	// NOI18N
	} else {
	    System.out.println("Sel.intersection(" + line + ")" +	// NOI18N
		"  sel_origin.row = " + sel_origin.row + 	// NOI18N
		"  sel_extent.row = " + sel_extent.row);	// NOI18N
	}
	*/

	Extent x = getExtent();
	if (x == null)
	    return INT_NONE;
	x.order();

	if (x.end.row < line)
	    return INT_ABOVE;
	else if (x.end.row == line)
	    return INT_ON;
	else if (x.begin.row > line)
	    return INT_BELOW;
	else
	    return INT_STRADDLES;
    }

    /**
     * Select inside one line
     * Rows and columns are in absolute coords.
     */
    private void paint(Graphics g, int row, int bcol, int ecol) {

	// Instead of doing this SHOULD clip the Extent to what's in view

	// Row is outside the view
	if (row < state.firstx)
	    return;
	if (row > state.firstx + state.rows)
	    return;

	// Construct the rectangle we're going to paint
	BCoord begin = new BCoord(row, bcol);
	BCoord end = new BCoord(row, ecol);

	begin = term.toViewCoord(begin);
	end = term.toViewCoord(end);
        
        //Hotfix for issue 40189
        if (begin == null || end == null) {
            return;
        }

	int lw;		// width of last character in selection
	Line l = term.buf().lineAt(row);
	lw = l.width(term.metrics(), ecol);

	Point pbegin = term.toPixel(begin);
	Point pend = term.toPixel(end);
	pend.y += term.metrics().height;
	pend.x += term.metrics().width * lw;	// xterm actually doesn't do this

	Dimension dim = new Dimension(pend.x - pbegin.x,
				      pend.y - pbegin.y);
	Rectangle rect = new Rectangle(pbegin, dim);


	if (term.isSelectionXOR())
	    g.setXORMode(xor_color);
	else
	    g.setColor(color);

	g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    void paint(final Graphics g) {
	/*
	 * Paint the selection.
	 */

	Extent x = getExtent();
	if (x == null)
	    return;
	x.order();

	// DEBUG System.out.println("Sel.paint extent: " + x);	// NOI18N

	term.visitLines(x.begin, x.end, true, new LineVisitor() {
	    public boolean visit(Line l, int row, int bcol, int ecol) {
		paint(g, row, bcol, ecol);
		return true;
	    }
	} );
    }
}
