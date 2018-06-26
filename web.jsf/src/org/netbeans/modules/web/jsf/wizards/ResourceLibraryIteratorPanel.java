/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
