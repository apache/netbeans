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

package org.netbeans.modules.tomcat5.ui.wizard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

/**
 * Add Tomcat wizard descriptor panel implementation.
 */
class InstallPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List listeners = new ArrayList();
    private WizardDescriptor wizard;
    private InstallPanelVisual component;

    public InstallPanel() {
        super();
    }

    @Override
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    @Override
    public void storeSettings(Object settings) {
    }

    @Override
    public void readSettings(Object settings) {
        wizard = (WizardDescriptor)settings;
    }

    @Override
    public boolean isValid() {
        boolean result = getVisual().hasValidData();
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(
                getVisual().isInfoMessage() ? WizardDescriptor.PROP_INFO_MESSAGE : WizardDescriptor.PROP_ERROR_MESSAGE,
                getVisual().getErrorMessage());
        return result;
    }

    @Override
    public java.awt.Component getComponent() {
        if (component == null) {
            component = new InstallPanelVisual();
            component.addChangeListener(this);
        }

        return component;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx("tomcat_addinstall"); // NOI18N
    }

    @Override
    public void stateChanged(javax.swing.event.ChangeEvent event) {
        fireChange(event);
    }

    public InstallPanelVisual getVisual() {
        return (InstallPanelVisual)getComponent();
    }

    private void fireChange(ChangeEvent event) {
        ArrayList tempList;

        synchronized (listeners) {
            tempList = new ArrayList(listeners);
        }

        Iterator iter = tempList.iterator();
        while (iter.hasNext()) {
            ((ChangeListener)iter.next()).stateChanged(event);
        }
    }
}
