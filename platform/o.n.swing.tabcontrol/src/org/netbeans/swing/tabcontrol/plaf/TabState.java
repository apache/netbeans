/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.swing.tabcontrol.plaf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.Timer;
import javax.swing.event.ListDataEvent;
import org.netbeans.swing.tabcontrol.event.ArrayDiff;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.VeryComplexListDataEvent;
import org.openide.util.Utilities;

/**
 * Used by BasicTabDisplayerUI and its subclasses.
 * Tracks and manages the state of tabs, mainly which one currently contains the
 * mouse, if the mouse is in the close button, if the tab is adjacent to a
 * selected tab, if it is leftmost, rightmost, active, etc. This class hides
 * most of the complexity of deciding what mouse events should trigger a repaint
 * of what areas in an optimized way.  It provides setters which a mouse
 * listener can call to indicate that the mouse has, say, moved into a tab, or
 * from one tab to another, or the selection has changed, etc.
 * <p>
 * Essentially, this class is fed indices of tabs that have various states (selected,
 * contains mouse, etc.), figures out if this affects one tab, two tabs (a different
 * tab had the state, such as the mouse moving from one tab to another) or all tabs (activated).  It determines
 * a change type, and consults a <i>repaint policy</i> (an integer bitmask) to decide
 * if one, both, all or no tabs should be repainted.
 * <p>
 * The typical use case is to implement a subclass, and override getState() to
 * mix in bitmasks for things like whether a tab is clipped, etc. - things the
 * base implementation can't know about.
 *
 * <p>
 * Subclasses implement the <code>repaintTab()</code> method to do the actual
 * repainting, and implement <code>getRepaintPolicy()</code>.  The repainting will be
 * called if an event happens that changes the state in a way that the repaint policy
 * bitmask indicates should cause a repaint.
 * <p>
 * BasicTabDisplayerUI implements a mouse listener which will call the appropriate
 * methods when the mouse enters/exits tabs, etc.
 * <p>
 * <strong>Details</strong>
 * <p>
 * State is composed as an integer bitmask which covers all of the supported
 * states of a tab that may affect the way they paint.  These are also the values that
 * are passed to the methods of a <code>TabCellRenderer</code> to tell it how to
 * paint itself. Two other integer
 * bitmasks are used:  The <code>changeType</code>, which indicates whether a
 * change was from one tab to another tab, one tab to no tab (i.e. selection set
 * to -1), one tab to the same tab (i.e. the mouse moved out of the tab control,
 * and so the tab with the mouse in it is now no tab).  RepaintPolicy is an integer
 * bitmask composed of conditions under which the control should repaint one or all
 * tabs, and determines what types of changes actually trigger repaints.
 * <p>
 * Subclasses are expected to override <code>getState()</code> to provide
 * information about non-mouse-or-focus related states, such as clipping of
 * scrollable tabs.  Predefined states for tabs are: CLIP_RIGHT, CLIP_LEFT,
 * ARMED, PRESSED, SELECTED, ACTIVE, NOT_ONSCREEN, LEFTMOST, RIGHTMOST,
 * CLOSE_BUTTON_ARMED, BEFORE_SELECTED, AFTER_SELECTED, MOUSE_IN_TABS_AREA,
 * MOUSE_PRESSED_IN_CLOSE_BUTTON. Subclasses must handle returning the following
 * states if they wish to, which are not handled directly in TabState: LEFTMOST,
 * RIGHTMOST, CLIP_LEFT, CLIP_RIGHT, NOT_ONSCREEN.
 * <p>
 * Most of the states are fairly self-explanatory; NOT_ONSCREEN is useful as an
 * optimization so that no work is done for events that would try to produce a
 * repaint for something not visible; CLIP_* refers to the case in scrollable
 * tab UIs, in which a tab may only be partially visible;
 * MOUSE_PRESSED_IN_CLOSE_BUTTON is distinct because the CLOSE_BUTTON_ARMED
 * state will be reset if the mouse moves out of the close button area, but UIs
 * should perform a close action if the mouse was pressed over the close button,
 * moved away from the close button and then back to it, so this state preserves
 * the information that the originating location of a mouse press was in the
 * close button.
 */
public abstract class TabState {

