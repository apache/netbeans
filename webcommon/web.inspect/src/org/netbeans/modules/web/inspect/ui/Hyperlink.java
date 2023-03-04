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
package org.netbeans.modules.web.inspect.ui;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Action;
import javax.swing.JLabel;

/**
 * Component that works like a hyperlink, it shows an underlined text
 * and performs some action when it is clicked.
 * 
 * @author Jan Stola
 */
public class Hyperlink extends JLabel {
    /** Action that should be performed when the link is clicked. */
    private Action action;

    /**
     * Creates a new {@code Hyperlink}.
     */
    public Hyperlink() {
        addMouseListener(new Listener());
    }

    @Override
    public void setText(String text) {
        super.setText("<html><u>"+text); // NOI18N
    }

    /**
     * Sets the action that should be performed when this component is clicked.
     * 
     * @param action action that should be performed when this component is clicked.
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * Listener for mouse events. It updates the mouse cursor and is responsible
     * for execution of the specified action when the component is clicked.
     */
    class Listener extends MouseAdapter {

        @Override
        public void mouseEntered(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public void mouseExited(MouseEvent e) {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            action.actionPerformed(new ActionEvent(Hyperlink.this, ActionEvent.ACTION_PERFORMED, null));
        }

    }
    
}
