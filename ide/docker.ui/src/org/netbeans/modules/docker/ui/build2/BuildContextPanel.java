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

import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.docker.ui.Validations;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class BuildContextPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private BuildContextVisual component;
    private final FileSystem fileSystem;
    private WizardDescriptor wizard;

    public BuildContextPanel(FileSystem fileSystem) {
        super();
        this.fileSystem = fileSystem;
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public BuildContextVisual getComponent() {
        if (component == null) {
            component = new BuildContextVisual(fileSystem);
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
                component.getBuildContext(),
                (String) wizard.getProperty(BuildImageWizard.DOCKERFILE_PROPERTY));
    }

    @NbBundle.Messages({
        "MSG_NonExistingBuildContext=The build context does not exist.",
        "MSG_EmptyRepository=The repository must not be empty when using tag."
    })
    @Override
    public boolean isValid() {
        // clear the error message
        wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, null);
        wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, null);

        String buildContext = component.getBuildContext();
        FileSystem fs = (FileSystem) wizard.getProperty(BuildImageWizard.FILESYSTEM_PROPERTY);
        FileObject buildContextFo = buildContext == null ? null : fs.getRoot().getFileObject(buildContext);
        if (buildContext == null || buildContextFo == null || !buildContextFo.isFolder()) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_NonExistingBuildContext());
            return false;
        }
        if (component.getRepository() == null && component.getTag() != null) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.MSG_EmptyRepository());
            return false;
        }
        String repository = component.getRepository();
        if (repository != null) {
            String message = Validations.validateRepository(repository);
            if (message != null) {
                wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);
                return false;
            }
        }
        String tag = component.getTag();
        if (tag != null) {
            String message = Validations.validateTag(tag);
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

        component.setBuildContext((String) wiz.getProperty(BuildImageWizard.BUILD_CONTEXT_PROPERTY));
        component.setRepository((String) wiz.getProperty(BuildImageWizard.REPOSITORY_PROPERTY));
        component.setTag((String) wiz.getProperty(BuildImageWizard.TAG_PROPERTY));

        // XXX revalidate; is this bug?
        changeSupport.fireChange();
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        wiz.putProperty(BuildImageWizard.BUILD_CONTEXT_PROPERTY, component.getBuildContext());
        wiz.putProperty(BuildImageWizard.REPOSITORY_PROPERTY, component.getRepository());
        wiz.putProperty(BuildImageWizard.TAG_PROPERTY, component.getTag());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }
}
