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

package org.netbeans.modules.cnd.simpleunit.editor.filecreation;

import java.awt.Component;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.cnd.editor.filecreation.CndPanel;
import org.netbeans.modules.cnd.editor.filecreation.NewCndFileChooserPanel;
import org.netbeans.modules.cnd.simpleunit.utils.MakefileUtils;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 */
public class NewTestSimplePanel extends CndPanel {

    private final String baseTestName;
    private final MIMEExtensions es;
    private final String defaultExt;
    private final boolean fileWithoutExtension;

    NewTestSimplePanel(Project project, SourceGroup[] folders, WizardDescriptor.Panel<WizardDescriptor> bottomPanel, MIMEExtensions es, String defaultExt, String baseTestName) {
        super(project, folders, bottomPanel);
        this.baseTestName = baseTestName;
        this.es = es;
        this.defaultExt = defaultExt;
        this.fileWithoutExtension = "".equals(defaultExt);
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new NewTestSimplePanelGUI(project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), es, defaultExt, baseTestName);
            gui.addChangeListener(this);
        }
        return gui;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("CreateTestWizardP2");
    }

    @Override
    protected void doStoreSettings(WizardDescriptor settings) {
        if (getTargetExtension().length() > 0) {
            if (((NewTestSimplePanelGUI)gui).useTargetExtensionAsDefault()) {
                es.setDefaultExtension(getTargetExtension());
            } else {
                es.addExtension(getTargetExtension());
            }
        }
    }

    @Override
    public boolean isValid() {
        boolean ok = super.isValid();

        setErrorMessage (""); // NOI18N

        if (!ok) {

            return false;
        }

        String documentName = gui.getTargetName();

        if ((!fileWithoutExtension && getTargetExtension().length() == 0) || documentName.charAt(0) == '.') {
            // ignore invalid filenames
            setErrorMessage(NbBundle.getMessage(NewCndFileChooserPanel.class, "MSG_Invalid_File_Name"));
            return false;
        }

        if (!fileWithoutExtension && !es.getValues().contains(getTargetExtension())) {
            //MSG_new_extension_introduced
            String msg = NbBundle.getMessage(NewCndFileChooserPanel.class, "MSG_new_extension_introduced", getTargetExtension()); // NOI18N

            setErrorMessage(msg); // NOI18N
        }

        // check if the file name can be created
        String errorMessage = canUseFileName(gui.getTargetGroup().getRootFolder(), gui.getTargetFolder(), documentName, false);
        if (errorMessage != null) {
            setErrorMessage(errorMessage); // NOI18N
            return false;
        }

        if (MakefileUtils.getMakefile(project) == null) {
            setInfoMessage( NbBundle.getMessage(NewTestSimplePanel.class, "MSG_Missing_Makefile") );
            return false;
        }
        
        return true;
    }

    private String getTargetExtension() {
        return ((NewTestSimplePanelGUI)gui).getTargetExtension();
    }

    public static FileObject getTemplateFileObject(String formType) {
        return FileUtil.getConfigFile("Templates/testFiles/" + formType); // NOI18N
    }

    public static DataObject getTemplateDataObject(String formType) {
        FileObject fileObj = getTemplateFileObject(formType);
        if (fileObj != null) {
            try {
                return DataObject.find(fileObj);
            } catch (DataObjectNotFoundException ex) {
                // do nothing here, return null later
            }
        }
        return null;
    }

    public static String getTemplateDisplayName(String formType) {
        DataObject dataObj = getTemplateDataObject(formType);
        if (dataObj != null) {
            return dataObj.getNodeDelegate().getDisplayName();
        }
        return formType;
    }
}
