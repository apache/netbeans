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
    @Override
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
