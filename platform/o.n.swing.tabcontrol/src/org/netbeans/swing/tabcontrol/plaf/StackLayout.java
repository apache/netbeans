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
