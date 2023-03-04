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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.Icon;
import org.netbeans.swing.tabcontrol.TabDataModel;

import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.openide.awt.HtmlRenderer;

/**
 * Implementation of layout model for View-type tabs, which are not scrollable.
 *
 * @author S. Aubrecht
 */
final class NonStretchingViewTabLayoutModel implements TabLayoutModel {

    private TabDataModel model;

    private TabDisplayer tabDisplayer;
    
    private static final int PADDING_RIGHT = 5+15+15;
    
    private static final int ICON_X_PAD = 4;
    
    private Dimension padding = new Dimension( 5+5, 0 );
    
    /**
     * Creates a new instance of ViewTabLayoutModel
     */
    public NonStretchingViewTabLayoutModel(TabDataModel model,
                              TabDisplayer tabDisplayer) {
        this.model = model;
        this.tabDisplayer = tabDisplayer;
    }

    @Override
    public int getH(int index) {
        checkIndex(index);
        Insets insets = tabDisplayer.getInsets();
        return tabDisplayer.getHeight() - (insets.bottom + insets.top);
    }

    @Override
    public int getW(int index) {
        checkIndex(index);
        return getXCoords()[index] - getX(index);
    }

    @Override
    public int getX(int index) {
        checkIndex(index);
        return index > 0 ? getXCoords()[index - 1] : tabDisplayer.getInsets().left;
    }

    @Override
    public int getY(int index) {
        checkIndex(index);
        return tabDisplayer.getInsets().top;
    }