    /**
     * Bitmask for state of tabs clipped on the right side - that is, partially
     * displayed
     */
    public static final int CLIP_RIGHT = 1;
    /**
     * Bitmask for state of tabs clipped on the right side - that is, partially
     * displayed
     */
    public static final int CLIP_LEFT = 2;
    /**
     * Bitmask indicating the tab contains the mouse
     */
    public static final int ARMED = 4;
    /**
     * Bitmask indicating the tab contains the mouse and the mouse button has
     * been pressed
     */
    public static final int PRESSED = 8;
    /**
     * Bitmask indicating the tab is selected
     */
    public static final int SELECTED = 16;
    /**
     * Bitmask indicating the tab is activated
     */
    public static final int ACTIVE = 32;
    /**
     * State bitmask indicating a tab is not displayed at all and shouldn't be
     * painted. Implementations may more simply avoid not painting a tab by not
     * including it in the range returned by getFirstVisibleTab()/getLastVisibleTab().
     * This state exists so that implementations have the option of returning
     * the entire range of tabs as visible and determining if one is truly
     * visble or not in getTabState() - sometimes doing it this way will be less
     * expensive.
     */
    public static final int NOT_ONSCREEN = 64;
    /**
     * Bitmask indicating the tab is at the extreme left and
     * <strong>not</strong> clipped
     */
    public static final int LEFTMOST = 128;
    /**
     * Bitmask indicating the tab is at the extreme right and
     * <strong>not</strong> clipped
     */
    public static final int RIGHTMOST = 256;
    /**
     * Bitmask indicating that the tab contains the mouse and the mouse is in
     * the close button
     */
    public static final int CLOSE_BUTTON_ARMED = 512;
    /**
     * Bitmask indicating that the tab's index is that of the selected index
     * less one
     */
    public static final int BEFORE_SELECTED = 1024;
    /**
     * Bitmask indicating that the tab's index is that of the selected index
     * plus one
     */
    public static final int AFTER_SELECTED = 2048;
    /**
     * Bitmask indicating that the mouse is in the tabs area
     */
    public static final int MOUSE_IN_TABS_AREA = 4096;

    /**
     * Bitmask indicating that the mouse is inside the close button and has 
     * been pressed.
     */
    public static final int MOUSE_PRESSED_IN_CLOSE_BUTTON = 8192;
    
    /**
     * Bitmask indicating that the tab is in "attention" mode - blinking or
     * flashing to get the user's attention.
     */
    public static final int ATTENTION = 16384;
    
    /**
     * Bitmask indicating that the tab's index is that of the armed index
     * less one
     */
    public static final int BEFORE_ARMED = 32768;

    /**
     * Indicates the given tab is 'busy', i.e. the editor is still loading its data
     * or there's some lengthy process running in that tab.
     * @since 1.34
     */
    public static final int BUSY = 2*32768;

    /**
     * Indicates that the tab should be highlighted to draw user's attention.
     * @since 1.38
     */
    public static final int HIGHLIGHT = 2*BUSY;
    /**
     * Indicates the last constant defined - renderers that wish to add their
     * own bitmasks should use multiples of this number
     */
    public static int STATE_LAST = HIGHLIGHT;


    private int pressedIndex = -1;
    private int containsMouseIndex = -1;
    private int closeButtonContainsMouseIndex = -1;
    private int mousePressedInCloseButtonIndex = -1;
    private boolean mouseInTabsArea = false;
    private boolean active = false;
    private int selectedIndex = -1;

    private int prev = -1;
    private int curr = -1;
    private int lastChangeType = NO_CHANGE;
    private int lastAffected = 0;
    private int lastChange = 0;

    /** Repaint policy bitmask indicating that a tab should be repainted whenever the mouse enters or exits it */
    public static final int REPAINT_ON_MOUSE_ENTER_TAB = 1;
    /** Repaint policy bitmask indicating that all tabs should be repainted whenever the mouse enters or leaves the
     * area in which tabs are painted */
    public static final int REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA = 3;
    /** Repaint policy bitmask indicating that the tab should be repainted when the mouse enters or exits the close
     * button region */
    public static final int REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON = 4;
    /** Repaint policy bitmask indicating that the tab should be repainted on mouse pressed events */
    public static final int REPAINT_ON_MOUSE_PRESSED = 8;
    /** Repaint policy bitmask indicating that the selected tab should be repainted when the activated state changes */
    public static final int REPAINT_SELECTION_ON_ACTIVATION_CHANGE = 16;
    /** Repaint policy bitmask indicating that all tabs should be repainted when the activated state changes */
    public static final int REPAINT_ALL_TABS_ON_ACTIVATION_CHANGE = 32; //includes selection
    /** Repaint policy bitmask indicating that a tab should be repainted when it becomes selected/unselected */
    public static final int REPAINT_ON_SELECTION_CHANGE = 64;
    /** Repaint policy bitmask indicating that all tabs should be repainted whenever the selection changes */
    public static final int REPAINT_ALL_TABS_ON_SELECTION_CHANGE = 128;
    /** Repaint policy bitmask indicating that the tab should be repainted when the close button is pressed */
    public static final int REPAINT_ON_CLOSE_BUTTON_PRESSED = 256;

