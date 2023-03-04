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
package org.netbeans.core.output2.options;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.openide.windows.IOContainer;
import org.openide.windows.IOContainer.CallBacks;

/**
 *
 * @author jhavlin
 */
public class PreviewIOProvider implements IOContainer.Provider {

    JPanel panel;

    public PreviewIOProvider(JPanel panel) {
        this.panel = panel;
    }

    @Override
    public void open() {
    }

    @Override
    public void requestActive() {
        panel.requestFocusInWindow();
    }

    @Override
    public void requestVisible() {
        panel.requestFocusInWindow();
    }

    @Override
    public boolean isActivated() {
        return panel.hasFocus();
    }

    @Override
    public void add(JComponent comp, CallBacks cb) {
        panel.add(comp);
    }

    @Override
    public void remove(JComponent comp) {
        panel.remove(comp);
    }

    @Override
    public void select(JComponent comp) {
    }

    @Override
    public JComponent getSelected() {
        return (JComponent) panel.getComponent(0);
    }

    @Override
    public void setTitle(JComponent comp, String name) {
    }

    @Override
    public void setToolTipText(JComponent comp, String text) {
    }

    @Override
    public void setIcon(JComponent comp, Icon icon) {
    }

    @Override
    public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
    }

    @Override
    public boolean isCloseable(JComponent comp) {
        return false;
    }
}
