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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Locale;
import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.accessibility.AccessibleState;
import javax.accessibility.AccessibleStateSet;
import javax.accessibility.AccessibleValue;


/**
 * Wrapper class for MultiSplitPane's split divider rectangle.
 */
public class MultiSplitDivider implements Accessible {
    
    MultiSplitPane splitPane;
    Rectangle rect = new Rectangle();
    MultiSplitCell first;
    MultiSplitCell second;
    
    Point currentDragLocation;
    int dragMin;
    int dragMax;
    int cursorPositionCompensation;
    
    private AccessibleContext accessibleContext;

    public MultiSplitDivider( MultiSplitPane parent, MultiSplitCell first, MultiSplitCell second ) {
        assert null != parent;
        assert null != first;
        assert null != second;
        this.splitPane = parent;
        this.first = first;
        this.second = second;
        
        reshape();
    }

    boolean isHorizontal() {
        return splitPane.isHorizontalSplit();
    }

    boolean isVertical() {
        return splitPane.isVerticalSplit();
    }
    
    int getDividerSize() {
        return splitPane.getDividerSize();
    }

    boolean containsPoint( Point p ) {
        return rect.contains( p );
    }
    
    void startDragging( Point p ) {
        currentDragLocation = new Point( rect.x, rect.y );

        if( isHorizontal() )
            cursorPositionCompensation = p.x - rect.x;
        else
            cursorPositionCompensation = p.y - rect.y;

        initDragMinMax();
    }
    
    void dragTo( Point p ) {
        if( isHorizontal() ) {
            if( p.x < dragMin )
                p.x = dragMin;
            if( p.x > dragMax )
                p.x = dragMax;
        } else {
            if( p.y < dragMin )
                p.y = dragMin;
            if( p.y > dragMax )
                p.y = dragMax;
        }
        
        currentDragLocation = p;
        
        resize(p);
    }
    
    void resize( int delta ) {
        Point p = rect.getLocation();
        if( isHorizontal() )
            p.x += delta;
        else
            p.y += delta;
        resize( p );
    }

    private void resize( Point p ) {
        if( isHorizontal() ) {
            p.x -= cursorPositionCompensation;
            if( p.x < dragMin )
                p.x = dragMin;
            if( p.x > dragMax )
                p.x = dragMax;

            if( p.x == rect.x ) {
                //split bar position didn't change
                return;
            }
        } else {
            p.y -= cursorPositionCompensation;
            if( p.y < dragMin )
                p.y = dragMin;
            if( p.y > dragMax )
                p.y = dragMax;

            if( p.y == rect.y ) {
                //split bar position didn't change
                return;
            }
        }

        int dividerSize = getDividerSize();

        if( isHorizontal() ) {
            int delta = p.x - rect.x;
            int x = first.getLocation();
            int y = 0;
            int width = first.getSize() + delta;
            int height = rect.height;
            first.layout( x, y, width, height );

            x = second.getLocation() + delta;
            width = second.getSize() - delta;
            second.layout( x, y, width, height );

            rect.x = p.x;
        } else {
            int delta = p.y - rect.y;
            int x = 0;
            int y = first.getLocation();
            int width = rect.width;
            int height = first.getSize() + delta;
            first.layout( x, y, width, height );

            y = second.getLocation() + delta;
            height = second.getSize() - delta;
            second.layout( x, y, width, height );

            rect.y = p.y;
        }
        splitPane.splitterMoved();//invalidate();
    }

    void finishDraggingTo( Point p ) {
        resize(p);
        currentDragLocation = null;
    }
    
    Point initDragMinMax() {
        int firstSize = first.getSize();
        int secondSize = second.getSize();
        int firstMinSize = first.getMinimumSize();
        int secondMinSize = second.getMinimumSize();
        
        if( isHorizontal() ) {
            dragMin = rect.x;
            dragMax = rect.x;
        } else {
            dragMin = rect.y;
            dragMax = rect.y;
        }
            
        if( firstSize >= firstMinSize ) {
            dragMin -= firstSize-firstMinSize;
        }
        if( secondSize >= secondMinSize ) {
            dragMax += secondSize-secondMinSize;
        }
        return rect.getLocation();
    }
    
    void reshape() {
        Dimension d = splitPane.getSize();
        int location = second.getLocation();

        if( isHorizontal() ) {
            rect.x = location-getDividerSize();
            rect.y = 0;
            rect.width = getDividerSize();
            rect.height = d.height;
        } else {
            rect.x = 0;
            rect.y = location-getDividerSize();
            rect.width = d.width;
            rect.height = getDividerSize();
        }
    }
    
    // *************************************************************************
    // Accessibility
    
    public AccessibleContext getAccessibleContext() {
        if( null == accessibleContext ) {
            accessibleContext = new AccessibleMultiSplitDivider();
        }
        return accessibleContext;
    }
    
    protected class AccessibleMultiSplitDivider extends AccessibleContext
        implements AccessibleValue {
        
        public AccessibleMultiSplitDivider() {
            setAccessibleParent( splitPane );
        }
        
        public Accessible getAccessibleChild(int i) {
            return null;
        }

        public int getAccessibleChildrenCount() {
            return 0;
        }

        public int getAccessibleIndexInParent() {
            return splitPane.getDividerAccessibleIndex( MultiSplitDivider.this );
        }

        public AccessibleRole getAccessibleRole() {
            return AccessibleRole.SPLIT_PANE;
        }

        public AccessibleStateSet getAccessibleStateSet() {
            AccessibleStateSet stateSet = new AccessibleStateSet();
            if( isHorizontal() ) {
                stateSet.add( AccessibleState.HORIZONTAL );
            } else {
                stateSet.add( AccessibleState.VERTICAL );
            }
            return stateSet;
        }

        public Locale getLocale() throws java.awt.IllegalComponentStateException {
            return Locale.getDefault();
        }
        
        public boolean setCurrentAccessibleValue(Number n) {
            initDragMinMax();
            int value = n.intValue();
            if( value < dragMin || value > dragMax ) {
                return false;
            }
            if( isHorizontal() ) {
                finishDraggingTo( new Point( value, 0 ) );
            } else {
                finishDraggingTo( new Point( 0, value ) );
            }
            return true;
        }

        public Number getMinimumAccessibleValue() {
            initDragMinMax();
            return Integer.valueOf( dragMin );
        }

        public Number getMaximumAccessibleValue() {
            initDragMinMax();
            return Integer.valueOf( dragMax );
        }

        public Number getCurrentAccessibleValue() {
            if( isHorizontal() )
                return Integer.valueOf( rect.x );
            else
                return Integer.valueOf( rect.y );
        }

        @Override
        public AccessibleValue getAccessibleValue() {
            return this;
        }
    } //end of AccessibleMultiSplitDivider inner class
}
