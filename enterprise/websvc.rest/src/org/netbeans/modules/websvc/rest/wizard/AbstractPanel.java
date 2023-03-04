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
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;

/**
 *
 * @author nam
 */
public abstract class AbstractPanel implements ChangeListener, FinishablePanel, 
    Panel 
{
    private final CopyOnWriteArrayList<ChangeListener> listeners = 
        new CopyOnWriteArrayList<ChangeListener>();
    protected java.lang.String panelName;
    protected org.openide.WizardDescriptor wizardDescriptor;

    public AbstractPanel (String name, WizardDescriptor wizardDescriptor) {
        this.panelName = name;
        this.wizardDescriptor = wizardDescriptor;
    }
    
    @Override
    public abstract java.awt.Component getComponent();

    @Override
    public abstract boolean isFinishPanel();

    public static interface Settings {
        void read(WizardDescriptor wizard);
        void store(WizardDescriptor wizard);
        boolean valid(WizardDescriptor wizard);
        void addChangeListener(ChangeListener l);
    }
    
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        ((Settings)getComponent()).read(wizardDescriptor);
    }
    
    @Override
    public void storeSettings(Object settings) {
        ((Settings)getComponent()).store(wizardDescriptor);
    }

    @Override
    public boolean isValid() {
        if (getComponent() instanceof Settings) {
            return ((Settings)getComponent()).valid(wizardDescriptor);
        }
        return false;
    }
    
    @Override
    public final void addChangeListener(ChangeListener l) {
        listeners.add( l );
    }

    protected final void fireChangeEvent(ChangeEvent ev) {
        for ( ChangeListener listener : listeners ){
            listener.stateChanged(ev);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    public static void clearMessage(WizardDescriptor wizard, MessageType type) {
        setMessage(wizard, type, (String) null);
    }
    
    public static void clearErrorMessage(WizardDescriptor wizard) {
        setMessage(wizard, MessageType.ERROR, (String) null);
    }
    
    public static void clearInfoMessage(WizardDescriptor wizard) {
        setMessage(wizard, MessageType.INFO, (String) null);
    }
    
    public static void setMessage(WizardDescriptor wizard, Throwable t, MessageType type) {
        String message = "";
        if (t != null) {
            message = (t.getLocalizedMessage());
        }
        wizard.putProperty(type.getName(), message);
    }
    
    static void setMessage(WizardDescriptor wizard, MessageType type, String key, String... params) {
        String message = "";
        if (key != null) {
            message = (NbBundle.getMessage(AbstractPanel.class, key, params));
        }
        wizard.putProperty(type.getName(), message);
    }
    
    public static void setMessage(WizardDescriptor wizard, MessageType type, String key) {
        String message = "";
        if (key != null) {
            message = (NbBundle.getMessage(AbstractPanel.class, key));
        }
        wizard.putProperty(type.getName(), message);
    }

    protected void setMessage(MessageType type, java.lang.String key) {
        setMessage(wizardDescriptor, type, key);
    }
    
    public static void setErrorMessage(WizardDescriptor wizard, String key) {
        setMessage(wizard, MessageType.ERROR, key);
    }

    protected void setErrorMessage(java.lang.String key) {
        setMessage(wizardDescriptor, MessageType.ERROR, key);
    }
    
    public static void setInfoMessage(WizardDescriptor wizard, String key) {
        setMessage(wizard, MessageType.INFO, key);
    }

    protected void setInfoMessage(java.lang.String key) {
        setMessage(wizardDescriptor, MessageType.INFO, key);
    }

    @Override
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        Component c = getComponent();
        if (c instanceof Settings) {
            ((Settings)c).valid(wizardDescriptor);
        }
        fireChangeEvent(e);
    }
    
    public String getName() {
        return panelName;
    }
    
    public enum MessageType {
        INFO(WizardDescriptor.PROP_INFO_MESSAGE),
        WARNING(WizardDescriptor.PROP_WARNING_MESSAGE),
        ERROR(WizardDescriptor.PROP_ERROR_MESSAGE);
                
        private String name;
        
        MessageType(String name) {
            this.name = name;
        }
        
        public String getName() {
            return this.name;
        }
    }
}