    @Override
    public int indexOfPoint(int x, int y) {
        Insets insets = tabDisplayer.getInsets();
        int contentWidth = tabDisplayer.getWidth()
                - (insets.left + insets.right +PADDING_RIGHT);
        int contentHeight = tabDisplayer.getHeight()
                - (insets.bottom + insets.top);
        if (y < insets.top || y > contentHeight || x < insets.left
                || x > contentWidth) {
            return -1;
        }
        int size = model.size();
        int diff;
        for (int i = 0; i < size; i++) {
            diff = x - getX(i);
            if ((diff >= 0) && (diff < getW(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int dropIndexOfPoint(int x, int y) {
        Insets insets = tabDisplayer.getInsets();
        int contentWidth = tabDisplayer.getWidth()
                - (insets.left + insets.right+PADDING_RIGHT);
        int contentHeight = tabDisplayer.getHeight()
                - (insets.bottom + insets.top);
        if (y < insets.top || y > contentHeight || x < insets.left
                || x > contentWidth) {
            return -1;
        }
        // can have rounding errors, not important here
        int size = model.size();
        int[] coords = getXCoords();
        for( int i=0; i<coords.length; i++ ) {
            if( x < coords[i] ) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void setPadding(Dimension d) {
        this.padding = new Dimension( d );
    }

    /**
     * Checks validity of given index
     */
    private void checkIndex(int index) {
        int size = model.size();
        if ((index < 0) || (index >= size)) {
            throw new IllegalArgumentException("Index out of valid scope 0.."
                                               + (size - 1)
                                               + ": "
                                               + index);
        }
    }
    
    private int[] getXCoords () {
        int size = model.size();
        int[] tabsXCoord = new int[size];

        Insets dispInsets = tabDisplayer.getInsets();
        double curX =  dispInsets.left;
        int maxRight = tabDisplayer.getWidth() - dispInsets.right - PADDING_RIGHT;

        if( maxRight - dispInsets.left / size < 5 ) {
            //the tab displayer is too narrow
            makeEqualSized( tabsXCoord, dispInsets.left, maxRight );
            return tabsXCoord;
        }
        String curText;
        int tabIndex;
        Icon buttonIcon = tabDisplayer.getUI().getButtonIcon(TabControlButton.ID_CLOSE_BUTTON, TabControlButton.STATE_DEFAULT);
        int iconPadding = 0;
        if( null != buttonIcon ) {
            iconPadding += buttonIcon.getIconWidth();
            iconPadding += ICON_X_PAD;
        }
        int maxTabWidth = 0;
        for (int i = 0; i < size; i++) {
            tabIndex = i;
            curText = model.getTab(tabIndex).getText();
            curX += HtmlRenderer.renderString(curText, BasicScrollingTabDisplayerUI.getOffscreenGraphics(tabDisplayer), 0, 0,
                                       Integer.MAX_VALUE,
                                       Integer.MAX_VALUE, tabDisplayer.getFont(),
                                       Color.BLACK, HtmlRenderer.STYLE_TRUNCATE,
                                       false);
            curX += padding.width;
            curX += iconPadding;

            tabsXCoord[i] = ( int ) Math.round(curX);
            int tabWidth = i == 0 ? tabsXCoord[i] : tabsXCoord[i] - tabsXCoord[i-1];
            maxTabWidth = Math.max( maxTabWidth, tabWidth );
        }
        if( tabsXCoord[tabsXCoord.length-1] > maxRight ) {
            //make some/all tabs shorter to fit into the tab displayer width
            int secondMaxTabWidth = findSecondMaxWidth( tabsXCoord, maxTabWidth );
            int widthToDistribute = tabsXCoord[tabsXCoord.length-1] - maxRight;
            
            makeShorter( tabsXCoord, secondMaxTabWidth, maxTabWidth, widthToDistribute );
            
            if( tabsXCoord[tabsXCoord.length-1] > maxRight ) {
                //second pass
                maxTabWidth = secondMaxTabWidth;
                secondMaxTabWidth = findSecondMaxWidth( tabsXCoord, secondMaxTabWidth );
                widthToDistribute = tabsXCoord[tabsXCoord.length-1] - maxRight;
            
                makeShorter( tabsXCoord, secondMaxTabWidth, maxTabWidth, widthToDistribute );
                
                if( tabsXCoord[tabsXCoord.length-1] > maxRight ) {
                    makeEqualSized( tabsXCoord, dispInsets.left, maxRight );
//                    widthToDistribute = tabsXCoord[tabsXCoord.length-1] - maxRight;
//                    makeShorterEqually( tabsXCoord, widthToDistribute );
                }
            }
        }
        
        return tabsXCoord;
    }
    
    private static void makeEqualSized( int[] coords, int leftInsets, int maxRight ) {
        int tabWidth = Math.max( (maxRight-leftInsets) / coords.length, 1 );
        coords[0] = leftInsets + tabWidth;
        for( int i=1; i<coords.length; i++ ) {
            coords[i] = coords[i-1] + tabWidth;
        }
    }
    
    private static void makeShorter( int[] coords, int tabsWiderThan, int maxTabWidth, int widthToDistribute ) {
        int tabCount = countTabsWiderThan( coords, tabsWiderThan );
        if( 0 == tabCount )
            return;
        int total = 0;
        int delta = Math.max( widthToDistribute / tabCount, 1 );
        for( int i=coords.length-1; i>=0; i-- ) {
            int tabWidth = coords[i] ;
            if( i > 0 )
                tabWidth -= coords[i-1];
            if( tabWidth < tabsWiderThan )
                continue;
            int currentDelta = Math.min( tabWidth-tabsWiderThan, delta );
            for( int j=i; j<coords.length; j++ ) {
                coords[j] -= currentDelta;
            }
            total += currentDelta;
            if( total >= widthToDistribute )
                break;
        }
    }
    
    private static void makeShorterEqually( int[] coords, int widthToDistribute ) {
        int delta = Math.max( widthToDistribute / coords.length, 1 );
        int total = 0;
        for( int i=coords.length-1; i>=0; i-- ) {
            int tabWidth = coords[i] ;
            if( i > 0 )
                tabWidth -= coords[i-1];
            if( tabWidth < 5 )
                continue;
            int currentDelta = Math.min( tabWidth-5, delta );
            for( int j=i; j<coords.length; j++ ) {
                coords[j] -= currentDelta;
            }
            total += currentDelta;
            if( total >= widthToDistribute )
                break;
        }
    }

    private static int findSecondMaxWidth( int[] coords, int maxTabWidth ) {
        int res = 0;
        for( int i=0; i<coords.length; i++ ) {
            int tabWidth = i == 0 ? coords[i] : coords[i] - coords[i-1];
            if( tabWidth > res && tabWidth < maxTabWidth ) {
                res = tabWidth;
            }
        }
        return res;
    }
    
    private static int countTabsWiderThan( int[] coords, int width ) {
        int res = 0;
        for( int i=0; i<coords.length; i++ ) {
            int tabWidth = i == 0 ? coords[i] : coords[i] - coords[i-1];
            if( tabWidth > width ) {
                res++;
            }
        }
        return res;
    }
}
