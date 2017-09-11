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
