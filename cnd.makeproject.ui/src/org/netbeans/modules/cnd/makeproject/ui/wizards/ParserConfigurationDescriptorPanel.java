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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.ProjectWizardPanels.NamedPanel;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel just asking for basic info.
 */
final class ParserConfigurationDescriptorPanel implements WizardDescriptor.Panel<WizardDescriptor>, NamedPanel, ChangeListener {

    private WizardDescriptor wizardDescriptor;
    private ParserConfigurationPanel component;
    private final String name;

    /** Create the wizard panel descriptor. */
    public ParserConfigurationDescriptorPanel() {
        name = NbBundle.getMessage(ParserConfigurationDescriptorPanel.class, "ParserConfigurationName"); // NOI18N
    }

    @Override
    public ParserConfigurationPanel getComponent() {
        if (component == null) {
            component = new ParserConfigurationPanel(this);
            component.setName(name);
        }
        return component;
    }

    @Override
    public String getName() {
        return name;
    }

    public WizardDescriptor getWizardDescriptor() {
        return wizardDescriptor;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("NewMakeWizardP4"); // NOI18N
    }

    @Override
    public boolean isValid() {
        boolean valid = getComponent().valid(wizardDescriptor);
        if (valid) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        }
        return valid;
    }
    private final Set<ChangeListener> listeners = new HashSet<>(1);

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            (it.next()).stateChanged(ev);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        getComponent().read(wizardDescriptor);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent().store(settings);
    }
}
