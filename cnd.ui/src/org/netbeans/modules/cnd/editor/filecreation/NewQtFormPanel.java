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
package org.netbeans.modules.cnd.editor.filecreation;

import java.awt.Component;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.NbBundle;

/**
 */
public class NewQtFormPanel extends CndPanel {

    NewQtFormPanel(Project project, SourceGroup[] folders) {
        super(project, folders, null);
    }

    @Override
    public Component getComponent() {
        synchronized (guiLock) {
            if (gui == null) {
                gui = new NewQtFormPanelGUI(project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), FORM_TYPES);
                gui.addChangeListener(this);
            }
        }
        return gui;
    }

    @Override
    protected void doStoreSettings(WizardDescriptor settings) {
        String table = (CppSettings.findObject(CppSettings.class, true)).getReplaceableStringsTable();
        table +="\nCLASSNAME=" + getGui().getFormName(); // NOI18N
        if (getGui().getHeaderFileName() != null) {
            table +="\nDEFAULT_HEADER_EXT=" + FileUtil.getExtension(getGui().getHeaderFileName()); // NOI18N
            table +="\nWIDGETCLASSNAME=" + getGui().getFormType().widgetClassName; // NOI18N
        }
        (CppSettings.findObject(CppSettings.class, true)).setReplaceableStringsTable(table);
    }

    NewQtFormPanelGUI getGui() {
        return (NewQtFormPanelGUI) gui;
    }

    @Override
    public boolean isValid() {
        boolean ok = super.isValid();

        if (!ok) {
            setErrorMessage(""); // NOI18N
            return false;
        }
        if (!CndLexerUtilities.isCppIdentifier(getGui().getFormName())) {
            setErrorMessage(NbBundle.getMessage(NewQtFormPanel.class, "MSG_not_valid_formname")); // NOI18N
            return false;
        }
        // check if the file name can be created
        FileObject groupFolder = getGui().getTargetGroup().getRootFolder();
        String targetFolder = getGui().getTargetFolder();
        String errorMessage = canUseFileName(groupFolder, targetFolder, getGui().getFormFileName(), false);
        if (errorMessage == null && getGui().getSourceFileName() != null) {
            errorMessage = canUseFileName(groupFolder, targetFolder, getGui().getSourceFileName(), false);
            if (errorMessage == null && getGui().getHeaderFileName() != null) {
                errorMessage = canUseFileName(groupFolder, targetFolder, getGui().getHeaderFileName(), false);
            }
        }
        setErrorMessage(errorMessage);

        return errorMessage == null;
    }

    private static final FormType[] FORM_TYPES = {
        new FormType("dialog-buttonsbottom.ui", "QDialog"), // NOI18N
        new FormType("dialog-buttonsright.ui", "QDialog"), // NOI18N
        new FormType("dialog-nobuttons.ui", "QDialog"), // NOI18N
        new FormType("mainwindow.ui", "QMainWindow"), // NOI18N
        new FormType("widget.ui", "QWidget") // NOI18N
    };

    public static class FormType {
        public final String templateFileName;
        public final String templateDisplayName;
        public final String widgetClassName;

        public FormType(String file, String clazz) {
            this.templateFileName = file;
            this.templateDisplayName = getTemplateDisplayName(file);
            this.widgetClassName = clazz;
        }

        @Override
        public String toString() {
            return templateDisplayName;
        }
    }

    public static FileObject getTemplateFileObject(String formType) {
        return FileUtil.getConfigFile("Templates/qtFiles/" + formType); // NOI18N
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
