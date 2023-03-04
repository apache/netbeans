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


/** Hacked JPopupMenu(Plus) - displayed in JScrollPane if too long.
 */
public class ScrollPopupMenu extends JPopupMenu {

    JWindow popWin;
    JScrollPane scrollPane;
    int posX, posY;
    int maxHeight;

    ScrollPopupMenu(int maxH) {
        maxHeight = maxH;
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible == isVisible())
            return;

        if (visible) {
            if (getInvoker() != null && !(getInvoker() instanceof JMenu)) {
		if (getSubElements().length > 0) {
		    MenuElement me[] = new MenuElement[2];
		    me[0] = (MenuElement)this;
		    me[1] = getSubElements()[0];
		    MenuSelectionManager.defaultManager().setSelectedPath(me);
		}
                else {
		    MenuElement me[] = new MenuElement[1];
		    me[0] = (MenuElement)this;
		    MenuSelectionManager.defaultManager().setSelectedPath(me);
		}
	    }

            firePopupMenuWillBecomeVisible();

            Component comp = getInvoker();
            while (comp.getParent() != null)
                comp = comp.getParent();

            popWin = comp instanceof Window ?
                        new JWindow((Window)comp) :
                        new JWindow(new JFrame());
            popWin.setLocation(posX, posY);

            pack();
            popWin.setVisible(true);
        }
        else {
            getSelectionModel().clearSelection();
            if (popWin != null) {
                firePopupMenuWillBecomeInvisible();
                popWin.dispose();
                popWin = null;
                scrollPane = null;
            }
        }
    }

    @Override
    public boolean isVisible() {
        return popWin != null ? popWin.isShowing() : false;
    }

    @Override
    public void setLocation(int x, int y) {
        if (popWin != null && popWin.isShowing())
            popWin.setLocation(x, y);
        else {
            posX = x;
            posY = y;
        }
    }

    @Override
    public void pack() {
        if (popWin == null)
            return;
        
        Dimension prefSize = getPreferredSize();
        if (maxHeight == 0 || prefSize.height <= maxHeight) {
            if (scrollPane != null) {
                popWin.getContentPane().remove(scrollPane);
                scrollPane = null;
            }
            popWin.getContentPane().setLayout(null);
            popWin.getContentPane().add(this);
            setBounds(0,0, prefSize.width, prefSize.height);
            popWin.setSize(prefSize.width, prefSize.height);
        }
        else {
            if (scrollPane == null) {
                JPanel view = new JPanel(new BorderLayout());
                view.add(this, BorderLayout.CENTER);

                scrollPane = new JScrollPane(view);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    //                scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                if (bar != null) {
                    Dimension d = bar.getPreferredSize();
                    d.width = 12;
                    bar.setPreferredSize(d);
                    bar.setUnitIncrement(21);
                }

                popWin.getContentPane().add(scrollPane, BorderLayout.CENTER);
            }
            popWin.pack();
            popWin.setSize(popWin.getSize().width+12, maxHeight);
            requestFocus();
        }
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JScrollBar getScrollBar() {
        return scrollPane != null ? scrollPane.getVerticalScrollBar() : null;
    }
}