    /**
     * Get the state of a given tab.  Subclasses are expected to override this
     * to provide information about states such as clipping which can only be
     * found out via layout information, such as LEFTMOST, RIGHTMOST, CLIP_LEFT
     * and CLIP_RIGHT.  The state is used by tab renderers to determine how they
     * should paint themselves.
     *
     * @param tab The index of the tab
     * @return The state
     */
    public int getState(int tab) {
        int result = 0;
        if (tab == pressedIndex) {
            result |= PRESSED;
        }
        if (tab == containsMouseIndex) {
            result |= ARMED;
        }
        if (tab == closeButtonContainsMouseIndex) {
            result |= CLOSE_BUTTON_ARMED;
        }
        if (tab == mousePressedInCloseButtonIndex) {
            result |= MOUSE_PRESSED_IN_CLOSE_BUTTON;
        }
        if (mouseInTabsArea) {
            result |= MOUSE_IN_TABS_AREA;
        }
        if (active) {
            result |= ACTIVE;
        }
        if (tab == selectedIndex) {
            result |= SELECTED;
        }
        if (tab != 0 && tab == selectedIndex + 1) {
            result |= AFTER_SELECTED;
        }
        if (tab == selectedIndex - 1) {
            result |= BEFORE_SELECTED;
        }
        if (tab == containsMouseIndex - 1) {
            result |= BEFORE_ARMED;
        }
        if (isAlarmTab(tab)) {
            result |= ATTENTION;
        }
        if (isHighlightTab( tab)) {
            result |= HIGHLIGHT;
        }
        return result;
    }
    
    /** For debugging, enables fetching tab state as a string */
    String getStateString (int tab) {
        return stateToString (getState(tab));
    }

    /**
     * Clear all mouse position related state information.  This should be done
     * following events in the model that alter the state sufficiently that the
     * cached information is probably wrong.
     */
    public void clearTransientStates() {
        pressedIndex = -1;
        containsMouseIndex = -1;
        closeButtonContainsMouseIndex = -1;
        mousePressedInCloseButtonIndex = -1;
        mouseInTabsArea = false;
        lastChangeType = NO_CHANGE;
        lastChange = 0;
        prev = -1;
        curr = -1;
    }

    /**
     * Set the index of the tab over which a mouse button has been pressed.
     *
     * @param i The tab which is pressed, or -1 to clear PRESSED from the state
     *          of the previously pressed tab
     * @return Index of the tab which previously had the state PRESSED, or -1
     */
    public final int setPressed(int i) {
        prev = pressedIndex;
        pressedIndex = i;
        curr = i;
        possibleChange(prev, curr, PRESSED);
        return prev;
    }

    /**
     * Set the index of the tab which currently contains the mouse cursor.
     *
     * @param i The tab which contains the mouse cursor, or -1 to clear ARMED
     *          from the state of the tab
     * @return Index of the tab which previously had the state ARMED, or -1
     */
    public final int setContainsMouse(int i) {
        prev = containsMouseIndex;
        containsMouseIndex = i;
        curr = i;
        possibleChange(prev, curr, ARMED);
        return prev;
    }

    /**
     * Set the index of the tab whose close button contains the mouse cursor.
     *
     * @param i The index of the tab whose close button contains the mouse
     *          cursor, or -1 to clear CLOSE_BUTTON_CONTAINS_MOUSE from the
     *          state of the tab which previously had it
     * @return Index of the tab which formerly had the state
     *         CLOSE_BUTTON_CONTAINS_MOUSE, or -1
     */
    public final int setCloseButtonContainsMouse(int i) {
        prev = closeButtonContainsMouseIndex;
        closeButtonContainsMouseIndex = i;
        curr = i;
        possibleChange(prev, curr, CLOSE_BUTTON_ARMED);
        return prev;
    }

    /**
     * Set the index of the tab in which the mouse button has been pressed in
     * the close button.  This is distinct from the combination of
     * CLOSE_BUTTON_ARMED and PRESSED, since the user may press the close button
     * and then drag away from the close button (clearing the CLOSE_BUTTON_ARMED
     * state) to abort closing a tab.
     *
     * @param i The tab in which the mouse was pressed while over the close
     *          button
     * @return Index of the tab which previously had the state
     *         MOUSE_PRESSED_IN_CLOSE_BUTTON, or -1
     */
    public final int setMousePressedInCloseButton(int i) {
        prev = mousePressedInCloseButtonIndex;
        mousePressedInCloseButtonIndex = i;
        curr = i;
        possibleChange(prev, curr, MOUSE_PRESSED_IN_CLOSE_BUTTON);
        return prev;
    }

    /**
     * Set the index of the tab which is currently selected. Note that users of
     * this class must ensure that this value stays up to date when changes
     * occur in the model such as inserting tabs before the selected one.
     *
     * @param i The tab index which is selected
     * @return Index of the tab which previously was selected, or -1 if none
     */
    public final int setSelected(int i) {
        prev = selectedIndex;
        selectedIndex = i;
        curr = i;
        removeAlarmTab(i);
        removeHighlightTab(i);
        possibleChange(prev, curr, SELECTED);
        return prev;
    }

    /**
     * Set the condition for all tabs of the mouse being in the tabs area.
     *
     * @param b Whether the mouse is in the tabs area or not
     * @return The previous state with regard to the mouse being in the tabs
     *         area
     */
    public final boolean setMouseInTabsArea(boolean b) {
        boolean prev = mouseInTabsArea;
        mouseInTabsArea = b;
        possibleChange(prev, b, MOUSE_IN_TABS_AREA);
        return prev;
    }

