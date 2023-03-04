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
package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDataModel;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/*
 * ScrollingTabLayoutModel.java
 *
 * Created on December 5, 2003, 5:16 PM
 */

/**
 * Layout model which manages an offset into a set of scrollable tabs, and
 * recalculates its layout on a change.  Also handles adding extra pixels to the
 * selected tab if necessary.  Basics of how it works:
 * <p>
 * Wrapppers a DefaultTabLayoutModel, which can simply calculate tab widths and
 * 0 based positions.  Listens to the data model for changes, and sets a flag
 * when a change happens to mark the cached widths and positions as dirty. On
 * any call to fetch sizes, first checks if the cached values are good,
 * recalculates if needed, and returns the result.
 *
 * @author Tim Boudreau
 */
public final class ScrollingTabLayoutModel implements TabLayoutModel {
    /**
     * The index of the first clipped, visible tab, or -1 if the first tab
     * should not be clippped
     */
    private int offset = -1;
    /**
     * The wrapped DefaultTabLayoutModel which will give us pure numbers for the
     * desired width of tabs
     */
    private TabLayoutModel wrapped;
    /**
     * Flag indicating that any call to get a value should trigger recalculation
     * of the cached values
     */
    private boolean changed = true;
    /**
     * The tabDataModel, which we occasionally need to get data from
     */
    TabDataModel mdl;
    /**
     * The selection model we will get the current selection from when we need
     * to ensure it is visible
     */
    SingleSelectionModel sel;
    /**
     * Holds the value of the tab that should be made visible if makeVisible is
     * called before the component has a valid (>0) width.  If not -1, a call to
     * setWidth() will trigger a call to makeVisible with this value.
     */
    private int makeVisibleTab = -1;
    /**
     * Integer count of pixels that should be added to the width of the selected
     * tab.  They will be subtracted from the surrounding tabs
     */
    int pixelsToAddToSelection = 0;
    /**
     * Stores the value of whether the final tab is clipped.  Recalculated in
     * <code>change()</code>
     */
    private boolean lastTabClipped = false;
    /**
     * Cached index of the first visible tab
     */
    private int firstVisibleTab = -1;
    /**
     * Cached index of the last visible tab
     */
    private int lastVisibleTab = -1;
    /**
     * The last known width for which values were calculated
     */
    private int width = -1;
    /**
     * Cache of the widths of tabs that *are* onscreen.  This will always have a
     * length of (lastVisibleTab + 1) - firstVisibleTab.
     */
    private int[] widths = null;

    /**
     * Creates a new instance of ScrollingTabLayoutModel
     */
    public ScrollingTabLayoutModel(TabLayoutModel wrapped,
                                   SingleSelectionModel sel, TabDataModel mdl) {
        this.wrapped = wrapped;
        this.mdl = mdl;
        this.sel = sel;
    }

    public ScrollingTabLayoutModel(TabLayoutModel wrapped,
                                   SingleSelectionModel sel, TabDataModel mdl,
                                   int minimumXposition) {
        this(wrapped, sel, mdl);
        this.minimumXposition = minimumXposition;
    }

    public void setMinimumXposition (int x) {
        this.minimumXposition = x;
        setChanged(true);
    }

    /**
     * Some UIs will want to make the selected tab a little wider than the
     * rest.
     * @param i
     */
    public void setPixelsToAddToSelection (int i) {
        pixelsToAddToSelection = i;
        setChanged (true);
    }

    private int minimumXposition = 0;

    /**
     * External operations on the selection or data model can invalidate cached
     * widths.  The UI will listen for such changes and call this method if the
     * data we have cached is probably no good anymore.
     */
    public void clearCachedData() {
        setChanged(true);
    }

    /**
     * Convenience getter for the "wrapped" model which will give us "pure"
     * numbers regarding the widths of tabs
     */
    private TabLayoutModel getWrapped() {
        return wrapped;
    }

    /**
     * Get the offset - the number of tabs that are scrolled over.  The default
     * value is -1, which means no tabs are scrolled off to the left.  0 means
     * the first tab is visible but clipped...and so forth
     */
    public int getOffset() {
        if (mdl.size() <= 1) {
            return -1;
        }
        return offset;
    }

