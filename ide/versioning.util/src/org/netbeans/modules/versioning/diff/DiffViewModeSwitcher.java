/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.versioning.diff;

import java.awt.Component;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.diff.DiffController;
import org.openide.util.WeakListeners;

/**
 *
 * @author Ondrej Vrabec
 */
public final class DiffViewModeSwitcher implements ChangeListener {

    private static final Map<Object, DiffViewModeSwitcher> INSTANCES = new WeakHashMap<>();

    private int diffViewMode = 0;
    private final Map<JComponent, ChangeListener> handledViews = new WeakHashMap<>();

    public static synchronized DiffViewModeSwitcher get(Object holder) {
        return INSTANCES.computeIfAbsent(holder, k -> new DiffViewModeSwitcher());
    }

    public void setupMode (DiffController view) {
        JTabbedPane tabPane = findTabbedPane(view.getJComponent());
        if (tabPane != null) {
            if (!handledViews.containsKey(tabPane)) {
                ChangeListener list = WeakListeners.change(this, tabPane);
                handledViews.put(tabPane, list);
                tabPane.addChangeListener(list);
            }
            if (tabPane.getTabCount() > diffViewMode) {
                tabPane.setSelectedIndex(diffViewMode);
            }
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source instanceof JTabbedPane) {
            JTabbedPane tabPane = (JTabbedPane) source;
            if (handledViews.containsKey(tabPane)) {
                diffViewMode = tabPane.getSelectedIndex();
            }
        }
    }

    private static JTabbedPane findTabbedPane(JComponent component) {
        if (component instanceof JTabbedPane && Boolean.TRUE.equals(component.getClientProperty("diff-view-mode-switcher"))) {
            return (JTabbedPane) component;
        } else {
            for (Component c : component.getComponents()) {
                if (c instanceof JComponent) {
                    JTabbedPane pane = findTabbedPane((JComponent) c);
                    if (pane != null) {
                        return pane;
                    }
                }
            }
        }
        return null;
    }

    public static synchronized void release(Object holder) {
        INSTANCES.remove(holder);
    }

}