    /**
     * Set the condition for all tabs of the component being activated.
     *
     * @param b Whether or not the component is activated
     * @return The previous state with regard to the component being activated
     */
    public final boolean setActive(boolean b) {
        boolean prev = active;
        active = b;
        possibleChange(prev, b, ACTIVE);
        removeAlarmTab(selectedIndex);
        removeHighlightTab(selectedIndex);
        return prev;
    }
    
    private boolean isAlarmTab (int tab) {
        return attentionToggle && alarmTabs.contains(tab);
    }
    
    private final HashSet<Integer> alarmTabs = new HashSet<>(6);
    
    /** Add a tab to the list of those which should "flash" or otherwise give
     * some notification to the user to get their attention */
    public final void addAlarmTab (int alarmTab) {
        Integer in = alarmTab;
        boolean added = alarmTabs.contains(in);
        boolean wasEmpty = alarmTabs.isEmpty();
        if (!added) {
            alarmTabs.add(alarmTab);
            repaintTab(alarmTab);
        }
        if (wasEmpty) {
            startAlarmTimer();
            attentionToggle = true;
            repaintTab (alarmTab);
        }
    }

    private final HashSet<Integer> highlightTabs = new HashSet<Integer>(6);

    private boolean isHighlightTab(int tab) {
        return highlightTabs.contains( tab ) && !alarmTabs.contains( tab );
    }

    /**
     * Highlight the given tab.
     * @param highlightTab
     * @since 1.38
     */
    public final void addHighlightTab(int highlightTab) {
        boolean added = highlightTabs.add(highlightTab);
        if (added) {
            repaintTab (highlightTab);
        }
    }

    /** Remove a tab to the list of those which should "flash" or otherwise give
     * some notification to the user to get their attention */
    public final void removeAlarmTab (int alarmTab) {
        Integer in = alarmTab;
        boolean contained = alarmTabs.contains(in);
        if (contained) {
            alarmTabs.remove(in);
            boolean wasAttentionToggled = attentionToggle;
            if (alarmTabs.isEmpty()) {
                stopAlarmTimer();
            }
            if (wasAttentionToggled) {
                repaintTab(alarmTab);
            }
        }
    }

    /**
     * Turn tab highlight off.
     * @param highlightTab
     * @since 1.38
     */
    public final void removeHighlightTab(int highlightTab) {
        boolean removed = highlightTabs.remove( highlightTab );
        if( removed ) {
            repaintTab( highlightTab );
        }
    }
    
    private Timer alarmTimer = null;
    private boolean attentionToggle = false;
    private void startAlarmTimer() {
        if (alarmTimer == null) {
            ActionListener al = new ActionListener() {
                @Override
                public void actionPerformed (ActionEvent ae) {
                    if( !isDisplayable() ) {
                        //#205421 - stop the timer if the TabbedContainer isn't 
                        //in component hierarchy anymore to avoid memory leaks
                        stopAlarmTimer();
                    }
                    attentionToggle = !attentionToggle;
                    for (Iterator<Integer> i = alarmTabs.iterator(); i.hasNext();) {
                        repaintTab(i.next());
                    }
                }
            };
            alarmTimer = new Timer (700, al);
            alarmTimer.setRepeats(true);
        }
        alarmTimer.start();
    }

    /**
     * Check if the tab container is displayable, i.e. connected to component hierarchy.
     * The default implementation in this class always returns true.
     * @return True if the tab container this TabState is associated with is
     * still in component hierarchy, false otherwise.
     * @since 1.31
     */
    boolean isDisplayable() {
        return true;
    }

    private final void stopAlarmTimer() {
        if (alarmTimer != null && alarmTimer.isRunning()) {
            alarmTimer.stop();
            attentionToggle = false;
            repaintAllTabs(); //XXX optimize
        }
    }
    
    boolean hasAlarmTabs() {
        return !alarmTabs.isEmpty();
    }

    boolean hasHighlightTabs() {
        return !highlightTabs.isEmpty();
    }
    
    void pruneTabs(int max) {
        if (!(hasAlarmTabs() || hasHighlightTabs())) {
            return;
        }
        for (Iterator<Integer> i=alarmTabs.iterator(); i.hasNext();) {
            if (i.next() >= max) {
                i.remove();
            }
        }
        for (Iterator<Integer> i=highlightTabs.iterator(); i.hasNext();) {
            if (i.next() >= max) {
                i.remove();
            }
        }
        if (alarmTabs.isEmpty()) {
            stopAlarmTimer();
        }
    }
    
    int[] getAlarmTabs() {
        int[] alarms = (int[]) Utilities.toPrimitiveArray((Integer[]) alarmTabs.toArray(new Integer[0]));
        Arrays.sort(alarms);
        return alarms;
    }
    
