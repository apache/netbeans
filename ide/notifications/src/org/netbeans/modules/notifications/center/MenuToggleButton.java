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
