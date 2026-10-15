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
package org.openide.awt;

import org.openide.util.NbBundle;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.text.MessageFormat;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;

import javax.swing.*;
import javax.swing.JComponent.AccessibleJComponent;
import javax.swing.border.*;


/** The SplittedPanel widget is a Panel that can contain one or two components and
* place them side-by-side vertically or horizontally with a splitter in the middle.
* User can move the split point by dragging the splitter with mouse.
* The two components are accessed by add/remove methods with constraints value
* ADD_SPLITTER, ADD_FIRST, ADD_SECOND, ....
* The split position could be either absolute or proportional (according to the
* "Absolute" property setting) - in thwe absolute mode the split point remains same
* when resizing (i.e. the left/top component keeps its size and only the
* right/bottom component resizes), while in the proportional mode the splitPosition
* is a percentage of the width/height assigned to the left/top component.

* <TABLE>
* <caption>properties of SplittedPanel</caption>
* <TR><TH style="width:15%" >Property<TH style="width:15%" >Property Type<TH>Description
* <TR><TD> SplitType            <TD> int       <TD> The type of the splitting - HORIZONTAL, VERTICAL or NONE
* <TR><TD> SplitPosition        <TD> int       <TD> The position of the split point - either absolute position or number of percents
*                                                   according to the "Absolute" property settings, could be one of FIRST_PREFERRED or
*                                                   SECOND_PREFERRED, which means that the split point should be placed so that
*                                                   the first(left/top) resp. second (bottom/rignt) is sized according to its preferredSize
*                                                   (in this case the Absolute property setting is ignored)
* <TR><TD> SplitterType         <TD> int       <TD> The type of the component that renders the splitter - DEFAULT_SPLITTER, RAISED_SPLITTER,
*                                                   EMPTY_SPLITTER.
* <TR><TD> SplitterComponent    <TD> Component <TD> The component that renders the splitter. A custom component can be provided in addition to EMPTY_SPLITTER and RAISED-SPLITTER using this method.
* <TR><TD> SplitAbsolute        <TD> boolean   <TD> if true then the meaning of the SplitPosition is absolute points,
*                                                   otherwise the SplitPosition is a number of percents
* <TR><TD> SplitDragable        <TD> boolean   <TD> if true then the split point can be dragged using a mouse,
*                                                   otherwise the SplitPosition is fixed
* <TR><TD> SplitTypeChangeEnabled  <TD> boolean<TD> if true then the split type can be changed via popup menu commands
* <TR><TD> SwapPanesEnabled        <TD> boolean<TD> if true then the panes can be swapped via popup menu command
* </TABLE>
*
* @author  Ian Formanek
* @deprecated This class does nothing interesting that cannot be done with a JSplitPane.
*  Use a JSplitPane instead.
*/
@Deprecated
public class SplittedPanel extends JComponent implements Accessible {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5058424218525927233L;

    /** constant for no split - only the first (left/top) component will be shown */
    public static final int NONE = 0;

    /** constant for vertical split */
    public static final int VERTICAL = 1;

    /** constant for horizontal split */
    public static final int HORIZONTAL = 2;

    /** constraints constant for adding a splitter */
    public static final Object ADD_SPLITTER = 0;

    /** constraints constant for adding a component to the first (left/top) pane */
    public static final Object ADD_FIRST = 1;

    /** constraints constant for adding a component to the second (right/bottom) pane */
    public static final Object ADD_SECOND = 2;

    /** constraints constant for adding a component to the left(top) pane (an alias for the ADD_FIRST constant) */
    public static final Object ADD_LEFT = ADD_FIRST;

    /** constraints constant for adding a component to the top(left) pane (an alias for the ADD_FIRST constant) */
    public static final Object ADD_TOP = ADD_FIRST;

    /** constraints constant for adding a component to the right(bottom) pane (an alias for the ADD_SECOND constant) */
    public static final Object ADD_RIGHT = ADD_SECOND;

    /** constraints constant for adding a component to the bottom(right) pane (an alias for the ADD_SECOND constant) */
    public static final Object ADD_BOTTOM = ADD_SECOND;

    /** constant for moving the split point so that the first (left/top) component is sized according to its preferredSize */
    public static final int FIRST_PREFERRED = -1;

    /** constant for moving the split point so that the second (right/bottom) component is sized according to its preferredSize */
    public static final int SECOND_PREFERRED = -2;

    /** constant for splitter component types - raised splitter*/
    public static final int RAISED_SPLITTER = 0;

    /** constant for splitter component types - empty splitter */
    public static final int EMPTY_SPLITTER = 1;

    /** constant for splitter component types - default splitter (raised)*/
    public static final int DEFAULT_SPLITTER = RAISED_SPLITTER;
    private static MessageFormat nameFormat = null;
    private static MessageFormat descriptionFormat = null;

    /** The default split type */
    private static final int DEFAULT_SPLIT_TYPE = HORIZONTAL;

