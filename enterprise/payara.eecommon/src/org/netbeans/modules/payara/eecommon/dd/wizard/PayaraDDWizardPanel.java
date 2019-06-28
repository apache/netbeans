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

package org.netbeans.modules.payara.eecommon.dd.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;

/*
 *
 * @author Peter Williams
 * @author Gaurav Gupta
 */
public class PayaraDDWizardPanel implements WizardDescriptor.Panel {
    
    private final Set listeners = new HashSet(1);
    private final PayaraDDVisualPanel component = new PayaraDDVisualPanel();
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    public PayaraDDWizardPanel() {
        component.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                fireChangeEvent();
            }
        });
    }
    
    File getSelectedLocation() {
        return component.getSelectedLocation();
    }
    
    Project getProject() {
        return project;
    }
    
    String getFileName() {
        return component.getFileName();
    }
    
    @Override
    public Component getComponent() {
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public boolean isValid() {
        String payaraDDFileName = component.getFileName();
        if (payaraDDFileName == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PayaraDDWizardPanel.class, "ERR_NoJavaEEModuleType")); //NOI18N
            return false;
        }

        File location = component.getSelectedLocation();
        if (location == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PayaraDDWizardPanel.class, "ERR_NoValidLocation", payaraDDFileName)); //NOI18N
            return false;
        }

        File payaraDDFile = component.getFile();
        if (payaraDDFile.exists()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                    NbBundle.getMessage(PayaraDDWizardPanel.class, "ERR_FileExists", payaraDDFileName)); //NOI18N
            return false;
        }

        return true;
    }
    
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
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        if (project == null) {
            project = Templates.getProject(wizardDescriptor);
            component.setProject(project);
        }
    }
    
    @Override
    public void storeSettings(Object settings) {
    }
    
}

