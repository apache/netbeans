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
/*
 *
 * BaseTabLayoutModel.java
 *
 * Created on May 16, 2003, 4:22 PM
 */

package org.netbeans.swing.tabcontrol.plaf;

import org.netbeans.swing.tabcontrol.TabDataModel;
import org.openide.awt.HtmlRenderer;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of TabLayoutModel.  Simply provides a series of
 * rectangles for each tab starting at 0 and ending at the last element, with
 * the width set to the calculated width for the string plus a padding value
 * assigned in <code>setPadding</code>.
 * <p>
 * To implement TabLayoutModel, it is often useful to create an implementation which
 * wraps an instance of <code>DefaultTabLayoutModel</code>, and uses it to calculate
 * tab sizes.
 *
 * <strong>Do not use this class directly, use DefaultTabLayoutModel - this class
 * exists to enable unit tests to provide a subclass</strong>
 *
 * @author Tim Boudreau
 */
class BaseTabLayoutModel implements TabLayoutModel {
    protected TabDataModel model;
    protected int textHeight = -1;
    protected int padX = 5;
    protected int padY = 5;
    protected JComponent renderTarget;

    protected BaseTabLayoutModel(TabDataModel model, JComponent renderTarget) {
        this.model = model;
        this.renderTarget = renderTarget;
    }

    private Font getFont() {
        return renderTarget.getFont();
    }

    protected int iconWidth(int index) {
        Icon ic = model.getTab(index).getIcon();
        int result;
        if (ic != null) {
            result = ic.getIconWidth();
        } else {
            result = 0;
        }
        return result;
    }
    
    protected int iconHeight (int index) {
        Icon ic = model.getTab(index).getIcon ();
        int result;
        if (ic != null) {
            result = ic.getIconHeight();
        } else {
            result = 0;
        }
        return result;
    }
    
    protected int textWidth(int index) {
        try {
            String text = model.getTab(index).getText();
            return textWidth(text, getFont(), renderTarget);
        } catch (NullPointerException npe) {
            IllegalArgumentException iae = new IllegalArgumentException(
                    "Error fetching width for tab " + //NOI18N
                    index
                    + " - model size is "
                    + model.size()
                    + " TabData is " + //NOI18N
                    model.getTab(index)
                    + " model contents: "
                    + model); //NOI18N
            throw iae;
        }
    }

    static int textWidth(String text, Font f, JComponent component) {
        /* The results of this method used to be cached, but the cache had memory leak problems as
        well as problems in multi-monitor setups with different HiDPI scale factors (NETBEANS-4066).
        Some investigation suggest this method is not being called very often, however. In fact, it
        is usually called only in cases where the string would have to be repainted in any case. So
        it seems safe to just do the measurement every time here. */
        double wid = HtmlRenderer.renderString(text, BasicScrollingTabDisplayerUI.getOffscreenGraphics(component), 0, 0,
                                       Integer.MAX_VALUE,
                                       Integer.MAX_VALUE, f,
                                       Color.BLACK, HtmlRenderer.STYLE_TRUNCATE,
                                       false);
        return Math.round(Math.round(wid));
    }

    protected int textHeight(int index, JComponent component) {
        if (textHeight == -1) {
            //No need to calculate for every string
            String testStr = "Zgj"; //NOI18N
            Font f = getFont();
            textHeight = (int)(f.getStringBounds(testStr, 
            BasicScrollingTabDisplayerUI.getOffscreenGraphics(component).getFontRenderContext()).getWidth()) + 2;
        }
        return textHeight;
    }

    public int getX(int index) {
        int result = renderTarget.getInsets().left;
        for (int i = 0; i < index; i++) {
            result += getW(i);
        }
        return result;
    }

    public int getY(int index) {
        return renderTarget.getInsets().top;
    }

    public int getH(int index) {
        return Math.max (textHeight(index, renderTarget) + padY, model.getTab(index).getIcon().getIconHeight() + padY);
    }

    public int getW(int index) {
        int iconWidth = iconWidth(index);
        if( 0 == iconWidth )
            iconWidth = 5;
        return textWidth(index) + iconWidth + padX;
    }

    public int indexOfPoint(int x, int y) {
        int max = model.size();
        int pos = renderTarget.getInsets().left;
        for (int i = 0; i < max; i++) {
            pos += getW(i);
            if (pos > x) {
                return i;
            }
        }
        return -1;
    }

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
        int max = model.size();
        int pos = insets.left;
        for (int i = 0; i < max; i++) {
            int delta = getW(i);
            pos += delta;
            if (x <= (pos - delta / 2)) {
                return i;
            } else if (x < pos) {
                return i + 1;
            }
        }
        return max;
    }

    public void setPadding(Dimension d) {
        padX = d.width;
        padY = d.height;
    }
}
