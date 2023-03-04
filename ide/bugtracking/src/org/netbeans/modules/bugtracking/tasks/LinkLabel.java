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
package org.netbeans.modules.bugtracking.tasks;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import org.openide.util.Utilities;

/**
 *
 * @author jpeska
 */
public abstract class LinkLabel extends JLabel implements MouseListener {

    private static final Color FOREGROUND_COLOR = UIManager.getColor("Label.foreground");
    private static final Color FOREGROUND_FOCUS_COLOR = UIManager.getColor("nb.html.link.foreground") != null ? UIManager.getColor("nb.html.link.foreground") : new Color(0x164B7B);
    private final Icon icon;

    private Action[] popupActions = new Action[0];

    public LinkLabel(String text, Icon icon) {
        this.icon = icon;
        if (!text.isEmpty()) {
            setText(text);
        }
        init();
    }

    private void init() {
        if (icon != null) {
            setIcon(icon);
        }
        setForeground(FOREGROUND_COLOR);
        addMouseListener(this);
    }

    public void setPopupActions(Action... popupActions) {
        this.popupActions = popupActions;
    }

    @Override
    public abstract void mouseClicked(MouseEvent e);

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e.getPoint());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e.getPoint());
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(FOREGROUND_FOCUS_COLOR);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        setForeground(FOREGROUND_COLOR);
    }

    private void showPopup(Point p) {
        if (popupActions.length > 0) {
            JPopupMenu menu = Utilities.actionsToPopup(popupActions, this);
            menu.show(this, p.x, p.y);
        }
    }
}
