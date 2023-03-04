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

package org.netbeans.modules.server.ui.wizard;

import java.awt.Component;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.server.ServerRegistry;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Andrei Badea
 * @author Petr Hejl
 */
class ServerWizardPanel implements WizardDescriptor.Panel, ChangeListener {

    private final CopyOnWriteArrayList<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    private ServerWizardVisual component;
    private ServerRegistry registry;
    
    public ServerWizardPanel(ServerRegistry registry) {
        super();
        assert registry != null;
        this.registry = registry;
    }

    public Component getComponent() {
        if (component == null) {
            component = new ServerWizardVisual(registry);
            component.addChangeListener(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public void readSettings(Object settings) {
        getVisual().read((AddServerInstanceWizard) settings);
    }

    public void storeSettings(Object settings) {
        getVisual().store((AddServerInstanceWizard) settings);
    }

    public boolean isValid() {
        return getVisual().hasValidData();
    }

    public void addChangeListener(ChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(ChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    public void stateChanged(ChangeEvent event) {
        fireChange(event);
    }

    private void fireChange(ChangeEvent event) {
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }

    private ServerWizardVisual getVisual() {
        return (ServerWizardVisual) getComponent();
    }
}
