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
package org.netbeans.modules.profiler.v2.impl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.lib.profiler.charts.swing.Utils;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.icons.GeneralIcons;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.v2.ProfilerFeature;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "FeaturesView_noData=No data collected yet. Click the {0} button in toolbar to start profiling."
})
public class FeaturesView extends JPanel {
    
    private JTabbedPane tabs;
    private final Component defaultView;
    
    private JLabel hintLabel;
    private final Color hintColor;
    
    private final Set<ChangeListener> listeners = new HashSet<>();
    
    
    public FeaturesView(Component defaultView, String buttonString) {
        if (UIUtils.isOracleLookAndFeel()) {
            setOpaque(true);
            setBackground(UIUtils.getProfilerResultsBackground());
        } else {
            setOpaque(false);
        }
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout(0, 0));
        
        if (defaultView != null) {
            JScrollPane sp = new JScrollPane(defaultView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
                public Dimension getMinimumSize() { return getPreferredSize(); }
            };
            sp.getVerticalScrollBar().setUnitIncrement(20);
            sp.setBorder(null);
            sp.setViewportBorder(null);

            this.defaultView = sp;
            add(this.defaultView, BorderLayout.CENTER);
        } else {
            this.defaultView = null;
        }
        
        if (buttonString != null) {
            hintLabel = new JLabel();
            hintLabel.setIcon(Icons.getIcon(GeneralIcons.INFO));
            hintLabel.setIconTextGap(hintLabel.getIconTextGap() + 1);
            hintLabel.setOpaque(false);
            
            Font font = new JToolTip().getFont();
            
            Color f = hintLabel.getForeground();
            int r = f.getRed() + 70;
            if (r > 255) r = f.getRed() - 70; else r = Math.min(r, 70);
            int g = f.getGreen() + 70;
            if (g > 255) g = f.getRed() - 70; else g = Math.min(g, 70);
            int b = f.getBlue() + 70;
            if (b > 255) b = f.getRed() - 70; else b = Math.min(b, 70);
            hintLabel.setText("<html><body text=\"rgb(" + r + ", " + g + ", " + b + ")\" style=\"font-size: " + //NOI18N
                              (font.getSize()) + "pt; font-family: " + font.getName() + ";\">" + //NOI18N
                              Bundle.FeaturesView_noData("<b>" + buttonString + "</b>") + "</body></html>"); //NOI18N
            
            hintLabel.setSize(hintLabel.getPreferredSize());
            
            Color c = UIUtils.getProfilerResultsBackground();
            hintColor = Utils.checkedColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 245));
        } else {
            hintColor = null;
        }
    }
    
    
    public final void resetNoDataHint() {
        hintLabel = null;
        repaint();
    }
    
    
    public final void addFeature(ProfilerFeature feature) {
        if (tabs == null) {
            if (defaultView != null) remove(defaultView);
            tabs = createTabs();
            add(tabs, BorderLayout.CENTER);
        }
        
        JPanel container = createContainer(feature);
        tabs.addTab(feature.getName(), feature.getIcon(), container, null);
        
        doLayout();
        repaint();
        fireViewOrIndexChanged();
    }
    
    private static final int XMAR = 40;
    private static final int YMAR = 40;
    
    private JPanel createContainer(ProfilerFeature feature) {
        JPanel container = new JPanel(new BorderLayout(0, 0));
        container.putClientProperty(ProfilerFeature.class, feature);
        
        JPanel results = feature.getResultsUI();
        JPanel xresults = new JPanel(new BorderLayout()) {
            public void paint(Graphics g) {
                super.paint(g);
                if (hintLabel != null) {
                    Dimension dim = hintLabel.getSize();
                    int x = (getWidth() - dim.width) / 2;
                    int y = (getHeight() - dim.height) / 2;
                    
                    g.setColor(hintColor);
                    g.fillRect(x - XMAR, y - YMAR, dim.width + XMAR * 2, dim.height + YMAR * 2);
                    g.setColor(Color.LIGHT_GRAY);
                    g.drawRect(x - XMAR, y - YMAR, dim.width + XMAR * 2, dim.height + YMAR * 2);
                    
                    g.translate(x, y);
                    hintLabel.paint(g);
                    g.translate(-x, -y);
                }
            }
        };
        xresults.add(results, BorderLayout.CENTER);
        container.add(xresults, BorderLayout.CENTER);
        
        JPanel settings = feature.getSettingsUI();
        if (settings != null) {
            JPanel pan = new JPanel(new BorderLayout(0, 0)) {
                public void setVisible(boolean visible) {
                    super.setVisible(visible);
                    for (Component c : getComponents()) c.setVisible(visible);
                }
            };
            pan.setOpaque(true);
            pan.setBackground(UIUtils.getProfilerResultsBackground());
            pan.add(settings, BorderLayout.CENTER);
            JSeparator sep = UIUtils.createHorizontalLine(pan.getBackground());
            pan.add(sep, BorderLayout.SOUTH);
            pan.setVisible(settings.isVisible());
            container.add(pan, BorderLayout.NORTH);
        }
        
        return container;
    }
    
    public final void removeFeature(ProfilerFeature feature) {
        if (tabs != null) {
            tabs.remove(feature.getResultsUI());
            doLayout();
            repaint();
            if (tabs.getTabCount() == 0) removeFeatures();
            else fireViewOrIndexChanged();
        }
    }
    
    public final void removeFeatures() {
        removeAll();
        tabs = null;
        if (defaultView != null) add(defaultView, BorderLayout.CENTER);
        doLayout();
        repaint();
        fireViewOrIndexChanged();
    }
    
    
    public final ProfilerFeature getSelectedFeature() {
        if (tabs == null) return null;
        JPanel container = (JPanel)tabs.getSelectedComponent();
        return (ProfilerFeature)container.getClientProperty(ProfilerFeature.class);
    }
    
    public final void selectFeature(ProfilerFeature feature) {
        if (tabs == null) return;
        for (Component c : tabs.getComponents())
            if (((JComponent)c).getClientProperty(ProfilerFeature.class) == feature)
                tabs.setSelectedComponent(c);
    }
    
    public final void selectFeature(int index) {
        if (tabs == null) return;
        tabs.setSelectedIndex(index);
    }
    
    public final void selectPreviousFeature() {
        if (tabs == null) return;
        int index = UIUtils.getPreviousSubTabIndex(tabs, tabs.getSelectedIndex());
        tabs.setSelectedIndex(index);
    }
    
    public final void selectNextFeature() {
        if (tabs == null) return;
        int index = UIUtils.getNextSubTabIndex(tabs, tabs.getSelectedIndex());
        tabs.setSelectedIndex(index);
    }
    
    
    public final void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }
    
    public final void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }
    
    public final void fireViewOrIndexChanged() {
        if (listeners.isEmpty()) return;
        ChangeEvent event = new ChangeEvent(this);
        for (ChangeListener listener : listeners)
            listener.stateChanged(event);
    }
    
    
    private JTabbedPane createTabs() {
        JTabbedPane tp = new JTabbedPane(JTabbedPane.BOTTOM) {
            protected final void fireStateChanged() {
                super.fireStateChanged();
                fireViewOrIndexChanged();
            }
        };
        tp.setOpaque(false);
        if (UIUtils.isAquaLookAndFeel()) {
            tp.setBorder(BorderFactory.createEmptyBorder(-13, -11, 0, -10));
        } else {
            tp.setBorder(BorderFactory.createEmptyBorder());
            Insets i = UIManager.getInsets("TabbedPane.contentBorderInsets"); // NOI18N
            int bottomOffset = 0;
            if (UIUtils.isMetalLookAndFeel()) {
                bottomOffset = -i.bottom + 1;
            } else if (UIUtils.isWindowsLookAndFeel()) {
                bottomOffset = -i.bottom;
            }
            if (i != null) tp.setBorder(BorderFactory.createEmptyBorder(-i.top, -i.left, bottomOffset, -i.right));
        }
        
        // Fix for Issue 115062 (CTRL-PageUp/PageDown should move between snapshot tabs)
        tp.getActionMap().getParent().remove("navigatePageUp"); // NOI18N
        tp.getActionMap().getParent().remove("navigatePageDown"); // NOI18N
        
        // support for traversing subtabs using Ctrl-Alt-PgDn/PgUp
        getActionMap().put("PreviousViewAction", new AbstractAction() { // NOI18N
            public void actionPerformed(ActionEvent e) { selectPreviousFeature(); }
        });
        getActionMap().put("NextViewAction", new AbstractAction() { // NOI18N
            public void actionPerformed(ActionEvent e) { selectNextFeature(); }
        });
        
        return tp;
    }
    
}
