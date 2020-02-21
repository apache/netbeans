/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