    /**
     * Called to recalculate cached values the first time a value that needs to
     * be calculated is requested, after some change that invalidates the cached
     * values
     */
    private void change() {
        if (mdl.size() == 0) {
            //no tabs, do nothing
            widths = new int[0];
            updateActions();
            setChanged(false);
            return;
        }
        //Create an array that will hold precalculated widths until something
        //changes
        if (widths == null || widths.length != mdl.size()) {
            widths = new int[mdl.size()];
            //Fill our array with 0's - any tabs not visible should get 0 width
        }
        Arrays.fill(widths, 0);

        if (widths.length == 1) {
            //there's only one tab, get rid of any offset - otherwise there's
            //no way to ever make the close button show because it won't be
            //able to be scrolled
            offset = -1;
        }
        
        //Handle throws case where we don't really even have enough room to
        //display one tab, by centering the clipped selected tag on what little
        //space we have.  The UI will make sure it looks clipped.
        if (width < getMinimumLeftClippedWidth()) {
            int toBeShown = makeVisibleTab != -1 ?
                    makeVisibleTab : sel.getSelectedIndex();
            toBeShown = Math.min(widths.length-1, toBeShown);
            if (toBeShown != -1) {
                widths[toBeShown] = width;
            } else {
                widths[0] = width;
            }
            firstVisibleTab = toBeShown;
            lastVisibleTab = toBeShown;
            setChanged(false);
            return;
        }
        
        //init an index to the current position while looping
        int x = minimumXposition;
        //Find the starting point, the first visible tab
        int start = offset >= 0 ? offset : 0;
        //Holds a count of pixels to redistribute among other tabs, if we don't
        //quite have room to fit the last tab, so we'll stretch the one next
        //to it, but we don't want to make it huge
        int toRedistribute = -1;
        //Reset stored value for the last visible tab, returned from 
        //getLastVisibleTab()
        lastVisibleTab = -1;
        //Reset stored value for first visible tab, returned from
        //getFirstVisibleTab()
        firstVisibleTab = start;
        //Reset the lastTabClipped flag returned by isLastTabClipped()
        lastTabClipped = false;

        //Special case - if the last tab the starting tab and there's not enough room for
        //it, show as much of it as possible
        if (start == mdl.size() - 1 && width < getWrapped().getW(start) + getMinimumLeftClippedWidth()) {
            lastVisibleTab = start;
            if (start != 0) {
                firstVisibleTab = start - 1;
                widths[start] = width - getMinimumLeftClippedWidth();
                widths[start - 1] = getMinimumLeftClippedWidth();
                lastTabClipped = width - getMinimumLeftClippedWidth() < getWrapped().getW(start);
            } else {
                firstVisibleTab = start;
                widths[start] = width;
                lastTabClipped = width < getWrapped().getW(start);
            }
            updateActions();
            //set the changed flag so we won't recalculate all this again until
            //the next time something warrants it
            setChanged(false);
            
            return;
        }

        for (int i = start; i < widths.length; i++) {
            int w;
            if (i == offset) {
                //If it's the first tab and it's an offset, it will use the
                //fixed width
                w = getMinimumLeftClippedWidth();
            } else {
                //Get a dynamic width from the underlying model, which tells us
                //how wide that tab wants to be
                w = getWrapped().getW(i);
            }
            //See if we've overshot the space available for tabs.  If we have,
            //we'll need to display this tab as right-clipped
            if (x + w > width) {
                if (width - x < getMinimumRightClippedWidth() && i != start) {
                    //There's not enough space to fit the current tab.  Add all
                    //the extra space to the previous one (we'll redistribute
                    //it later - this just makes the algorithm work even if
                    //you comment out the redistribution code)
                    widths[i - 1] += (width - x) - 1;
                    //Now we know how many extra pixels we'll have to redistribute
                    toRedistribute = (width - x);
                    //Decrement the last visible tab so it will show a correct
                    //value
                    lastVisibleTab = i - 1;
                    //Set the width of the tab that wouldn't fit to 0
                    widths[i] = 0;
                } else {
                    //Okay, there's enough space for this last tab as a clipped
                    //tab.  Truncate it at the last possible pixel a tab can
                    //occupy
                    widths[i] = (width - x) - 1;
                    //set this to the last visible tab
                    lastVisibleTab = i;
                }
                //Set the clipped flag - the UI will use this to decide what
                //border to give the last tab
                lastTabClipped = true;
                //We're done looping - this tab will be clipped, so it's the last
                break;
            }
            //Okay, we're just iterating through a tab in the middle.  Set its
            //width to whatever its measurements are and move on
            widths[i] = w;
            x += w;
            //make sure the last visible tab is really set correctly if there
            //is no right clipped tab
            if (i == widths.length - 1) {
                lastVisibleTab = widths.length - 1;
            }
        }
        
        //Some UIs want to make the selected tab bigger.  So try to do that here.
        //Get the selection from the selection model
        int selected = sel.getSelectedIndex();
        //See if we have to add some pixels to the selected tab, but ignore if
        //it's the first or last clipped tabs
        if (pixelsToAddToSelection != 0 && selected > start
                && selected < lastVisibleTab) {
            //Add the pixels to the selected index
            widths[selected] += pixelsToAddToSelection;
            //Get the average number of pixels per tab to remove.  If a small
            //number, it may round to 0.  Note we are intentionally dividing
            //by the number of tabs-1 because the selected tab doesn't count.
            int perTab = pixelsToAddToSelection
                    - 1 / (lastVisibleTab - start);
            //In case it does round to 0, keep an exact count
            int pixels = pixelsToAddToSelection - 1;
            //Iterate all the tabs, skipping the selected one
            for (int i = start; i <= lastVisibleTab; i++) {
                if (i != selected) {
                    //if it rounded to 0, we'll just subtract 2 until we get
                    //there - this will work most of the time and be harmless
                    //the rest
                    if (perTab == 0) {
                        //remove 2 pixels from the tab width
                        widths[i] -= 2;
                        pixels -= 2;
                        if (pixels <= 0) {
                            //if we'return out of pixels, stop
                            break;
                        }
                    } else {
                        //Okay, we have an exact (+/- rounding errors) number of
                        //pixels to remove.  Remove them,
                        widths[i] -= perTab;
                        //Subtract from our exact count, it will avoid rounding
                        //errors showing up
                        pixels -= perTab;
                        //if we'return out of pixels, stop
                        if (pixels <= 0) {
                            break;
                        }
                    }
                }
            }
        }
        
        //Now, do we have some spare pixels in the last tab that we need to redistribute
        //so we don't have a huge last tab?  Only do this if there are > 2 tabs,
        //or there's really no point - both are clipped
        if (toRedistribute != -1 && lastVisibleTab != start
                && lastVisibleTab != start + 1) {
            //Similar algorithm as above
            int perTab = toRedistribute / ((lastVisibleTab + 1) - start);
            for (int i = start; i < lastVisibleTab; i++) {
                if (perTab != 0) {
                    widths[i] += perTab;
                    widths[lastVisibleTab] -= perTab;
                } else {
                    int use = toRedistribute > 2 ? 2 : toRedistribute;
                    widths[i] += use;
                    widths[lastVisibleTab] -= use;
                    toRedistribute -= use;
                    if (toRedistribute <= 0) {
                        //out of pixels, quit
                        break;
                    }
                }
            }
        }
        updateActions();
        //set the changed flag so we won't recalculate all this again until
        //the next time something warrants it
        setChanged(false);
    }