    /** Save the last preferred setting (first or second). Double click reset the splitPosition to this value */
    private int resetPosition = FIRST_PREFERRED;

    /** Is popup menu enabled?*/
    private Boolean popupMenuEnabled;
    private boolean drawBumps;

    // Private variables //
    /** the first (left/top) component */
    private Component firstComponent = null;

    /** the second (right/bottom) component */
    private Component secondComponent = null;

    /** the splitter component */
    private Component splitter = null;

    /** the splitter component type */
    private int splitterType = DEFAULT_SPLITTER;

    /** the mouse adapter that does the dragging of the splitter*/
    private transient MouseListenerAdapter mouseAdapter;

    /** current split type */
    private int splitType = NONE;

    /** current split position */
    private int splitPosition = 50;
    private boolean absolute = false;
    private boolean dragable = true;
    private boolean continuousLayout = true;

    /** current enabled/disabled state of change of split type */
    private boolean splitTypeChangeEnabled = true;

    /** current enabled/disabled state of change of swapping panes */
    private boolean swapPanesEnabled = true;

    /** current keepSecondSame state - this has bigger priority than keepFirstSame  */
    private boolean keepSecondSame = false;

    /** current keepFirstSame state */
    private boolean keepFirstSame = false;
    private transient boolean splitIsChanging = false;
    private int dragPos = -1;

    /** true if the panes were swapped, false otherwise */
    private boolean panesSwapped = false;

    /** popup menu for setting vertical/horizontal splitting */
    private transient JPopupMenu popupMenu;

    /** The popup menu item */
    private transient JRadioButtonMenuItem verticalCMI;

    /** The popup menu item */
    private transient JRadioButtonMenuItem horizontalCMI;

    /** The popup menu item */
    private transient JMenuItem swapCMI;

    /** The popup menu item */
    private transient JMenuItem splitterCMI;

    /** A Vector of SplitChangeListeners */
    private transient Vector<SplitChangeListener> listeners;

    /** Accessible context */
    private AccessibleContext accessibleContext;

    /** Constructs a new empty SplittedPanel with no spliting.
    */
    public SplittedPanel() {
        splitter = new DefaultSplitter(getDefaultSplitterSize());
        accessibleContext = null;
        setLayout(new SplitLayout());
        add(splitter, ADD_SPLITTER);
        init();

        RuntimeException rte = new RuntimeException("SplittedPanel is deprecated.  Please use JSplitPane instead"); //NOI18N
        Logger.getLogger(SplittedPanel.class.getName()).log(Level.WARNING, null, rte);
    }

    /** Initializes the SplittedPanel */
    private void init() {
        setSplitterCursor();
        mouseAdapter = new MouseListenerAdapter();

        if (dragable) {
            splitter.addMouseMotionListener(mouseAdapter);
            splitter.addMouseListener(mouseAdapter);
            addSplitChangeListener(mouseAdapter);
        }

        initAccessible();
    }

    /** Updates splitting, too. */
    @Override
    public void updateUI() {
        super.updateUI();
        updateSplitting();

        Object o = UIManager.get("nb.SplittedPanel.drawBumps");
        drawBumps = Boolean.TRUE.equals(o);
    }

    /** Updates the visual state and layout when the split state changes. */
    protected void updateSplitting() {
        if ((firstComponent != null) && (secondComponent != null)) {
            invalidate();
            firstComponent.invalidate();
            splitter.invalidate();
            secondComponent.invalidate();
            validate();
        }
    }

    /** Computes component sizes after performing the flip,
    * it means splitTypeChange */
    protected void computeSizesAfterFlip() {
        if ((firstComponent == null) || (secondComponent == null)) {
            return;
        }

        Dimension ourSize = getSize();
        int splitterSize;

        switch (splitType) {
        case VERTICAL:

            if (ourSize.width == 0) {
                break;
            }

            splitterSize = splitter.getPreferredSize().height;

            int newHeight = ((ourSize.height - splitterSize) * firstComponent.getSize().width) / ourSize.width;
            firstComponent.setSize(new Dimension(ourSize.width, newHeight));
            secondComponent.setSize(new Dimension(ourSize.width, ourSize.height - newHeight - splitterSize));

            break;

        case HORIZONTAL:

            if (ourSize.height == 0) {
                break;
            }

            splitterSize = splitter.getPreferredSize().width;

            int newWidth = ((ourSize.width - splitterSize) * firstComponent.getSize().height) / ourSize.height;
            firstComponent.setSize(new Dimension(newWidth, ourSize.height));
            secondComponent.setSize(new Dimension(ourSize.width - newWidth - splitterSize, ourSize.height));

            break;
        }
    }

