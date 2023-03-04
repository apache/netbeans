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

package org.netbeans.modules.project.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.openide.awt.CloseButtonFactory;
import org.openide.util.NbPreferences;
import org.openide.util.WeakListeners;

/**
 *
 * @author mkozeny
 */
public class NodeSelectionProjectPanel extends javax.swing.JPanel implements PreferenceChangeListener {

    public static final Preferences prefs = NbPreferences.forModule(NodeSelectionProjectPanel.class);

    public static final String KEY_ACTUALSELECTIONPROJECT = "enable.actualselectionproject";
    private boolean enabled;
    private boolean isMinimized;

    public static final int COMPONENT_HEIGHT = 22;
    private static final int BORDER_WIDTH = 1;
    
    /**
     * Creates new form ActualSelectionProjectPanel
     */
    public NodeSelectionProjectPanel() {
        super(new BorderLayout());
        JButton closeButton = CloseButtonFactory.createBigCloseButton();
        prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prefs.putBoolean(KEY_ACTUALSELECTIONPROJECT, false);
            }
        });
        add(closeButton, BorderLayout.EAST);

        setBorder(new SeparatorBorder());
        preferenceChange(null);
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt == null || KEY_ACTUALSELECTIONPROJECT.equals(evt.getKey())) {
            enabled = prefs.getBoolean(KEY_ACTUALSELECTIONPROJECT, false);
            updatePreferredSize();
        }
    }

    private void updatePreferredSize() {
        if (enabled) {
            setPreferredSize(new Dimension(Integer.MAX_VALUE, COMPONENT_HEIGHT + BORDER_WIDTH));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            isMinimized = false;
        } else {
            setPreferredSize(new Dimension(0, 0));
            setMaximumSize(new Dimension(0, 0));
            isMinimized = true;
        }
        revalidate();
    }
    
    void minimize() {
        setPreferredSize(new Dimension(0, 0));
        setMaximumSize(new Dimension(0, 0));
        isMinimized = true;
        revalidate();
    }
    
    void maximize() {
        setPreferredSize(new Dimension(Integer.MAX_VALUE, COMPONENT_HEIGHT + BORDER_WIDTH));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        isMinimized = false;
        revalidate();
    }
    
    public boolean isMinimized() {
        return isMinimized;
    }

    private static final class SeparatorBorder implements Border {

        private static final int BORDER_WIDTH = 1;
        private final Insets INSETS = new Insets(BORDER_WIDTH, 0, 0, 0);

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color originalColor = g.getColor();
            g.setColor(UIManager.getColor("controlShadow")); //NOI18N
            g.drawLine(0, 0, c.getWidth(), 0);
            g.setColor(originalColor);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }
}