    private void setChanged(boolean val) {
        if (changed != val) {
            changed = val;
        }
    }

    /**
     * Some look and feel specs require that the selected tab be wider.  This
     * method sets the number of pixels to add to its width.  It is important
     * that the underlying layout model's padX property include enough padding
     * that 1-2 pixels may be stolen without causing overlap problems.  The
     * default is 0.
     */
    public int getPixelsToAddToSelection() {
        return pixelsToAddToSelection;
    }

    /**
     * Returns true if the last tab displayed is clipped and should therefore be
     * painted as a clipped tab
     */
    public boolean isLastTabClipped() {
        if (width < getMinimumLeftClippedWidth()) {
            return true;
        }
        return lastTabClipped;
    }

    /**
     * Make a tab visible, according to the rules of the spec.  Returns whether
     * or not a repaint of the entire control is required.  The width of the tab
     * view is passed to this method, so that it can tell if the width has
     * changed (in which case it needs to recalculate tab bounds), or if it can
     * use the existing cached values.
     * <p>
     * This method will not trigger a repaint - it just adjusts the cached withs
     * and positions of tabs so that the next repaint will paint correctly. It
     * may be called as part of a more complex operation which would not want to
     * trigger spurious repaints - but the return value should be noted, and if
     * the return value is true, the caller should repaint the tab displayer
     * whenever it is done doing what it is doing.
     */
    public boolean makeVisible (int index, int width) {
        if (width < 0) {
            setWidth (width);
            makeVisibleTab = index;
            return false;
        }

        boolean resized = width != this.width || recentlyResized;
        recentlyResized = false;

        //First, make sure we have an accurate first/last visible tab
        setWidth(width);

        if (index == -1) {
            return false;
        }

        //Special case a single tab model - the index should always be 0
        if (mdl.size() == 1) {
            setOffset (-1);
            return changed;
        }

        //Special case two tabs in a very small area - try to show them both
        if (mdl.size() == 2) {
            int totalWidth = getWrapped().getW(0) + getWrapped().getW(1);
            if (totalWidth > width) {
                setOffset (0);
                return changed;
            }
        }

        if (changed) {
            change();
        }

        //Special case index 0 - it will always get -1
        if (index == 0) {
            int off = setOffset(-1);
            return off != -1;
        }
        int cachedWidthOfRequestedTab = getW( index );
        int widthForRequestedTab = getWrapped().getW(index);

        //Special case a single tab which is wider than the entire
        //tab displayer area
        if (widthForRequestedTab > width) {
            //It will be left clipped, but what can you do...
            setOffset (index-1);
            return changed;
        }

        //If it's the last tab and it's already not clipped, don't
        //do anything
        if (index == mdl.size() - 1 && !isLastTabClipped() && !resized && cachedWidthOfRequestedTab == width) {
            return false;
        }

        int newOffset = -2;

        int currW = 0;
        boolean isOffBack = false;
        boolean result = changed;
        boolean switchForward = false;
        //If it's after the last tab, we'll find it's width, then count
        //backward until we're out of tabs or out of space
        if (index >= getLastVisibleTab(width)) {
            int selIdx = sel.getSelectedIndex();
            switchForward = index >= selIdx;
            
            //Find the width of this tab, and count back
            currW = getWrapped().getW(index);
            if (index == selIdx) {
                currW += pixelsToAddToSelection;
            }
            int firstTab = index;
            //Count backward from the requested tab until we're out of space
            do {
                firstTab--;
                if (firstTab > -1) {
                    if (firstTab == selIdx) {
                        currW += pixelsToAddToSelection;
                    }
                    int wid = getWrapped().getW(firstTab);
                    currW += wid;
                }
            } while (currW <= width && firstTab >= -1);
            newOffset = firstTab;
            if( currW <= width || switchForward ) {
                newOffset++;
                if( getOffset() == -1 && newOffset == -1 )
                    newOffset = 0;
            }
        } else if (index <= getFirstVisibleTab(width)) {
            isOffBack = true;
            newOffset = index-1;
        }

        if (resized || !isOffBack || index == mdl.size() && getFirstVisibleTab(width) == index) {
            if (newOffset != -2) {
                setOffset (newOffset);
            }
            result = ensureAvailableSpaceUsed(false);
            
        } else {
            if (newOffset != -2) {
                int old = offset;
                int nue = setOffset (Math.min (mdl.size(), newOffset));
                result = old != nue;
            }
        }
        return result;
    }
    
