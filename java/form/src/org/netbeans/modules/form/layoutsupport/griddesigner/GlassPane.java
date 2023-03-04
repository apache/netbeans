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

package org.netbeans.modules.form.layoutsupport.griddesigner;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import org.netbeans.modules.form.FormLoaderSettings;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.AbstractGridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.DeleteComponentAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridAction;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridActionPerformer;
import org.netbeans.modules.form.layoutsupport.griddesigner.actions.GridBoundsChange;

/**
 * Glass pane of the grid designer.
 *
 * @author Jan Stola
 * @author Petr Somol
 */
public class GlassPane extends JPanel implements GridActionPerformer {
    /** Color of the grid. */
    private static final Color GRID_COLOR = new Color(192, 192, 192, 128);
    /** Color of the highlighted cell. */
    private static final Color HIGHLIGHT_COLOR = new Color(64, 255, 64, 64);
    /** Gap between the grid and the header. */
    static final int HEADER_GAP = 10;

    private GridDesigner designer;
    /** The container we are glasspane for. */
    private Container innerPane;
    /** The subcontainer of {@code innerPane} that contains the grid. */
    private Container componentPane;
    /** Grid manager. */
    private GridManager gridManager;
    /** Grid information provider. */
    private GridInfoProvider gridInfo;
    /** Grid customizer. */
    private GridCustomizer customizer;

    // SELECTION

    /** Selected components. */
    private Set<Component> selection = new HashSet<Component>();
    /** Focused component, i.e., component recently clicked. */
    private Component focusedComponent;
    /** Selected columns. */
    private BitSet selectedColumns = new BitSet();
    /** Selected rows. */
    private BitSet selectedRows = new BitSet();
    /** Row of focused cell - used by actions with CELL context. */
    private int focusedCellRow;
    /** Column of focused cell - used by actions with CELL context. */
    private int focusedCellColumn;

    /** The height of the column header. */
    private int headerHeight;
    /** The width of the row header. */
    private int headerWidth;

    // HEADER emphaisizing

    /** Index of header under the cursor. */
    private int emphColumnHeader = -1;
    /** Index of header under the cursor. */
    private int emphRowHeader = -1;

    // RESIZING and MOVING

    /** Determines if we are in the middle of moving. */
    private boolean moving;
    /** Determines if we are in the middle of resizing. */
    private boolean resizing;
    /** Determines if we are in the middle of area selection. */
    private boolean selecting;
    /** Determines the direction in which we are (or will start) resizing. */
    private int resizingMode;
    /** The initial point of the current resizing/moving. */
    private Point draggingStart;
    /** The current resizing/moving rectangle. */
    private Rectangle draggingRect;

    /** Minimum grid X coordinate of moved/resized component. */
    private int selMinX;
    /** Maximum grid X coordinate of a cell occupied by moved/resized component. */
    private int selMaxX;
    /** Minimum grid Y coordinate of moved/resized component. */
    private int selMinY;
    /** Maximum grid Y coordinate of a cell occupied by moved/resized component. */
    private int selMaxY;
    /** Minimum width of moved/resized component. */
    private int selMinWidth;
    /** Maximum width of moved/resized component. */
    private int selMinHeight;
    
    /** New grid x coordinate of the resized/moved component. */
    private int newGridX;
    /** New grid y coordinate of the resized/moved component. */
    private int newGridY;
    /** New grid width of the resized component. */
    private int newGridWidth;
    /** New grid height of the resized component. */
    private int newGridHeight;

    // ANIMATION

    /** Animation layer. */
    private AnimationLayer animLayer = new AnimationLayer();
    /** Determines whether the animation is in a progress. */
    boolean animation;
    /** The current phase of the animation. */
    float animPhase;
    /** Information about column/row changes. */
    GridBoundsChange animChange;

    /**
     * Creates new {@code GlassPane}.
     */
    public GlassPane(GridDesigner designer) {
        this.designer = designer;
        setLayout(null);
        Listener listener = new Listener();
        addMouseListener(listener);
        addMouseMotionListener(listener);
        initHeaderWidth();
    }

    /**
     * Sets the underlying container of this glass pane ({@code innerPane})
     * and the subcontainer of {@code innerPane} that contains the grid.
     *
     * @param innerPane underlying container of this glass pane.
     * @param componentPane subcontainer of {@code innerPane} that
     * contains the grid.
     */
    public void setPanes(Container innerPane, Container componentPane) {
        this.innerPane = innerPane;
        this.componentPane = componentPane;
    }

    /**
     * Determines whether some user action is in progress, i.e,
     * if some animation is running.
     * 
     * @return {@code true} if some user action is in progress,
     * returns {@code false} otherwise.
     */
    public boolean isUserActionInProgress() {
        return animation;
    }