    /** Updates the splitter's cursor according to the current SplittedPanel settings. */
    protected void setSplitterCursor() {
        if (dragable) {
            if (splitType == VERTICAL) {
                splitter.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            } else {
                splitter.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
            }
        } else {
            splitter.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /** Is popup menu on spliter enabled? See issue 25216.*/
    private boolean isPopupMenuEnabled() {
        if (popupMenuEnabled == null) {
            Object o = getClientProperty("popupMenuEnabled");

            if (o instanceof Boolean) {
                popupMenuEnabled = (Boolean) o;
            } else {
                popupMenuEnabled = Boolean.TRUE;
            }
        }

        return popupMenuEnabled.booleanValue();
    }

    /** Updates the splitter's popup menu. */
    protected void updatePopupMenu() {
        if (popupMenu == null) {
            popupMenu = new JPopupMenu();

            java.util.ResourceBundle awtBundle = NbBundle.getBundle(SplittedPanel.class);

            popupMenu.add(verticalCMI = new JRadioButtonMenuItem(awtBundle.getString("SplittedPanelVertical")));
            popupMenu.add(horizontalCMI = new JRadioButtonMenuItem(awtBundle.getString("SplittedPanelHorizontal")));
            popupMenu.add(new JSeparator());
            popupMenu.add(swapCMI = new JMenuItem(awtBundle.getString("SplittedPanelSwap")));
            popupMenu.add(new JSeparator());
            popupMenu.add(splitterCMI = new JMenuItem(awtBundle.getString("ResetSplitter")));

            ActionListener al = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (horizontalCMI.equals(e.getSource())) {
                            setSplitType(HORIZONTAL);
                        } else {
                            setSplitType(VERTICAL);
                        }
                    }
                };

            verticalCMI.addActionListener(al);
            horizontalCMI.addActionListener(al);
            swapCMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        swapPanes();
                    }
                }
            );
            splitterCMI.addActionListener(
                new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        resetSplitter();
                    }
                }
            );
        }

        if (splitType == VERTICAL) {
            verticalCMI.setSelected(true);
            horizontalCMI.setSelected(false);
        } else {
            verticalCMI.setSelected(false);
            horizontalCMI.setSelected(true);
        }

        if (splitTypeChangeEnabled) {
            verticalCMI.setEnabled(true);
            horizontalCMI.setEnabled(true);
        } else {
            verticalCMI.setEnabled(false);
            horizontalCMI.setEnabled(false);
        }

        if (swapPanesEnabled) {
            swapCMI.setEnabled(true);
        } else {
            swapCMI.setEnabled(false);
        }

        splitterCMI.setEnabled((getSplitPosition() != FIRST_PREFERRED) && (getSplitPosition() != SECOND_PREFERRED));
    }

    /** Reset split line to follow the largest name. */
    private void resetSplitter() {
        if ((getSplitPosition() != FIRST_PREFERRED) && (getSplitPosition() != SECOND_PREFERRED)) {
            setSplitPosition(resetPosition);

            if (splitterCMI != null) {
                splitterCMI.setEnabled(false);
            }
        }
    }

    /** Swaps the panes.*/
    public void swapPanes() {
        if (!swapPanesEnabled) {
            return;
        }

        if ((firstComponent == null) || (secondComponent == null)) {
            return;
        }

        splitIsChanging = true;
        panesSwapped = !panesSwapped;

        if (keepSecondSame) {
            keepSecondSame = false;
            keepFirstSame = true;
        } else if (keepFirstSame) {
            keepSecondSame = true;
            keepFirstSame = false;
        }

        Component aFirstComponent = firstComponent;
        Component aSecondComponent = secondComponent;

        remove(aFirstComponent);
        remove(aSecondComponent);
        add(aSecondComponent, ADD_FIRST);
        add(aFirstComponent, ADD_SECOND);
        updateSplitting();
        splitIsChanging = false;
    }

    /** @return true if the panes are swapped, false otherwise */
    public boolean getPanesSwapped() {
        return panesSwapped;
    }

    // Property accessor methods //
    /** Getter method for the SplitType property.
    * @return Current SplitType value.
    */
    public int getSplitType() {
        return splitType;
    }

    /** Setter method for the SplitType property.
    * @param value New SplitType value.
    */
    public void setSplitType(int value) {
        if (splitType == value) {
            return;
        }

        int oldSplitType = splitType;
        splitType = value;

        if ((oldSplitType != NONE) && (splitType != NONE)) {
            computeSizesAfterFlip();
        }

        setSplitterCursor();
        updateSplitting();
        updatePopupMenu();

        initAccessible();
    }

    /** Getter method for the SplitPosition property.
    * @return Current SplitPosition value.
    */
    public int getSplitPosition() {
        return splitPosition;
    }

    /** Setter method for the SplitPosition property.
    * @param value New SplitPosition value.
    */
    public void setSplitPosition(int value) {
        if (splitPosition == value) {
            return;
        }

        int oldValue = splitPosition;
        splitPosition = value;
        splitIsChanging = true;
        updateSplitting();
        fireSplitChange(oldValue, splitPosition);
        splitIsChanging = false;
    }

    /** Getter method for the SplitterType property.
    * @return Current SplitterType value.
    * @see #EMPTY_SPLITTER
    * @see #RAISED_SPLITTER
    * @see #DEFAULT_SPLITTER
    */
    public int getSplitterType() {
        return splitterType;
    }

    private int getDefaultSplitterSize() {
        Object o = UIManager.get("nb.SplittedPanel.dividerSize"); //NOI18N

        if (o != null) {
            return ((Integer) o).intValue();
        }

        o = UIManager.get("SplitPane.dividerSize"); //NOI18N

        if (o != null) {
            return ((Integer) o).intValue();
        }

        return 6;
    }

    /** Setter method for the SplitterType property.
    * @param type New SplitterType value.
    * @see #EMPTY_SPLITTER
    * @see #RAISED_SPLITTER
    * @see #DEFAULT_SPLITTER
    */
    public void setSplitterType(int type) {
        if (splitterType == type) {
            return;
        }

        splitterType = type;

        switch (splitterType) {
        case EMPTY_SPLITTER:
            splitter = new EmptySplitter();

            break;

        default:
        case RAISED_SPLITTER:
            splitter = new DefaultSplitter(getDefaultSplitterSize());

            break;
        }

        add(splitter, ADD_SPLITTER);
        updateSplitting();
    }

    /** Getter method for the SplitterComponent property.
    * @return Current SplitterComponent value.
    * @see #getSplitterType
    */
    public Component getSplitterComponent() {
        return splitter;
    }

    /** Setter method for the SplitterComponent property.
    * @param comp New SplitterComponent value.
    * @see #setSplitterType
    */
    public void setSplitterComponent(Component comp) {
        if (splitter == comp) {
            return;
        }

        if (dragable) {
            splitter.removeMouseMotionListener(mouseAdapter);
            splitter.removeMouseListener(mouseAdapter);
        }

        remove(splitter);
        splitter = comp;
        add(splitter, ADD_SPLITTER);

        if (dragable) {
            splitter.addMouseMotionListener(mouseAdapter);
            splitter.addMouseListener(mouseAdapter);
        }

        setSplitterCursor();
        updateSplitting();
    }

    /** Getter method for the SplitAbsolute property.
    * @return Current SplitAbsolute value.
    */
    public boolean isSplitAbsolute() {
        return absolute;
    }

    /** Setter method for the SplitAbsolute property.
    * @param value New SplitAbsolute value.
    */
    public void setSplitAbsolute(boolean value) {
        if (absolute == value) {
            return;
        }

        absolute = value;
        updateSplitting();
    }

    /** Getter method for the SplitDragable property.
    * @return Current SplitDragable value.
    */
    public boolean isSplitDragable() {
        return dragable;
    }

    /** Setter method for the Dragable property.
    * @param value New Dragable value.
    */
    public void setSplitDragable(boolean value) {
        if (dragable == value) {
            return;
        }

        dragable = value;

        if (dragable) {
            splitter.addMouseMotionListener(mouseAdapter);
            splitter.addMouseListener(mouseAdapter);
        } else {
            splitter.removeMouseMotionListener(mouseAdapter);
            splitter.removeMouseListener(mouseAdapter);
        }

        setSplitterCursor();
    }

    /** Getter method for the ContinuousLayout property.
    * @return Current ContinuousLayout value.
    */
    public boolean isContinuousLayout() {
        return continuousLayout;
    }

    /** Setter method for the ContinuousLayout property.
    * @param value New ContinuousLayout value.
    */
    public void setContinuousLayout(boolean value) {
        continuousLayout = value;
    }

    /** Getter method for the KeepFirstSame property.
    * @return Current KeepFirstSame value.
    */
    public boolean getKeepFirstSame() {
        return keepFirstSame;
    }

    /** Setter method for the KeepFirstSame property.
    * @param value New KeepFirstSame value.
    */
    public void setKeepFirstSame(boolean value) {
        keepFirstSame = value;
    }

    /** Getter method for the KeepSecondSame property.
    * @return Current KeepSecondSame value.
    */
    public boolean getKeepSecondSame() {
        return keepSecondSame;
    }

    /** Setter method for the KeepSecondSame property.
    * @param value New KeepSecondSame value.
    */
    public void setKeepSecondSame(boolean value) {
        keepSecondSame = value;
    }

    /** Getter method for the SplitTypeChangeEnabled property.
    * @return Current SplitTypeChangeEnabled value.
    */
    public boolean isSplitTypeChangeEnabled() {
        return splitTypeChangeEnabled;
    }

    /** Setter method for the SplitTypeChangeEnabled property.
    * @param value New SplitTypeChangeEnabled value.
    */
    public void setSplitTypeChangeEnabled(boolean value) {
        if (splitTypeChangeEnabled == value) {
            return;
        }

        splitTypeChangeEnabled = value;
        updatePopupMenu();
    }

    /** Getter method for the SwapPanesEnabled property.
    * @return Current SwapPanesEnabled value.
    */
    public boolean isSwapPanesEnabled() {
        return swapPanesEnabled;
    }

    /** Setter method for the SwapPanesEnabled property.
    * @param value New SwapPanesEnabled value.
    */
    public void setSwapPanesEnabled(boolean value) {
        if (swapPanesEnabled == value) {
            return;
        }

        swapPanesEnabled = value;
        updatePopupMenu();
    }

    // Event Listeners //
    /** Adds specified listener to the current set of SplitChangeListeners */
    public void addSplitChangeListener(SplitChangeListener l) {
        if (listeners == null) {
            listeners = new Vector<SplitChangeListener>();
        }

        listeners.addElement(l);
    }

    /** Removes specified listener from the current set of SplitChangeListeners */
    public void removeSplitChangeListener(SplitChangeListener l) {
        if (listeners == null) {
            return;
        }

        listeners.removeElement(l);
    }

    /** Fires the SplitChange event */
    protected void fireSplitChange(int oldValue, int newValue) {
        if (listeners == null) {
            return;
        }

        Vector<SplitChangeListener> l;

        synchronized (this) {
            l = new Vector<SplitChangeListener>(listeners);
        }

        Enumeration en = l.elements();
        SplitChangeEvent evt = new SplitChangeEvent(this, oldValue, newValue);

        while (en.hasMoreElements()) {
            SplitChangeListener scl = (SplitChangeListener) en.nextElement();
            scl.splitChanged(evt);
        }
    }

    /* Read accessible context
     * @return - accessible context
     */
    public @Override AccessibleContext getAccessibleContext() {
        if (accessibleContext == null) {
            accessibleContext = new AccessibleJComponent() {
                        public @Override AccessibleRole getAccessibleRole() {
                            return AccessibleRole.SPLIT_PANE;
                        }
                    };
            initAccessible();
        }

        return accessibleContext;
    }

    private void initAccessible() {
        if (nameFormat == null) {
            ResourceBundle bundle = NbBundle.getBundle(SplittedPanel.class);
            nameFormat = new MessageFormat(bundle.getString("ACS_SplittedPanel_Name"));
        }

        getAccessibleContext().setAccessibleName(
            nameFormat.format(
                    new Object[]{
                            (!(firstComponent instanceof Accessible)) ? null
                                    : firstComponent.getAccessibleContext().getAccessibleName(),
                            (!(secondComponent instanceof Accessible)) ? null
                                    : secondComponent.getAccessibleContext().getAccessibleName()
                    }
            )
        );

        if (descriptionFormat == null) {
            ResourceBundle bundle = NbBundle.getBundle(SplittedPanel.class);
            descriptionFormat = new MessageFormat(bundle.getString("ACS_SplittedPanel_Description"));
        }

        getAccessibleContext().setAccessibleDescription(
            descriptionFormat.format(
                    new Object[]{
                            (!(firstComponent instanceof Accessible)) ? null
                                    : firstComponent.getAccessibleContext().getAccessibleDescription(),
                            (!(secondComponent instanceof Accessible)) ? null
                                    : secondComponent.getAccessibleContext().getAccessibleDescription()
                    }
            )
        );
    }

    /** Deserializes the component and initializes it. */
    private void readObject(java.io.ObjectInputStream ois)
    throws java.io.IOException, ClassNotFoundException {
        ois.defaultReadObject();
        init();
    }

    // Inner Classes //
    /** A listener interface for tracking split point changes */
    public static interface SplitChangeListener {
        /** Called when a split point changes
        * @param evt The SplitChangeEvent that describes the change
        */
        public void splitChanged(SplitChangeEvent evt);
    }

    /** An event that describes a split point change
      * @deprecated This class does nothing interesting that cannot be done with a JSplitPane.
      * Use a JSplitPane instead.
     */
    @Deprecated
    public static class SplitChangeEvent extends java.util.EventObject {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 6748966611210836878L;
        private int oldValue;
        private int newValue;

        /** Constructs a new SplitChangeEvent for specified source SplittedPanel and
        * old and new SplitPositions
        */
        public SplitChangeEvent(SplittedPanel splittedPanel, int oldValue, int newValue) {
            super(splittedPanel);
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        /** @return the old splitterPosition */
        public int getOldValue() {
            return oldValue;
        }

        /** @return the new splitterPosition */
        public int getNewValue() {
            return newValue;
        }
    }

    /** The EmptySplitter is an empty splitter component with specified width.
     * It can be used as the splitter via setSplitterComponent.
     * @deprecated This class does nothing interesting that cannot be done with a JSplitPane.
     * Use a JSplitPane instead.
    */
    @Deprecated
    public static class EmptySplitter extends JComponent implements Accessible {
        /** generated Serialized Version UID */
        static final long serialVersionUID = 929648193440460693L;
        private int width;
        private AccessibleContext accessibleContext;

        public EmptySplitter() {
            this(0);
        }

        public EmptySplitter(int width) {
            ResourceBundle bundle = NbBundle.getBundle(SplittedPanel.class);

            accessibleContext = null;
            this.width = width;

            getAccessibleContext().setAccessibleName(bundle.getString("ACS_SplittedPanel_EmptySplitter"));
            getAccessibleContext().setAccessibleName(bundle.getString("ACSD_SplittedPanel_EmptySplitter"));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width, width);
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleJComponent() {
                            @Override
                            public AccessibleRole getAccessibleRole() {
                                return AccessibleRole.SPLIT_PANE;
                            }
                        };
            }

            return accessibleContext;
        }
    }

    /** The DefaultSplitter class implements a splitting line that is to be used as a default splitter for the SplittedPanel.
    * It paints a raised 3D-line with given width.
    */
    class DefaultSplitter extends JComponent implements Accessible {
        static final long serialVersionUID = -4223135481223014719L;
        private int splitterSize;

        /**
        * Constructs a new DefaultSplitter with given width.
        * @param aWidth  the desired width of the splitting line, if the value is lower than 2, the width of 2 is used
        */
        public DefaultSplitter(int aSplitterSize) {
            splitterSize = aSplitterSize;

            if (splitterSize < 2) {
                splitterSize = 2;
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(splitterSize, splitterSize);
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);

            if (splitterSize <= 2) {
                return;
            }

            Dimension size = this.getSize();
            int height = size.height - 1;
            g.setColor(this.getBackground());

            Color high = UIManager.getColor("controlLtHighlight");

            //Color bg=UIManager.getColor("control");
            Color low = UIManager.getColor("controlDkShadow");

            boolean isMetal = UIManager.getLookAndFeel().getClass() == javax.swing.plaf.metal.MetalLookAndFeel.class;

            boolean firstHasBorder = true;
            boolean secondHasBorder = true;

            if (firstComponent instanceof JComponent) {
                Border b1 = ((JComponent) firstComponent).getBorder();
                firstHasBorder = (b1 != null) && (!(b1 instanceof EmptyBorder));
            }

            if (secondComponent instanceof JComponent) {
                Border b2 = ((JComponent) secondComponent).getBorder();
                secondHasBorder = (b2 != null) && (!(b2 instanceof EmptyBorder));
            }

            if (panesSwapped) {
                boolean b = firstHasBorder;
                firstHasBorder = secondHasBorder;
                secondHasBorder = b;
            }

            //draw the bumps
            if (isMetal && (splitterSize > 3) && drawBumps) {
                //looks backwards, but isn't - splitType==vertical gives you a
                //horizontal splitter
                int starty = (firstHasBorder && (splitType == VERTICAL)) ? 0 : 2;
                int startx = (firstHasBorder && (splitType == HORIZONTAL)) ? 0 : 2;

                for (int x = startx; (x + 1) < size.width; x += 4) {
                    for (int y = starty; (y + 1) < height; y += 4) {
                        g.setColor(this.getBackground().brighter());
                        g.drawLine(x, y, x, y);

                        if ((x < size.width) && (y < height)) {
                            g.drawLine(x + 2, y + 2, x + 2, y + 2);
                        }

                        g.setColor(this.getBackground().darker().darker());
                        g.drawLine(x + 1, y + 1, x + 1, y + 1);

                        if ((x < size.width) && (y < height)) {
                            g.drawLine(x + 3, y + 3, x + 3, y + 3);
                        }
                    }
                }
            }

            if (splitType == HORIZONTAL) {
                int pos = (size.width - splitterSize) / 2;

                if (!firstHasBorder) {
                    g.setColor(isMetal ? low : high);
                    g.drawLine(pos, 0, pos, size.height - 1);

                    if (isMetal) {
                        g.setColor(high);
                        g.drawLine(pos + 1, 0, pos + 1, size.height - 1);
                    }
                }

                if (!secondHasBorder) {
                    g.setColor(isMetal ? high : low);
                    g.drawLine((pos + splitterSize) - 1, 0, (pos + splitterSize) - 1, size.height - 1);

                    if (isMetal) {
                        g.setColor(low);
                        g.drawLine((pos + splitterSize) - 2, 0, (pos + splitterSize) - 2, size.height - 1);
                    }
                }
            } else if (splitType == VERTICAL) {
                int pos = (size.height - splitterSize) / 2;

                if (!firstHasBorder) {
                    g.setColor(isMetal ? low : high);
                    g.drawLine(0, pos, size.width - 1, pos);

                    if (isMetal) {
                        g.setColor(high);
                        g.drawLine(0, pos + 1, size.width - 1, pos + 1);
                    }
                }

                if (!secondHasBorder) {
                    g.setColor(isMetal ? high : low);
                    g.drawLine(0, (pos + splitterSize) - 1, size.width - 1, (pos + splitterSize) - 1);

                    if (isMetal) {
                        g.setColor(low);
                        g.drawLine(0, (pos + splitterSize) - 2, size.width - 1, (pos + splitterSize) - 2);
                    }
                }
            }
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            return SplittedPanel.this.getAccessibleContext();
        }
    }

    /**
    * The MouseListenerAdapter class implements the dragging behaviour of the splitter.
    */
    class MouseListenerAdapter extends MouseUtils.PopupMouseAdapter implements MouseListener, MouseMotionListener,
        SplitChangeListener {
        /** Called when the sequnce of mouse events should lead to actual
         * showing of the popup menu.
         * Should be redefined to show the menu.
         * param evt The mouse release event - should be used to obtain the
         *           position of the popup menu
         */
        protected void showPopup(MouseEvent e) {
            updatePopupMenu();

            if (isPopupMenuEnabled()) {
                popupMenu.show(splitter, e.getX(), e.getY());
            }
        }

        /** A method implemented from the MouseListener interface to handle the splitter dragging */
        public void mouseReleased(MouseEvent e) {
            super.mouseReleased(e);

            if (continuousLayout == false) {
                if (dragPos == -1) {
                    return;
                }

                if (!absolute) {
                    Dimension d = getSize();

                    if (splitType == VERTICAL) {
                        dragPos = (100 * dragPos) / d.height;
                    } else {
                        dragPos = (100 * dragPos) / d.width;
                    }
                }

                setSplitPosition(dragPos);
                dragPos = -1;
            }
        }

        /** A method implemented from the MouseMotionListener interface to handle the splitter dragging */
        public void mouseDragged(MouseEvent e) {
            if (continuousLayout == true) {
                Dimension d = getSize();
                Point splitterPos = splitter.getLocation();
                e.translatePoint(splitterPos.x, splitterPos.y);

                if (splitType == VERTICAL) {
                    dragPos = e.getY();

                    if (dragPos > d.height) {
                        dragPos = d.height;
                    }
                } else {
                    dragPos = e.getX();

                    if (dragPos > d.width) {
                        dragPos = d.width;
                    }
                }

                if (dragPos < 0) {
                    dragPos = 0;
                }

                if (continuousLayout) {
                    if (dragPos == -1) {
                        return;
                    }

                    int newDragPos = dragPos;

                    if (!absolute) {
                        if (splitType == VERTICAL) {
                            newDragPos = (100 * dragPos) / d.height;
                        } else {
                            newDragPos = (100 * dragPos) / d.width;
                        }
                    }

                    setSplitPosition(newDragPos);
                }
            }
        }

        /** A method implemented from the MouseMotionListener interface to handle the splitter dragging */
        public void mouseMoved(MouseEvent e) {
        }

        /* Double click on the splitter re-sets the splitter to follow one of the fields */
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            if ((e.getClickCount() == 2) && isPopupMenuEnabled()) {
                resetSplitter();
            }
        }

        public void splitChanged(SplitChangeEvent evt) {
            if ((evt.getNewValue() == FIRST_PREFERRED) || (evt.getNewValue() == SECOND_PREFERRED)) {
                resetPosition = evt.getNewValue();

                if (splitterCMI != null) {
                    splitterCMI.setEnabled(true);
                }
            }
        }
    }

    /**
    * The SplitLayout class implements a LayoutManager for the SplittedPanel.
    */
    class SplitLayout extends Object implements LayoutManager2, java.io.Serializable {
        static final long serialVersionUID = 2034500275182524789L;

        public void addLayoutComponent(String name, Component comp) {
            throw new IllegalArgumentException("You must use the add(Component, Object) method for adding"); // NOI18N
        }

        public void addLayoutComponent(Component comp, Object constraints) {
            if (constraints == ADD_SPLITTER) { // adding a splitter
                splitter = comp;
            } else if (constraints == ADD_FIRST) { // adding to the left/top

                if ((firstComponent != null) && (secondComponent == null)) { // if we altready have ... [PENDING]
                    secondComponent = firstComponent;
                }

                firstComponent = comp;

                if ((secondComponent != null) && (splitType == NONE)) {
                    splitType = DEFAULT_SPLIT_TYPE;
                }
            } else if (constraints == ADD_SECOND) {
                if (firstComponent == null) {
                    firstComponent = comp;
                } else {
                    secondComponent = comp;

                    if (splitType == NONE) {
                        splitType = DEFAULT_SPLIT_TYPE;
                    }
                }
            } else {
                throw new IllegalArgumentException("You must use one of the SplittedPanel.ADD_XXX constraints Objects"); // NOI18N
            }

            initAccessible();
        }

        public void removeLayoutComponent(Component comp) {
            if (comp.equals(secondComponent)) {
                secondComponent = null;
            } else if (comp.equals(firstComponent)) {
                firstComponent = null;

                if (secondComponent != null) {
                    firstComponent = secondComponent;
                    secondComponent = null;
                }
            }

            initAccessible();
        }

        public Dimension preferredLayoutSize(Container parent) {
            int width = 0;
            int height = 0;

            if (firstComponent != null) {
                Dimension d = firstComponent.getPreferredSize();
                width = d.width;
                height = d.height;
            }

            if (secondComponent != null) {
                Dimension d = secondComponent.getPreferredSize();

                if (splitType == VERTICAL) {
                    int splitterSize = splitter.getPreferredSize().height;

                    if (width < d.width) {
                        width = d.width;
                    }

                    height += (splitterSize + d.height);
                } else {
                    int splitterSize = splitter.getPreferredSize().width;

                    if (height < d.height) {
                        height = d.height;
                    }

                    width += (splitterSize + d.width);
                }
            }

            return new Dimension(width, height);
        }

        public Dimension minimumLayoutSize(Container parent) {
            int width = 0;
            int height = 0;

            if (firstComponent != null) {
                Dimension d = firstComponent.getMinimumSize();
                width = d.width;
                height = d.height;
            }

            if (secondComponent != null) {
                Dimension d = secondComponent.getMinimumSize();

                if (splitType == VERTICAL) {
                    int splitterSize = splitter.getMinimumSize().height;

                    if (width < d.width) {
                        width = d.width;
                    }

                    height += (splitterSize + d.height);
                } else {
                    int splitterSize = splitter.getMinimumSize().width;

                    if (height < d.height) {
                        height = d.height;
                    }

                    width += (splitterSize + d.width);
                }
            }

            return new Dimension(width, height);
        }

        public void layoutContainer(Container parent) {
            Dimension d = parent.getSize();
            int sPosition = splitPosition;

            // 1. first preferred
            if (splitPosition == FIRST_PREFERRED) {
                if (splitType == VERTICAL) {
                    sPosition = firstComponent.getPreferredSize().height;
                } else {
                    sPosition = firstComponent.getPreferredSize().width;
                }

                // 2. second preferred
            } else if (splitPosition == SECOND_PREFERRED) {
                if (splitType == VERTICAL) {
                    sPosition = d.height - splitter.getPreferredSize().width -
                        secondComponent.getPreferredSize().height;
                } else {
                    sPosition = d.width - splitter.getPreferredSize().height -
                        secondComponent.getPreferredSize().width;
                }

                // 3. percent position
            } else if (!absolute) {
                int sp = splitPosition;

                if (sp > 100) {
                    sp = 100;
                }

                if (splitType == VERTICAL) {
                    sPosition = (d.height * sp) / 100;
                } else {
                    sPosition = (d.width * sp) / 100;
                }
            }

            if ((splitType != NONE) && (firstComponent != null) && (secondComponent != null)) { // splitted

                if (splitType == VERTICAL) {
                    int splitterSize = splitter.getPreferredSize().height;

                    if ((firstComponent == null) || (secondComponent == null)) {
                        splitterSize = 0;
                    }

                    if (keepSecondSame && !splitIsChanging) {
                        Dimension secondSize = secondComponent.getSize();

                        if (secondSize.height != 0) {
                            sPosition = d.height - secondSize.height - splitterSize;
                        }
                    }

                    if ((sPosition + splitterSize) > d.height) {
                        sPosition = d.height - splitterSize;
                    }

                    if (sPosition < 0) {
                        sPosition = 0;
                    }

                    firstComponent.setBounds(new Rectangle(0, 0, d.width, sPosition));
                    splitter.setBounds(new Rectangle(0, sPosition, d.width, splitterSize));
                    secondComponent.setBounds(
                        new Rectangle(0, sPosition + splitterSize, d.width, d.height - sPosition - splitterSize)
                    );
                } else {
                    int splitterSize = splitter.getPreferredSize().width;

                    if ((firstComponent == null) || (secondComponent == null)) {
                        splitterSize = 0;
                    }

                    if (keepSecondSame && !splitIsChanging) {
                        Dimension secondSize = secondComponent.getSize();

                        if (secondSize.width != 0) {
                            sPosition = d.width - secondSize.width - splitterSize;
                        }
                    }

                    if ((sPosition + splitterSize) > d.width) {
                        sPosition = d.width - splitterSize;
                    }

                    if (sPosition < 0) {
                        sPosition = 0;
                    }

                    firstComponent.setBounds(new Rectangle(0, 0, sPosition, d.height));
                    splitter.setBounds(new Rectangle(sPosition, 0, splitterSize, d.height));
                    secondComponent.setBounds(
                        new Rectangle(sPosition + splitterSize, 0, d.width - sPosition - splitterSize, d.height)
                    );
                }
            } else if (firstComponent != null) {
                firstComponent.setBounds(new Rectangle(0, 0, d.width - 1, d.height - 1));

                if (splitter != null) {
                    splitter.setBounds(0, 0, 0, 0);
                }
            }
        }

        public Dimension maximumLayoutSize(Container target) {
            return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        public float getLayoutAlignmentX(Container target) {
            return 0;
        }

        public float getLayoutAlignmentY(Container target) {
            return 0;
        }

        public void invalidateLayout(Container target) {
        }
    }
}