    boolean ensureAvailableSpaceUsed(boolean useCached) {
        if (mdl.size() == 0) {
            return false;
        }
        boolean result = false;
        if (changed && !useCached) {
            result = true;
            change();
        }
        int last = mdl.size() -1;
        int lastTab = useCached ? getCachedLastVisibleTab() : getLastVisibleTab(width);
        if (lastTab == last || lastTab == mdl.size() && last > -1) { //one has been removed
            int off = offset;
            int availableWidth = width - (getX(last) + getW(last));

            while (availableWidth > 0 && off > -1) {
                availableWidth -= getWrapped().getW(off);
                if (availableWidth > 0) {
                    off--;
                }
            }
            setOffset (off);
            if (changed) {
                result = true;
                change();
            }
        } 
        return result;
    }

    /**
     * Probably these should be made into constructor arguments.  The minimum
     * space to be used for a right-clipped tab
     */
    int getMinimumRightClippedWidth() {
        return 40;
    }

    /**
     * Probably these should be made into constructor arguments.  The minimum
     * space to be used for a left-clipped tab
     */
    int getMinimumLeftClippedWidth() {
        return 40;
    }

    /**
     * Sets the current cached width the model thinks it has for displaying
     * tabs.  This is used to trigger a recalculation if it differs from the
     * previously passed value
     */
    public void setWidth(int width) {
        if (this.width != width) {
            recentlyResized = true;
            //see if someone called makeVisible before the component was shown -
            //we'll want to do that now
            if (width < this.width) {
                //Ensure that the current selection stays visible in a resize
                makeVisibleTab = sel.getSelectedIndex();
            }
            boolean needMakeVisible = (width > 0 && this.width < 0
                    && makeVisibleTab != -1);
            this.width = width - minimumXposition;
            setChanged(width > getMinimumLeftClippedWidth());
            if (changed && needMakeVisible
                    && width > getMinimumLeftClippedWidth()) {
                makeVisible(makeVisibleTab, width);
                makeVisibleTab = -1;
            }
        }
    }

