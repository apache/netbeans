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
package org.openide.awt;

import java.awt.*;
import java.awt.event.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.Serializable;

import java.util.Vector;

import javax.swing.*;
import javax.swing.event.*;


/** ListPane. This component is derived from JList component and enables
 * list objects in several columns.
 *
 * @author   Petr Hamernik, Ian Formanek, Jaroslav Tulach
 * @deprecated This class does nothing interesting which cannot be
 * done in a more reliable, scalable way with a JTable.
 */
@Deprecated
public class ListPane extends JList {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3828318151121500783L;
    private int fixedCellWidth = 100;
    private int fixedCellHeight = 100;
    private int visibleRowCount = 6;
    private int visibleColumnCount = 4;
    private int realRowCount = 1;
    private int realColumnCount = 1;
    ListDataListener dataL;
    PropertyChangeListener propertyL;
    InputListener inputL;
    ListSelectionListener selectionL;
    boolean updateLayoutStateNeeded = true;

    /**
     * Construct a JList that displays the elements in the specified,
     * non-null model.  All JList constructors delegate to this one.
     */
    public ListPane(ListModel dataModel) {
        super(dataModel);

        addListListeners();
    }

    /**
     * Construct a JList that displays the elements in the specified
     * array.  This constructor just delegates to the ListModel
     * constructor.
     */
    public ListPane(final Object[] listData) {
        this(
            new AbstractListModel() {
                public int getSize() {
                    return listData.length;
                }

                public Object getElementAt(int i) {
                    return listData[i];
                }
            }
        );
    }

    /**
     * Construct a JList that displays the elements in the specified
     * Vector.  This constructor just delegates to the ListModel
     * constructor.
     */
    public ListPane(final Vector listData) {
        this(
            new AbstractListModel() {
                public int getSize() {
                    return listData.size();
                }

                public Object getElementAt(int i) {
                    return listData.elementAt(i);
                }
            }
        );
    }

    /**
     * Constructs a JList with an empty model.
     */
    public ListPane() {
        this(
            new AbstractListModel() {
                public int getSize() {
                    return 0;
                }

                public Object getElementAt(int i) {
                    return null;
                }
            }
        );
    }

    /**
     * JList components are always opaque.
     * @return true
     */
    @Override
    public boolean isOpaque() {
        return true;
    }

    /**
     * Return the value of the visibleRowCount property.
     * @see #setVisibleRowCount
     */
    public int getVisibleColumnCount() {
        return visibleColumnCount;
    }

    /**
     * Set the preferred number of rows in the list that are visible within
     * the nearest JViewport ancestor, if any.  The value of this property
     * only affects the value of the JLists preferredScrollableViewportSize.
     * <p>
     * The default value of this property is 8.
     * <p>
     * This is a JavaBeans bound property.
     *
     * @see #getVisibleRowCount
     * @see JComponent#getVisibleRect
     */
    public void setVisibleColumnCount(int visibleColumnCount) {
        int oldValue = this.visibleColumnCount;
        this.visibleColumnCount = Math.max(0, visibleColumnCount);
        firePropertyChange("visibleColumnCount", oldValue, visibleColumnCount); // NOI18N
    }

    /**
     * If this JList is being displayed withing a JViewport and the
     * specified cell isn't completely visible, scroll the viewport.
     *
     * @param index The index of the cell to make visible
     * @see JComponent#scrollRectToVisible
     * @see #getVisibleRect
     */
    @Override
    public void ensureIndexIsVisible(int index) {
        Point first = indexToLocation(index);

        if (first != null) {
            Rectangle cellBounds = new Rectangle(first.x, first.y, fixedCellWidth, fixedCellHeight);
            scrollRectToVisible(cellBounds);
        }
    }

    /**
     * Convert a point in JList coordinates to the index
     * of the cell at that location.  Returns -1 if there's no
     * cell the specified location.
     *
     * @param location The JList relative coordinates of the cell
     * @return The index of the cell at location, or -1.
     */
    @Override
    public int locationToIndex(Point location) {
        int x = location.x / fixedCellWidth;

        if (x >= realColumnCount) {
            return -1;
        }

        int y = location.y / fixedCellHeight;

        if (y >= realRowCount) {
            return -1;
        }

        int ret = (y * realColumnCount) + x;

        return (ret >= getModel().getSize()) ? (-1) : ret;
    }