    //Handling of insertions/deletions where we'll need to update the 
    //list of blinking tabs here.
    void intervalAdded (ListDataEvent evt) {
        int start = evt.getIndex0();
        int end = evt.getIndex1();
        if (hasAlarmTabs()) {
            int[] alarms = (int[]) Utilities.toPrimitiveArray((Integer[]) alarmTabs.toArray(new Integer[0]));
            boolean changed = false;
            for (int i=0; i < alarms.length; i++) {
                if (alarms[i] >= start) {
                    alarms[i] += (end - start) + 1;
                    changed = true;
                }
            }
            if (changed) {
                alarmTabs.clear();
                for (int i=0; i < alarms.length; i++) {
                    addAlarmTab(alarms[i]);
                }
            }
        }
        if( hasHighlightTabs() ) {
            int[] highlights = (int[]) Utilities.toPrimitiveArray((Integer[]) highlightTabs.toArray(new Integer[0]));
            boolean changed = false;
            for (int i=0; i < highlights.length; i++) {
                if (highlights[i] >= start) {
                    highlights[i] += (end - start) + 1;
                    changed = true;
                }
            }
            if (changed) {
                highlightTabs.clear();
                for (int i=0; i < highlights.length; i++) {
                    addHighlightTab(highlights[i]);
                }
            }
        }
    }
    
    void intervalRemoved (ListDataEvent evt) {
        if (hasAlarmTabs()) {
            int start = evt.getIndex0();
            int end = evt.getIndex1();

            int[] alarms = (int[]) Utilities.toPrimitiveArray((Integer[]) alarmTabs.toArray(new Integer[0]));
            Arrays.sort(alarms);

            if (end == start) {
                //Faster to handle this case separately
                boolean changed = true;
                for (int i=0; i < alarms.length; i++) {
                    if (alarms[i] > end) {
                        alarms[i]--;
                    } else if (alarms[i] == end) {
                        alarms[i] = -1;
                    }
                }
                if (changed) {
                    alarmTabs.clear();
                    boolean added = false;
                    for (int i=0; i < alarms.length; i++) {
                        if (alarms[i] != -1) {
                            addAlarmTab(alarms[i]);
                            added = true;
                        }
                    }
                    if (!added) {
                        stopAlarmTimer();
                    }
                }
                return;
            }

            boolean changed = false;
            for (int i=0; i < alarms.length; i++) {
                if (alarms[i] >= start && alarms[i] <= end) {
                    alarms[i] = -1;
                    changed = true;
                }
            }
            for (int i=0; i < alarms.length; i++) {
                if (alarms[i] > end) {
                    alarms[i] -= (end - start) + 1;
                    changed = true;
                }
            }
            if (changed) {
                alarmTabs.clear();
                boolean added = false;
                for (int i=0; i < alarms.length; i++) {
                    if (alarms[i] != -1) {
                        addAlarmTab(alarms[i]);
                        added = true;
                    }
                }
                if (!added) {
                    stopAlarmTimer();
                }
            }
        }
        if( hasAlarmTabs() ) {
            int start = evt.getIndex0();
            int end = evt.getIndex1();

            int[] highlights = (int[]) Utilities.toPrimitiveArray((Integer[]) highlightTabs.toArray(new Integer[0]));
            Arrays.sort(highlights);

            boolean changed = false;
            for (int i=0; i < highlights.length; i++) {
                if (highlights[i] >= start && highlights[i] <= end) {
                    highlights[i] = -1;
                    changed = true;
                }
            }
            for (int i=0; i < highlights.length; i++) {
                if (highlights[i] > end) {
                    highlights[i] -= (end - start) + 1;
                    changed = true;
                }
            }
            if (changed) {
                highlightTabs.clear();
                for (int i=0; i < highlights.length; i++) {
                    if (highlights[i] != -1) {
                        addHighlightTab(highlights[i]);
                    }
                }
            }
        }
    }
    
    void indicesAdded (ComplexListDataEvent e) {
        if (hasAlarmTabs()) {
            int[] alarms = (int[]) Utilities.toPrimitiveArray((Integer[]) alarmTabs.toArray(new Integer[0]));
            java.util.Arrays.sort(alarms);

            int[] indices = e.getIndices();
            java.util.Arrays.sort(indices);

            boolean changed = false;
            for (int i=0; i < indices.length; i++) {
                for (int j=0; j < alarms.length; j++) {
                    if (alarms[j] >= indices[i]) {
                        alarms[j]++;
                        changed = true;
                    }
                }
            }
            if (changed) {
                alarmTabs.clear();
                for (int i=0; i < alarms.length; i++) {
                    if (alarms[i] != -1) {
                        addAlarmTab(alarms[i]);
                    }
                }
            }
        }
        if( hasHighlightTabs() ) {
            int[] highlights = (int[]) Utilities.toPrimitiveArray((Integer[]) highlightTabs.toArray(new Integer[0]));
            java.util.Arrays.sort(highlights);

            int[] indices = e.getIndices();
            java.util.Arrays.sort(indices);

            boolean changed = false;
            for (int i=0; i < indices.length; i++) {
                for (int j=0; j < highlights.length; j++) {
                    if (highlights[j] >= indices[i]) {
                        highlights[j]++;
                        changed = true;
                    }
                }
            }
            if (changed) {
                highlightTabs.clear();
                for (int i=0; i < highlights.length; i++) {
                    if (highlights[i] != -1) {
                        addHighlightTab(highlights[i]);
                    }
                }
            }
        }
    }
    
