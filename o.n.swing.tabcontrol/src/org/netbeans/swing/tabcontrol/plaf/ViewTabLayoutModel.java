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
