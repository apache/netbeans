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
package org.netbeans.modules.docker.ui.run;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerImageDetail;
import org.netbeans.modules.docker.ui.Validations;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

public class RunContainerPropertiesPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private final DockerImageDetail info;

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private RunContainerPropertiesVisual component;

    private WizardDescriptor wizard;

    public RunContainerPropertiesPanel(DockerImageDetail info) {
        this.info = info;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public RunContainerPropertiesVisual getComponent() {
        if (component == null) {
            component = new RunContainerPropertiesVisual();
            component.addChangeListener(this);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public boolean isValid() {
        // clear the error message
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);

        String name = component.getContainerName();
        if (name != null) {
            String message = Validations.validateContainer(name);
            if (message != null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
                return false;
            }
        }
        return true;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        if (wizard == null) {
            wizard = wiz;
        }

        component.setContainerName((String) wiz.getProperty(RunTagWizard.NAME_PROPERTY));
        component.setCommand((String) wiz.getProperty(RunTagWizard.COMMAND_PROPERTY));
        component.setUser((String) wiz.getProperty(RunTagWizard.USER_PROPERTY));
        Boolean interactive = (Boolean) wiz.getProperty(RunTagWizard.INTERACTIVE_PROPERTY);
        component.setInteractive(interactive != null ? interactive : false);
        Boolean tty = (Boolean) wiz.getProperty(RunTagWizard.TTY_PROPERTY);
        component.setTty(tty != null ? tty : false);
        Boolean privileged = (Boolean) wiz.getProperty(RunTagWizard.PRIVILEGED_PROPERTY);
        component.setPrivileged(privileged != null ? privileged : false);
        Boolean mountVolumes = (Boolean) wiz.getProperty(RunTagWizard.VOLUMES_PROPERTY);
        component.setMountVolumesSelected(mountVolumes != null ? mountVolumes : false);
        // XXX revalidate; is this bug?
        changeSupport.fireChange();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(RunTagWizard.NAME_PROPERTY, component.getContainerName());
        wiz.putProperty(RunTagWizard.COMMAND_PROPERTY, component.getCommand());
        wiz.putProperty(RunTagWizard.USER_PROPERTY, component.getUser());
        wiz.putProperty(RunTagWizard.INTERACTIVE_PROPERTY, component.isInteractive());
        wiz.putProperty(RunTagWizard.TTY_PROPERTY, component.hasTty());
        wiz.putProperty(RunTagWizard.PRIVILEGED_PROPERTY, component.isPrivileged());
        wiz.putProperty(RunTagWizard.VOLUMES_PROPERTY, component.areMountVolumesSelected());
        wiz.putProperty(RunTagWizard.VOLUMES_TABLE_PROPERTY, component.getVolumesTable());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
