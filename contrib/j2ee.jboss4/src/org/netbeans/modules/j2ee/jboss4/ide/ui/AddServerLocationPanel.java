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
package org.netbeans.modules.j2ee.jboss4.ide.ui;

import java.io.File;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.jboss4.ide.ui.JBPluginUtils.Version;
import org.openide.util.NbBundle;

/**
 *
 * @author Ivan Sidorkin
 */
public class AddServerLocationPanel implements WizardDescriptor.Panel, ChangeListener {
    
    private JBInstantiatingIterator instantiatingIterator;
    
    private AddServerLocationVisualPanel component;
    private WizardDescriptor wizard;
    private transient Set listeners = new HashSet(1);
    
    public AddServerLocationPanel(JBInstantiatingIterator instantiatingIterator){
        this.instantiatingIterator = instantiatingIterator;
    }
    
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }
    
    private void fireChangeEvent(ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new AddServerLocationVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx("j2eeplugins_registering_app_server_jboss_location"); //NOI18N
    }

    @Override
    public boolean isValid() {
        String locationStr = component.getInstallLocation();
        if (locationStr == null || locationStr.trim().length() < 1) {
            wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE,
                    NbBundle.getMessage(AddServerLocationPanel.class, "MSG_SpecifyServerLocation")); // NOI18N
            return false;
        }

        File f = new File(locationStr);
        Version version = JBPluginUtils.getServerVersion(new File(locationStr));

        if (!JBPluginUtils.isGoodJBServerLocation(f, version)) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddServerLocationPanel.class, "MSG_InvalidServerLocation")); // NOI18N
            return false;
        }

        if (version != null
                && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_7_0_0) >= 0
                && version.compareToIgnoreUpdate(JBPluginUtils.JBOSS_7_1_0) < 0) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(AddServerLocationPanel.class, "MSG_InvalidJBoss7Versio")); // NOI18N
            return false;
        }
                
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        JBPluginProperties.getInstance().setInstallLocation(component.getInstallLocation());
        JBPluginProperties.getInstance().saveProperties();
        instantiatingIterator.setInstallLocation(locationStr);
        return true;
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    public void readSettings(Object settings) {
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
    }
    
    public void storeSettings(Object settings) {
        instantiatingIterator.setInstallLocation(
                ((AddServerLocationVisualPanel) getComponent()).getInstallLocation());
    }
}