    void indicesRemoved (ComplexListDataEvent e) {
        if (hasAlarmTabs()) {
            int[] indices = e.getIndices();
            java.util.Arrays.sort(indices);

            int[] alarms = (int[]) Utilities.toPrimitiveArray((Integer[]) alarmTabs.toArray(new Integer[0]));
            java.util.Arrays.sort(alarms);

            if (alarms[alarms.length -1] >= indices[0]) {
                boolean changed = false;
                for (int i=0; i < alarms.length; i++) {
                    //First weed out all deleted alarm tabs
                    for (int j=0; j < indices.length; j++) {
                        if (alarms[i] == indices[j]) {
                            alarms[i] = -1;
                            changed = true;
                        }
                    }
                }
                for (int i=0; i < alarms.length; i++) {
                    //Now decrement those that remain that are affected
                    int alarm = alarms[i];
                    for (int j=0; j < indices.length; j++) {
                        if (alarm > indices[j]) {
                            alarms[i]--;
                            changed = true;
                        }
                    }
                }

                if (changed) {
                    alarmTabs.clear();
                    boolean addedSome = false;
                    for (int i=0; i < alarms.length; i++) {
                        if (alarms[i] >= 0) {
                            addAlarmTab(alarms[i]);
                            addedSome = true;
                        }
                    }
                    if (!addedSome) {
                        stopAlarmTimer();
                    }
                }
            } else {
                //Some tab removed after the last blinking tab, don't care
            }

        }
        if( hasHighlightTabs() ) {
            int[] indices = e.getIndices();
            java.util.Arrays.sort(indices);

            int[] highlights = (int[]) Utilities.toPrimitiveArray((Integer[]) highlightTabs.toArray(new Integer[0]));
            java.util.Arrays.sort(highlights);

            if (highlights[highlights.length -1] >= indices[0]) {

                boolean changed = false;
                for (int i=0; i < highlights.length; i++) {
                    //First weed out all deleted alarm tabs
                    for (int j=0; j < indices.length; j++) {
                        if (highlights[i] == indices[j]) {
                            highlights[i] = -1;
                            changed = true;
                        }
                    }
                }
                for (int i=0; i < highlights.length; i++) {
                    //Now decrement those that remain that are affected
                    int alarm = highlights[i];
                    for (int j=0; j < indices.length; j++) {
                        if (alarm > indices[j]) {
                            highlights[i]--;
                            changed = true;
                        }
                    }
                }

                if (changed) {
                    highlightTabs.clear();
                    for (int i=0; i < highlights.length; i++) {
                        if (highlights[i] >= 0) {
                            addHighlightTab(highlights[i]);
                        }
                    }
                }
            }
        }
        repaintAllTabs();
    }
    
    void indicesChanged (ComplexListDataEvent e) {
        if (hasAlarmTabs()) {
            if (e instanceof VeryComplexListDataEvent) { //it always will be
                VeryComplexListDataEvent ve = (VeryComplexListDataEvent) e;

                ArrayDiff dif = ((VeryComplexListDataEvent) e).getDiff();

                List old = Arrays.asList(dif.getOldData());
                List nue = Arrays.asList(dif.getNewData());

                int[] alarms = (int[]) Utilities.toPrimitiveArray((Integer[]) alarmTabs.toArray(new Integer[0]));

                boolean changed = false;
                for (int i=0; i < alarms.length; i++) {
                    Object o = old.get(alarms[i]);
                    int idx = nue.indexOf(o);
                    changed |= idx != alarms[i];
                    alarms[i] = nue.indexOf(o);
                }
                if (changed) {
                    alarmTabs.clear();
                    boolean addedSome = false;
                    for (int i=0; i < alarms.length; i++) {
                        if (alarms[i] >= 0) {
                            addAlarmTab(alarms[i]);
                            addedSome = true;
                        }
                    }
                    if (!addedSome) {
                        stopAlarmTimer();
                    }
                }
            }
        }
        if( hasHighlightTabs() ) {
            if (e instanceof VeryComplexListDataEvent) { //it always will be
                VeryComplexListDataEvent ve = (VeryComplexListDataEvent) e;

                ArrayDiff dif = ((VeryComplexListDataEvent) e).getDiff();

                List old = Arrays.asList(dif.getOldData());
                List nue = Arrays.asList(dif.getNewData());

                int[] highlights = (int[]) Utilities.toPrimitiveArray((Integer[]) highlightTabs.toArray(new Integer[0]));

                boolean changed = false;
                for (int i=0; i < highlights.length; i++) {
                    Object o = old.get(highlights[i]);
                    int idx = nue.indexOf(o);
                    changed |= idx != highlights[i];
                    highlights[i] = nue.indexOf(o);
                }
                if (changed) {
                    highlightTabs.clear();
                    for (int i=0; i < highlights.length; i++) {
                        if (highlights[i] >= 0) {
                            addHighlightTab(highlights[i]);
                        }
                    }
                }
            }
        }
    }

    
    void contentsChanged(ListDataEvent evt) {
        if (!hasAlarmTabs()) return;
        //Do nothing, just means some text or icons changed
    }    

