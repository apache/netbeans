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

package org.netbeans.modules.form.palette;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.Field;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.explorer.view.MenuView;

/**
 * Hacked MenuView.Menu to use ScrollablePopupMenu instead of JPopupMenu
 * as its popup, and to filter invalid palette item nodes.
 */

public class PaletteMenuView extends org.openide.awt.JMenuPlus {

    private Node menuNode;
    private NodeAcceptor menuAction;

    private boolean hacked = false;
    private boolean filled = false;
    private int level;

    private static int maxHeight = Utilities.getUsableScreenBounds().height - 25;

    public PaletteMenuView(NodeAcceptor acceptor) {
        this(PaletteUtils.getPaletteNode(), acceptor);
    }

    public PaletteMenuView(Node node, NodeAcceptor acceptor) {
        this(node, acceptor, 0);
    }

    private PaletteMenuView(Node node, NodeAcceptor acceptor, int level) {
        menuNode = node;
        menuAction = acceptor;
        this.level = level;
        setText(node.getDisplayName());
        getSubNodes(); // force subnodes creation
    }

    // Allow to disable the popup hack. It doesn't work for popups
    // in modal dialog. It is better to have the hack switched off
    // there (than to have a completely broken popup).
    private boolean shouldHack = true;
    public void disableHack() {
        shouldHack = false;
    }

    /** popupMenu field should be set here because getPopupMenu() is called from
     * superclass constructor.
     */
    @Override
    public JPopupMenu getPopupMenu() {
        if (shouldHack && !hacked) {
            try {
                Field f = JMenu.class.getDeclaredField("popupMenu"); // NOI18N
                f.setAccessible(true);
                if (f.get(this) == null) {
                    ScrollPopupMenu popup = new ScrollPopupMenu(maxHeight);
                    popup.setInvoker(this);
                    f.set(this, popup);
                }
                hacked = true;
            }
            catch (Exception ex) {
                System.out.println("[WARNING] Cannot create scrollable popup menu."); // NOI18N
            }
        }

        JPopupMenu popup = super.getPopupMenu();
        if (menuNode != null) { // #248319
            fillSubMenu(popup);
        }
        return popup;
    }

    private void fillSubMenu(JPopupMenu popup) {
        if (!filled) {
            filled = true;
            popup.addPopupMenuListener(new PopupListener(popup));
            removeAll();

            Node[] nodes = getSubNodes();
            if (nodes.length > 0) {
                for (int i=0; i < nodes.length; i++) {
                    JMenuItem item;
                    if (nodes[i].isLeaf()) {
                        item = new MenuView.MenuItem(nodes[i], menuAction);
                    } else {
                        PaletteMenuView view = new PaletteMenuView(nodes[i], menuAction, level+1);
                        if (!shouldHack) {
                            view.disableHack();
                        }
                        item = view;
                    }
                    add(item);
                }
            }
            else {
                JMenuItem empty = new JMenuItem(
                    PaletteUtils.getBundleString("CTL_EmptyPaletteMenu")); // NOI18N
                empty.setEnabled(false);
                add(empty);
            }
        }
    }

    private Node[] getSubNodes() {
        return level == 0 ? PaletteUtils.getCategoryNodes(menuNode, true) :
                            PaletteUtils.getItemNodes(menuNode, true);
    }

    private class PopupListener implements PopupMenuListener {
        private JPopupMenu popup;

        PopupListener(JPopupMenu popup) {
            this.popup = popup;
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            filled = false; // clear the status and stop listening
            popup.removePopupMenuListener(this);
        }
        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {}
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
    }

    @Override
    protected Point getPopupMenuOrigin() {
        int x = 0;
        int y = 0;
        JPopupMenu pm = getPopupMenu();
        // Figure out the sizes needed to caclulate the menu position
        Dimension screenSize =Toolkit.getDefaultToolkit().getScreenSize();
        Dimension s = getSize();
        Dimension pmSize = pm.getSize();
        // For the first time the menu is popped up, 
        // the size has not yet been initiated
        if (pmSize.width==0) {
            pmSize = pm.getPreferredSize();
        }
        if (pmSize.height > maxHeight) {
            pmSize.height = maxHeight + 2;
            pmSize.width += 14;
        }

        Point position = getLocationOnScreen();

        Container parent = getParent();
        if (parent instanceof JPopupMenu) {
            // We are a submenu (pull-right)

            if( getComponentOrientation().isLeftToRight() ) {
                // First determine x:
                if (position.x+s.width + pmSize.width < screenSize.width) {
                    x = s.width;         // Prefer placement to the right
                } else {
                    x = 0-pmSize.width;  // Otherwise place to the left
                }
            } else {
                // First determine x:
                if (position.x < pmSize.width) {
                    x = s.width;         // Prefer placement to the right
                } else {
                    x = 0-pmSize.width;  // Otherwise place to the left
                }
            }
            // Then the y:
            if (position.y+pmSize.height < screenSize.height) {
                y = 0;                       // Prefer dropping down
            } else {
                y = s.height-pmSize.height;  // Otherwise drop 'up'
                if (y < -position.y)
                    y = -position.y + 6;
            }
        } else {
            // We are a toplevel menu (pull-down)

            if( getComponentOrientation().isLeftToRight() ) {
                // First determine the x:
                if (position.x+pmSize.width < screenSize.width) {
                    x = 0;                     // Prefer extending to right 
                } else {
                    x = s.width-pmSize.width;  // Otherwise extend to left
                }
            } else {
                // First determine the x:
                if (position.x+s.width < pmSize.width) {
                    x = 0;                     // Prefer extending to right 
                } else {
                    x = s.width-pmSize.width;  // Otherwise extend to left
                }
            }
            // Then the y:
            if (position.y+s.height+pmSize.height < screenSize.height) {
                y = s.height;          // Prefer dropping down
            } else {
                y = -pmSize.height;   // Otherwise drop 'up'
            }
        }
        return new Point(x,y);
    }
}