    /**
     * Returns the origin of the specified item in JList
     * coordinates, null if index isn't valid.
     *
     * @param index The index of the JList cell.
     * @return The origin of the index'th cell.
     */
    @Override
    public Point indexToLocation(int index) {
        if (index >= getModel().getSize()) {
            return null;
        }

        int y = index / realColumnCount;
        int x = index % realColumnCount;

        return new Point(x * fixedCellWidth, y * fixedCellHeight);
    }

    /**
     * Returns the bounds of the specified item in JList
     * coordinates, null if index isn't valid.
     *
     * @param index1 start index of the JList cell.
     * @param index2 end index of the JList cell.
     * @return The bounds of the index'th cell.
     */
    @Override
    public Rectangle getCellBounds(int index1, int index2) {
        /*
        int minIndex = Math.min(index1, index2);
        int maxIndex = Math.max(index1, index2);
         */
        Point p1 = indexToLocation(index1);
        Point p2 = indexToLocation(index2);

        int x1 = p1.x;
        int y1 = p1.y;
        int x2 = p2.x + fixedCellWidth;
        int y2 = p2.y + fixedCellHeight;

        if (p1.y != p2.y) {
            x1 = 0;
            x2 = fixedCellWidth * realColumnCount;
        }

        return new Rectangle(x1, y1, x2 - x1, y2 - y1);
    }

    /**
     * --- The Scrollable Implementation ---
     */

    // @see #setPrototypeCellValue

