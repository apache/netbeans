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
package org.netbeans.modules.docker.ui.build2;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.api.DockerInstance;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileSystem;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class BuildInstancePanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private BuildInstanceVisual component;

    private WizardDescriptor wizard;

    public BuildInstancePanel() {
        super();
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public BuildInstanceVisual getComponent() {
        if (component == null) {
            component = new BuildInstanceVisual();
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
        return BuildImageWizard.isFinishable((FileSystem) wizard.getProperty(BuildImageWizard.FILESYSTEM_PROPERTY),
                (String) wizard.getProperty(BuildImageWizard.BUILD_CONTEXT_PROPERTY),
                (String) wizard.getProperty(BuildImageWizard.DOCKERFILE_PROPERTY));
    }

    @NbBundle.Messages({
        "MSG_NoInstance=No build instance specified."
    })
    @Override
    public boolean isValid() {
        // clear the error message
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);

        DockerInstance buildInstance = component.getInstance();
        if (buildInstance == null) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_NoInstance());
            return false;
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

        component.setInstance((DockerInstance) wiz.getProperty(BuildImageWizard.INSTANCE_PROPERTY));

        // XXX revalidate; is this bug?
        changeSupport.fireChange();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(BuildImageWizard.INSTANCE_PROPERTY, component.getInstance());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
