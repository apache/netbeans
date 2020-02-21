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
package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.ProjectWizardPanels.MakeSamplePanel;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.ProjectWizardPanels.NamedPanel;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.makeproject.api.wizards.MakeSampleProjectGenerator;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

public class MakeSampleProjectIterator implements TemplateWizard.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final long serialVersionUID = 4L;
    private transient int index = 0;
    private transient WizardDescriptor.Panel<WizardDescriptor> panel;
    private transient TemplateWizard wiz;

    static Object create() {
        return new MakeSampleProjectIterator();
    }

    public MakeSampleProjectIterator() {
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panel;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        int i = 0;
        this.wiz = (TemplateWizard)wizard;
        String name = wiz.getTemplate().getNodeDelegate().getName();
        if (name != null) {
            name = name.replaceAll(" ", ""); // NOI18N
        }
        WizardConstants.PROPERTY_NAME.put(wiz, name);
        String wizardTitle = getString("SAMPLE_PROJECT") + name; // NOI18N
        String wizardTitleACSD = getString("SAMPLE_PROJECT_ACSD"); // NOI18N

        panel = getPanel(-1, name, wizardTitle, wizardTitleACSD, false);
        String[] steps = new String[1];
            JComponent jc = (JComponent) panel.getComponent();
            steps[i] = ((NamedPanel) panel).getName();
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
    }
    
    public static MakeSamplePanel<WizardDescriptor> getPanel(int wizardtype, String name, String wizardTitle, String wizardACSD, boolean fullRemote, String helpCtx) {
        return new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, fullRemote, helpCtx);
    }
    
    public static MakeSamplePanel<WizardDescriptor> getPanel(int wizardtype, String name, String wizardTitle, String wizardACSD, boolean fullRemote) {
        return getPanel(wizardtype, name, wizardTitle, wizardACSD, fullRemote, null);
    }

    @Override
    public void uninitialize(WizardDescriptor templateWizard) {
        panel = null;
        index = -1;
        WizardConstants.PROPERTY_PROJECT_FOLDER.put(wiz, null);
        WizardConstants.PROPERTY_NAME.put(wiz, null);
    }

    @Override
    public Set<?> instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start();
            return instantiate();
        } finally {
            handle.finish();
        }
    }
    
    @Override
    public Set<DataObject> instantiate() throws IOException {
        FSPath projectLocation = WizardConstants.PROPERTY_PROJECT_FOLDER.get(wiz);
        String name = WizardConstants.PROPERTY_NAME.get(wiz);
        String hostUID = WizardConstants.PROPERTY_HOST_UID.get(wiz);
        if (WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wiz) != null) {
            hostUID = ExecutionEnvironmentFactory.toUniqueID(ExecutionEnvironmentFactory.getLocal());
        }
        CompilerSet toolchain = WizardConstants.PROPERTY_TOOLCHAIN.get(wiz);
        boolean defaultToolchain = Boolean.TRUE.equals(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.get(wiz));
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(name, projectLocation);
        prjParams.setHostToolchain(hostUID, toolchain, defaultToolchain);
        Set<FileObject> resFO = MakeSampleProjectGenerator.createProjectFromTemplate(wiz.getTemplate().getPrimaryFile(), prjParams);
        Set<DataObject> resDO = new HashSet<>();
        for(FileObject fo : resFO) {
            DataObject dao = DataObject.find(fo);
            if (dao != null) {
                resDO.add(dao);
            }
        }
        return resDO;
    }

    @Override
    public String name() {
        return current().getComponent().getName();
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(NewMakeProjectWizardIterator.class);
        }
        return bundle.getString(s);
    }
}
