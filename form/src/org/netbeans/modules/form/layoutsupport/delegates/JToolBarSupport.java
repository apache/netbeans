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
import javax.swing.*;

import org.netbeans.modules.form.layoutsupport.*;

/**
 * Dedicated layout support class for JToolBar.
 *
 * @author Tomas Pavek
 */

public class JToolBarSupport extends AbstractLayoutSupport {

    /** Gets the supported layout manager class - JToolBar.
     * @return the class supported by this delegate
     */
    @Override
    public Class getSupportedClass() {
        return JToolBar.class;
    }

    @Override
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index) {
        // Issue 63955 and JDK bug 4294758
        LayoutManager lm = containerDelegate.getLayout();
        // Cannot use instanceof BoxLayout because JToolBar
        // uses DefaultToolBarLayout wrapper around BoxLayout
        if (lm instanceof LayoutManager2) {
            ((LayoutManager2)lm).invalidateLayout(containerDelegate);
        }
        super.addComponentsToContainer(container, containerDelegate, components, index);
    }

    /** This method calculates position (index) for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
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
        if (!(container instanceof JToolBar))
            return -1;

        int orientation = ((JToolBar)container).getOrientation();
        
        assistantParams = 0;
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            if (component == components[i]) {
                assistantParams--;
                continue;
            }
            Rectangle b = components[i].getBounds();
            if (orientation == SwingConstants.HORIZONTAL) {
                if (posInCont.x < b.x + b.width / 2) {
                    assistantParams += i;
                    return i;
                }
            }
            else {
                if (posInCont.y < b.y + b.height / 2) {   
                    assistantParams += i;
                    return i;
                }
            }
        }

        assistantParams += components.length;
        return components.length;
    }

    private int assistantParams;
    @Override
    public String getAssistantContext() {
        return "toolbarLayout"; // NOI18N
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
     * @param containerDelegate effective container delegate of the container
     *        (for layout managers we always use container delegate instead of
     *        the container)
     * @param component the real component being dragged, not needed here
     * @param newConstraints component layout constraints to be presented;
     *        not used for JToolBar
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
        if (!(container instanceof JToolBar))
            return false;

        int orientation = ((JToolBar)container).getOrientation();
        Component[] components = container.getComponents();
        Rectangle rect;

        if ((newIndex >= 0) && (newIndex < components.length) && (component == components[newIndex])) {
            newIndex++;
        }
        if ((components.length == 0) || ((components.length == 1) && (components[0] == component))) {
            Insets ins = container.getInsets();
            rect = orientation == SwingConstants.HORIZONTAL ?
                   new Rectangle(ins.left,
                                 ins.top + (container.getHeight() - ins.top
                                            - ins.bottom - 20) / 2,
                                 30, 20) :
                   new Rectangle(ins.left + (container.getWidth() - ins.left
                                 - ins.right - 30) / 2,
                                 ins.top,
                                 30, 20);
        }
        else if (newIndex < 0 || newIndex >= components.length) {
            int index = (components[components.length-1] == component) ? components.length-2 : components.length-1;
            Rectangle b = components[index].getBounds();
            rect = orientation == SwingConstants.HORIZONTAL ?
                   new Rectangle(b.x + b.width - 10, b.y, 20, b.height) :
                   new Rectangle(b.x, b.y + b.height - 10, b.width, 20);
        }
        else {
            Rectangle b = components[newIndex].getBounds();
            rect = orientation == SwingConstants.HORIZONTAL ?
                   new Rectangle(b.x - 10, b.y, 20, b.height) :
                   new Rectangle(b.x, b.y - 10, b.width, 20);
        }

        g.drawRect(rect.x, rect.y, rect.width, rect.height);

        return true;
    }
}
