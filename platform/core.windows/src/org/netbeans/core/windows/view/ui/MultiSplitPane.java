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

package org.netbeans.core.windows.view.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.netbeans.core.windows.Switches;
import org.netbeans.core.windows.view.ViewElement;




/**
 * A split pane that can display two or more resizeable components separated
 * by draggable split bars. The child components can be arranged in a row 
 * (horizontal orientation) or in a column (vertical orientation).
 *
 * @author Stanislav Aubrecht
 */
public class MultiSplitPane extends JPanel 
                implements MouseMotionListener, MouseListener {
    
    //the divider that user is currently dragging with a mouse
    private MultiSplitDivider draggingDivider;
    //a list of draggable split dividers
    private ArrayList<MultiSplitDivider> dividers = new ArrayList<MultiSplitDivider>();
    //true if the list of children has been updated
    private boolean dirty = true;
    //a list of children cells (component wrappers)
    private ArrayList<MultiSplitCell> cells = new ArrayList<MultiSplitCell>();
    //split orientation JSplitPane.HORIZONTAL_SPLIT or JSplitPane.VERTICAL_SPLIT
    private int orientation;
    //the width or height of the divider bar
    private int dividerSize;
    
    private boolean userMovedSplit = false;
    
    public MultiSplitPane() {
        setLayout( new MultiSplitLayout() );
        addMouseMotionListener( this );
        addMouseListener( this );

        Color bkColor = UIManager.getColor("NbSplitPane.background");
        if( null != bkColor ) {
            setBackground(bkColor);
            setOpaque(true);
        }
        if( UIManager.getBoolean( "NbMainWindow.showCustomBackground" ) ) //NOI18N
            setOpaque(false);
    }
    
    /**
     * Set new list of components to be displayed in the split and also their
     * resize weights and initial split weights.
     *
     * @param orientation Use JSplitPane.HORIZONTAL_SPLIT for horizontal orientation
     * (children components are arranged in a single row) or JSplitPane.VERTICAL_SPLIT
     * for vertical orientation (children components are arranged in a single column).
     * @param childrenComponents ViewElements to be displayed in the split pane.
     * @param splitWeights Initial split positions, i.e. what portion of the split window
     * the child components initially require.
     */
    public void setChildren( int orientation,
                             ViewElement[] childrenViews, 
                             double[] splitWeights ) {
        
        assert childrenViews.length == splitWeights.length;

        this.orientation = orientation;

        //list of components currently displayed in the split
        List<Component> currentComponents = collectComponents();
        
        cells.clear();
        for( int i=0; i<childrenViews.length; i++ ) {
            cells.add( new MultiSplitCell( childrenViews[i], 
                                           splitWeights[i], 
                                           isHorizontalSplit() ) );
        }
        List<Component> updatedComponents = collectComponents();
        
        ArrayList<Component> removed = new ArrayList<Component>( currentComponents );
        removed.removeAll( updatedComponents ); //componets that were removed from the split
        ArrayList<Component> added = new ArrayList<Component>( updatedComponents );
        added.removeAll( currentComponents ); //components that were added to the split
        
        for(Component c: removed) {
            remove( c );
        }
        
        for(Component c: added) {
            add( c );
        }

        dirty = true;
        
        //active TC has changed or some TC opened/closed - stop keyboard resizing (if any)
        ModeResizer.stop();
    }
    
    int getCellCount() {
        return cells.size();
    }
    
    MultiSplitCell cellAt( int index ) {
        assert index >= 0;
        assert index < cells.size();
        return (MultiSplitCell)cells.get( index );
    }
    
    /**
     * Remove child component at the given position from the split.
     */
    public void removeViewElementAt( int index ) {
        if( index < 0 || index >= cells.size() )
            return;
        MultiSplitCell cellToRemove = (MultiSplitCell)cells.remove( index );
        remove( cellToRemove.getComponent() );
        dirty = true;
    }
    
    public int getOrientation() {
        return orientation;
    }
    
    public boolean isVerticalSplit() {
        return orientation == JSplitPane.VERTICAL_SPLIT;
    }
    
    public boolean isHorizontalSplit() {
        return orientation == JSplitPane.HORIZONTAL_SPLIT;
    }
    
    private List<Component> collectComponents() {
        ArrayList<Component> res = new ArrayList<Component>( getCellCount() );
        for( int i=0; i<getCellCount(); i++ ) {
            MultiSplitCell cell = cellAt( i );
            Component c = cell.getComponent();
            assert null != c;
            res.add( c );
        }
        return res;
    }
    
    /**
     * Calculate split weights for all children components according to split's current dimensions.
     */
    public void calculateSplitWeights( List<ViewElement> visibleViews, List<Double> splitWeights ) {
        double size = isHorizontalSplit() ? getSize().width : getSize().height;
        if( size <= 0.0 )
            return;
        for( int i=0; i<getCellCount(); i++ ) {
            MultiSplitCell cell = cellAt( i );
            double weight = cell.getSize() / size;
            splitWeights.add(weight);
            visibleViews.add( cell.getViewElement() );
        }
    }
    
    public int getDividerSize() {
        return dividerSize;
    }
    
    public void setDividerSize( int newDividerSize ) {
        dirty |= newDividerSize != dividerSize;
        this.dividerSize = newDividerSize;
    }
    
    @Override
    public Dimension getMinimumSize() {
        //the minimum size is a sum of minimum sizes of all children components
        Dimension d = new Dimension();
        for( int i=0; i<getCellCount(); i++ ) {
            MultiSplitCell cell = cellAt( i );
            int size = cell.getMinimumSize();
            Dimension minDim = cell.getComponent().getMinimumSize();
            if( isHorizontalSplit() ) {
                d.width += size;
                if( minDim.height > d.height )
                    d.height = minDim.height;
            } else {
                d.height += size;
                if( minDim.width > d.width )
                    d.width = minDim.width;
            }
        }
        //the minimum size must hold at least the size of all split bars
        if( isHorizontalSplit() ) {
            d.width += (getCellCount()-1) * getDividerSize();
        } else {
            d.height += (getCellCount()-1) * getDividerSize();
        }
        return d;
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        switchCursor( e );
        e.consume();
    }

    @Override
    public void mouseDragged( MouseEvent e ) {
        if( null == draggingDivider )
            return;
        
        draggingDivider.dragTo( e.getPoint() );
        e.consume();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if( null == draggingDivider )
            return;
        
        final Point p = new Point( e.getPoint() );
        draggingDivider.finishDraggingTo( p );
        draggingDivider = null;
        setCursor( Cursor.getDefaultCursor() );
        e.consume();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if( !Switches.isTopComponentResizingEnabled() )
            return;
        doLayout();
        MultiSplitDivider divider = dividerAtPoint( e.getPoint() );
        if( null == divider )
            return;
        
        draggingDivider = divider;
        divider.startDragging( e.getPoint() );
        e.consume();
    }

    @Override
    public void mouseExited(MouseEvent e) {
        if( null == draggingDivider ) {
            setCursor( Cursor.getDefaultCursor() );
        }
        e.consume();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }
    

    private void switchCursor( MouseEvent e ) {
        if( !Switches.isTopComponentResizingEnabled() )
            return;
        MultiSplitDivider divider = dividerAtPoint( e.getPoint() );
        if( null == divider ) {
            setCursor( Cursor.getDefaultCursor() );
        } else {
            if( divider.isHorizontal() ) {
                setCursor( Cursor.getPredefinedCursor( Cursor.E_RESIZE_CURSOR ) );
            } else {
                setCursor( Cursor.getPredefinedCursor( Cursor.N_RESIZE_CURSOR ) );
            }
        }
    }

    private MultiSplitDivider dividerAtPoint( Point p ) {
        for(MultiSplitDivider d: dividers) {
            if( d.containsPoint( p ) )
                return d;
        }
        return null;
    }

    /**
     * Shrink/grow children components.
     * 
     * @param newSize Split pane's new widht/height depending on split orientation.
     */
    private void resize( int newSize ) {
        //find out what the delta is
        int currentSize = 0;
        for( int i=0; i<getCellCount(); i++ ) {
            currentSize += cellAt( i ).getRequiredSize();
        }
        int totalDividerSize = getDividerSize() * (getCellCount()-1);
        int newNetSize = newSize - totalDividerSize;
        int delta = newNetSize - currentSize;

        if( delta > 0 ) {
            //the child cells will grow

            grow( delta );

        } else if( delta < 0 ) {

            delta = shrink( delta );

            if( delta > 0 ) {
                //the complete delta couldn't be distributed because of minimum sizes constraints
                newNetSize -= delta;
            }
        }
            
        //check for rounding errors and add 'missing' pixel(s) to the last cell
        int totalSize = 0;
        for( int i=0; i<getCellCount(); i++ ) {
            MultiSplitCell cell = cellAt( i );
            totalSize += cell.getRequiredSize();
        }
        if( totalSize < newNetSize ) {
            MultiSplitCell lastCell = cellAt( getCellCount()-1 );
            lastCell.setRequiredSize( lastCell.getRequiredSize() + (newNetSize-totalSize) );
        }
    }
    
    /**
     * Grow children cell dimensions.
     */
    private void grow( int delta ) {
        //children with resize weight > 0 that are not collapsed
        List<MultiSplitCell> hungryCells = getResizeHungryCells();

        //grow some/all child windows
        if( !hungryCells.isEmpty() ) {
            //we have children with non-zero resize weight so let them consume the whole delta
            normalizeResizeWeights( hungryCells );
            distributeDelta( delta, hungryCells );
        } else {
            //resize all children proportionally
            ArrayList<MultiSplitCell> resizeableCells = new ArrayList<MultiSplitCell>( cells );
            normalizeResizeWeights( resizeableCells );
            distributeDelta( delta, resizeableCells );
        }
    }
    
    /**
     * Shrink children cell dimensions.
     * The children cells will not shrink below their minimum sizes.
     *
     * @return The remaining resize delta that has not been distributed among children cells.
     */
    private int shrink( int negativeDelta ) {
        int delta = -negativeDelta;

        //children with resize weight > 0 that are not collapsed
        List<MultiSplitCell> hungryCells = getResizeHungryCells();

        //first find out how much cells with non-zero resize weight can shrink
        int resizeArea = calculateShrinkableArea( hungryCells );
        if( resizeArea >= delta ) {
            resizeArea = delta;
            delta = 0;
        } else {
            delta -= resizeArea;
        }
        if( resizeArea > 0  ) {
            //shrink cells with non-zero resize weight
            distributeDelta( -resizeArea, hungryCells );
        }

        if( delta > 0 ) {
            //hungry cells did not consume the complete delta, 
            //distribute the remaining delta among other resizeable cells
            ArrayList<MultiSplitCell> resizeableCells = new ArrayList<MultiSplitCell>( cells );

            resizeArea = calculateShrinkableArea( resizeableCells );
            if( resizeArea >= delta ) {
                resizeArea = delta;
                delta = 0;
            } else {
                delta -= resizeArea;
            }
            if( resizeArea > 0 ) {
                distributeDelta( -resizeArea, resizeableCells );
            }
        }
        return delta;
    }
    
    /**
     * Sum up the available resize space of given cells. The resize space is the difference
     * between child cell's current size and child cell's minimum size.
     * Children cells that cannot be resized are removed from the given list and
     * resize weights of remaining cells are normalized.
     */
    private int calculateShrinkableArea( List<MultiSplitCell> cells ) {
        int res = 0;
        ArrayList<MultiSplitCell> nonShrinkable = new ArrayList<MultiSplitCell>( cells.size() );
        for( int i=0; i<cells.size(); i++ ) {
            MultiSplitCell c = (MultiSplitCell)cells.get( i );
            int currentSize = c.getRequiredSize();
            int minSize = c.getMinimumSize();
            if( currentSize - minSize > 0 ) {
                res += currentSize - minSize;
            } else {
                nonShrinkable.add( c );
            }
        }
        
        cells.removeAll( nonShrinkable );
        for(MultiSplitCell c: cells) {
            int currentSize = c.getRequiredSize();
            int minSize = c.getMinimumSize();
            c.setNormalizedResizeWeight( 1.0*(currentSize-minSize)/res );
        }        
        
        return res;
    }
    
    /**
     * Distribute the given delta among given cell dimensions using their normalized weights.
     */
    private void distributeDelta( int delta, List<MultiSplitCell> cells ) {
        int totalDistributed = 0; 
        for( int i=0; i<cells.size(); i++ ) {
            MultiSplitCell cell = cells.get( i );
            int cellDelta = (int)(cell.getNormalizedResizeWeight()*delta);
            totalDistributed += cellDelta;
            if( i == cells.size()-1 ) //fix rounding errors
                cellDelta += delta - totalDistributed;
            cell.setRequiredSize( cell.getRequiredSize() + cellDelta );
        }
    }
    
    /**
     * Normalize resize weights so that their sum equals to 1.
     */
    private void normalizeResizeWeights( List cells ) {
        if( cells.isEmpty() )
            return;
        
        double totalWeight = 0.0;
        for( Iterator i=cells.iterator(); i.hasNext(); ) {
            MultiSplitCell c = (MultiSplitCell)i.next();
            totalWeight += c.getResizeWeight();
        }
        
        double deltaWeight = (1.0 - totalWeight) / cells.size();

        for( Iterator i=cells.iterator(); i.hasNext(); ) {
            MultiSplitCell c = (MultiSplitCell)i.next();
            c.setNormalizedResizeWeight( c.getResizeWeight() + deltaWeight );
        }
    }
    
    /**
     * @return List of children cells with non-zero resize weight.
     */
    List<MultiSplitCell> getResizeHungryCells() {
        List<MultiSplitCell> res = new ArrayList<MultiSplitCell>( cells.size() );
        for( int i=0; i<getCellCount(); i++ ) {
            MultiSplitCell cell = cellAt( i );
            if( cell.getResizeWeight() <= 0.0 )
                continue;
            res.add( cell );
        }
        return res;
    }

    /**
     * (Re)create wrapper classes for split divider rectangles.
     */
    void createDividers() {
        dividers.clear();
        for( int i=0; i<getCellCount()-1; i++ ) {
            MultiSplitCell first = cellAt( i );
            MultiSplitCell second = cellAt( i+1 );

            MultiSplitDivider divider = new MultiSplitDivider( MultiSplitPane.this, first, second );
            dividers.add( divider );
        }
    }
    
    void splitterMoved() {
        userMovedSplit = true;
        validate();
    }
    
    // *************************************************************************
    // Accessibility
    
    @Override
    public AccessibleContext getAccessibleContext() {
        if( accessibleContext == null ) {
            accessibleContext = new AccessibleMultiSplitPane();
        }
        return accessibleContext;
    }
    
    int getDividerAccessibleIndex( MultiSplitDivider divider ) {
        int res = dividers.indexOf( divider );
        res += getAccessibleContext().getAccessibleChildrenCount() - dividers.size();
        return res;
    }

    public void startResizing( Component child ) {
        doLayout();
        MultiSplitDivider divider = findSplitDividerFor( child );
        if( null == divider )
            return;
        MultiSplitCell resizingCell = findSplitCellFor( child );
        MultiSplitDivider parentDivider = null;
        MultiSplitPane parentSplit = ( MultiSplitPane ) SwingUtilities.getAncestorOfClass( MultiSplitPane.class, this );
        if( null != parentSplit ) {
            parentSplit.doLayout();
            parentDivider = parentSplit.findSplitDividerFor( this );
        }
        ModeResizer.start( resizingCell.getComponent(), divider, parentDivider );
    }
    
    private MultiSplitDivider findSplitDividerFor( Component c ) {
        MultiSplitCell cell = findSplitCellFor( c );
        if( null == cell )
            return null;
        int index = cells.indexOf( cell );
        if( index >= dividers.size() )
            index--;
        return dividers.get( index ) ;
    }
    
    private MultiSplitCell findSplitCellFor( Component c ) {
        Container container = SwingUtilities.getAncestorOfClass( DefaultSplitContainer.ModePanel.class, c);
        if( null == container ) {
            if( c instanceof MultiSplitPane )
                container = ( Container ) c;
            else
                return null;
        }
        
        for( MultiSplitCell cell : cells ) {
            if( cell.getComponent().equals( container ) ) {
                return cell;
            }
        }
        return null;
    }

    protected class AccessibleMultiSplitPane extends AccessibleJComponent {
        @Override
        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet states = super.getAccessibleStateSet();
            if( isHorizontalSplit() ) {
                states.add( AccessibleState.HORIZONTAL );
            } else {
                states.add( AccessibleState.VERTICAL );
            }
            return states;
        }

        @Override
        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SPLIT_PANE;
        }

        @Override
        public Accessible getAccessibleAt( Point p ) {
            MultiSplitDivider divider = dividerAtPoint( p );
            if( null != divider ) {
                return divider;
            }
            return super.getAccessibleAt( p );
        }

        @Override
        public Accessible getAccessibleChild(int i) {

            int childrenCount = super.getAccessibleChildrenCount();
            if( i < childrenCount ) {
                return super.getAccessibleChild( i );
            }
            if( i-childrenCount >= dividers.size() ) {
                return null;
            }
            
            MultiSplitDivider divider = dividers.get( i-childrenCount );
            return divider;
        }

        @Override
        public int getAccessibleChildrenCount() {
            return super.getAccessibleChildrenCount() + dividers.size();
        }
        
    } // inner class AccessibleMultiSplitPane

    // *************************************************************************
    
    protected class MultiSplitLayout implements LayoutManager {
        
        @Override
        public void layoutContainer( Container c ) {
            if( c != MultiSplitPane.this )
                return;
            
            int newSize = isHorizontalSplit() ? getSize().width : getSize().height;
            //if the list of children has been modified then let the cells calculate
            //their initial sizes
            for( int i=0; i<getCellCount(); i++ ) {
                MultiSplitCell cell = cellAt( i );
                cell.maybeResetToInitialSize( newSize );
            }
            
            //calculate new sizes for children cells
            resize( newSize );
            
            //set children bounds
            layoutCells();

            if( userMovedSplit ) {
                //user dragged splitbar to a new location -> fire a property change
                userMovedSplit = false;
                firePropertyChange( "splitPositions", null, this );
            }
            
            //update the rectangles of split divider bars
            createDividers();
        }
        
        private void layoutCells() {
            int x = 0;
            int y = 0;
            int width = getWidth();
            int height = getHeight();
            for( int i=0; i<getCellCount(); i++ ) {
                MultiSplitCell cell = cellAt( i );
                
                //the child component may have been removed from this container 
                //(e.g. the view has been maximalized)
                if( cell.getComponent().getParent() != MultiSplitPane.this ) {
                    add( cell.getComponent() );
                }
                
                if( isHorizontalSplit() ) {
                    width = cell.getRequiredSize();
                } else {
                    height = cell.getRequiredSize();
                }
                cell.layout( x, y, width, height );
                
                if( isHorizontalSplit() ) {
                    x += width;
                    if( i < getCellCount() ) {
                        x += getDividerSize();
                    }
                } else {
                    y += height;
                    if( i < getCellCount()-1 ) {
                        y += getDividerSize();
                    }
                }
            }
        }

        @Override
        public Dimension minimumLayoutSize(Container container) {
            return container.getSize();
        }

        @Override
        public Dimension preferredLayoutSize(Container container) {
            return container.getSize();
        }

        @Override
        public void removeLayoutComponent(Component c) {}

        @Override
        public void addLayoutComponent(String string, Component c) {}
    }
}
