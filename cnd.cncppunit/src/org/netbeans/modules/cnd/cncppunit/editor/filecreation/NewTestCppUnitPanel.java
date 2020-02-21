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

package org.netbeans.modules.cnd.cncppunit.editor.filecreation;

import java.awt.Component;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.editor.filecreation.CndPanel;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.netbeans.modules.cnd.simpleunit.utils.MakefileUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class NewTestCppUnitPanel extends CndPanel {

    private static final String CPPUNIT = "cppunit"; // NOI18N

    private String baseTestName = null;
    private RequestProcessor.Task libCheckTask;
    private volatile boolean libCheckResult;

    NewTestCppUnitPanel(Project project, SourceGroup[] folders, WizardDescriptor.Panel<WizardDescriptor> bottomPanel, String baseTestName) {
        super(project, folders, bottomPanel);
        this.baseTestName = baseTestName;
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new NewTestCppUnitPanelGUI(project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), baseTestName);
            gui.addChangeListener(this);
        }
        return gui;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e instanceof TestLibChecker.LibCheckerChangeEvent) {
            libCheckResult = ((TestLibChecker.LibCheckerChangeEvent) e).getResult();
            libCheckTask = null;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    getGui().setControlsEnabled(true);
                }
            });
        }
        super.stateChanged(e);
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("CreateCppUnitTestWizardP2");
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        super.readSettings(settings);

        libCheckResult = false;
        getGui().setControlsEnabled(false);

        AbstractCompiler cppCompiler = TestLibChecker.getCppCompiler(project);
        if (cppCompiler != null) {
            libCheckTask = TestLibChecker.asyncCheck(CPPUNIT, cppCompiler, this);
        } else {
            libCheckTask = null;
        }
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

        FileObject headerFolder = rootFolder.getFileObject(folderName);
        if ( headerFolder == null ) {
            try {
                headerFolder = FileUtil.createFolder( rootFolder, folderName );
            } catch (IOException ioe) {
            }
        }
        return headerFolder;
    }

    NewTestCppUnitPanelGUI getGui() {
        return (NewTestCppUnitPanelGUI)gui;
    }

    @Override
    public boolean isValid() {
        setInfoMessage(null);
        setErrorMessage(null);

        if (libCheckTask != null) {
            // Need time for the libCheckTask to finish. Pretend that the panel is invalid.
            setInfoMessage(NbBundle.getMessage(NewTestCppUnitPanel.class, "MSG_Checking_Library", CPPUNIT, TestLibChecker.getExecutionEnvironment(project))); // NOI18N
            return false;
        }

        if (!libCheckResult) {
            // Library not found. Display warning but still allow to create test.
            setErrorMessage(NbBundle.getMessage(NewTestCppUnitPanel.class, "MSG_Missing_Library", CPPUNIT)); // NOI18N
        }

        if (!super.isValid()) {
            return false;
        }

        if (!CndLexerUtilities.isCppIdentifier( getGui().getClassName() )) {
            setErrorMessage( NbBundle.getMessage(NewTestCppUnitPanel.class, "MSG_not_valid_classname") );
            return false;
        }

        // check if the file name can be created
        String errorMessage = canUseFileName(getGui().getTargetGroup().getRootFolder(), getGui().getTargetFolder(), getGui().getSourceFileName(), false);
        if (errorMessage == null) {
            errorMessage = canUseFileName(getGui().getTargetGroup().getRootFolder(), getGui().getTargetFolder(), getGui().getHeaderFileName(), false);
            if (errorMessage == null) {
                errorMessage = canUseFileName(getGui().getTargetGroup().getRootFolder(), getGui().getTargetFolder(), getGui().getRunnerFileName(), false);
            }
        }

        if (errorMessage != null) {
            setErrorMessage(errorMessage);
        }

        if (MakefileUtils.getMakefile(project) == null) {
            setInfoMessage( NbBundle.getMessage(NewTestCppUnitPanel.class, "MSG_Missing_Makefile") );
            return false;
        }

        return errorMessage == null;
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
}
