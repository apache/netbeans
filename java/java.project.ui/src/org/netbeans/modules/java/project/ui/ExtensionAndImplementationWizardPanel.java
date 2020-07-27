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
package org.netbeans.modules.java.project.ui;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * @author Arthur Sadykov
 */
public class ExtensionAndImplementationWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private ExtensionAndImplementationVisualPanel component;
    private WizardDescriptor wizardDescriptor;
    private final Set<ChangeListener> listeners = new HashSet<>(1);

    @Override
    public Component getComponent() {
        if (component == null) {
            component = ExtensionAndImplementationVisualPanel.create(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(WizardDescriptor wizardDescriptor) {
        this.wizardDescriptor = wizardDescriptor;
        component.readSettings(wizardDescriptor);
    }

    @Override
    public void storeSettings(WizardDescriptor wizardDescriptor) {
        component.storeSettings(wizardDescriptor);
    }

    @Override
    public boolean isValid() {
        getComponent();
        return component.isValid(wizardDescriptor);
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    void fireChangeEvent() {
        ChangeEvent event = new ChangeEvent(this);
        synchronized (listeners) {
            listeners.forEach(listener -> {
                listener.stateChanged(event);
            });
        }
    }
}
