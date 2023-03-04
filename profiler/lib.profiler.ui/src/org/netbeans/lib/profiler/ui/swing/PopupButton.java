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

package org.netbeans.lib.profiler.ui.swing;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;

/**
 *
 * @author Jiri Sedlacek
 */
public class PopupButton extends SmallButton {
    
    private static final Icon DROPDOWN_ICON = Icons.getIcon(GeneralIcons.POPUP_ARROW);
    private static final int DROPDOWN_ICON_WIDTH = DROPDOWN_ICON.getIconWidth();
    private static final int DROPDOWN_ICON_HEIGHT = DROPDOWN_ICON.getIconHeight();
    
    private int iconOffset;
    private int popupAlign = SwingConstants.LEADING;
    
    
    {
        if (UIUtils.isMetalLookAndFeel()) iconOffset = 6;
        else if (UIUtils.isNimbusLookAndFeel()) iconOffset = 8;
        else iconOffset = 7;
        
        setHorizontalAlignment(LEADING);
    }
    
    
    public PopupButton() { super(); }

    public PopupButton(Icon icon) { super(icon); }

    public PopupButton(String text) { super(text); }

    public PopupButton(Action a) { super(a); }

    public PopupButton(String text, Icon icon) { super(text, icon); }
    
    
    public void setPopupAlign(int align) {
        popupAlign = align;
    }
    
    public int getPopupAlign() {
        return popupAlign;
    }
    
    
    protected void fireActionPerformed(ActionEvent e) {
        super.fireActionPerformed(e);
        displayPopup();
    }
    
    protected void displayPopup() {
        JPopupMenu menu = new JPopupMenu();
        populatePopup(menu);
        if (menu.getComponentCount() > 0) {
            Dimension size = menu.getPreferredSize();
            size.width = Math.max(size.width, getWidth());
            menu.setPreferredSize(size);
            
            int align = getPopupAlign();
            
            int x;
            switch (align) {
                case SwingConstants.EAST:
                case SwingConstants.NORTH_EAST:
                case SwingConstants.SOUTH_EAST:
                    x = getWidth() - size.width;
                    break;
                default:
                    x = 0;
                    break;
            }
            
            int y;
            switch (align) {
                case SwingConstants.NORTH:
                case SwingConstants.NORTH_EAST:
                case SwingConstants.NORTH_WEST:
                    y = -size.height;
                    break;
                default:
                    y = getHeight();
                    break;
            }
            
            menu.show(this, x, y);
        }
    }
    
    protected void populatePopup(JPopupMenu popup) {
        // Implementation here
    }
    
    
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width += DROPDOWN_ICON_WIDTH + (isEmpty() ? 3 : 5);
        return size;
    }
    
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
    
    public void addNotify() {
        super.addNotify();
        if (UIUtils.isWindowsLookAndFeel() && getParent() instanceof JToolBar) {
            if (getIcon() == NO_ICON) setIconTextGap(2);
            iconOffset = 5;
        }
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (isEmpty()) {
            DROPDOWN_ICON.paintIcon(this, g, (getWidth() - DROPDOWN_ICON_WIDTH) / 2,
                                             (getHeight() - DROPDOWN_ICON_HEIGHT) / 2);
        } else {
            DROPDOWN_ICON.paintIcon(this, g, getWidth() - DROPDOWN_ICON_WIDTH - iconOffset,
                                            (getHeight() - DROPDOWN_ICON_HEIGHT) / 2);
        }
    }
    
    
    private boolean isEmpty() {
        if (getIcon() != NO_ICON) return false;
        String text = getText();
        return text == null || text.isEmpty();
    }
    
}
