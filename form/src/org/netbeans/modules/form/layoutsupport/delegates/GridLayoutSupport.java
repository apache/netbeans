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

package org.netbeans.modules.form.layoutsupport.delegates;

import java.awt.*;
import org.netbeans.modules.form.layoutsupport.*;
import org.netbeans.modules.form.codestructure.*;

/**
 * Support class for GridLayout.  This is an example of very simple layout
 * with no constraints; just basic drag & drop is implemented.
 *
 * @author Tran Duc Trung, Tomas Pavek
 */

public class GridLayoutSupport extends AbstractLayoutSupport
{
    /** Gets the supported layout manager class - GridLayout.
     * @return the class supported by this delegate
     */
    @Override
    public Class getSupportedClass() {
        return GridLayout.class;
    }

    /** This method calculates position (index) for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     * @param component the real component being dragged; not needed here
     * @param index position (index) of the component in its current container;
     *        not needed here
     * @param posInCont position of mouse in the container delegate
     * @param posInComp position of mouse in the dragged component;
     *        not needed here
     * @return index corresponding to the position of the component in the
     *         container
     */
    @Override
    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (!(containerDelegate.getLayout() instanceof GridLayout))
            return -1;

        Component[] components = containerDelegate.getComponents();
        GridLayout layout = (GridLayout) containerDelegate.getLayout();
        int nrows = layout.getRows();
        int ncols = layout.getColumns();

        if ((nrows <= 0 && ncols <= 0) || components.length == 0)
            return components.length;
        
        if (nrows != 0)
            ncols = (components.length + nrows - 1) / nrows;
        else
            nrows = (components.length + ncols - 1) / ncols;

        Dimension sz = containerDelegate.getSize();
        Insets insets = containerDelegate.getInsets();
        sz.width -= insets.left + insets.right;
        sz.height -= insets.top + insets.bottom;

        int colwidth = sz.width / ncols;
        if (colwidth <= 0) {
            assistantParams = components.length;
            return components.length;
        }
        int col = (posInCont.x - insets.left + colwidth / 2) / colwidth;
        
        int rowheight = sz.height / nrows;
        if (rowheight <= 0) {
            assistantParams = components.length;
            return components.length;
        }
        int row = (posInCont.y - insets.top) / rowheight;

        int newIndex = row * ncols + col;
        newIndex = newIndex >= components.length ? components.length : newIndex;
        assistantParams = newIndex;
        return newIndex;
    }

    private int assistantParams;
    @Override
    public String getAssistantContext() {
        return "gridLayout"; // NOI18N
    }

    @Override
    public Object[] getAssistantParams() {
        return new Object[] {Integer.valueOf(assistantParams+1)};
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container;
     *        for layout managers we always use container delegate instead of
     *        the container
     * @param component the real component being dragged, not needed here
     * @param newConstraints component layout constraints to be presented;
     *        not used for GridLayout
     * @param newIndex component's index position to be presented
     * @param g Graphics object for painting (with color and line style set)
     * @return whether any feedback was painted (true in this case)
     */
    @Override
    public boolean paintDragFeedback(Container container, 
                                     Container containerDelegate,
                                     Component component,
                                     LayoutConstraints newConstraints,
                                     int newIndex,
                                     Graphics g)
    {
        if (!(containerDelegate.getLayout() instanceof GridLayout))
            return false;

        Component[] components = containerDelegate.getComponents();
        GridLayout layout = (GridLayout) containerDelegate.getLayout();
        int dx = 12 + layout.getHgap() / 2;
        int x = 0, w = 24, y = 0, h = 0;
        
        if ((newIndex <= 0) || ((components.length == 1) && (components[0] == component))) {
            if ((components.length > 1) || ((components.length == 1) && (components[0] != component))) {
                Component comp = components[0];
                if (comp == component) {
                    comp = components[1];
                }
                Rectangle b = comp.getBounds();
                x = b.x - dx ;
                y = b.y;
                h = b.height;
            }
            else {
                Insets ins = containerDelegate.getInsets();
                x = ins.left + 1;
                w = containerDelegate.getWidth() - ins.right - ins.left - 2;
                y = ins.top + 1;
                h = containerDelegate.getHeight() - ins.bottom - ins.top - 2;
            }
        }
        else if ((newIndex >= components.length) ||
            ((newIndex == components.length - 1) && (components[newIndex] == component))) {
            Component comp = components[components.length-1];
            if (comp == component) {
                comp = components[components.length-2];
            }
            Rectangle b = comp.getBounds();
            x = b.x + b.width - dx;
            y = b.y;
            h = b.height;
        }
        else {
            Component comp = components[newIndex];
            if (comp == component) {
                comp = components[newIndex+1];
            }
            Rectangle b = comp.getBounds();
            x = b.x - dx;
            y = b.y;
            h = b.height;
        }

        g.drawRect(x, y, w, h);
        return true;
    }

    // ------------

    /** This method is called from readLayoutCode to read the layout manager
     * bean code (i.e. code for constructor and properties). This method is
     * overridden here because "rows" and "columns" properties are mutually
     * dependent (so not true JavaBean properties).
     * @param layoutExp CodeExpressin of the layout manager
     * @param initLayoutCode CodeGroup to be filled with relevant
     *        initialization code
     */
    @Override
    protected void readInitLayoutCode(CodeExpression layoutExp,
                                      CodeGroup initLayoutCode)
    {
        CodeExpression[] params = layoutExp.getOrigin().getCreationParameters();
        if (params.length > 0) {
            Object rowsValue = params[0].getOrigin().getValue();
            if (rowsValue instanceof Integer
                && ((Integer)rowsValue).intValue() == 0)
            {   // number of rows is to be set to 0, we must preset
                // columns property to something else than 0
                try {
                    getProperty("columns").setValue(new Integer(1));
                }
                catch (Exception ex) {} // ignore
            }
        }

        super.readInitLayoutCode(layoutExp, initLayoutCode);
    }
}
