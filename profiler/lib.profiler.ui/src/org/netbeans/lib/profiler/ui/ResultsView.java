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
package org.netbeans.lib.profiler.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Jiri Sedlacek
 */
public class ResultsView extends JPanel {
    
    private Component mainToolbar;
    private Component currentToolbar;
    private final List<Component> toolbars = new ArrayList<>();
    
    private JTabbedPane tabs;
    
    private Component firstView;
    private String firstName;
    private Icon firstIcon;
    private String firstDescription;
    
    private final Set<ChangeListener> listeners = new HashSet<>();
    
    
    public ResultsView() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout(0, 0));
    }
    
    
    public final void setMainToolbar(Component toolbar) {
        if (mainToolbar == toolbar) return;
        mainToolbar = toolbar;
        setToolbar(mainToolbar);
    }
    
    
    public final void addView(String name, Icon icon, String description, Component view, Component toolbar) {
        if (view == null) return;
        if (tabs == null) {
            if (firstView == null) {
                firstView = view;
                firstName = name;
                firstIcon = icon;
                firstDescription = description;
                toolbars.add(toolbar);
                add(view, BorderLayout.CENTER);
                setToolbar(toolbar);
                fireViewOrIndexChanged();
            } else {
                remove(firstView);
                toolbars.add(toolbar);
                tabs = createTabs();
                tabs.addTab(firstName, firstIcon, firstView, firstDescription);
                tabs.addTab(name, icon, view, description);
                add(tabs, BorderLayout.CENTER);
                firstView = null;
                firstName = null;
                firstIcon = null;
                firstDescription = null;
            }
        } else {
            if (tabs.indexOfComponent(view) != -1) return;
            tabs.addTab(name, icon, view, description);
            toolbars.add(toolbar);
        }
    }
    
    public final void removeView(Component view) {
        if (view == null) return;
        if (tabs != null) {
            int viewIndex = tabs.indexOfComponent(view);
            if (viewIndex == -1) return;
            if (tabs.getTabCount() > 2) {
                toolbars.remove(viewIndex);
                tabs.remove(view);
            } else {
                tabs.remove(view);
                firstView = tabs.getComponentAt(0);
                firstName = tabs.getTitleAt(0);
                firstIcon = tabs.getIconAt(0);
                firstDescription = tabs.getToolTipTextAt(0);
                remove(tabs);
                add(firstView);
                setToolbar(toolbars.get(0));
                tabs = null;
            }
        } else if (firstView == view) {
            remove(firstView);
            setToolbar(null);
            toolbars.clear();
            firstView = null;
            firstName = null;
            firstIcon = null;
            firstDescription = null;
            fireViewOrIndexChanged();
        }
    }
    
    public final void removeViews() {
        if (getViewsCount() == 0) return;
        setToolbar(null);
        removeAll();
        toolbars.clear();
        tabs = null;
        firstView = null;
        firstName = null;
        firstIcon = null;
        firstDescription = null;
        fireViewOrIndexChanged();
    }
    
    public final void setViewName(Component view, String name) {
        int viewIndex = getViewIndex(view);
        if (viewIndex == -1) return;
        if (tabs == null) firstName = name;
        else tabs.setTitleAt(viewIndex, name);
    }
    
    public final String getViewName(Component view) {
        int viewIndex = getViewIndex(view);
        if (viewIndex == -1) return null;
        if (tabs == null) return firstName;
        else return tabs.getTitleAt(viewIndex);
    }
    
    public final void setViewEnabled(Component view, boolean enabled) {
        int viewIndex = getViewIndex(view);
        if (viewIndex == -1) return;
        if (tabs != null) tabs.setEnabledAt(viewIndex, enabled);
        // TODO: handle single view (no tabs) - introduce firstEnabled?
    }
    
    public final boolean isViewEnabled(Component view) {
        int viewIndex = getViewIndex(view);
        if (viewIndex == -1) return false;
        return tabs == null ? false : tabs.isEnabledAt(viewIndex);
        // TODO: handle single view (no tabs) - introduce firstEnabled?
    }
    
    public final void selectView(Component view) {
        if (tabs == null) return;
        tabs.setSelectedComponent(view);
    }
    
    public final void selectView(int index) {
        if (tabs == null) return;
        tabs.setSelectedIndex(index);
    }
    
    public final void selectPreviousView() {
        if (tabs == null) return;
        int index = UIUtils.getPreviousSubTabIndex(tabs, tabs.getSelectedIndex());
        tabs.setSelectedIndex(index);
    }
    
    public final void selectNextView() {
        if (tabs == null) return;
        int index = UIUtils.getNextSubTabIndex(tabs, tabs.getSelectedIndex());
        tabs.setSelectedIndex(index);
    }
    
    public final Component getSelectedView() {
        return tabs != null ? tabs.getSelectedComponent() :
               (firstView != null ? firstView : null);
    }
    
    public final int getSelectedViewIndex() {
        return tabs != null ? tabs.getSelectedIndex() :
               (firstView != null ? 0 : -1);
    }
    
    public final int getViewsCount() {
        return tabs != null ? tabs.getTabCount() :
               (firstView != null ? 1 : 0);
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
    
    
    private int getViewIndex(Component view) {
        if (view == null) return -1;
        if (tabs != null) {
            return tabs.indexOfComponent(view);
        } else {
            return view == firstView ? 0 : -1;
        }
    }
    
    private void setToolbar(Component toolbar) {
        if (currentToolbar == toolbar) return;
        if (currentToolbar != null) remove(currentToolbar);
        if (toolbar != null) currentToolbar = toolbar;
        else currentToolbar = mainToolbar;
        if (currentToolbar != null) add(currentToolbar, BorderLayout.NORTH);
        revalidate();
        repaint();
    }
    
    private void updateToolbar() {
        setToolbar(toolbars.get(getSelectedViewIndex()));
    }
    
    private JTabbedPane createTabs() {
        JTabbedPane tp = new JTabbedPane(JTabbedPane.BOTTOM) {
            protected final void fireStateChanged() {
                super.fireStateChanged();
                updateToolbar();
                fireViewOrIndexChanged();
            }
        };
        tp.setOpaque(false);
        if (UIUtils.isAquaLookAndFeel()) {
            tp.setBorder(BorderFactory.createEmptyBorder(-13, -11, 0, -10));
        } else {
            tp.setBorder(BorderFactory.createEmptyBorder());
            Insets i = UIManager.getInsets("TabbedPane.contentBorderInsets"); // NOI18N
            if (i != null) tp.setBorder(BorderFactory.createEmptyBorder(-i.top, -i.left, 0, -i.right));
        }
        
        // Fix for Issue 115062 (CTRL-PageUp/PageDown should move between snapshot tabs)
        tp.getActionMap().getParent().remove("navigatePageUp"); // NOI18N
        tp.getActionMap().getParent().remove("navigatePageDown"); // NOI18N
        
        // support for traversing subtabs using Ctrl-Alt-PgDn/PgUp
        getActionMap().put("PreviousViewAction", new AbstractAction() { // NOI18N
            public void actionPerformed(ActionEvent e) { selectPreviousView(); }
        });
        getActionMap().put("NextViewAction", new AbstractAction() { // NOI18N
            public void actionPerformed(ActionEvent e) { selectNextView(); }
        });
        
        return tp;
    }
    
}
