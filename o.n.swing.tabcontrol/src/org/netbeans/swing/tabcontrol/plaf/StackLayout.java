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

import java.awt.*;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;

/**
 * Special simple layout used in TabbedContainer. Shows component in the
 * "stack", it means that only one component is visible at any time, others are
 * always hidden "below" the visible one. Use method showComponent to select
 * visible component.
 *
 * @author Dafe Simonek
 */
class StackLayout implements LayoutManager {
    
    // #100486 - hold visibleComp weakly, because removeLayoutComponent may not
    // be called and then visibleComp is not freed. See StackLayoutTest for details.
    /**
     * Holds currently visible component or null if no comp is visible
     */
    private WeakReference<Component> visibleComp = null;

    /**
     * Set the currently displayed component.  If passed null for the component,
     * all contained components will be made invisible (sliding windows do this)
     * @param c Component to show
     * @param parent Parent container
     */
    public void showComponent(Component c, Container parent) {
        Component comp = getVisibleComponent();
        if (comp != c) {
            if (!parent.isAncestorOf(c) && c != null) {
                parent.add(c);
            }
            synchronized (parent.getTreeLock()) {
                if (comp != null) {
                    comp.setVisible(false);
                }
                visibleComp = new WeakReference<Component>(c);
                if (c != null) {
                    c.setVisible(true);
                }
		// trigger re-layout
		if (c instanceof JComponent) {
		    ((JComponent)c).revalidate();
		}
		else {
		    parent.validate(); //XXX revalidate should work!
		}
            }
        }
    }
    
    /** Allows support for content policies 
     * @return Currently visible component or null
     */
    public Component getVisibleComponent() {
        return visibleComp == null ? null : visibleComp.get();
    }

    /**
     * ********** Implementation of LayoutManager interface *********
     */

    public void addLayoutComponent(String name, Component comp) {
        synchronized (comp.getTreeLock()) {
            comp.setVisible(false);
            // keep consistency if showComponent was already called on this
            // component before
            if (comp == getVisibleComponent()) {
                visibleComp = null;
            }
/*System.out.println("Border dump for " + comp.getName());
borderDump((javax.swing.JComponent)comp, "");*/
        }
    }
    
/*private void borderDump (javax.swing.JComponent comp, String space) {
    javax.swing.border.Border compBorder = comp.getBorder();
    if (compBorder == null) {
        System.out.println(space + comp.getClass().getName() + " has no border.");
    } else {
        System.out.println(space + comp.getClass().getName() + ": " + compBorder.getClass().getName());
    }
    Component curComp;
    for (int i = 0; i < comp.getComponentCount(); i++) {
        curComp = comp.getComponent(i);
        if (curComp instanceof javax.swing.JComponent) {
            borderDump((javax.swing.JComponent)curComp, space + " ");
        }
    }
}*/
    
    public void removeLayoutComponent(Component comp) {
        synchronized (comp.getTreeLock()) {
            if (comp == getVisibleComponent()) {
                visibleComp = null;
            }
            // kick out removed component as visible, so that others
            // don't have problems with hidden components
            comp.setVisible(true);
        }
    }

    public void layoutContainer(Container parent) {
        Component visibleComp = getVisibleComponent();
        if (visibleComp != null) {
            synchronized (parent.getTreeLock()) {
                Insets insets = parent.getInsets();
                visibleComp.setBounds(insets.left, insets.top, parent.getWidth()
                   - (insets.left + insets.right), parent.getHeight()
                   - (insets.top + insets.bottom));
            }
        }
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
        Component c = getVisibleComponent();
        return c != null ? c.getMinimumSize() : getEmptySize();
    }

    @Override
    public Dimension preferredLayoutSize(Container parent) {
        Component c = getVisibleComponent();
        return c != null ? c.getPreferredSize() : getEmptySize();
    }

    /**
     * Specifies default size of empty container
     */
    private static Dimension getEmptySize() {
        return new Dimension(50, 50);
    }

}
