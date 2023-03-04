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
package org.netbeans.modules.nativeexecution.support;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JPanel;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ui.util.ValidatablePanelListener;
import org.netbeans.modules.nativeexecution.api.ui.util.ValidateablePanel;
import org.netbeans.modules.nativeexecution.spi.ui.HostPropertiesPanelProvider;
import org.openide.util.Lookup;

/**
 * A Composite panel that contains all panels provided by
 * {@link HostPropertiesPanelProvider}s.
 *
 * <p>The panel has
 * <code>GridBagLayout</code> layout and all panels are placed one under another
 * (in the order, of providers registration)</p>
 *
 * @author akrasny
 */
public final class HostConfigurationPanel extends ValidateablePanel {

    private final ValidatablePanelListener listener;
    private final List<ValidateablePanel> panels;

    public HostConfigurationPanel(final ExecutionEnvironment env) {
        panels = new ArrayList<>();

        GridBagConstraints gridBagConstraints;
        GridBagLayout layout = new GridBagLayout();
        setLayout(layout);

        listener = new ValidatablePanelListener() {
            @Override
            public void stateChanged(ValidateablePanel src) {
                fireChange();
            }
        };

        addPropertyChangeListener("ExecutionEnvironment", new PropertyChangeListener() { // NOI18N
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                for (ValidateablePanel panel : panels) {
                    panel.putClientProperty(evt.getPropertyName(), evt.getNewValue());
                }
            }
        });

        Collection<? extends HostPropertiesPanelProvider> allProviders =
                Lookup.getDefault().lookupAll(HostPropertiesPanelProvider.class);

        int y = 0;

        for (HostPropertiesPanelProvider provider : allProviders) {
            ValidateablePanel panel = provider.getHostPropertyPanel(env);
            panels.add(panel);
            panel.addValidationListener(listener);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = y++;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            add(panel, gridBagConstraints);
        }

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = y++;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(new JPanel(), gridBagConstraints);
    }

    @Override
    public boolean hasProblem() {
        for (ValidateablePanel panel : panels) {
            if (panel.hasProblem()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getProblem() {
        for (ValidateablePanel panel : panels) {
            String problem = panel.getProblem();
            if (problem != null) {
                return problem;
            }
        }
        return null;
    }

    @Override
    public void applyChanges(Object customData) {
        for (ValidateablePanel panel : panels) {
            panel.applyChanges(customData);
        }
    }
}