    /**
     * Updates the layout in the grid designer according to the current
     * state of the model (meta-data). This method should be called
     * when some change is not done through {@code gridManager}
     * (for example, for changes made through property sheet).
     */
    public void updateLayout() {
        performAction(new AbstractGridAction() {
            @Override
            public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
                GridInfoProvider info = gridManager.getGridInfo();
                int oldColumns = info.getColumnCount();
                int oldRows = info.getRowCount();
                GridUtils.removePaddingComponents(gridManager);
                gridManager.updateLayout(false);
                GridUtils.revalidateGrid(gridManager);
                gridManager.updateGaps(false);
                int newColumns = info.getColumnCount();
                int newRows = info.getRowCount();
                int columns = Math.max(oldColumns, newColumns);
                int rows = Math.max(oldRows, newRows);
                GridUtils.addPaddingComponents(gridManager, columns, rows);
                GridUtils.revalidateGrid(gridManager);
                return null;
            }
        });
    }

    /**
     * Initializes the {@code headerWidth} (i.e., the width of the row header).
     */
    private void initHeaderWidth() {
        // Simple heuristics for the row header width
        JLabel label = new JLabel("99"); // NOI18N
        label.setBorder(BorderFactory.createRaisedBevelBorder());
        headerWidth = label.getPreferredSize().width+HEADER_GAP;
    }

    /**
     * Sets the grid manager.
     *
     * @param gridManager grid manager.
     */
    public void setGridManager(GridManager gridManager) {
        this.gridManager = gridManager;
        this.gridInfo = gridManager.getGridInfo();
        this.customizer = gridManager.getCustomizer(this);
        GridUtils.revalidateGrid(gridManager);
        GridUtils.addPaddingComponents(gridManager, gridInfo.getColumnCount(), gridInfo.getRowCount());
    }

    /**
     * Glass-pane painting.
     *
     * @param g graphics object.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (animation) {
            Point shift = fromComponentPane(new Point());
            g.translate(shift.x, shift.y);
            animLayer.paint(g);
            g.translate(-shift.x, -shift.y);
            animPhase = animLayer.getPhase();
            emphColumnHeader = -1;
            emphRowHeader = -1;
        }
        paintGrid(g);
        if (resizing) {
            paintResizing(g);
        } else if (moving) {
            paintResizing(g);
        } else {
            if (selecting) {
                g.setColor(GridDesigner.SELECTION_COLOR);
                g.drawRect(draggingRect.x, draggingRect.y, draggingRect.width, draggingRect.height);
            }
            if (!animation) {
                paintConstraints(g);
                paintSelection(g);
            }
        }
        if (animation && (animPhase == 1f)) {
            // End of animation
            animation = false;
            animPhase = 0;
            repaint(); // Repaint without the animation layer (to show selection, etc.)
        }
    }

    /**
     * Paints the grid.
     *
     * @param g graphics object.
     */
    private void paintGrid(Graphics g) {
        if (gridInfo == null) {
            return;
        }

        Color oldColor = g.getColor();
        g.setColor(GRID_COLOR);
        boolean animReady = animation && (animChange != null);
        
        // Calculate column bounds
        int[] columnBounds;
        if (animReady) {
            int[] animOldColumnBounds = animChange.getOldColumnBounds();
            int[] animNewColumnBounds = animChange.getNewColumnBounds();
            columnBounds = new int[animOldColumnBounds.length];
            for (int i=0; i<animOldColumnBounds.length; i++) {
                columnBounds[i] = Math.round(animOldColumnBounds[i]*(1-animPhase)+animPhase*animNewColumnBounds[i]);
            }
        } else {
            columnBounds = gridInfo.getColumnBounds();
        }
        int columns = columnBounds.length-1;

        // Calculate row bounds
        int[] rowBounds;
        if (animReady) {
            int[] animOldRowBounds = animChange.getOldRowBounds();
            int[] animNewRowBounds = animChange.getNewRowBounds();
            rowBounds = new int[animOldRowBounds.length];
            for (int i=0; i<animOldRowBounds.length; i++) {
                rowBounds[i] = Math.round(animOldRowBounds[i]*(1-animPhase)+animPhase*animNewRowBounds[i]);
            }
        } else {
            rowBounds = gridInfo.getRowBounds();
        }
        int rows = rowBounds.length-1;

        // Check if there are any newly created columns/rows
        if (moving || resizing) {
            int deltaX = newGridX - gridInfo.getGridX(focusedComponent);
            int deltaWidth = newGridWidth - gridInfo.getGridWidth(focusedComponent);
            columns = Math.max(columns, selMaxX+deltaX+deltaWidth+1);
            int deltaY = newGridY - gridInfo.getGridY(focusedComponent);
            int deltaHeight = newGridHeight - gridInfo.getGridHeight(focusedComponent);
            rows = Math.max(rows, selMaxY+deltaY+deltaHeight+1);
        }
        
        // Paint the grid
        Point shift = new Point();
        shift = fromComponentPane(shift);
        int x = columnBounds[0]+shift.x;
        int y = rowBounds[0]+shift.y;
        int width = extendedColumnBound(columnBounds, columns)-columnBounds[0];
        int height = extendedRowBound(rowBounds, rows)-rowBounds[0];
        for (int i=0; i<=columns; i++) {
            int bound = extendedColumnBound(columnBounds, i);
            g.drawLine(bound+shift.x, y, bound+shift.x, y+height);
        }
        for (int i=0; i<=rows; i++) {
            int bound = extendedRowBound(rowBounds, i);
            g.drawLine(x, bound+shift.y, x+width, bound+shift.y);
        }
        g.setColor(oldColor);

        // Paint the columns
        Color c = new Color(GRID_COLOR.getRGB()); // Getting rid of transparency
        JLabel header = new JLabel();
        header.setOpaque(true);
        header.setBackground(c);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i=0; i<columns; i++) {
            if (!gridInfo.isGapColumn(i)) {
                header.setText(Integer.toString(i));
                if(i == emphColumnHeader) {
                    header.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
                } else {
                    header.setBorder(selectedColumns.get(i) ? BorderFactory.createLoweredBevelBorder() : BorderFactory.createRaisedBevelBorder());
                }
                headerHeight = header.getPreferredSize().height;
                int start = extendedColumnBound(columnBounds, i);
                int end = extendedColumnBound(columnBounds, i+1);
                int w = end-start;
                Graphics gg = g.create(start+shift.x, rowBounds[0]-HEADER_GAP-headerHeight+shift.y, w, headerHeight);
                header.setSize(w, headerHeight);
                header.paint(gg);
                gg.dispose();
            }
        }

        // Paint the rows
        for (int i=0; i<rows; i++) {
            if (!gridInfo.isGapRow(i)) {
                header.setText(Integer.toString(i));
                Border border;
                if(i == emphRowHeader) {
                    border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
                } else {
                    border = selectedRows.get(i) ? BorderFactory.createLoweredBevelBorder() : BorderFactory.createRaisedBevelBorder();
                }
                header.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(0,HEADER_GAP/2,0,HEADER_GAP/2)));
                int start = extendedRowBound(rowBounds, i);
                int end = extendedRowBound(rowBounds, i+1);
                int h = end-start;
                Graphics gg = g.create(columnBounds[0]-HEADER_GAP-headerWidth+shift.x, start+shift.y, headerWidth, h);
                header.setSize(headerWidth, h);
                header.paint(gg);
                gg.dispose();
            }
        }
    }

    /**
     * Identifies whether a header is under mouse pointer.
     * Such header is then emphasized to point out
     * that it can be actively used (right-click opens menu).
     * See #200671 for reasoning.
     *
     * @param cursorLocation current mouse cursor location.
     */
    void updateActiveHeaders(Point cursorLocation) {
        if (cursorLocation == null) {
            emphColumnHeader = -1;
            emphRowHeader = -1;
        } else {
            int oldColumnHeader = emphColumnHeader;
            int oldRowHeader = emphRowHeader;
            emphColumnHeader = findColumnHeader(cursorLocation);
            emphRowHeader = findRowHeader(cursorLocation);
            if(oldColumnHeader != emphColumnHeader || oldRowHeader != emphRowHeader) {
                repaint();
            }
        }
    }
    
    /**
     * Paints additional information about component constraints.
     * 
     * @param g graphics to use for painting.
     */
    private void paintConstraints(Graphics g) {
        Point shift = fromComponentPane(new Point());
        Graphics gClip = g.create();
        Rectangle paneRect = fromComponentPane(new Rectangle(new Point(), componentPane.getSize()));
        gClip.clipRect(paneRect.x, paneRect.y, paneRect.width, paneRect.height);
        gClip.translate(shift.x, shift.y);
        for (Component comp : componentPane.getComponents()) {
            if (GridUtils.isPaddingComponent(comp)) {
                continue;
            }
            boolean selected = selection.contains(comp);
            gridInfo.paintConstraints(gClip, comp, selected);
        }
        gClip.dispose();
    }

    /**
     * Paints the selection.
     *
     * @param g graphics object.
     */
    private void paintSelection(Graphics g) {
        Graphics gClip = g.create();
        Rectangle paneRect = fromComponentPane(new Rectangle(new Point(), componentPane.getSize()));
        gClip.clipRect(paneRect.x, paneRect.y, paneRect.width, paneRect.height);
        for (Component selComp : selection) {
            Rectangle rect = fromComponentPane(selectionResizingBounds(selComp));
            Rectangle inner = fromComponentPane(selComp.getBounds());
            gClip.setColor(HIGHLIGHT_COLOR);
            if ((inner.width == 0) || (inner.height == 0)) {
                // GridBagLayout sets location of such components
                // to (0,0) which makes inner rectangle incorrect.
                // Happily, we can ignore inner rectangle in this case.
                gClip.fillRect(rect.x, rect.y, rect.width, rect.height);
            } else {
                gClip.fillRect(rect.x, rect.y, rect.width, inner.y-rect.y);
                gClip.fillRect(rect.x, inner.y, inner.x-rect.x, inner.height);
                gClip.fillRect(inner.x+inner.width, inner.y, rect.width-(inner.x+inner.width-rect.x), inner.height);
                gClip.fillRect(rect.x, inner.y+inner.height, rect.width, rect.height-(inner.y+inner.height-rect.y));
            }
            g.setColor(GridDesigner.SELECTION_COLOR);
            int x = rect.x-1;
            int y = rect.y-1;
            int w = rect.width/2+1;
            int h = rect.height/2+1;
            g.drawRect(x, y, rect.width+1, rect.height+1);
            Image resizeHandle = GridDesigner.RESIZE_HANDLE;
            int rw = resizeHandle.getWidth(null);
            int rh = resizeHandle.getHeight(null);
            g.drawImage(resizeHandle, x-rw, y-rh, null); // left-top
            x += w;
            g.drawImage(resizeHandle, x-rw/2, y-rh, null); // top
            x += rect.width+2-w;
            g.drawImage(resizeHandle, x, y-rh, null); // right-top
            y += h;
            g.drawImage(resizeHandle, x, y-rh/2, null); // right
            y += rect.height+2-h;
            g.drawImage(resizeHandle, x, y, null); // right-bottom
            x -= rect.width+2-w;
            g.drawImage(resizeHandle, x-rw/2, y, null); // bottom
            x -= w;
            g.drawImage(resizeHandle, x-rw, y, null); // left-bottom
            y -= rect.height+2-h;
            g.drawImage(resizeHandle, x-rw, y-rh/2, null); // left
        }
        gClip.dispose();
    }

    /**
     * Paints resizing feedback.
     *
     * @param g graphics object.
     */
    private void paintResizing(Graphics g) {
        int xDelta = newGridX - gridInfo.getGridX(focusedComponent);
        int yDelta = newGridY - gridInfo.getGridY(focusedComponent);
        int heightDelta = newGridHeight - gridInfo.getGridHeight(focusedComponent);
        int widthDelta = newGridWidth - gridInfo.getGridWidth(focusedComponent);
        g.setColor(GridDesigner.SELECTION_COLOR);
        g.drawRect(draggingRect.x, draggingRect.y, draggingRect.width, draggingRect.height);
        g.setColor(HIGHLIGHT_COLOR);
        int[] columnBounds = gridInfo.getColumnBounds();
        int[] rowBounds = gridInfo.getRowBounds();
        Point shift = fromComponentPane(new Point());
        for (Component selComp : selection) {
            int newCompGridX = gridInfo.getGridX(selComp)+xDelta;
            int newCompGridWidth = gridInfo.getGridWidth(selComp)+widthDelta;
            int newCompGridY = gridInfo.getGridY(selComp)+yDelta;
            int newCompGridHeight = gridInfo.getGridHeight(selComp)+heightDelta;
            for(int i = newCompGridX; i < newCompGridX + newCompGridWidth; i++) {
                for(int j = newCompGridY; j < newCompGridY + newCompGridHeight; j++) {
                    if(!gridInfo.isGapColumn(i) && !gridInfo.isGapRow(j)) {
                        int x = extendedColumnBound(columnBounds, i);
                        int width = extendedColumnBound(columnBounds, i + 1) - x;
                        int y = extendedRowBound(rowBounds, j);
                        int height = extendedRowBound(rowBounds, j + 1) - y;
                        g.fillRect(x + shift.x, y + shift.y, width, height);
                    }
                }
            }
        }
    }
    
    private Rectangle selectionResizingBounds(Component selComp) {
        int[] columnBounds = gridInfo.getColumnBounds();
        int[] rowBounds = gridInfo.getRowBounds();
        int gridX = gridInfo.getGridX(selComp);
        int gridY = gridInfo.getGridY(selComp);
        int gridWidth = gridInfo.getGridWidth(selComp);
        int gridHeight = gridInfo.getGridHeight(selComp);
        int x = columnBounds[gridX];
        int width = columnBounds[gridX+gridWidth]-x;
        int y = rowBounds[gridY];
        int height = rowBounds[gridY+gridHeight]-y;
        return new Rectangle(x,y,width,height);
    }

    /**
     * Returns the position of the specified grid vertical line (= column edge).
     * It returns either the position of the existing line or the suggested
     * position of a newly created line.
     *
     * @param bounds positions of the existing lines.
     * @param index index of the line we are interested in.
     * @return position of the specified grid line.
     */
    private int extendedColumnBound(int[] bounds, int index) {
        int bound;
        if (index<bounds.length) {
            bound = bounds[index];
        } else {
            if(gridInfo.hasGaps()) {
                int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
                bound = bounds[bounds.length-1];
                for(int i = bounds.length; i <= index; i++) {
                    if(gridInfo.isGapColumn(i + 1)) {
                        bound += gapWidth;
                    } else {
                        bound += GridUtils.PADDING_SIZE_STANDARD;
                    }
                }
            } else {
                bound = bounds[bounds.length-1]+(index-bounds.length+1)*GridUtils.PADDING_SIZE_STANDARD;
            }
        }
        return bound;
    }

    /**
     * Returns the position of the specified grid horizontal line (= row edge).
     * It returns either the position of the existing line or the suggested
     * position of a newly created line.
     *
     * @param bounds positions of the existing lines.
     * @param index index of the line we are interested in.
     * @return position of the specified grid line.
     */
    private int extendedRowBound(int[] bounds, int index) {
        int bound;
        if (index<bounds.length) {
            bound = bounds[index];
        } else {
            if(gridInfo.hasGaps()) {
                int gapWidth = FormLoaderSettings.getInstance().getGapHeight();
                bound = bounds[bounds.length-1];
                for(int i = bounds.length; i <= index; i++) {
                    if(gridInfo.isGapRow(i + 1)) {
                        bound += gapWidth;
                    } else {
                        bound += GridUtils.PADDING_SIZE_STANDARD;
                    }
                }
            } else {
                bound = bounds[bounds.length-1]+(index-bounds.length+1)*GridUtils.PADDING_SIZE_STANDARD;
            }
        }
        return bound;
    }

    /**
     * Determines if we are moving east side of the component during resizing.
     *
     * @return {@code true} is we are moving east side of the component,
     * returns {@code false} otherwise.
     */
    private boolean isResizingEastward() {
        return (resizingMode == SwingConstants.EAST
                || resizingMode == SwingConstants.SOUTH_EAST
                || resizingMode == SwingConstants.NORTH_EAST);
    }

    /**
     * Determines if we are moving west side of the component during resizing.
     *
     * @return {@code true} is we are moving west side of the component,
     * returns {@code false} otherwise.
     */
    private boolean isResizingWestward() {
        return (resizingMode == SwingConstants.WEST
                || resizingMode == SwingConstants.SOUTH_WEST
                || resizingMode == SwingConstants.NORTH_WEST);
    }

    /**
     * Determines if we are moving south side of the component during resizing.
     *
     * @return {@code true} is we are moving south side of the component,
     * returns {@code false} otherwise.
     */
    private boolean isResizingSouthward() {
        return (resizingMode == SwingConstants.SOUTH
                || resizingMode == SwingConstants.SOUTH_EAST
                || resizingMode == SwingConstants.SOUTH_WEST);
    }

    /**
     * Determines if we are moving north side of the component during resizing.
     *
     * @return {@code true} is we are moving north side of the component,
     * returns {@code false} otherwise.
     */
    private boolean isResizingNorthward() {
        return (resizingMode == SwingConstants.NORTH
                || resizingMode == SwingConstants.NORTH_EAST
                || resizingMode == SwingConstants.NORTH_WEST);
    }

    /**
     * Calculates a resizing rectangle (a part of the resizing feedback).
     *
     * @param resizingEnd potential end of the resizing (the current location
     * of the mouse cursor).
     * @return resizing rectangle that is used as a part of the resizing feedback.
     */
    Rectangle calculateResizingRectangle(Point resizingEnd, Component selComp) {
        Rectangle rect = fromComponentPane(selectionResizingBounds(selComp));
        int dx = resizingEnd.x - draggingStart.x;
        int dy = resizingEnd.y - draggingStart.y;
        if (isResizingEastward()) {
            rect.width += dx;
            if (rect.width < 0) {
                rect.width = 0;
            }
        }
        if (isResizingSouthward()) {
            rect.height += dy;
            if (rect.height < 0) {
                rect.height = 0;
            }
        }
        if (isResizingWestward()) {
            rect.width -= dx;
            rect.x += dx;
            if (rect.width < 0) {
                rect.x += rect.width;
                rect.width = 0;
            }
        }
        if (isResizingNorthward()) {
            rect.height -= dy;
            rect.y += dy;
            if (rect.height < 0) {
                rect.y += rect.height;
                rect.height = 0;
            }
        }
        return rect;
    }

    /**
     * Calculates the grid location of the current resizing rectangle
     * ({@code draggingRect}).
     */
    void calculateResizingGridLocation() {
        Rectangle rect = toComponentPane(draggingRect);
        int x = gridInfo.getGridX(focusedComponent);
        int y = gridInfo.getGridY(focusedComponent);
        int width = gridInfo.getGridWidth(focusedComponent);
        int height = gridInfo.getGridHeight(focusedComponent);

        if (isResizingEastward()) {
            int currentX = gridXLocation(rect.x+rect.width, false);
            if( !gridInfo.isGapColumn(currentX) ) {
                newGridWidth = Math.max(width-selMinWidth+1, currentX-x+1);
            }
        }
        if (isResizingWestward()) {
            int currentX = gridXLocation(rect.x, false);
            if( !gridInfo.isGapColumn(currentX) ) {
                newGridX = Math.max(x-selMinX, Math.min(x+width-(width-selMinWidth+1), currentX));
                newGridWidth = x+width-newGridX;
            }
        }
        if (isResizingSouthward()) {
            int currentY = gridYLocation(rect.y+rect.height, false);
            if( !gridInfo.isGapRow(currentY) ) {
                newGridHeight = Math.max(height-selMinHeight+1, currentY-y+1);
            }
        }
        if (isResizingNorthward()) {
            int currentY = gridYLocation(rect.y, false);
            if( !gridInfo.isGapRow(currentY) ) {
                newGridY = Math.max(y-selMinY, Math.min(y+height-(height-selMinHeight+1), currentY));
                newGridHeight = y+height-newGridY;
            }
        }
    }

    /**
     * Calculates a moving rectangle (a part of the moving feedback).
     *
     * @param movingEnd potential end of the moving (the current location
     * of the mouse cursor).
     * @return moving rectangle that is used as a part of the moving feedback.
     */
    Rectangle calculateMovingRectangle(Point movingEnd) {
        Rectangle rect = focusedComponent.getBounds();
        rect.x += movingEnd.x-draggingStart.x;
        rect.y += movingEnd.y-draggingStart.y;
        return fromComponentPane(rect);
    }

    /**
     * Calculates the grid location of the moved component.
     *
     * @param cursorLocation current position of the cursor.
     */
    void calculateMovingGridLocation(Point cursorLocation) {
        Point start = toComponentPane(draggingStart);
        Point end = toComponentPane(cursorLocation);
        int startX = gridXLocation(start.x, true);
        int startY = gridYLocation(start.y, true);
        int endX = gridXLocation(end.x, false);
        int endY = gridYLocation(end.y, false);
        int deltaX = endX-startX;
        int deltaY = endY-startY;
        deltaX = Math.max(deltaX, -selMinX);
        deltaY = Math.max(deltaY, -selMinY);
        int tempGridX = gridInfo.getGridX(focusedComponent) + deltaX;
        int tempGridY = gridInfo.getGridY(focusedComponent) + deltaY;
        if( !gridInfo.isGapColumn(tempGridX) && !gridInfo.isGapRow(tempGridY) ) {
            newGridX = tempGridX;
            newGridY = tempGridY;
        }
    }

    /**
     * Updates cursor according to its location. For example, it sets
     * the appropriate resizing cursor when the mouse is over the boundary
     * of the selection component.
     *
     * @param cursorLocation current mouse cursor location.
     */
    void updateCursor(Point cursorLocation) {
        Cursor cursor = Cursor.getDefaultCursor();
        if (cursorLocation == null) {
            resizingMode = 0;
        } else {
            int x = cursorLocation.x;
            int y = cursorLocation.y;
            Image resizeHandle = GridDesigner.RESIZE_HANDLE;
            int rw = resizeHandle.getWidth(null);
            int rh = resizeHandle.getHeight(null);
            for (Component selComp : selection) {
                Rectangle rect = fromComponentPane(selectionResizingBounds(selComp));
                boolean w = (rect.x-rw<=x) && (x<=rect.x+rect.width+rw);
                boolean h = (rect.y-rh<=y) && (y<=rect.y+rect.height+rh);
                boolean top = w && (rect.y-rh<=y) && (y<=rect.y+2);
                boolean bottom = w && (rect.y+rect.height-2<=y) && (y<=rect.y+rect.height+rh);
                boolean left = h && (rect.x-rw<=x) && (x<=rect.x+2);
                boolean right = h && (rect.x+rect.width-2<=x) && (x<=rect.x+rect.width+rw);
                if (top) {
                    if (left) {
                        cursor = Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
                        resizingMode = SwingConstants.NORTH_WEST;
                    } else if (right) {
                        cursor = Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
                        resizingMode = SwingConstants.NORTH_EAST;
                    } else {
                        cursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
                        resizingMode = SwingConstants.NORTH;
                    }
                } else if (bottom) {
                    if (left) {
                        cursor = Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
                        resizingMode = SwingConstants.SOUTH_WEST;
                    } else if (right) {
                        cursor = Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
                        resizingMode = SwingConstants.SOUTH_EAST;
                    } else {
                        cursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
                        resizingMode = SwingConstants.SOUTH;
                    }
                } else if (left) {
                    cursor = Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
                    resizingMode = SwingConstants.WEST;
                } else if (right) {
                    cursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                    resizingMode = SwingConstants.EAST;
                } else {
                    cursor = Cursor.getDefaultCursor();
                    resizingMode = 0;
                }
                if (resizingMode != 0) {
                    focusedComponent = selComp;
                    break;
                }
            }
        }
        setCursor(cursor);
    }
    
    // Conversion of coordinates

    /**
     * Converts a {@code point} in {@code innerPane} coordinates to
     * a corresponding point in {@code componentPane} coordinates.
     *
     * @param point point in {@code innerPane} coordinates.
     * @return point in {@code componentPane} coordinates.
     */
    Point toComponentPane(Point point) {
        return SwingUtilities.convertPoint(innerPane, point, componentPane);
    }

    /**
     * Converts a {@code rectangle} in {@code innerPane} coordinates to
     * a corresponding rectangle in {@code componentPane} coordinates.
     *
     * @param rectangle rectangle in {@code innerPane} coordinates.
     * @return rectangle in {@code componentPane} coordinates.
     */
    Rectangle toComponentPane(Rectangle rectangle) {
        return SwingUtilities.convertRectangle(innerPane, rectangle, componentPane);
    }

    /**
     * Converts a {@code point} in {@code componentPane} coordinates to
     * a corresponding point in {@code innerPane} coordinates.
     *
     * @param point point in {@code componentPane} coordinates.
     * @return point in {@code innerPane} coordinates.
     */
    Point fromComponentPane(Point point) {
        return SwingUtilities.convertPoint(componentPane, point, innerPane);
    }

    /**
     * Converts a {@code rectangle} in {@code componentPane} coordinates to
     * a corresponding rectangle in {@code innerPane} coordinates.
     *
     * @param rectangle rectangle in {@code componentPane} coordinates.
     * @return rectangle in {@code innerPane} coordinates.
     */
    Rectangle fromComponentPane(Rectangle rectangle) {
        return SwingUtilities.convertRectangle(componentPane, rectangle, innerPane);
    }

    /**
     * Returns {@code x} grid coordinate that corresponds to the given
     * pixel coordinate ({@code xComponentPaneCoordinate}).
     *
     * @param xComponentPaneCoordinate pixel coordinate.
     * @param mustBeInside determines whether the returned coordinate
     * must be forced to lay in the existing grid.
     * @return grid {@code x} coordinate.
     */
    private int gridXLocation(int xComponentPaneCoordinate, boolean mustBeInside) {
        int[] bounds = gridInfo.getColumnBounds();
        int gridX = -1;
        while (gridX+1<bounds.length && bounds[gridX+1]<=xComponentPaneCoordinate) {
            gridX++;
        }
        if (mustBeInside) {
            gridX = Math.max(0,Math.min(gridX, bounds.length-2));
        } else if (gridX == bounds.length-1) {
            if(gridInfo.hasGaps()) {
                int gapWidth = FormLoaderSettings.getInstance().getGapWidth();
                int coordinateX = bounds[bounds.length-1]+1;
                while (coordinateX <= xComponentPaneCoordinate) {
                    if(gridInfo.isGapColumn(gridX + 1)) {
                        coordinateX += gapWidth;
                    } else {
                        coordinateX += GridUtils.PADDING_SIZE_STANDARD;
                    }
                    gridX++;
                }
            } else {
                gridX = (xComponentPaneCoordinate-bounds[bounds.length-1]+1)/GridUtils.PADDING_SIZE_STANDARD+bounds.length-1;
            }
        }
        return gridX;
    }

    /**
     * Returns {@code y} grid coordinate that corresponds to the given
     * pixel coordinate ({@code yComponentPaneCoordinate}).
     *
     * @param yComponentPaneCoordinate pixel coordinate.
     * @param mustBeInside determines whether the returned coordinate
     * must be forced to lay in the existing grid.
     * @return grid {@code y} coordinate.
     */
    private int gridYLocation(int yComponentPaneCoordinate, boolean mustBeInside) {
        int[] bounds = gridInfo.getRowBounds();
        int gridY = -1;
        while (gridY+1<bounds.length && bounds[gridY+1]<=yComponentPaneCoordinate) {
            gridY++;
        }
        if (mustBeInside) {
            gridY = Math.max(0,Math.min(gridY, bounds.length-2));
        } else if (gridY == bounds.length-1) {
            if(gridInfo.hasGaps()) {
                int gapHeight = FormLoaderSettings.getInstance().getGapHeight();
                int coordinateY = bounds[bounds.length-1]+1;
                while (coordinateY <= yComponentPaneCoordinate) {
                    if(gridInfo.isGapRow(gridY + 1)) {
                        coordinateY += gapHeight;
                    } else {
                        coordinateY += GridUtils.PADDING_SIZE_STANDARD;
                    }
                    gridY++;
                }
            } else {
                gridY = (yComponentPaneCoordinate-bounds[bounds.length-1]+1)/GridUtils.PADDING_SIZE_STANDARD+bounds.length-1;
            }
        }
        return gridY;
    }

    /**
     * Returns the component at the specified point.
     *
     * @param innerPanePoint point where we are looking for a component.
     * @return component at the specified point or {@code null}.
     */
    Component findComponent(Point innerPanePoint) {
        Rectangle paneRect = fromComponentPane(new Rectangle(new Point(), componentPane.getSize()));
        if (paneRect.contains(innerPanePoint)) {
            for (Component comp : componentPane.getComponents()) {
                if (GridUtils.isPaddingComponent(comp)) {
                    continue;
                }
                Rectangle rect = fromComponentPane(comp.getBounds());
                if (rect.contains(innerPanePoint)) {
                    return comp;
                }
            }
            // no component clicked directly, try to select some based on click in a grid cell
            for (Component comp : componentPane.getComponents()) {
                if (GridUtils.isPaddingComponent(comp)) {
                    continue;
                }
                Rectangle rect = fromComponentPane(selectionResizingBounds(comp));
                if (rect.contains(innerPanePoint)) {
                    return comp;
                }
            }
        }
        return null;
    }

    /**
     * Returns the index of the column header at the specified point.
     *
     * @param innerPanePoint point whete we are looking for a column header.
     * @return index of the column header or {@code -1}.
     */
    int findColumnHeader(Point innerPanePoint) {
        Point shift = fromComponentPane(new Point());
        int y = gridInfo.getY()+shift.y-HEADER_GAP;
        if (y-headerHeight <= innerPanePoint.y && innerPanePoint.y <= y) {
            int[] bounds = gridInfo.getColumnBounds();
            for (int i=0; i<bounds.length-1; i++) {
                int x = innerPanePoint.x-shift.x;
                if (bounds[i]<=x && x<bounds[i+1]) {
                    if(gridInfo.isGapColumn(i)) {
                        return -1;
                    } else {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Returns the index of the row header at the specified point.
     *
     * @param innerPanePoint point whete we are looking for a row header.
     * @return index of the row header or {@code -1}.
     */
    int findRowHeader(Point innerPanePoint) {
        Point shift = fromComponentPane(new Point());
        int x = gridInfo.getX()+shift.x-HEADER_GAP;
        if (x-headerWidth <= innerPanePoint.x && innerPanePoint.x <= x) {
            int[] bounds = gridInfo.getRowBounds();
            for (int i=0; i<bounds.length-1; i++) {
                int y = innerPanePoint.y-shift.y;
                if (bounds[i]<=y && y<bounds[i+1]) {
                    if(gridInfo.isGapRow(i)) {
                        return -1;
                    } else {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * Returns {@ DesignerContext} covering the current state of the designer.
     *
     * @return current designer context.
     */
    DesignerContext currentContext() {
        DesignerContext context = new DesignerContext();
        context.setSelectedColumns((BitSet)selectedColumns.clone());
        context.setSelectedRows((BitSet)selectedRows.clone());
        context.setSelectedComponents(new HashSet<Component>(selection));
        context.setGridInfo(gridInfo);
        context.setFocusedRow(focusedCellRow);
        context.setFocusedColumn(focusedCellColumn);
        return context;
    }

    /**
     * Updates the current context of the designer according to the given
     * designer context. This method is used to update the designer context
     * according to needs of grid actions (it is invoked when a grid
     * action is finised).
     * 
     * @param context designer context that should be reflected
     * the designer/glass pane.
     */
    void updateCurrentContext(DesignerContext context) {
        setSelection(context.getSelectedComponents());
    }

    /**
     * Change the grid location of the selected component according
     * to data collected during moving/resizing.
     */
    void changeLocation() {
        int oldX = gridInfo.getGridX(focusedComponent);
        int oldY = gridInfo.getGridY(focusedComponent);
        int oldWidth = gridInfo.getGridWidth(focusedComponent);
        int oldHeight = gridInfo.getGridHeight(focusedComponent);
        boolean xChanged = (oldX != newGridX);
        boolean yChanged = (oldY != newGridY);
        boolean heightChanged = (oldHeight != newGridHeight);
        boolean widthChanged = (oldWidth != newGridWidth);
        if (xChanged || yChanged || widthChanged || heightChanged) {
            performAction(new ChangeLocationAction());
        }
    }

    /**
     * Performs the given {@code GridAction}.
     * 
     * @param action grid action to perform.
     */
    @Override
    public void performAction(GridAction action) {
        GridActionWrapper wrapper = new GridActionWrapper(action);
        wrapper.setDesignerContext(currentContext());
        wrapper.actionPerformed(null);
    }

    /**
     * Sets the selection.
     * 
     * @param selComp component that should be the only selected component,
     * if it is {@code null} then the current selection is cleared.
     */
    void setSelection(Component selComp) {
        setSelection((selComp==null) ? Collections.EMPTY_SET : Collections.singleton(selComp));
    }

    /**
     * Sets the selection.
     * 
     * @param selection new selection.
     */
    void setSelection(Set<Component> selection) {
        assert !selection.contains(null);
        if (selection == this.selection) {
            return;
        }
        if (selection.isEmpty()) {
            updateCursor(null);
        }
        this.selection = selection;
        designer.setSelection(selection);
        requestFocusInWindow();
    }

    /**
     * Processes key events.
     * 
     * @param e key event.
     */
    @Override
    protected void processKeyEvent(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_DELETE) {
            if (!selection.isEmpty()) {
                performAction(new DeleteComponentAction());
            }
        } else if ((keyCode == KeyEvent.VK_ESCAPE) && (moving || resizing)) {
            // Cancel moving and resizing when Esc is pressed.
            moving = false;
            resizing = false;
            repaint();
        } else {
            super.processKeyEvent(e);
        }
    }
    
    /**
     * Initializes {@code selMinX}, {@code selMaxX}, {@code selMinY},
     * {@code selMaxY}, {@code selMinWidth} and {@code selMinHeight} fields.
     */
    void initSelFields() {
        selMinX = Integer.MAX_VALUE;
        selMaxX = 0;
        selMinY = Integer.MAX_VALUE;
        selMaxY = 0;
        selMinWidth = Integer.MAX_VALUE;
        selMinHeight = Integer.MAX_VALUE;
        for (Component selComp : selection) {
            int gridX = gridInfo.getGridX(selComp);
            int gridY = gridInfo.getGridY(selComp);
            int gridWidth = gridInfo.getGridWidth(selComp);
            int gridHeight = gridInfo.getGridHeight(selComp);
            selMinX = Math.min(selMinX, gridX);
            selMaxX = Math.max(selMaxX, gridX+gridWidth-1);
            selMinY = Math.min(selMinY, gridY);
            selMaxY = Math.max(selMaxY, gridY+gridHeight-1);
            selMinWidth = Math.min(selMinWidth, gridWidth);
            selMinHeight = Math.min(selMinHeight, gridHeight);
        }
    }

    private static boolean ctrlOrCmdModifier(MouseEvent e) {
        if (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() == InputEvent.META_MASK) { // on Mac
            return (e.getModifiersEx() & InputEvent.META_DOWN_MASK) == InputEvent.META_DOWN_MASK;
        }
        return (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK;
    }

    private void showPopupMenu(Point point) {
        List<GridAction> actions = null;
        // Component actions
        DesignerContext context = currentContext();
        if (!selection.isEmpty()) {
            context.setFocusedComponent(focusedComponent);
            actions = gridManager.designerActions(GridAction.Context.COMPONENT);
        }

        // Column actions
        int column = findColumnHeader(point);
        context.setFocusedColumn(column);
        if (column != -1) {
            actions = gridManager.designerActions(GridAction.Context.COLUMN);
        }

        // Row actions
        int row = findRowHeader(point);
        context.setFocusedRow(row);
        if (row != -1) {
            actions = gridManager.designerActions(GridAction.Context.ROW);
        }

        // Grid actions
        if (column == -1 && row == -1) {
            Point shift = fromComponentPane(new Point());
            int x = gridInfo.getX();
            int y = gridInfo.getY();
            int width = gridInfo.getWidth();
            int height = gridInfo.getHeight();
            Rectangle rect = new Rectangle(x+shift.x, y+shift.y, width, height);
            if (rect.contains(point)) {
                focusedCellColumn = gridXLocation(point.x-shift.x, true);
                focusedCellRow = gridYLocation(point.y-shift.y, true);
                if( !gridInfo.isGapColumn(focusedCellColumn) && !gridInfo.isGapRow(focusedCellRow) ) {
                    context.setFocusedColumn(focusedCellColumn);
                    context.setFocusedRow(focusedCellRow);
                    List<GridAction> moreActions = gridManager.designerActions(GridAction.Context.CELL);
                    if (moreActions != null && !moreActions.isEmpty()) {
                        if (actions == null) {
                            actions = moreActions;
                        } else {
                            List<GridAction> l = new ArrayList<GridAction>(actions.size() + moreActions.size());
                            l.addAll(actions);
                            l.addAll(moreActions);
                            actions = l;
                        }
                    }
                } else {
                    focusedCellColumn = -1;
                    focusedCellRow = -1;
                }
            }
        } else {
            focusedCellColumn = -1;
            focusedCellRow = -1;
        }

        if (actions != null) {
            JPopupMenu menu = new JPopupMenu();
            for (GridAction action : actions) {
                JMenuItem menuItem = action.getPopupPresenter(GlassPane.this);
                if (menuItem == null) {
                    GridActionWrapper wrapper = new GridActionWrapper(action);
                    wrapper.setDesignerContext(context);
                    menu.add(wrapper);
                } else {
                    menu.add(menuItem);
                }
            }
            designer.updateContextMenu(context, menu);
            draggingStart = null;
            menu.show(GlassPane.this, point.x, point.y);
        }
    }

    /**
     * Listener for glass-pane user gestures.
     */
    class Listener extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            Point point = e.getPoint();
            draggingStart = point;
            boolean leftMousePressed = (e.getButton() == MouseEvent.BUTTON1);
            boolean rightMousePressed = (e.getButton() == MouseEvent.BUTTON3);
            if (leftMousePressed) {
                if (resizingMode == 0) {
                    focusedComponent = findComponent(point);
                    if (e.isPopupTrigger()) { // Mac
                        if (!selection.contains(focusedComponent)) {
                            setSelection(focusedComponent);
                        }
                        showPopupMenu(point);
                    } else  if (!e.isShiftDown()) { // shift adding to selection on mouse release
                        if (focusedComponent != null && ctrlOrCmdModifier(e)) {
                            Set<Component> newSelection = new HashSet<Component>();
                            newSelection.addAll(selection);
                            if (selection.contains(focusedComponent)) {
                                newSelection.remove(focusedComponent);
                            } else {
                                newSelection.add(focusedComponent);
                            }
                            setSelection(newSelection);
                        } else if (!selection.contains(focusedComponent)) {
                            setSelection(focusedComponent);
                        }
                    }
//                    // Column selection
//                    int column = findColumnHeader(point);
//                    if (column != -1) {
////                        selectedColumns.flip(column);
//                    }
//                    // Row selection
//                    int row = findRowHeader(point);
//                    if (row != -1) {
////                        selectedRows.flip(row);
//                    }
                } else {
                    // Resizing (start)
                    resizing = true;
                    requestFocusInWindow();
                    draggingRect = fromComponentPane(selectionResizingBounds(focusedComponent));
                    newGridX = gridInfo.getGridX(focusedComponent);
                    newGridY = gridInfo.getGridY(focusedComponent);
                    newGridHeight = gridInfo.getGridHeight(focusedComponent);
                    newGridWidth = gridInfo.getGridWidth(focusedComponent);
                    initSelFields();
                }
            } else if (rightMousePressed) {
                if (moving || resizing) {
                    moving = false;
                    resizing = false;
                    draggingStart = null;
                } else {
                    focusedComponent = findComponent(point);
                    if (!selection.contains(focusedComponent)) {
                        setSelection(focusedComponent);
                    }
                    if (e.isPopupTrigger()) { // Linux
                        showPopupMenu(point);
                    }
                }
            }
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            Point point = e.getPoint();
            if (moving) {
                moving = false;
                changeLocation();
            } else if (resizing) {
                resizing = false;
                changeLocation();
            } else if (selecting) {
                selecting = false;
                Set<Component> newSelection = new HashSet<Component>();
                Rectangle paneRect = fromComponentPane(new Rectangle(new Point(), componentPane.getSize()));
                for (Component comp : componentPane.getComponents()) {
                    if (GridUtils.isPaddingComponent(comp)) {
                        continue;
                    }
                    Rectangle rect = fromComponentPane(comp.getBounds());
                    rect = rect.intersection(paneRect);
                    if (draggingRect.intersects(rect)) {
                        if (newSelection.contains(comp)) {
                            newSelection.remove(comp);
                        } else {
                            newSelection.add(comp);
                        }
                    }
                }
                setSelection(newSelection);
            } else { // no dragging, just a click
                boolean leftMouseReleased = (e.getButton() == MouseEvent.BUTTON1);
                focusedComponent = findComponent(point);
                if (e.isPopupTrigger()) {
                    if (draggingStart != null) { // i.e. right click did not cancel dragging
                        if (!selection.contains(focusedComponent)) {
                            setSelection(focusedComponent);
                        }
                        showPopupMenu(point); // Windows
                    }
                } else if (leftMouseReleased && e.isShiftDown()) {
                    if (focusedComponent != null && !selection.contains(focusedComponent)) {
                        Set<Component> newSelection = new HashSet<Component>();
                        newSelection.addAll(selection);
                        newSelection.add(focusedComponent);
                        setSelection(newSelection);
                    }
                } else if (!ctrlOrCmdModifier(e) && draggingStart != null) { // not releasing after multiselect or canceled dragging
                    setSelection(focusedComponent); // force single select
                }
            }
            repaint();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            if (!selection.isEmpty()) {
                updateCursor(e.getPoint());
            }
            updateActiveHeaders(e.getPoint());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (draggingStart == null) {
                draggingStart = e.getPoint();
            }
            if (resizing) {
                draggingRect = calculateResizingRectangle(e.getPoint(), focusedComponent);
                calculateResizingGridLocation();
            } else if (focusedComponent == null || e.isShiftDown()) {
                selecting = true;
                draggingRect = new Rectangle(draggingStart);
                draggingRect.add(e.getPoint());
            } else if (!selection.isEmpty()) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (!moving) {
                        // Moving start
                        newGridHeight = gridInfo.getGridHeight(focusedComponent);
                        newGridWidth = gridInfo.getGridWidth(focusedComponent);
                        initSelFields();
                        requestFocusInWindow();
                    }
                    moving = true;
                    draggingRect = calculateMovingRectangle(e.getPoint());
                    calculateMovingGridLocation(e.getPoint());
                }
            }
            repaint();
        }

    }

    /**
     * Adapts {@code GridAction} to be a {@code javax.swing.Action}
     * and supports animated changes of the layout and the grid.
     */
    class GridActionWrapper extends AbstractAction {
        /** Wrapped {@code GridAction}. */
        private GridAction delegate;
        /** The designer context given to this action. */
        private DesignerContext currentContext;

        /**
         * Creates a new wrapper around the specified action.
         *
         * @param action action to wrap.
         */
        GridActionWrapper(GridAction action) {
            this.delegate = action;
        }

        /**
         * Gives a designer context to this action.
         *
         * @param currentContext current designer context.
         */
        public void setDesignerContext(DesignerContext currentContext) {
            this.currentContext = currentContext;
            setEnabled(delegate.isEnabled(currentContext));
        }

        /**
         * Invokes the wrapped action and animates its changes.
         *
         * @param e action event.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            animation = true;
            int[] animOldColumnBounds = gridInfo.getColumnBounds();
            int[] animOldRowBounds = gridInfo.getRowBounds();
            animLayer.setContainer(componentPane);
            animLayer.setGlassPane(GlassPane.this);
            animLayer.setVIPComponents(new ArrayList<Component>(selection));
            animLayer.loadStart();
            GridBoundsChange change = delegate.performAction(gridManager, currentContext);
            updateCurrentContext(currentContext);
            animLayer.loadEnd();
            int[] animNewColumnBounds;
            int[] animNewRowBounds;
            if (change == null) {
                animNewColumnBounds = gridInfo.getColumnBounds();
                animNewRowBounds = gridInfo.getRowBounds();
                if(gridInfo.hasGaps()) {
                    if( animNewColumnBounds.length > gridInfo.getLastGapColumn() + 3 ) {
                        animNewColumnBounds = resizeArray(animNewColumnBounds, gridInfo.getLastGapColumn() + 3);
                    }
                    if( animNewRowBounds.length > gridInfo.getLastGapRow() + 3 ) {
                        animNewRowBounds = resizeArray(animNewRowBounds, gridInfo.getLastGapRow() + 3);
                    }
                }
            } else {
                animOldColumnBounds = change.getOldColumnBounds();
                animOldRowBounds = change.getOldRowBounds();
                animNewColumnBounds = change.getNewColumnBounds();
                animNewRowBounds = change.getNewRowBounds();
            }
            if (animNewColumnBounds.length != animOldColumnBounds.length) {
                if (animNewColumnBounds.length > animOldColumnBounds.length) {
                    animOldColumnBounds = resizeArray(animOldColumnBounds, animNewColumnBounds.length);
                } else {
                    animNewColumnBounds = resizeArray(animNewColumnBounds, animOldColumnBounds.length);
                }
            }
            if (animNewRowBounds.length != animOldRowBounds.length) {
                if (animNewRowBounds.length > animOldRowBounds.length) {
                    animOldRowBounds = resizeArray(animOldRowBounds, animNewRowBounds.length);
                } else {
                    animNewRowBounds = resizeArray(animNewRowBounds, animOldRowBounds.length);
                }
            }
            change = new GridBoundsChange(animOldColumnBounds, animOldRowBounds, animNewColumnBounds, animNewRowBounds);
            
            // Update customizer
            if (customizer != null) {
                DesignerContext context = currentContext();
                customizer.setContext(context);                
            }
            // Animate, if there is something to animate
            if(!noChange(change)) {
                animChange = change;
                animLayer.animate();
            } else {
                animation = false;
            }
        }

        /**
         * Checks whether column/row bounds have changed and need to be animated
         *
         * @param change between old and new column/row bounds.
         */
        public boolean noChange(GridBoundsChange change) {
            if(!Arrays.equals(change.getNewColumnBounds(), change.getOldColumnBounds()) ||
                    !Arrays.equals(change.getNewRowBounds(), change.getOldRowBounds())) {
                return false;
            }
            return true;
        }

        /**
         * Returns larger/equal/shorter copy of the passed array. The additional slots
         * of this array are filled by the last value of the original array,
         * or the superfluous slots are trimmed.
         * 
         * @param original original array.
         * @param newLength length of the new array.
         * @return resized array.
         */
        private int[] resizeArray(int[] original, int newLength) {
            int[] result = original;
            if(newLength > original.length) {
                result = new int[newLength];
                System.arraycopy(original, 0, result, 0, original.length);
                int last = original[original.length-1];
                for (int i=original.length; i<newLength; i++) {
                    result[i] = last;
                }
            }
            if(newLength < original.length) {
                assert newLength > 0;
                result = new int[newLength];
                System.arraycopy(original, 0, result, 0, newLength);
            }
            return result;
        }

        @Override
        public Object getValue(String key) {
            Object value = delegate.getValue(key);
            if (value == null) {
                value = super.getValue(key);
            }
            return value;
        }

    }

    /**
     * Action that changes location of the selected component.
     */
    class ChangeLocationAction extends AbstractGridAction {

        @Override
        public GridBoundsChange performAction(GridManager gridManager, DesignerContext context) {
            GridInfoProvider info = gridManager.getGridInfo();
            int[] originalColumnBounds = info.getColumnBounds();
            int[] originalRowBounds = info.getRowBounds();

            GridUtils.revalidateGrid(gridManager);
            int columns = info.getColumnCount();
            int rows = info.getRowCount();
            int xDelta = newGridX - info.getGridX(focusedComponent);
            int yDelta = newGridY - info.getGridY(focusedComponent);
            int heightDelta = newGridHeight - info.getGridHeight(focusedComponent);
            int widthDelta = newGridWidth - info.getGridWidth(focusedComponent);
            GridUtils.removePaddingComponents(gridManager);
            if ((xDelta != 0) || (yDelta != 0) || (widthDelta != 0) || (heightDelta != 0)) {
                for (Component selComp : selection) {
                    int gridX = info.getGridX(selComp);
                    int gridY = info.getGridY(selComp);
                    int width = info.getGridWidth(selComp);
                    int height = info.getGridHeight(selComp);
                    // We set all grid location properties even when
                    // just one changes to avoid unexpected behaviour
                    // caused by GridBagConstraint.RELATIVE or REMAINDER
                    gridManager.setGridPosition(selComp,
                        gridX+xDelta, gridY+yDelta, width+widthDelta, height+heightDelta);
                    columns = Math.max(columns, gridX+xDelta+width+widthDelta);
                    rows = Math.max(rows, gridY+yDelta+height+heightDelta);
                }
            }
            gridManager.updateLayout(false);
            GridUtils.revalidateGrid(gridManager);
            gridManager.updateGaps(false);
            GridUtils.addPaddingComponents(gridManager, columns, rows);
            GridUtils.revalidateGrid(gridManager);
            int[] newColumnBounds = gridInfo.getColumnBounds();
            int[] newRowBounds = gridInfo.getRowBounds();
            if (newColumnBounds.length>originalColumnBounds.length) {
                // Moving/resizing outside the original grid
                int[] oldBounds = new int[newColumnBounds.length];
                for (int i=0; i<oldBounds.length; i++) {
                    oldBounds[i] = extendedColumnBound(originalColumnBounds, i);
                }
                originalColumnBounds = oldBounds;
            }
            if (newRowBounds.length>originalRowBounds.length) {
                // Moving/resizing outside the original grid
                int[] oldBounds = new int[newRowBounds.length];
                for (int i=0; i<oldBounds.length; i++) {
                    oldBounds[i] = extendedRowBound(originalRowBounds, i);
                }
                originalRowBounds = oldBounds;
            }
            
            return new GridBoundsChange(originalColumnBounds, originalRowBounds, newColumnBounds, newRowBounds);
        }

    }

}
