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

package org.netbeans.modules.navigator;

import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.netbeans.spi.navigator.NavigatorPanelWithToolbar;
import org.netbeans.spi.navigator.NavigatorPanelWithUndo;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;

/**
 * Delegating panel for use from {@link NavigatorPanel.Registration}.
 */
public class LazyPanel implements NavigatorPanelWithUndo, NavigatorPanelWithToolbar {

    /**
     * Referenced from generated layer.
     */
    public static NavigatorPanel create(Map<String,?> attrs) {
        return new LazyPanel(attrs);
    }

    private final Map<String,?> attrs;
    private NavigatorPanel delegate;

    private LazyPanel(Map<String,?> attrs) {
        this.attrs = attrs;
    }

    private synchronized NavigatorPanel initialize() {
        if (delegate == null) {
            delegate = (NavigatorPanel) attrs.get("delegate");
        }
        return delegate;
    }

    @Override public String getDisplayName() {
        if (delegate != null) {
            return delegate.getDisplayName();
        } else {
            return (String) attrs.get("displayName");
        }
    }

    @Override public String getDisplayHint() {
        if (delegate != null) {
            return delegate.getDisplayHint();
        } else { // unused currently, so no separate attr
            return (String) attrs.get("displayName");
        }
    }

    @Override public JComponent getComponent() {
        return initialize().getComponent();
    }

    @Override public void panelActivated(Lookup context) {
        initialize().panelActivated(context);
    }

    @Override public void panelDeactivated() {
        initialize().panelDeactivated();
    }

    @Override public Lookup getLookup() {
        return initialize().getLookup();
    }

    @Override public UndoRedo getUndoRedo() {
        NavigatorPanel p = initialize();
        return p instanceof NavigatorPanelWithUndo ? ((NavigatorPanelWithUndo) p).getUndoRedo() : UndoRedo.NONE;
    }

    @Override
    public JComponent getToolbarComponent() {
        NavigatorPanel p = initialize();
        return p instanceof NavigatorPanelWithToolbar ? ((NavigatorPanelWithToolbar) p).getToolbarComponent() : null;
    }

    public boolean panelMatch(NavigatorPanel panel) {
        if (panel == null) {
            return false;
        }
        if (this.getClass().equals(panel.getClass())) {
            return super.equals(panel);
        }
        if (delegate != null) {
            return delegate.equals(panel);
        } else if (panel.getDisplayName().equals(attrs.get("displayName"))) {
            return initialize().equals(panel);
        } else {
            return false;
        }
    }
}
