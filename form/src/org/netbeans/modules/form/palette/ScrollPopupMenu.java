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