    private boolean recentlyResized = true;

    /**
     * Set the offset - the number of tabs that should be hidden to the left.
     * The default is -1 - tab 0 is showing.  If set to 0, tab 0 still shows but
     * is clipped, and so forth.
     */
    public int setOffset(int i) {
        int prevOffset = offset;
        if (mdl.size() == 1) {
            if (offset > -1) {
                offset = -1;
                setChanged(true);
            }
            return prevOffset;
        }

        if (mdl.size() == 2 && width < getMinimumLeftClippedWidth()
                + getMinimumRightClippedWidth()) {
            offset = -1;
            setChanged(false);
            return prevOffset;
        }

        if (i < -1) {
            //repeated action calls can do this
            i = -1;
        }
        if (i != offset) {
            setChanged(true);
            offset = i;
        }
        return prevOffset;
    }

    /**
     * Returns the index of the first tab that is visible (may be clipped - if
     * it == getOffset() then it is
     */
    public int getFirstVisibleTab(int width) {
        setWidth(width);
        if (mdl.size() == 0) {
            return -1;
        }
        if (width < getMinimumLeftClippedWidth()) {
            int first = makeVisibleTab == -1 ?
                    sel.getSelectedIndex() : makeVisibleTab;
            return first;
        }
        if (changed) {
            change();
        }
        return firstVisibleTab;
    }

    /**
     * Return the number of tabs currently visible
     */
    public int countVisibleTabs(int width) {
        return (getLastVisibleTab(width) + 1)
                - getFirstVisibleTab(width);
    }

    /**
     * Returns the last visible tab, which may or may not be clipped
     */
    public int getLastVisibleTab(int width) {
        setWidth(width);
        if (mdl.size() == 0) {
            return -1;
        }
        if (width < getMinimumLeftClippedWidth()) {
            int first = makeVisibleTab == -1 ?
                    sel.getSelectedIndex() : makeVisibleTab;
            return first;
        }
        if (changed) {
            change();
        }
        return lastVisibleTab;
    }
    
    /**
     * Used when components are deleted, so that if the user scrolls to close
     * some tabs, and the selection is offscreen, we don't infuriatingly
     * re-scroll away from the end tabs.
     */
    int getCachedLastVisibleTab() {
        return lastVisibleTab;
    }
    
    /**
     * Used when components are deleted, so that if the user scrolls to close
     * some tabs, and the selection is offscreen, we don't infuriatingly
     * re-scroll away from the end tabs.
     */
    int getCachedFirstVisibleTab() {
        return firstVisibleTab;
    }

