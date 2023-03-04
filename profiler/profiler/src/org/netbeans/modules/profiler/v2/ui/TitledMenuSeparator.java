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

package org.netbeans.modules.profiler.v2.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public class TitledMenuSeparator extends JPanel {
    
    public TitledMenuSeparator(String text) {
        setLayout(new BorderLayout());
        setOpaque(false);

        JLabel l = new JLabel(text);
        l.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        if (UIUtils.isWindowsLookAndFeel()) l.setOpaque(true);
        l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize2D() - 1));
        if (UIUtils.isWindowsLookAndFeel()) l.setForeground(UIUtils.getDisabledLineColor());
        
        add(l, BorderLayout.WEST);
        
        if (UIUtils.isGTKLookAndFeel()) {
            add(UIUtils.createHorizontalSeparator(), BorderLayout.CENTER);
        } else {
            JComponent sep = new JPopupMenu.Separator();
            add(sep, BorderLayout.CENTER);
            
            if (UIUtils.isOracleLookAndFeel()) {
                setOpaque(true);
                setBackground(sep.getBackground());
                l.setForeground(sep.getForeground());
            }
        }
    }
    
    public void doLayout() {
        super.doLayout();
        Component c = getComponent(1);
        
        int h = c.getPreferredSize().height;
        Rectangle b = c.getBounds();
        
        b.y = (b.height - h) / 2;
        b.height = h;
        c.setBounds(b);
    }

    public Dimension getPreferredSize() {
        Dimension d = getComponent(0).getPreferredSize();
        d.width += 25;
        return d;
    }

}
