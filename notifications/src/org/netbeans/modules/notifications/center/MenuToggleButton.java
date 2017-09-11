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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.notifications.center;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

/**
 *
 * @author S. Aubrecht
 * @author jpeska
 */
class MenuToggleButton extends JToggleButton {

    private boolean mouseInArrowArea = false;

    /**
     * Creates a new instance of MenuToggleButton
     */
    public MenuToggleButton(final Icon regIcon, Icon rollOverIcon, int arrowWidth) {
        assert null != regIcon;
        assert null != rollOverIcon;
        final Icon lineIcon = new LineIcon(rollOverIcon, arrowWidth);
        setIcon(regIcon);
        setRolloverIcon(lineIcon);
        setRolloverSelectedIcon(lineIcon);
        setFocusable(false);

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseInArrowArea = isInArrowArea(e.getPoint());
                setRolloverIcon(mouseInArrowArea ? regIcon : lineIcon);
                setRolloverSelectedIcon(mouseInArrowArea ? regIcon : lineIcon);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isInArrowArea(e.getPoint())) {
                    JPopupMenu popup = getPopupMenu();
                    if (null != popup) {
                        popup.show(MenuToggleButton.this, 0, getHeight());
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                mouseInArrowArea = isInArrowArea(e.getPoint());
                setRolloverIcon(mouseInArrowArea ? regIcon : lineIcon);
                setRolloverSelectedIcon(mouseInArrowArea ? regIcon : lineIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                mouseInArrowArea = false;
                setRolloverIcon(regIcon);
                setRolloverSelectedIcon(regIcon);
            }
        });

        setModel(new Model());
    }

    protected JPopupMenu getPopupMenu() {
        return null;
    }

    private boolean isInArrowArea(Point p) {
        return p.getLocation().x >= getWidth() - 3 - 2 - getInsets().right;
    }

    private static class LineIcon implements Icon {

        private final Icon origIcon;
        private final int arrowWidth;

        public LineIcon(Icon origIcon, int arrowWidth) {
            this.origIcon = origIcon;
            this.arrowWidth = arrowWidth;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            origIcon.paintIcon(c, g, x, y);

            g.setColor(UIManager.getColor("controlHighlight")); //NOI18N
            g.drawLine(x + origIcon.getIconWidth() - arrowWidth - 2, y,
                    x + origIcon.getIconWidth() - arrowWidth - 2, y + getIconHeight());
            g.setColor(UIManager.getColor("controlShadow")); //NOI18N
            g.drawLine(x + origIcon.getIconWidth() - arrowWidth - 3, y,
                    x + origIcon.getIconWidth() - arrowWidth - 3, y + getIconHeight());
        }

        @Override
        public int getIconWidth() {
            return origIcon.getIconWidth();
        }

        @Override
        public int getIconHeight() {
            return origIcon.getIconHeight();
        }
    }

    private class Model extends JToggleButton.ToggleButtonModel {

        @Override
        public void setPressed(boolean b) {
            if (mouseInArrowArea) {
                return;
            }
            super.setPressed(b);
        }
    }
}
