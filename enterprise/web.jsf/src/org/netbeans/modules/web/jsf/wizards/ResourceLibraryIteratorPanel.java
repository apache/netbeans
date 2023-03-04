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
package org.netbeans.modules.web.jsf.wizards;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.web.jsf.wizards.ResourceLibraryIterator.ProjectType;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class ResourceLibraryIteratorPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    
    private static final Pattern FOLDER_NAME_EXCLUDES = Pattern.compile(".*[!@%^&(){}/\\`?*<>|\":]+.*"); //NOI18N

    protected static final String PROP_CONTRACT_NAME = "contractName";
    protected static final String PROP_TEMPLATE_NAME = "templateName";
    protected static final String PROP_CREATE_TEMPLATE = "createTemplate";
    protected static final String PROP_TEMPLATE_PANEL = "templatePanel";

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final WizardDescriptor descriptor;
    private final Project project;
    private final FileObject contractsParent;
    private final ProjectType projectType;

    private ResourceLibraryIteratorPanelVisual gui;

    ResourceLibraryIteratorPanel(WizardDescriptor descriptor, FileObject contractsParent, ProjectType projectType) {
        this.descriptor = descriptor;
        this.contractsParent = contractsParent;
        this.projectType = projectType;
        this.project = Templates.getProject(descriptor);
    }

    @Override
    public synchronized ResourceLibraryIteratorPanelVisual getComponent() {
        if (gui == null) {
            gui = new ResourceLibraryIteratorPanelVisual(project, contractsParent, projectType);
        }
        return gui;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        getComponent();
        gui.addChangeListener(this);
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        getComponent();
        gui.removeChangeListener(this);

        settings.putProperty(PROP_CONTRACT_NAME, gui.getContractName());
        settings.putProperty(PROP_CREATE_TEMPLATE, gui.isCreateInitialTemplate());
        settings.putProperty(PROP_TEMPLATE_NAME, gui.getTemplateName());
        settings.putProperty(PROP_TEMPLATE_PANEL, gui.getTemplatePanel());
    }

    @Messages({
        "ResourceLibraryIteratorPanel.err.contract.name.empty=Contract name is empty",
        "ResourceLibraryIteratorPanel.err.contract.name.not.valid=Contract name is not valid folder name",
        "ResourceLibraryIteratorPanel.err.template.name.empty=Template name is empty",
        "ResourceLibraryIteratorPanel.err.template.name.not.valid=Template name is not valid file name",
        "ResourceLibraryIteratorPanel.err.contracts.parent.not.extists=Contracts parent folder doesn't exist",
        "ResourceLibraryIteratorPanel.err.contracts.parent.not.writeable=Contracts parent folder is not writeable",
        "ResourceLibraryIteratorPanel.err.contracts.folder.not.writeable=Contracts folder is not writeable",
        "ResourceLibraryIteratorPanel.err.contract.already.exists=Such contract folder already exists",
    })
    @Override
    public boolean isValid() {
        getComponent();
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); //NOI18N

        // folders permissions
        if (!contractsParent.isValid() || !contractsParent.isFolder()) {
            descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.ResourceLibraryIteratorPanel_err_contracts_parent_not_extists());
            return false;
        }
        FileObject contractsFolder = contractsParent.getFileObject(ResourceLibraryIterator.CONTRACTS);
        if (contractsFolder == null) {
            if (!contractsParent.canWrite()) {
                descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.ResourceLibraryIteratorPanel_err_contracts_parent_not_writeable());
                return false;
            }
        } else {
            if (!contractsFolder.canWrite()) {
                descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.ResourceLibraryIteratorPanel_err_contracts_folder_not_writeable());
                return false;
            }
            if (contractsFolder.getFileObject(gui.getContractName()) != null) {
                descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.ResourceLibraryIteratorPanel_err_contract_already_exists());
                return false;
            }
        }

        // contact naming
        if (gui.getContractName().isEmpty()) {
            descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.ResourceLibraryIteratorPanel_err_contract_name_empty());
            return false;
        }
        if (!isValidFileName(gui.getContractName())) {
            descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.ResourceLibraryIteratorPanel_err_contract_name_not_valid());
            return false;
        }

        // initial template naming
        if (gui.isCreateInitialTemplate()) {
            if (gui.getTemplateName().isEmpty()) {
                descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.ResourceLibraryIteratorPanel_err_template_name_empty());
                return false;
            }
            if (!isValidFileName(gui.getTemplateName())) {
                descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, Bundle.ResourceLibraryIteratorPanel_err_template_name_not_valid());
                return false;
            }
        }
        return true;
    }

    private static boolean isValidFileName(String name) {
        Matcher matcher = FOLDER_NAME_EXCLUDES.matcher(name);
        return !matcher.matches();
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
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

    private void fireChangeEvent() {
        changeSupport.fireChange();
    }

}
