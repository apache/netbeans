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
import java.awt.Insets;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.netbeans.lib.profiler.ui.UIUtils;

/**
 *
 * @author Jiri Sedlacek
 */
public class SmallButton extends JButton {
    
    protected static final Icon NO_ICON = new Icon() {
        public int getIconWidth() { return 0; }
        public int getIconHeight() { return 16; }
        public void paintIcon(Component c, Graphics g, int x, int y) {}
    };
    
    
    {
        setDefaultCapable(false);
        if (UIUtils.isWindowsLookAndFeel()) setOpaque(false);
    }
    
    
    public SmallButton() { this(null, null);  }

    public SmallButton(Icon icon) { this(null, icon); }

    public SmallButton(String text) { this(text, null); }

    public SmallButton(Action a) { super(a); }

    public SmallButton(String text, Icon icon) { super(text); setIcon(icon); }
    
    
    public void setIcon(Icon defaultIcon) {
        if (defaultIcon == null) {
            defaultIcon = NO_ICON;
            setIconTextGap(0);
        }
        super.setIcon(defaultIcon);
    }
    
    public Insets getMargin() {
        Insets margin = super.getMargin();
        if (margin != null) {
            if (getParent() instanceof JToolBar) {
                if (UIUtils.isNimbus()) {
                    margin.left = margin.top + 3;
                    margin.right = margin.top + 3;
                }
            } else {
                if (UIUtils.isNimbus()) {
                    margin.left = margin.top - 6;
                    margin.right = margin.top - 6;
                } else {
                    margin.left = margin.top + 3;
                    margin.right = margin.top + 3;
                }
            }
        }
        return margin;
    }
    
}
