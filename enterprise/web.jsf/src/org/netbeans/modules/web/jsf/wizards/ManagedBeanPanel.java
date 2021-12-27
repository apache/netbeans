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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.common.ServerUtil;

import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel asking for web frameworks to use.
 * @author Radko Najman
 */
final class ManagedBeanPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {

    private TemplateWizard wizard;
    private ManagedBeanPanelVisual component;
    private String managedBeanClass;
    
    private Project project;
    /** Create the wizard panel descriptor. */
    public ManagedBeanPanel(Project project, TemplateWizard wizard) {
        this.project = project;
        this.wizard = wizard;
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            ManagedBeanPanelVisual gui = new ManagedBeanPanelVisual(project);
            gui.addChangeListener(this);
            component = gui;
        }

        return component;
    }

    public void updateManagedBeanName(WizardDescriptor.Panel panel) {
        String targetName = null;
        Component gui = panel.getComponent();
        try {
            // XXX JavaTargetChooserPanel should introduce new API to get current contents
            // of its component JavaTargetChooserPanelGUI (see Issue#154655)
            Method getTargetName = gui.getClass().getMethod("getTargetName", (Class[]) null); // NOI18N
            targetName = (String) getTargetName.invoke(gui, (Object[]) null);
        } catch (Exception ex) {
            return;
        }

        if ((targetName == null) || targetName.trim().equals("")) {
            return;
        }

        if (managedBeanClass!=null && targetName.equals(managedBeanClass.substring(0, 1).toUpperCase()+ managedBeanClass.substring(1))) {
            return;
        } else {
            managedBeanClass = targetName.substring(0, 1).toLowerCase()+targetName.substring(1);
        }

        getComponent();
        String name = component.getManagedBeanName();
        if ((name == null) || !name.equals(managedBeanClass)) {
            component.setManagedBeanName(managedBeanClass);
        }
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.jsf.wizards.ManagedBeanPanel");
    }

    @Override
    public boolean isValid() {
        getComponent();
        if (component.valid(wizard)) {
            // check that this project has a valid target server
            if (!ServerUtil.isValidServerInstance(project)) {
                wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                        NbBundle.getMessage(ManagedBeanPanel.class, "WARN_MissingTargetServer"));
            }
            return true;
        }
        return false;
    }

    public boolean isAddBeanToConfig() {
        if (component == null) {
            return false;
        }
        return component.isAddBeanToConfig();
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
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    @Override
    public void readSettings(Object settings) {
        wizard = (TemplateWizard) settings;
        component.read(wizard);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizard.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);

        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        isValid();
    }
}
