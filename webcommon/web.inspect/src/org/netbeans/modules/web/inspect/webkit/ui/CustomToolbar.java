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
package org.netbeans.modules.web.inspect.webkit.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.util.Collection;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

public class CustomToolbar extends Box {

    private static final Dimension space = new Dimension(4, 0);
    private JToolBar toolbar;

    public CustomToolbar() {
        super(BoxLayout.X_AXIS);
        initPanel();
    }

    private void initPanel() {
        setBorder(new EmptyBorder(1, 2, 1, 2));

        // configure toolbar
        toolbar = new NoBorderToolBar(JToolBar.HORIZONTAL);
        toolbar.setFloatable(false);
        toolbar.setRollover(true);
        toolbar.setBorderPainted(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder());
        toolbar.setOpaque(false);
        toolbar.setFocusable(false);

        add(toolbar);
    }

    public void addButtons(Collection<AbstractButton> buttons) {
        for (AbstractButton button : buttons) {
            addButton(button);
        }
    }

    public void addButton(AbstractButton button) {
        Icon icon = button.getIcon();
        Dimension size = new Dimension(icon.getIconWidth() + 6, icon.getIconHeight() + 10);
        button.setMinimumSize(size);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMargin(new Insets(5, 4, 5, 4));
        toolbar.add(button);
    }
    
    public void addSpaceSeparator() {
        toolbar.addSeparator(space);
    }
    
    public void addLineSeparator() {
        toolbar.addSeparator(space);
        toolbar.add(new JSeparator(JSeparator.VERTICAL));
        toolbar.addSeparator(space);
    }

    /**
     * ToolBar that doesn't paint any border.
     *
     * @author S. Aubrecht
     */
    public static class NoBorderToolBar extends JToolBar {

        /**
         * Creates a new instance of NoBorderToolbar
         */
        public NoBorderToolBar() {
        }

        /**
         * Creates a new instance of NoBorderToolbar
         *
         * @param layout
         */
        public NoBorderToolBar(int layout) {
            super(layout);
        }

        @Override
        protected void paintComponent(Graphics g) {
        }
    }
}
