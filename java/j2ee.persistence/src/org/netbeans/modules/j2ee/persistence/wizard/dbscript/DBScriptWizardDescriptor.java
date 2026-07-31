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

package org.netbeans.modules.j2ee.persistence.wizard.dbscript;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.util.SourceLevelChecker;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class DBScriptWizardDescriptor implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    private DBScriptPanel.WizardPanel p;
    private List<javax.swing.event.ChangeListener> changeListeners = new ArrayList<>();
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    @Override
    public void addChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.add(l);
    }
    
    @Override
    public java.awt.Component getComponent() {
        if (p == null) {
            p = new DBScriptPanel.WizardPanel();
//            p.addPropertyChangeListener(new PropertyChangeListener() {
//                @Override
//                public void propertyChange(PropertyChangeEvent evt) {
//                    if (evt.getPropertyName().equals(DBScriptWizardPanel.IS_VALID)) {
//                        Object newvalue = evt.getNewValue();
//                        if ((newvalue != null) && (newvalue instanceof Boolean)) {
//                            stateChanged(null);
//                        }
//                    }
//                }
//            });
        }
        return p.getComponent();
    }
    
    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(DBScriptWizardDescriptor.class);
    }
    
    @Override
    public boolean isValid() {
        // XXX add the following checks
        // p.getName = valid NmToken
        // p.getName not already in module
        if (wizardDescriptor == null) {
            return true;
        }
        if (SourceLevelChecker.isSourceLevel14orLower(project)) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(DBScriptWizardDescriptor.class, "ERR_NeedProperSourceLevel")); // NOI18N
            return false;
        }
        
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); //NOI18N
        return true;
    }
    
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        if (project == null) {
            project = Templates.getProject(wizardDescriptor);
            p.readSettings(settings);
        }
    }
    
    private boolean isPersistenceUnitDefined() throws InvalidPersistenceXmlException {
        return ProviderUtil.persistenceExists(project);
    }
    
    @Override
    public void removeChangeListener(javax.swing.event.ChangeListener l) {
        changeListeners.remove(l);
    }
    
    @Override
    public void storeSettings(Object settings) {
        
    }

    @Override
    public boolean isFinishPanel() {
        return isValid();
    }
    
    

    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (changeListeners) {
            it = new HashSet(changeListeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }


}