    public int dropIndexOfPoint(int x, int y) {
        if (changed) {
            change();
        }
        int first = getFirstVisibleTab(width);
        int last = getLastVisibleTab(width);
        int pos = 0; //XXX - may not be 0 with insets
        for (int i = first; i <= last; i++) {
            int lastPos = pos;
            pos += getW(i);
            int h = getH(i);
            int ay = getY(i);
            if (y < 0 || y > ay + h) {
                return -1;
            }
            if (i == last && x > lastPos + (getW(i) / 2)) {
                return last + 1;
            }
            if (x >= lastPos && x <= pos) {
                return i;
            }
        }
        return -1;
    }

    public void setPadding(Dimension d) {
        getWrapped().setPadding(d);
        setChanged (true);
    }

    public int getH(int index) {
        if (changed) {
            change();
        }
        try {
            return getWrapped().getH(index);
        } catch (IndexOutOfBoundsException e) {
            //The tab was just removed, and the selection model was notified,
            //by the data model, but not everything else has been notified yet
            return 0;
        }
    }

    /**
     * Returns a cached width, after checking the changed flag and calling
     * change() if recalculation is needed
     */
    public int getW(int index) {
        //widths can be null on OS-X if component is instantiated with
        //0 size (some bug with reloading winsys) and has never been painted
        if (changed || widths == null || index > widths.length) {
            change();
        }
        if (index >= widths.length) {
            //If a tab has just been removed, there may be a request to 
            //repaint it
            return 0;
        }
        return widths[index];
    }

    public int getX(int index) {
        if (changed) {
            change();
        }
        int result = minimumXposition;
        for (int i = 0; i < index; i++) {
            result += getW(i);
        }
        return result;
    }

    public int getY(int index) {
        if (changed) {
            change();
        }
        return getWrapped().getY(index);
    }

    public int indexOfPoint(int x, int y) {
        if (changed) {
            change();
        }
        int pos = minimumXposition;
        int lastPos;
        for (int i = offset == -1 ? 0 : offset; i < mdl.size(); i++) {
            lastPos = pos;
            int w = getW(i);
            pos += w;
            if (w == 0) {
                break;
            }
            int h = getH(i);
            int ay = getY(i);
            if (y < 0 || y > ay + h) {
                return -1;
            }
            if (x > lastPos && x < pos) {
                return i;
            }
        }
        return -1;
    }


    private Action fAction = null;
    private Action bAction = null;

    /**
     * Returns an Action that the control buttons can call to scroll forward
     */
    public Action getForwardAction() {
        if (fAction == null) {
            fAction = new ForwardAction();
        }
        return fAction;
    }

    /**
     * Returns an Action that the control buttons can call to scroll backward
     */
    public Action getBackwardAction() {
        if (bAction == null) {
            bAction = new BackwardAction();
        }
        return bAction;
    }

    /**
     * Update the enabled state of the button actions if the state of the layout
     * has changed in a way that affects them
     */
    private void updateActions() {
        if (width <= getMinimumLeftClippedWidth()) {
            bAction.setEnabled(false);
            fAction.setEnabled(false);
        }
        if (bAction != null) {
            bAction.setEnabled(mdl.size() > 1 && offset > -1);
        }
        if (fAction != null) {
            fAction.setEnabled(isLastTabClipped() && mdl.size() > 2 
                    && (lastVisibleTab-firstVisibleTab > 1 //special case when a tab is too wide
                        || lastVisibleTab < mdl.size()-1));
        }
    }

    /**
     * An action which will scroll forward
     */
    private class ForwardAction extends AbstractAction {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            setOffset(getOffset() + 1);
            Component jc = (Component) getValue("control"); //NOI18N
            //Use a convenient hack to get the control to paint
            if (jc != null) {
                jc.repaint();
            }
        }
    }

    /**
     * An action which will scroll backward
     */
    private class BackwardAction extends AbstractAction {
        public void actionPerformed(java.awt.event.ActionEvent e) {
            setOffset(getOffset() - 1);
            //Use a convenient hack to get the control to paint
            Component jc = (Component) getValue("control"); //NOI18N
            if (jc != null) {
                jc.repaint();
            }
        }
    }
}
