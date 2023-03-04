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

package org.netbeans.modules.profiler;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.ui.UIConstants;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jiri Sedlacek
 */
public class ProfilerTopComponent extends TopComponent {
    
    public static final String RECENT_FILE_KEY = "nb.recent.file.path"; // NOI18N
    
    private Component lastFocusOwner;
    
    private final PropertyChangeListener focusListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            Component c = evt.getNewValue() instanceof Component ?
                    (Component)evt.getNewValue() : null;
            processFocusedComponent(c);
        }
        private void processFocusedComponent(Component c) {
            Component cc = c;
            while (c != null) {
                if (c == ProfilerTopComponent.this) {
                    lastFocusOwner = cc;
                    return;
                }
                c = c.getParent();
            }
        }
    };
    
    protected void componentActivated() {
        super.componentActivated();
        if (lastFocusOwner != null) {
            lastFocusOwner.requestFocus();
        } else {
            Component defaultFocusOwner = defaultFocusOwner();
            if (defaultFocusOwner != null) defaultFocusOwner.requestFocus();
        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                addPropertyChangeListener("focusOwner", focusListener); // NOI18N
    }

    protected void componentDeactivated() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                removePropertyChangeListener("focusOwner", focusListener); // NOI18N
        super.componentDeactivated();
    }
    
    protected Component defaultFocusOwner() {
        return null;
    }
    
    public Dimension getMinimumSize() {
        return new Dimension(0, 0);
    }
    
    public void paintComponent(Graphics g) {
        Color background = UIManager.getColor(UIConstants.PROFILER_PANELS_BACKGROUND);
        if (background != null) {
            g.setColor(background);
            Insets i = getInsets();
            g.fillRect(i.left, i.top, getWidth() - i.left - i.right, getHeight() - i.top - i.bottom);
        } else {
            super.paintComponent(g);
        }
    }
    
}