    /**
     * Compute the size of the viewport needed to display visibleRowCount
     * rows.  This is trivial if fixedCellWidth and fixedCellHeight
     * were specified.  Note that they can specified implicitly with
     * the prototypeCellValue property.  If fixedCellWidth wasn't specified,
     * it's computed by finding the widest list element.  If fixedCellHeight
     * wasn't specified then we resort to heuristics:
     * <ul>
     * <li>
     * If the model isn't empty we just multiply the height of the first row
     * by visibleRowCount.
     * <li>
     * If the model is empty, i.e. JList.getModel().getSize() == 0, then
     * we just allocate 16 pixels per visible row, and 100 pixels
     * for the width (unless fixedCellWidth was set), and hope for the best.
     * </ul>
     *
     * @see #getPreferredScrollableViewportSize
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        Insets insets = getInsets();
        int w = insets.left + insets.right + (visibleColumnCount * fixedCellWidth);
        int h = insets.top + insets.bottom + (visibleRowCount * fixedCellHeight);
        Dimension dim = new Dimension(w, h);

        return dim;
    }

    /**
     * If we're scrolling downwards (<code>direction</code> is
     * greater than 0), and the first row is completely visible with respect
     * to <code>visibleRect</code>, then return its height.  If
     * we're scrolling downwards and the first row is only partially visible,
     * return the height of the visible part of the first row.  Similarly
     * if we're scrolling upwards we return the height of the row above
     * the first row, unless the first row is partially visible.
     *
     * @return The distance to scroll to expose the next or previous row.
     * @see Scrollable#getScrollableUnitIncrement
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return 1;
        } else {
            int row = getFirstVisibleIndex();

            if (row == -1) {
                return 0;
            } else {
                /* Scroll Down */
                if (direction > 0) {
                    Rectangle r = getCellBounds(row, row);

                    return (r == null) ? 0 : (r.height - (visibleRect.y - r.y));
                }
                /* Scroll Up */
                else {
                    Rectangle r = getCellBounds(row, row);

                    /* The first row is completely visible and it's row 0.
                     * We're done.
                     */
                    if ((r.y == visibleRect.y) && (row == 0)) {
                        return 0;
                    }
                    /* The first row is completely visible, return the
                     * height of the previous row.
                     */
                    else if (r.y == visibleRect.y) {
                        Rectangle prevR = getCellBounds(row - 1, row - 1);

                        return (prevR == null) ? 0 : prevR.height;
                    }
                    /* The first row is partially visible, return the
                     * height of hidden part.
                     */
                    else {
                        return visibleRect.y - r.y;
                    }
                }
            }
        }
    }

    /**
     * @return The visibleRect.height or visibleRect.width per the orientation.
     * @see Scrollable#getScrollableUnitIncrement
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return (orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width;
    }

    /**
     * If this JList is displayed in a JViewport, don't change its width
     * when the viewports width changes.  This allows horizontal
     * scrolling if the JViewport is itself embedded in a JScrollPane.
     *
     * @return False - don't track the viewports width.
     * @see Scrollable#getScrollableTracksViewportWidth
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    /**
     * If this JList is displayed in a JViewport, don't change its height
     * when the viewports height changes.  This allows vertical
     * scrolling if the JViewport is itself embedded in a JScrollPane.
     *
     * @return False - don't track the viewports width.
     * @see Scrollable#getScrollableTracksViewportWidth
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    /**
     * If the list is opaque, paint its background.
     * Subclasses may want to override this method rather than paint().
     *
     * @see #paint
     */
    protected void paintBackground(Graphics g) {
        if (isOpaque()) {
            Color backup = g.getColor();
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(backup);
        }
    }

    /**
     * Paint one List cell: compute the relevant state, get the "rubber stamp"
     * cell renderer component, and then use the CellRendererPane to paint it.
     * Subclasses may want to override this method rather than paint().
     *
     * @see #paint
     */
    private void paintCell(Graphics g, int index) {
        Object value = getModel().getElementAt(index);

        boolean cellHasFocus = hasFocus() && (index == getSelectionModel().getLeadSelectionIndex());
        boolean isSelected = getSelectionModel().isSelectedIndex(index);

        Component renderer = getCellRenderer().getListCellRendererComponent(
                this, value, index, isSelected, cellHasFocus
            );
        renderer.setSize(fixedCellWidth, fixedCellHeight);
        renderer.paint(g);
    }

    /**
     * Paint the rows that intersect the Graphics objects clipRect.  This
     * method calls paintBackground and paintCell as necessary.  Subclasses
     * may want to override these methods.
     *
     * @see #paintBackground
     */
    @Override
    protected void paintComponent(Graphics g) {
        updateLayoutState();

        if (getCellRenderer() == null) {
            return;
        }

        paintBackground(g);

        int last = getModel().getSize();
        int dx;
        int dy;

        for (int i = 0; i < last; i++) {
            //Point p = indexToLocation(i);
            paintCell(g, i);

            if (((i + 1) % realColumnCount) == 0) {
                dx = -fixedCellWidth * (realColumnCount - 1);
                dy = fixedCellHeight;
            } else {
                dx = fixedCellWidth;
                dy = 0;
            }

            g.translate(dx, dy);
        }
    }

    /** Recalculates the value of variables realRowCount and
     * realColumnCount. If the view needs update calls
     * revalidate.
     */
    private void updateLayoutState() {
        Dimension d = getSize();
        int x = d.width / fixedCellWidth;

        if (x < 1) {
            x = 1;
        }

        if (x != realColumnCount) {
            realColumnCount = x;
            updateLayoutStateNeeded = true;
        }

        int y = d.height / fixedCellHeight;

        if (y != realRowCount) {
            realRowCount = y;
            updateLayoutStateNeeded = true;
        }

        while ((realRowCount * realColumnCount) < getModel().getSize()) {
            realRowCount++;
        }

        locationToIndex(getVisibleRect().getLocation());

        if (updateLayoutStateNeeded) {
            updateLayoutStateNeeded = false;
            revalidate();
        }
    }

    /**
     * The preferredSize of a list is total height of the rows
     * and the maximum width of the cells.  If JList.fixedCellHeight
     * is specified then the total height of the rows is just
     * (cellVerticalMargins + fixedCellHeight) * model.getSize() where
     * rowVerticalMargins is the space we allocate for drawing
     * the yellow focus outline.  Similarly if JListfixedCellWidth is
     * specified then we just use that plus the horizontal margins.
     *
     * @return The total size of the
     */
    @Override
    public Dimension getPreferredSize() {
        Insets insets = getInsets();

        /*
        int dx = insets.left + insets.right;
        int dy = insets.top + insets.bottom;
         */
        int max = getModel().getSize() - 1;

        if (max <= 0) {
            return new Dimension(fixedCellWidth, fixedCellHeight);
        }

        int y = (max / realColumnCount) + 1;
        int x = ((max < realColumnCount) ? (max + 1) : realColumnCount);

        int xParent = getParent().getSize().width;
        int yParent = getParent().getSize().height;

        int xRes = Math.max(xParent, x * fixedCellWidth);
        int yRes = Math.max(yParent, y * fixedCellHeight);

        Dimension d = new Dimension(xRes, yRes);

        return d;
    }

    /**
     * @return the size of one cell
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(fixedCellWidth, fixedCellHeight);
    }

    /**
     * Create and install the listeners for the JList, its model, and its
     * selectionModel.  This method is called at creation time.
     */
    private void addListListeners() {
        inputL = createInputListener();
        addMouseListener(inputL);
        addKeyListener(inputL);
        addFocusListener(inputL);

        /* When a property changes that effects layout we set
         * updateLayoutStateNeeded to the appropriate code.  We also
         * add/remove List data model listeners when the "model"
         * property changes.
         */
        propertyL = createPropertyListener();
        addPropertyChangeListener(propertyL);

        dataL = createDataListener();

        ListModel model = getModel();

        if (model != null) {
            model.addListDataListener(dataL);
        }

        if (selectionL == null) {
            selectionL = new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e) {
                            repaint();
                        }
                    };

            ListSelectionModel selectionModel = getSelectionModel();

            if (selectionModel != null) {
                selectionModel.addListSelectionListener(selectionL);
            }
        }
    }

    //
    //   ========== Listener inner classes ===============
    //
    private InputListener createInputListener() {
        return new InputListener();
    }

    private ListDataListener createDataListener() {
        return new DataListener();
    }

    //  protected ListSelectionListener createSelectionListener() {
    //    return new SelectionListener();
    //  }
    private PropertyChangeListener createPropertyListener() {
        return new PropertyListener();
    }

    /**
     */
    private void mySetSelectionInterval(int anchor, int lead) {
        super.setSelectionInterval(anchor, lead);
    }

    /** Sets the selection to be the union of the specified interval with current
     */
    private void myAddSelectionInterval(int anchor, int lead) {
        super.addSelectionInterval(anchor, lead);
    }

    /** Sets the selection to be the set difference of the specified interval
     */
    private void myRemoveSelectionInterval(int index0, int index1) {
        super.removeSelectionInterval(index0, index1);
    }

    @Override
    public void setSelectionInterval(int anchor, int lead) {
        //        super.setSelectionInterval(anchor, lead);
    }

    @Override
    public void addSelectionInterval(int anchor, int lead) {
        //        super.addSelectionInterval(anchor, lead);
    }

    @Override
    public void removeSelectionInterval(int index0, int index1) {
        //        super.removeSelectionInterval(index0, index1);
    }

    //
    //   ------- Input Listener ------------
    //

    /**
     * Mouse input, and focus handling for JList.  An instance of this
     * class is added to the appropriate java.awt.Component lists
     * at creation time.  Note keyboard input is handled with JComponent
     * KeyboardActions, see registerKeyboardActions().
     * See createInputListener and registerKeyboardActions.
     */
    private class InputListener extends MouseAdapter implements FocusListener, KeyListener, Serializable {
        static final long serialVersionUID = -7907848327510962576L;
        transient int dragFirstIndex = -1;
        transient int dragLastIndex = -1;

        InputListener() {
        }

        // ==== Mouse methods =====
        @Override
        public void mousePressed(MouseEvent e) {
            updateSelection(locationToIndex(e.getPoint()), e);

            if (!hasFocus()) {
                requestFocus();
            }
        }

        // ==== Focus methods =====
        public void focusGained(FocusEvent e) {
            repaintCellFocus();
        }

        public void focusLost(FocusEvent e) {
            repaintCellFocus();
        }

        protected void repaintCellFocus() {
            repaint();
        }

        // ==== Key methods =====
        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
            int s = getLeadSelectionIndex();

            if (s < 0) {
                if (getModel().getSize() > 0) {
                    s = 0;
                } else {
                    return;
                }
            } else {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    s -= 1;

                    break;

                case KeyEvent.VK_RIGHT:
                    s += 1;

                    break;

                case KeyEvent.VK_UP:
                    s -= realColumnCount;

                    break;

                case KeyEvent.VK_DOWN:
                    s += realColumnCount;

                    break;

                case KeyEvent.VK_HOME:
                    s = 0;

                    break;

                case KeyEvent.VK_END:
                    s = getModel().getSize() - 1;

                    break;

                case KeyEvent.VK_PAGE_UP:
                    s -= (realColumnCount * realRowCount);

                    break;

                case KeyEvent.VK_PAGE_DOWN:
                    s += (realColumnCount * realRowCount);

                    break;

                default:
                    return;
                }
            }

            if (s < 0) {
                s = 0;
            }

            if (s > (getModel().getSize() - 1)) {
                s = getModel().getSize() - 1;
            }

            if (s >= 0) {
                updateSelection(s, e);
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        // ==== Update selection =====
        protected void updateSelection(int index, InputEvent e) {
            ListSelectionModel sm = getSelectionModel();

            if (index != -1) {
                setValueIsAdjusting(true);

                if (e.isShiftDown()) {
                    if (e.isControlDown()) {
                        if (dragFirstIndex == -1) {
                            myAddSelectionInterval(index, index);
                        } else {
                            if (dragLastIndex == -1) {
                                myAddSelectionInterval(dragFirstIndex, index);
                            } else {
                                myRemoveSelectionInterval(dragFirstIndex, dragLastIndex);
                                myAddSelectionInterval(dragFirstIndex, index);
                            }
                        }
                    } else {
                        if (dragFirstIndex == -1) {
                            myAddSelectionInterval(index, index);
                        } else {
                            mySetSelectionInterval(dragFirstIndex, index);
                        }
                    }

                    if (dragFirstIndex == -1) {
                        dragFirstIndex = index;
                        dragLastIndex = -1;
                    } else {
                        dragLastIndex = index;
                    }
                } else {
                    if (e.isControlDown()) {
                        if (isSelectedIndex(index)) {
                            myRemoveSelectionInterval(index, index);
                        } else {
                            myAddSelectionInterval(index, index);
                        }
                    } else {
                        mySetSelectionInterval(index, index);
                    }

                    dragFirstIndex = index;
                    dragLastIndex = -1;
                }

                setValueIsAdjusting(false);
            } else {
                sm.clearSelection();
            }
        }
    }

    //
    //   ------- Data Listener ------------
    //

    /**
     * The ListDataListener that's added to the JLists model at
     * creation time, and whenever the JList.model property changes.
     * See createDataListener.
     *
     * @see JList#getModel
     */
    private class DataListener implements ListDataListener, Serializable {
        static final long serialVersionUID = -2252515707418441L;

        DataListener() {
        }

        public void intervalAdded(ListDataEvent e) {
            updateLayoutStateNeeded = true;

            int minIndex = Math.min(e.getIndex0(), e.getIndex1());
            int maxIndex = Math.max(e.getIndex0(), e.getIndex1());

            /* Sync the SelectionModel with the DataModel.
             */
            ListSelectionModel sm = getSelectionModel();

            if (sm != null) {
                sm.insertIndexInterval(minIndex, maxIndex - minIndex, true);
            }
        }

        public void intervalRemoved(ListDataEvent e) {
            updateLayoutStateNeeded = true;

            /* Sync the SelectionModel with the DataModel. */
            ListSelectionModel sm = getSelectionModel();

            if (sm != null) {
                sm.removeIndexInterval(e.getIndex0(), e.getIndex1());
            }
        }

        public void contentsChanged(ListDataEvent e) {
            updateLayoutStateNeeded = true;
        }
    }

    //
    //   ------- Property Listener ------------
    //

    /**
     * The PropertyChangeListener that's added to the JList at
     * creation time.  When the value of a JList property that
     * affects layout changes, we set a bit in updateLayoutStateNeeded.
     * If the JLists model changes we additionally remove our listeners
     * from the old model.  Likewise for the JList selectionModel.
     * See createPropertyListener.
     */
    private class PropertyListener implements PropertyChangeListener, Serializable {
        static final long serialVersionUID = -6765578311995604737L;

        PropertyListener() {
        }

        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();

            if (propertyName.equals("model")) { // NOI18N

                ListModel oldModel = (ListModel) e.getOldValue();
                ListModel newModel = (ListModel) e.getNewValue();

                if (oldModel != null) {
                    oldModel.removeListDataListener(dataL);
                }

                if (newModel != null) {
                    newModel.addListDataListener(dataL);
                    updateLayoutStateNeeded = true;
                    repaint();
                }
            } else if (propertyName.equals("selectionModel")) { // NOI18N

                ListSelectionModel oldModelS = (ListSelectionModel) e.getOldValue();
                ListSelectionModel newModelS = (ListSelectionModel) e.getNewValue();

                if (oldModelS != null) {
                    oldModelS.removeListSelectionListener(selectionL);
                }

                if (newModelS != null) {
                    newModelS.addListSelectionListener(selectionL);
                }

                updateLayoutStateNeeded = true;
                repaint();
            } else if (
                propertyName.equals("cellRenderer") || // NOI18N
                    propertyName.equals("font") || // NOI18N
                    propertyName.equals("fixedCellHeight") || // NOI18N
                    propertyName.equals("fixedCellWidth")
            ) { // NOI18N
                updateLayoutStateNeeded = true;
                repaint();
            }
        }
    }
}