    //Change types
    /** Change type indicating no change happened (i.e. calling setSelected() with the same value it was previously
     * called with).
     */
    public static final int NO_CHANGE = 0;
    /** Change type indicating a change of state for two tabs */
    public static final int CHANGE_TAB_TO_TAB = 1;
    /** Change type indicating a change happened (such as the mouse leaving a tab) such that now no tab has the
     * state previously held by the affected tab */
    public static final int CHANGE_TAB_TO_NONE = 2;
    /** Change type indicating that a state was added that no tab previously had */
    public static final int CHANGE_NONE_TO_TAB = 3;
    public static final int CHANGE_TAB_TO_SELF = 4;
    /** Change type indicating one of the boolean state changes, such as STATE_ACTIVE */
    public static final int ALL_TABS = Integer.MAX_VALUE;

    protected void possibleChange(boolean prevVal, boolean currVal, int type) {
        if (prevVal == currVal) {
            lastChangeType = NO_CHANGE;
        } else {
            lastChangeType = ALL_TABS;
        }
        if (lastChangeType != NO_CHANGE) {
            lastAffected = ALL_TABS;
            change(ALL_TABS, ALL_TABS, type, lastChangeType);
        }
    }

    protected void possibleChange(int lastTab, int currTab, int type) {
        if (lastTab == currTab) {
            lastChangeType = NO_CHANGE;
        } else {
            if (currTab == -1) {
                lastChangeType = CHANGE_TAB_TO_NONE;
            } else if (lastTab == -1) {
                lastChangeType = CHANGE_NONE_TO_TAB;
            } else {
                lastChangeType = CHANGE_TAB_TO_TAB;
            }
        }
        if (lastChangeType != NO_CHANGE) {
            lastAffected = currTab;
            change(lastTab, currTab, type, lastChangeType);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(50);
        sb.append("TabState [lastTab=");
        sb.append(tabToString(prev));
        sb.append(" currTab=");
        sb.append(tabToString(curr));
        sb.append(" lastAffected=");
        sb.append(tabToString(lastAffected));
        sb.append(" lastChangeType=");
        sb.append(changeToString(lastChangeType));
        sb.append(" lastChange=");
        sb.append(stateToString(lastChange));
        sb.append(" <active=");
        sb.append(active);
        sb.append(" sel=");
        sb.append(tabToString(selectedIndex));
        sb.append(" mouse=");
        sb.append(tabToString(containsMouseIndex));
        sb.append(" inTabs=");
        sb.append(mouseInTabsArea);
        sb.append(" pressed=");
        sb.append(tabToString(pressedIndex));
        sb.append(" inCloseButton=");
        sb.append(tabToString(closeButtonContainsMouseIndex));
        sb.append(" pressedCloseButton=");
        sb.append(tabToString(mousePressedInCloseButtonIndex));
        sb.append(">]");
        return sb.toString();
    }

    /**
     * Called when a setter for a tab index has produced a change in a
     * state-affecting property, such as which tab contains the mouse.  Fetches
     * the repaint policies, and if the change is one that the policy says
     * should produce a repaint, calls repaintTab for the appropriate tabs.
     *
     * @param lastTab    The tab previously holding the state which has changed,
     *                   or -1
     * @param currTab    The tab currently holding the state which has changed,
     *                   or -1
     * @param type       The thing that changed.  This will be one of the state
     *                   constants.
     * @param changeType This is one of the defined change types such as
     *                   ALL_TABS, TAB_TO_TAB, etc.
     */
    protected void change(int lastTab, int currTab, int type, int changeType) {
        lastChange = type;
//        System.err.println("Change-type: " + stateToString(type) + " - " + changeToString (changeType) + " from " + tabToString (lastTab) + " to " + tabToString (currTab));
        if (changeType == CHANGE_TAB_TO_TAB) {
            maybeRepaint(lastTab, type);
        } else if (changeType == CHANGE_TAB_TO_NONE) {
            maybeRepaint (lastTab, type);
            return;
        } else if (changeType == ALL_TABS && (getRepaintPolicy(currTab) & REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA) != 0) {
            repaintAllTabs();
            return;
        }
        maybeRepaint(currTab, type);
    }

    protected void maybeRepaint(int tab, int type) {
        int rpol = getRepaintPolicy (tab);
        boolean go = false;
        switch (type) {
            case ACTIVE:
                go = (rpol
                        & REPAINT_SELECTION_ON_ACTIVATION_CHANGE)
                        != 0;
                if ((rpol
                        & REPAINT_ALL_TABS_ON_ACTIVATION_CHANGE)
                        != 0) {
                    type = ALL_TABS;
                    go = true;
                }
                break;
            case ARMED:
                go = (rpol & REPAINT_ON_MOUSE_ENTER_TAB) != 0 || 
                    tab == closeButtonContainsMouseIndex;
                closeButtonContainsMouseIndex = -1;
                break;
            case CLOSE_BUTTON_ARMED:
                go = (rpol & REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON)
                        != 0;
                break;
            case MOUSE_IN_TABS_AREA:
                go = (rpol
                        & REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA)
                        != 0;
                break;
            case MOUSE_PRESSED_IN_CLOSE_BUTTON:
                go = (rpol & REPAINT_ON_CLOSE_BUTTON_PRESSED)
                        != 0;
                break;
            case PRESSED:
                go = (rpol & REPAINT_ON_MOUSE_PRESSED) != 0;
                break;
            case SELECTED:
                go = (rpol & REPAINT_ON_SELECTION_CHANGE) != 0;
                if ((rpol & REPAINT_ALL_TABS_ON_SELECTION_CHANGE)
                        != 0) {
                    type = ALL_TABS;
                    go = true;
                }
                break;
            case ATTENTION:
            case HIGHLIGHT:
                go = true;
        }
        if (go) {
            if (type == ALL_TABS) {
                repaintAllTabs();
            } else {
                repaintTab(tab);
            }
        }
    }

    protected abstract void repaintTab(int tab);

    protected abstract void repaintAllTabs();

    static final String changeToString(int change) {
        switch (change) {
            case NO_CHANGE:
                return "no change"; //NOI18N
            case CHANGE_TAB_TO_TAB:
                return "tab to tab"; //NOI18N
            case CHANGE_TAB_TO_NONE:
                return "tab to none"; //NOI18N
            case CHANGE_NONE_TO_TAB:
                return "none to tab"; //NOI18N
            case CHANGE_TAB_TO_SELF:
                return "tab to self"; //NOI18N
            case ALL_TABS:
                return "all tabs"; //NOI18N
            default :
                return "??? " + change; //NOI18N
        }
    }

    static final String tabToString(int tab) {
        if (tab == ALL_TABS) {
            return "all tabs"; //NOI18N
        } else if (tab == -1) {
            return "none"; //NOI18N
        } else {
            return Integer.toString(tab);
        }
    }

    /**
     * Static utility method to get a string representation of a state
     */
    static final String stateToString(int st) {
        String[] states = new String[]{
            "clip right", "clip left", "armed", "pressed", "selected", "active", "not onscreen", "leftmost", //NOI18N
            "rightmost", "in closebutton", "before selected", "after selected", "mouse in tabs area", //NOI18N
            "mouse pressed in close button" //NOI18N
        }; //NOI18N
        int[] vals = new int[]{
            CLIP_RIGHT, CLIP_LEFT, ARMED, PRESSED, SELECTED, ACTIVE,
            NOT_ONSCREEN, LEFTMOST, RIGHTMOST, CLOSE_BUTTON_ARMED,
            BEFORE_SELECTED, AFTER_SELECTED, MOUSE_IN_TABS_AREA,
            MOUSE_PRESSED_IN_CLOSE_BUTTON};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < vals.length; i++) {
            if ((st & vals[i]) != 0) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(states[i]);
            }
        }
        if (sb.length() == 0) {
            sb.append("no flags set"); //NOI18N
        }
        sb.append("=");
        sb.append(st);
        return sb.toString();
    }

    static String repaintPolicyToString (int policy) {
        if (policy == 0) {
            return "repaint nothing";
        }
        String[] names = new String[] {
            "REPAINT_ON_MOUSE_ENTER_TAB",
            "REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA",
            "REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON",
            "REPAINT_ON_MOUSE_PRESSED",
            "REPAINT_SELECTION_ON_ACTIVATION_CHANGE",
            "REPAINT_ALL_TABS_ON_ACTIVATION_CHANGE",
            "REPAINT_ON_SELECTION_CHANGE",
            "REPAINT_ALL_TABS_ON_SELECTION_CHANGE",
            "REPAINT_ON_CLOSE_BUTTON_PRESSED",
        };
        int[] vals = new int[] {
            REPAINT_ON_MOUSE_ENTER_TAB,
            REPAINT_ALL_ON_MOUSE_ENTER_TABS_AREA,
            REPAINT_ON_MOUSE_ENTER_CLOSE_BUTTON,
            REPAINT_ON_MOUSE_PRESSED,
            REPAINT_SELECTION_ON_ACTIVATION_CHANGE,
            REPAINT_ALL_TABS_ON_ACTIVATION_CHANGE,
            REPAINT_ON_SELECTION_CHANGE,
            REPAINT_ALL_TABS_ON_SELECTION_CHANGE,
            REPAINT_ON_CLOSE_BUTTON_PRESSED,
        };
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < vals.length; i++) {
            if ((policy & vals[i]) != 0) {
                sb.append (names[i]);
                if (i != vals.length-1) {
                    sb.append ('+');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Get the repaint policy that will be used to determine what tabs to repaint, based on state changes.
     * The default implementation in BasicTabDisplayerUI simply ignores the tab argument and returns a
     * single policy for all tabs created in <code>BasicTabDisplayerUI.createRepaintPolicy()</code>
     *
     * @param tab Index of tab in question 
     * @return Type of repaint policy for given tab
     */
    public abstract int getRepaintPolicy(int tab);
}
