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

package org.netbeans.modules.j2ee.weblogic9.ui.wizard;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author Petr Hejl
 */
public class ServerLocalPropertiesPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    private final AtomicBoolean isValidating = new AtomicBoolean();

    private ServerLocalPropertiesVisual component;

    private WizardDescriptor wizard;

    private transient WLInstantiatingIterator instantiatingIterator;

    public ServerLocalPropertiesPanel (WLInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ServerLocalPropertiesVisual(instantiatingIterator);
            component.addChangeListener(this);
        }
        return component;
    }

    public  ServerLocalPropertiesVisual getVisual() {
        return (ServerLocalPropertiesVisual) getComponent();
    }

    @Override
    public HelpCtx getHelp() {
         return new HelpCtx("j2eeplugins_registering_app_server_weblogic_properties_local"); // NOI18N
    }

    @Override
    public boolean isValid() {
        if (isValidating.compareAndSet(false, true)) {
            try {
                return getVisual().valid(wizard);
            } finally {
                isValidating.set(false);
            }
        }
        return true;
    }

    @Override
    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
    }

    @Override
    public void storeSettings(Object settings) {

    }

    /**
     * Adds a listener
     *
     * @param listener the listener to be added
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a registered listener
     *
     * @param listener the listener to be removed
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void stateChanged(ChangeEvent event) {
        fireChangeEvent(event);
    }

    private void fireChangeEvent(ChangeEvent event) {
        for (ChangeListener listener : listeners) {
            listener.stateChanged(event);
        }
    }
}
