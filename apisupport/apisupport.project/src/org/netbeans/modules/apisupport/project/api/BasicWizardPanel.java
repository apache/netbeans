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

package org.netbeans.modules.apisupport.project.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Basic wizard panel for APISupport projects.
 *
 * @author Martin Krauskopf
 */
public abstract class BasicWizardPanel implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {
    
    private boolean valid = true;
    private WizardDescriptor settings;
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    protected BasicWizardPanel(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    public void setSettings(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    protected WizardDescriptor getSettings() {
        return settings;
    }
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    
    protected void fireChange() {
        changeSupport.fireChange();
    }
    
    public HelpCtx getHelp() {
        return null;
    }
    
    public void storeSettings(WizardDescriptor settings) {}
    
    public void readSettings(WizardDescriptor settings) {}
    
    public boolean isValid() {
        return valid;
    }
    
    /**
     * Mainly for receiving events from wrapped component about its validity.
     * Firing events further to Wizard descriptor so it will reread this panel's
     * state and reenable/redisable its next/prev/finish/... buttons.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if ("valid".equals(evt.getPropertyName())) { // NOI18N
            boolean nueValid = ((Boolean) evt.getNewValue()).booleanValue();
            if (nueValid != valid) {
                valid = nueValid;
                fireChange();
            }
        }
    }
    
}
