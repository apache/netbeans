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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 *
 * @author thuy
 */
public class ServerLocationPanel  implements WizardDescriptor.Panel, ChangeListener {

    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<ChangeListener>();

    private ServerLocationVisual component;

    private WizardDescriptor wizard;

    private transient WLInstantiatingIterator instantiatingIterator;

    public ServerLocationPanel(WLInstantiatingIterator instantiatingIterator) {
        this.instantiatingIterator = instantiatingIterator;
    }

    public Component getComponent() {
         if (component == null) {
            component = new ServerLocationVisual(instantiatingIterator);
            component.addChangeListener(this);
        }
        return component;
    }

     private ServerLocationVisual getVisual() {
        return (ServerLocationVisual) getComponent();
    }

    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_weblogic_location"); // NOI18N
    }

    public boolean isValid() {
        return getVisual().valid(wizard);
    }

    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
    }

    public void storeSettings(Object settings) {
    }

    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    private void fireChangeEvent(ChangeEvent event) {
        for (ChangeListener l : listeners) {
            l.stateChanged(event);
        }
    }

    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }
}
