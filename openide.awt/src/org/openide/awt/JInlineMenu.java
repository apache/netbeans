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
package org.openide.awt;

import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.List;

import javax.swing.*;
import javax.swing.event.*;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;


/**
 * Menu element that can contain other menu items. These items are then
 * displayed "inline". The JInlineMenu can be used to compose more menu items
 * into one that can be added/removed at once.
 *
 * @deprecated since org.openide.awt 6.5 JInlineMenu is a simple implementation of {@link DynamicMenuContent}, it
 * doesn't update when visible and doesn't handle the separators itself anymore.
 *
 * @author Jan Jancura
 */
@Deprecated
public class JInlineMenu extends JMenuItem implements DynamicMenuContent {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -2310488127953523571L;
    private static final Icon BLANK_ICON = ImageUtilities.loadImageIcon("org/openide/resources/actions/empty.gif", false); // NOI18N            

//    /** north separator */
//    private JSeparator north = new JSeparator();
//
//    /** south separator */
//    private JSeparator south = new JSeparator();

    /** Stores inner MenuItems added to outer menu. */
    private JComponent[] items = new JComponent[0];

    /** true iff items of this menu are up to date */
    boolean upToDate;

    /** private List of the items previously added to the parent menu */
    private List addedItems;

    /**
    * Creates new JInlineMenu.
    */
    public JInlineMenu() {
        setEnabled(false);
        setVisible(false);
        upToDate = true;
    }

    /** Overriden to eliminate big gap at top of JInline popup painting.
     * @return cleared instets (0, 0, 0, 0) */
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }

    /**
     * Setter for array of items to display. Can be called only from event queue
     * thread.
     *
     * @param newItems array of menu items to display
     */
    public void setMenuItems(final JMenuItem[] newItems) {
        //        if(!SwingUtilities.isEventDispatchThread()) {
        //System.err.println("JInlineMenu.setMenuItems called outside of event queue !!!");
        //Thread.dumpStack();
        //        }
        // make a tuned private copy
        JComponent[] local = new JComponent[newItems.length];

        for (int i = 0; i < newItems.length; i++) {
            local[i] = (newItems[i] != null) ? (JComponent) newItems[i] : new JSeparator();
        }

        items = local;
        upToDate = false;

        alignItems();

    }


    /** Overriden to return first non null icon of current items or null if
     * all items has null icons.
     */
    private void alignItems() {
        // hack - we use also getIcon() result of JInlineMenu as indicator if we
        // should try to align items using empty icon or not 
        boolean shouldAlign = getIcon() != null;

        if (!shouldAlign) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] instanceof JMenuItem) {
                    if (((JMenuItem) items[i]).getIcon() != null) {
                        shouldAlign = true;

                        break;
                    }
                }
            }
        }

        if (!shouldAlign) {
            return;
        }

        // align items using empty icon
        JMenuItem curItem = null;

        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof JMenuItem) {
                curItem = (JMenuItem) items[i];

                if (curItem.getIcon() == null) {
                    curItem.setIcon(BLANK_ICON);
                }
            }
        }
    }



    /** Finds the index of a component in array of components.
     * @return index or -1
     */
    private static int findIndex(Object of, Object[] arr) {
        int menuLength = arr.length;

        for (int i = 0; i < menuLength; i++) {
            if (of == arr[i]) {
                return i;
            }
        }

        return -1;
    }


    public JComponent[] synchMenuPresenters(JComponent[] items) {
        return this.items;
    }

    public JComponent[] getMenuPresenters() {
        return items;
    }

}
