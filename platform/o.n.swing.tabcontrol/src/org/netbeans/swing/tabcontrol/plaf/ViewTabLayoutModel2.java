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

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.tabcontrol.TabDisplayer;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.openide.awt.HtmlRenderer;

/**
 * @author Dafe Simonek
 */
final class ViewTabLayoutModel2 implements TabLayoutModel, ChangeListener {

    private TabDisplayer displayer;
    
    private ViewTabLayoutModel2.PaddingInfo padding;
    
    private java.util.List<Integer> index2Pos;
    private java.util.List<Integer> pos2Index;
    
    private int tabFixedWidth = -1;

    static final class PaddingInfo {
        Dimension txtPad;
        int txtIconsXPad;
        int iconsXPad;
    } // end of PaddingInfo
    
    
    /**
     * Creates a new instance of ViewTabLayoutModel
     */
    public ViewTabLayoutModel2(TabDisplayer displayer,
                              PaddingInfo padding) {
        this.displayer = displayer;
        this.padding = padding;
        updatePermutations();
        displayer.getModel().addChangeListener(this);
    }

    @Override
    public int getH(int index) {
        checkIndex(index);
        Insets insets = displayer.getInsets();
        return displayer.getHeight() - (insets.bottom + insets.top);
    }
    
    @Override
    public int getY(int index) {
        checkIndex(index);
        return displayer.getInsets().top;
    }

    @Override
    public int getW(int index) {
        checkIndex(index);
        
        int tabPos = index2Pos.get(index);
        return getXCoords()[tabPos] - getX(index);
    }

    @Override
    public int getX(int index) {
        checkIndex(index);
        
        int tabPos = index2Pos.get(index);
        return tabPos > 0 ? getXCoords()[tabPos - 1] : displayer.getInsets().left;
    }

    @Override
    public int indexOfPoint(int x, int y) {
        Insets insets = displayer.getInsets();
        int contentWidth = displayer.getWidth()
                - (insets.left + insets.right);
        int contentHeight = displayer.getHeight()
                - (insets.bottom + insets.top);
        if (y < insets.top || y > contentHeight || x < insets.left
                || x > contentWidth) {
            return -1;
        }
        int size = displayer.getModel().size();
        int diff;
        int leftSide;
        int[] tabsXCoordinates = getXCoords();
        // go through tab positions
        for (int i = 0; i < size; i++) {
            if (tabsXCoordinates[i] > 0) {
                leftSide = i > 0 ? tabsXCoordinates[i - 1] : insets.left;
                diff = x - leftSide;
                if ((diff >= 0) && (diff < getW(i))) {
                    return pos2Index.get(i);
                }
            }
        }
        return -1;
    }

    @Override
    public int dropIndexOfPoint(int x, int y) {
        Insets insets = displayer.getInsets();
        int contentWidth = displayer.getWidth()
                - (insets.left + insets.right);
        int contentHeight = displayer.getHeight()
                - (insets.bottom + insets.top);
        if (y < insets.top || y > contentHeight || x < insets.left
                || x > contentWidth) {
            return -1;
        }
        // XXX - TBD
        throw new UnsupportedOperationException("not implemenetd yet....");
        /*
        // can have rounding errors, not important here
        int size = displayer.getModel().size();
        float tabWidth = (float) contentWidth / (float) size;
        // move in between tabs
        x = x - insets.left + (int) tabWidth / 2;
        int result = (int) (x / tabWidth);
        return Math.min(result, displayer.getModel().size());*/
    }

    @Override
    public void setPadding(Dimension d) {
        // do nothing
    }

    /**
     * Checks validity of given index
     */
    private void checkIndex(int index) {
        int size = displayer.getModel().size();
        if ((index < 0) || (index >= size)) {
            throw new IllegalArgumentException("Index out of valid scope 0.."
                                               + (size - 1)
                                               + ": "
                                               + index);
        }
    }

    private int[] getXCoords () {
        TabDataModel model = displayer.getModel();
        int size = model.size();
        int[] tabsXCoord = new int[size];

        if (tabFixedWidth < 0) {
            tabFixedWidth = padding.txtPad.width + padding.txtIconsXPad
                    + padding.iconsXPad;
        }

        Insets dispInsets = displayer.getInsets();
        double curX =  dispInsets.left;
        int maxRight = displayer.getWidth() - dispInsets.right;

        String curText;
        int tabIndex;
        Icon buttonIcon;
        for (int i = 0; i < size; i++) {
            tabIndex = pos2Index.get(i);
            curText = model.getTab(tabIndex).getText();
            curX += HtmlRenderer.renderString(curText, BasicScrollingTabDisplayerUI.getOffscreenGraphics(displayer), 0, 0,
                                       Integer.MAX_VALUE,
                                       Integer.MAX_VALUE, displayer.getFont(),
                                       Color.BLACK, HtmlRenderer.STYLE_TRUNCATE,
                                       false) + tabFixedWidth;
            if (tabIndex == displayer.getSelectionModel().getSelectedIndex()) {
                // add icon sizes if any
                buttonIcon = displayer.getUI().getButtonIcon(TabControlButton.ID_CLOSE_BUTTON, tabIndex);
                if (buttonIcon != null) {
                    curX += buttonIcon.getIconWidth()+padding.iconsXPad;
                }
                buttonIcon = displayer.getUI().getButtonIcon(TabControlButton.ID_PIN_BUTTON, tabIndex);
                if (buttonIcon != null) {
                    curX += buttonIcon.getIconWidth()+padding.iconsXPad;
                }
            }

            tabsXCoord[i] = Math.round(Math.round(curX));
            if (curX > maxRight) {
                break;
            }
        }
        
        return tabsXCoord;
    }
    
    private void updatePermutations () {
        int itemCount = displayer.getModel().size();
        index2Pos = new ArrayList<Integer>(itemCount); 
        pos2Index = new ArrayList<Integer>(itemCount); 
        for (int i = 0; i < itemCount; i++) {
            index2Pos.add(Integer.valueOf(itemCount - i - 1));
            pos2Index.add(0, Integer.valueOf(i));
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        updatePermutations();
    }
    
}
