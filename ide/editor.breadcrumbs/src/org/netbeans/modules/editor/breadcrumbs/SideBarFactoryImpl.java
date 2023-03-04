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
package org.netbeans.modules.editor.breadcrumbs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.SideBarFactory;
import org.openide.awt.CloseButtonFactory;
import org.openide.explorer.ExplorerManager;
import org.openide.util.WeakListeners;

/**
 *
 * @author lahvac
 */
public class SideBarFactoryImpl implements SideBarFactory {

    public static final String KEY_BREADCRUMBS = "enable.breadcrumbs";
    public static final boolean DEF_BREADCRUMBS = true;
    
    @Override
    public JComponent createSideBar(JTextComponent target) {
        final Document doc = target.getDocument();
        
        return new SideBar(doc);
    }
    
    private static final class SideBar extends JPanel implements ExplorerManager.Provider, PreferenceChangeListener {
        private final Document forDocument;
        private final Preferences prefs;
        private boolean enabled;

        public SideBar(Document forDocument) {
            super(new BorderLayout());
            this.forDocument = forDocument;
            add(new BreadCrumbComponent(), BorderLayout.CENTER);

            JButton closeButton = CloseButtonFactory.createBigCloseButton();

            add(closeButton, BorderLayout.EAST);
            
            prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            prefs.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, prefs));
            
            closeButton.addActionListener(new ActionListener() {
                @Override public void actionPerformed(ActionEvent e) {
                    prefs.putBoolean(KEY_BREADCRUMBS, false);
                }
            });
            
            setBorder(new SeparatorBorder());
            preferenceChange(null);
        }
        
        @Override public ExplorerManager getExplorerManager() {
            return HolderImpl.get(forDocument).getManager();
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (evt == null || KEY_BREADCRUMBS.equals(evt.getKey())) {
                enabled = prefs.getBoolean(KEY_BREADCRUMBS, DEF_BREADCRUMBS);
                updatePreferredSize();
            }
        }
        
        private void updatePreferredSize() {
            if (enabled) {
                setPreferredSize(new Dimension(Integer.MAX_VALUE, BreadCrumbComponent.COMPONENT_HEIGHT + SeparatorBorder.BORDER_WIDTH));
                setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            }else{
                setPreferredSize(new Dimension(0,0));
                setMaximumSize(new Dimension(0,0));
            }
            revalidate();
        }
    }
    
    private static final class SeparatorBorder implements Border {
        private static final int BORDER_WIDTH = 1;
        private final Insets INSETS = new Insets(BORDER_WIDTH, 0, 0, 0);

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Color originalColor = g.getColor();
            g.setColor (UIManager.getColor ("controlShadow")); //NOI18N
            g.drawLine(0, 0, c.getWidth(), 0);
            g.setColor(originalColor);
        }

        @Override public Insets getBorderInsets(Component c) {
            return INSETS;
        }

        @Override public boolean isBorderOpaque() {
            return true;
        }
    }

}
