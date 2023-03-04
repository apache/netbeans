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

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 * JPopupMenu which supports custom background color.
 *
 * @author Jiri Sedlacek
 */
public class ProfilerPopupMenu extends JPopupMenu {
    
    private boolean forceBackground;
    
    
    public ProfilerPopupMenu() {
        super();
    }

    public ProfilerPopupMenu(String label) {
        super(label);
    }
    
    
    // --- Tweaking UI ---------------------------------------------------------
    
    public JMenuItem add(JMenuItem menuItem) {
        if (forceBackground && !UIUtils.isOracleLookAndFeel()) menuItem.setOpaque(false);
        return super.add(menuItem);
    }
    
    public void add(Component comp, Object constraints) {
        if (forceBackground && !UIUtils.isOracleLookAndFeel() && comp instanceof JComponent)
            ((JComponent)comp).setOpaque(false);
        comp.setMinimumSize(comp.getPreferredSize());
        super.add(comp, constraints);
    }
    
    
    public void setForceBackground(boolean force) {
        if (!UIUtils.isNimbus() || !Boolean.TRUE.equals(UIManager.getBoolean("nb.dark.theme"))) // NOI18N
            this.forceBackground = force;
    }
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (forceBackground) {
            g.setColor(getBackground());
            g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
        }
    }
    
}
