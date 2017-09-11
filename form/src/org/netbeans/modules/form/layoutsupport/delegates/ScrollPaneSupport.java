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
import org.netbeans.modules.form.fakepeer.FakePeerSupport;

/**
 * Dedicated layout support class for ScrollPane.
 *
 * @author Tomas Pavek
 */

public class ScrollPaneSupport extends AbstractLayoutSupport {

    /** Gets the supported layout manager class - ScrollPane.
     * @return the class supported by this delegate
     */
    @Override
    public Class getSupportedClass() {
        return ScrollPane.class;
    }

    /** This method should calculate position (index) for a component dragged
     * over a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     * @param component the real component being dragged; not needed here
     * @param index position (index) of the component in its current container;
     *        not needed here
     * @param posInCont position of mouse in the container delegate; not needed
     * @param posInComp position of mouse in the dragged component; not needed
     * @return index corresponding to the position of the component in the
     *         container; we just return 0 here - as the drag&drop does not
     *         have much sense in JScrollPane
     */
    @Override
    public int getNewIndex(Container container,
                           Container containerDelegate,
                           Component component,
                           int index,
                           Point posInCont,
                           Point posInComp)
    {
        if (container.getComponentCount() > 1) // [or containerDelegate??]
            return -1;
        return 0;
    }

    @Override
    public String getAssistantContext() {
        return "scrollPaneLayout"; // NOI18N
    }

    /** This method paints a dragging feedback for a component dragged over
     * a container (or just for mouse cursor being moved over container,
     * without any component).
     * @param container instance of a real container over/in which the
     *        component is dragged
     * @param containerDelegate effective container delegate of the container
     * @param component the real component being dragged; not needed here
     * @param newConstraints component layout constraints to be presented;
     *        not used for ScrollPane
     * @param newIndex component's index position to be presented; not needed
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
        Dimension sz = container.getSize();
        Insets insets = container.getInsets();
        sz.width -= insets.left + insets.right;
        sz.height -= insets.top + insets.bottom;
        
        g.drawRect(0, 0, sz.width, sz.height);
        return true;
    }

    /** Adds real components to given container (according to layout
     * constraints stored for the components).
     * @param container instance of a real container to be added to
     * @param containerDelegate effective container delegate of the container
     * @param components components to be added
     * @param index position at which to add the components to container
     */
    @Override
    public void addComponentsToContainer(Container container,
                                         Container containerDelegate,
                                         Component[] components,
                                         int index)
    {
        if (components.length == 0)
            return;

        if (container instanceof ScrollPane) {
            // [do not allow adding a component when some already added??]
            ScrollPane scroll = (ScrollPane) container;
            Component removedComp = null;
            if (scroll.getComponentCount() > 0)
                removedComp = scroll.getComponent(0);
            try {
                scroll.add(components[0]);
            } catch (NullPointerException npex) {
                // Issue 36629 - ScrollPane attempts to place
                // components with a lightweight peer into a Panel
                // This collides with our fake peers and can result
                // in a NPE on JDK1.5. The correct peer for this
                // Panel used to be set below.
            }
            // hack for AWT components - we must attach the fake peer again
            // to the component that was removed by adding new component
            ensureFakePeerAttached(removedComp);
        }
    }

    /** Removes a real component from a real container.
     * @param container instance of a real container
     * @param containerDelegate effective container delegate of the container
     * @param component component to be removed
     * @return whether it was possible to remove the component (some containers
     *         may not support removing individual components reasonably)
     */
    @Override
    public boolean removeComponentFromContainer(Container container,
                                                Container containerDelegate,
                                                Component component)
    {
        return false; // cannot remove component from JSplitPane
    }

    static private void ensureFakePeerAttached(Component comp) {
        FakePeerSupport.attachFakePeer(comp);
        if (comp instanceof Container)
            FakePeerSupport.attachFakePeerRecursively((Container)comp);
    }
}
