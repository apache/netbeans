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
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;

/**
 *
 */
public class NewCndClassPanel extends CndPanel {

    NewCndClassPanel(Project project, SourceGroup[] folders, WizardDescriptor.Panel<WizardDescriptor> bottomPanel) {
        super(project, folders, bottomPanel);
    }

    @Override
    public Component getComponent() {
        synchronized (guiLock) {
            if (gui == null) {
                gui = new NewCndClassPanelGUI(project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), NewCndClassPanelGUI.Kind.Class);
                gui.addChangeListener(this);
            }
        }
        return gui;
    }

    @Override
    protected void doStoreSettings(WizardDescriptor settings) {
        String table = (CppSettings.findObject(CppSettings.class, true)).getReplaceableStringsTable();
        table +="\nCLASSNAME=" + getGui().getClassName(); // NOI18N
        table +="\nDEFAULT_HEADER_EXT=" + getGui().getHeaderExt(); // NOI18N
        (CppSettings.findObject(CppSettings.class, true)).setReplaceableStringsTable(table);
        settings.putProperty("headerFileName", getGui().getHeaderFileName()); // NOI18N
        FileObject fo = getHeaderFolderFromGUI();
        if (fo != null){
            settings.putProperty("headerFolder", DataFolder.findFolder(fo)); // NOI18N
        }
    }

    private FileObject getHeaderFolderFromGUI() {
        FileObject rootFolder = getGui().getTargetGroup().getRootFolder();
        String folderName = getGui().getHeaderFolder();
        String newObject = getGui().getHeaderName();

        if (newObject.indexOf ('/') > 0) { // NOI18N
            String path = newObject.substring (0, newObject.lastIndexOf ('/')); // NOI18N
            folderName = folderName.isEmpty() ? path : folderName + '/' + path; // NOI18N
        }

        FileObject headerFolder= rootFolder.getFileObject(folderName);
        if ( headerFolder == null ) {
            try {
                headerFolder = FileUtil.createFolder( rootFolder, folderName );
            } catch (IOException ioe) {
            }
        }
        return headerFolder;
    }

    NewCndClassPanelGUI getGui() {
        return (NewCndClassPanelGUI)gui;
    }

    @Override
    public boolean isValid() {
        boolean ok = super.isValid();

        if (!ok) {
            setErrorMessage (""); // NOI18N

            return false;
        }
        if (!CndLexerUtilities.isCppIdentifier( getGui().getClassName() )) {
            setErrorMessage( NbBundle.getMessage(NewCndClassPanel.class, "MSG_not_valid_classname") );
            return false;
        }
        // check if the file name can be created
        String errorMessage = canUseFileName(getGui().getTargetGroup().getRootFolder(), getGui().getTargetFolder(), getGui().getSourceFileName(), false);
        if (errorMessage == null) {
            errorMessage = canUseFileName(getGui().getTargetGroup().getRootFolder(), getGui().getTargetFolder(), getGui().getHeaderFileName(), false);
        }
        setErrorMessage(errorMessage);

        return errorMessage == null;
    }
}
