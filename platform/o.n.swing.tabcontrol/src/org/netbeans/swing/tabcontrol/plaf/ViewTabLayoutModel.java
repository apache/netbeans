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

/**
 * Implementation of layout model for View-type tabs, which are not scrollable
 * and are shrinking and extending their size to always cover whole tabs area.
 *
 * @author Dafe Simonek
 */
final class ViewTabLayoutModel implements TabLayoutModel {

    private TabDataModel model;

    private JComponent renderTarget;

    /**
     * Creates a new instance of ViewTabLayoutModel
     */
    public ViewTabLayoutModel(TabDataModel model,
                              JComponent renderTarget) {
        this.model = model;
        this.renderTarget = renderTarget;
    }

    @Override
    public int getH(int index) {
        checkIndex(index);
        Insets insets = renderTarget.getInsets();
        return renderTarget.getHeight() - (insets.bottom + insets.top);
    }

    @Override
    public int getW(int index) {
        checkIndex(index);
        int x = computeX(index);
        int nextX;
        if (index < model.size() - 1) {
            nextX = computeX(index + 1);
        } else {
            // last tab, special case
            Insets insets = renderTarget.getInsets();
            nextX = renderTarget.getWidth() - insets.right;
        }
        // substract from next tab to get width
        return nextX - x;
    }

    @Override
    public int getX(int index) {
        checkIndex(index);
        return computeX(index);
    }

    @Override
    public int getY(int index) {
        checkIndex(index);
        return renderTarget.getInsets().top;
    }

    @Override
    public int indexOfPoint(int x, int y) {
        Insets insets = renderTarget.getInsets();
        int contentWidth = renderTarget.getWidth()
                - (insets.left + insets.right);
        int contentHeight = renderTarget.getHeight()
                - (insets.bottom + insets.top);
        if (y < insets.top || y > contentHeight || x < insets.left
                || x > contentWidth) {
            return -1;
        }
        int size = model.size();
        int diff;
        for (int i = 0; i < size; i++) {
            diff = x - computeX(i);
            if ((diff >= 0) && (diff < getW(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int dropIndexOfPoint(int x, int y) {
        Insets insets = renderTarget.getInsets();
        int contentWidth = renderTarget.getWidth()
                - (insets.left + insets.right);
        int contentHeight = renderTarget.getHeight()
                - (insets.bottom + insets.top);
        if (y < insets.top || y > contentHeight || x < insets.left
                || x > contentWidth) {
            return -1;
        }
        // can have rounding errors, not important here
        int size = model.size();
        float tabWidth = (float) contentWidth / (float) size;
        // move in between tabs
        x = x - insets.left + (int) tabWidth / 2;
        int result = (int) (x / tabWidth);
        return Math.min(result, model.size());
    }

    @Override
    public void setPadding(Dimension d) {
        //do nothing
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

    /**
     * Computes and returns x coordination of left side of the tab with given
     * index
     */
    private int computeX(int index) {
        Insets insets = renderTarget.getInsets();
        int contentWidth = renderTarget.getWidth()
                - (insets.left + insets.right);
        return (contentWidth * index / model.size()) + insets.left;
    }


}
