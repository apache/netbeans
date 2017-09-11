/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            return textWidth(text, getFont());
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

    private static Map<String,Integer> widthMap = new HashMap<String,Integer>(31);

    static int textWidth(String text, Font f) {
        //Note:  If we choose to support multiple fonts in different
        //tab controls in the system, make the cache non-static and
        //dump it if the font changes.
        Integer result = widthMap.get(text);
        if (result == null) {
            double wid = HtmlRenderer.renderString(text, BasicScrollingTabDisplayerUI.getOffscreenGraphics(), 0, 0,
                                           Integer.MAX_VALUE,
                                           Integer.MAX_VALUE, f,
                                           Color.BLACK, HtmlRenderer.STYLE_TRUNCATE,
                                           false);
            result = new Integer(Math.round(Math.round(wid)));
            widthMap.put(text, result);
        }
        return result.intValue();
    }

    protected int textHeight(int index) {
        if (textHeight == -1) {
            //No need to calculate for every string
            String testStr = "Zgj"; //NOI18N
            Font f = getFont();
            textHeight = new Double(f.getStringBounds(testStr, 
            BasicScrollingTabDisplayerUI.getOffscreenGraphics().getFontRenderContext()).getWidth()).intValue() + 2;
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
        return Math.max (textHeight(index) + padY, model.getTab(index).getIcon().getIconHeight() + padY);
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
