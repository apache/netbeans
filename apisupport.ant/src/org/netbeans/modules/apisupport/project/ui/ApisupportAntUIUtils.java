/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.NewNbModuleWizardIterator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf
 */
public final class ApisupportAntUIUtils {
    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    private static final String ICON_PATH = "org/netbeans/modules/apisupport/project/resources/defaultFolder.gif"; // NOI18N
    private static final String OPENED_ICON_PATH = "org/netbeans/modules/apisupport/project/resources/defaultFolderOpen.gif"; // NOI18N
    
    private ApisupportAntUIUtils() {}
    
    /**
     * Calls in turn {@link ProjectChooser#setProjectsFolder} if the
     * <code>folder</code> is not <code>null</code> and is a directory.
     */
    public static void setProjectChooserDir(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return;
        }
        ProjectChooser.setProjectsFolder(folder);
    }
    
    /**
     * Calls {@link #setProjectChooserDir} with the <code>fileOrFolder</code>'s
     * parent if it isn't <code>null</code>. Otherwise fallbacks to
     * <code>fileOrFolder</code> itself if it is a directory.
     */
    public static void setProjectChooserDirParent(File fileOrFolder) {
        if (fileOrFolder == null) {
            return;
        }
        File parent = fileOrFolder.getParentFile();
        setProjectChooserDir(parent != null ? parent :
            (fileOrFolder.isDirectory() ? fileOrFolder : null));
    }
    
    /**
     * Set the <code>text</code> for the <code>textComp</code> and set its
     * caret position to the end of the text.
     */
    public static void setText(JTextComponent textComp, String text) {
        textComp.setText(text);
        textComp.setCaretPosition(text == null ? 0 : text.length());
    }
    
    public static NbModuleProject chooseSuiteComponent(Component parent, SuiteProject suite) {
        Project project = chooseProject(parent);
        if (project == null) {
            return null;
        }
        if (!(project instanceof NbModuleProject)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ApisupportAntUIUtils.class, "MSG_TryingToAddNonNBModule",
                    ProjectUtils.getInformation(project).getDisplayName())));
            return null;
        }
        NbModuleProject p = (NbModuleProject) project;
        if (SuiteUtils.getSubProjects(suite).contains((NbModuleProject) project)) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ApisupportAntUIUtils.class, "MSG_SuiteAlreadyContainsProject",
                    ProjectUtils.getInformation(suite).getDisplayName(),
                    ProjectUtils.getInformation(project).getDisplayName())));
            return null;
        }
        switch (p.getModuleType()) {
        case SUITE_COMPONENT:
            Object[] params = new Object[] {
                ProjectUtils.getInformation(project).getDisplayName(),
                getSuiteProjectName(project),
                getSuiteProjectDirectory(project),
                ProjectUtils.getInformation(suite).getDisplayName(),};
            NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(ApisupportAntUIUtils.class, "MSG_MoveFromSuiteToSuite", params),
                    NotifyDescriptor.OK_CANCEL_OPTION);
            DialogDisplayer.getDefault().notify(confirmation);
            if (confirmation.getValue() == NotifyDescriptor.OK_OPTION) {
                return p;
            } else {
                return null;
            }
        case STANDALONE:
            return p;
        case NETBEANS_ORG:
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(ApisupportAntUIUtils.class, "MSG_TryingToAddNBORGModule",
                    ProjectUtils.getInformation(project).getDisplayName())));
            return null;
        default:
            throw new AssertionError();
        }
    }
    
    /**
     * Returns default folder icon as {@link java.awt.Image}. Never returns
     * <code>null</code>.
     *
     * @param opened wheter closed or opened icon should be returned.
     */
    public static Image getTreeFolderIcon(boolean opened) {
        Image base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263;
        if (base == null) {
            Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else { // fallback to our owns
                base = ImageUtilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, true);
            }
        }
        assert base != null;
        return base;
    }
    
    public static @CheckForNull NbModuleProject runLibraryWrapperWizard(final Project suiteProvider) {
        NewNbModuleWizardIterator iterator = NewNbModuleWizardIterator.createLibraryModuleIterator(suiteProvider);
        return ApisupportAntUIUtils.runProjectWizard(iterator, "CTL_NewLibraryWrapperProject"); // NOI18N
    }
    
    public static @CheckForNull NbModuleProject runProjectWizard(
            final NewNbModuleWizardIterator iterator, final String titleBundleKey) {
        WizardDescriptor wd = new WizardDescriptor(iterator);
        wd.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wd.setTitle(NbBundle.getMessage(ApisupportAntUIUtils.class, titleBundleKey));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wd);
        dialog.setVisible(true);
        dialog.toFront();
        NbModuleProject project = null;
        boolean cancelled = wd.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            FileObject folder = iterator.getCreateProjectFolder();
            if (folder == null) {
                return null;
            }
            try {
                project = (NbModuleProject) ProjectManager.getDefault().findProject(folder);
                OpenProjects.getDefault().open(new Project[] { project }, false);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return project;
    }
    
    public static Project chooseProject(Component parent) {
        JFileChooser chooser = ProjectChooser.projectChooser();
        int option = chooser.showOpenDialog(parent);
        Project project = null;
        if (option == JFileChooser.APPROVE_OPTION) {
            File projectDir = chooser.getSelectedFile();
            ApisupportAntUIUtils.setProjectChooserDirParent(projectDir);
            try {
                project = ProjectManager.getDefault().findProject(
                        FileUtil.toFileObject(projectDir));
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
        return project;
    }
    
    private static String getSuiteProjectDirectory(Project suiteComp) {
        File d = SuiteUtils.getSuiteDirectory(suiteComp);
        return d != null ? d.getAbsolutePath() : "???"; // NOI18N
    }
    
    private static String getSuiteProjectName(Project suiteComp) {
        File d = SuiteUtils.getSuiteDirectory(suiteComp);
        FileObject suiteDir = d != null ? FileUtil.toFileObject(d) : null;
        if (suiteDir == null) {
            // #94915
            return "???"; // NOI18N
        }
        return ApisupportAntUtils.getDisplayName(suiteDir);
    }
    
    /**
     * Show an OK/cancel-type dialog with customized button texts.
     * Only a separate method because it is otherwise cumbersome to replace
     * the OK button with a button that is set as the default.
     * @param title the dialog title
     * @param message the body of the message (usually HTML text)
     * @param acceptButton a label for the default accept button; should not use mnemonics
     * @param cancelButton a label for the cancel button (or null for default); should not use mnemonics
     * @param messageType {@link NotifyDescriptor#WARNING_MESSAGE} or similar
     * @return true if user accepted the dialog
     */
    public static boolean showAcceptCancelDialog(String title, String message, String acceptButton, String cancelButton, int messageType) {
        return showAcceptCancelDialog(title, message, acceptButton, null , 
                cancelButton, messageType) ;
    }
    
    /**
     * Show an OK/cancel-type dialog with customized button texts.
     * Only a separate method because it is otherwise cumbersome to replace
     * the OK button with a button that is set as the default.
     * @param title the dialog title
     * @param message the body of the message (usually HTML text)
     * @param acceptButton a label for the default accept button; should not use mnemonics
     * @param accDescrAcceptButton a accessible description for acceptButton 
     * @param cancelButton a label for the cancel button (or null for default); should not use mnemonics
     * @param messageType {@link NotifyDescriptor#WARNING_MESSAGE} or similar
     * @return true if user accepted the dialog
     */
    public static boolean showAcceptCancelDialog(String title, String message, 
            String acceptButton, String accDescrAcceptButton , 
            String cancelButton, int messageType) 
    {
        DialogDescriptor d = new DialogDescriptor(message, title);
        d.setModal(true);
        JButton accept = new JButton(acceptButton);
        accept.setDefaultCapable(true);
        if ( accDescrAcceptButton != null ){
            accept.getAccessibleContext().
            setAccessibleDescription( accDescrAcceptButton);
        }
        d.setOptions(new Object[] {
            accept,
            cancelButton != null ? new JButton(cancelButton) : NotifyDescriptor.CANCEL_OPTION,
        });
        d.setMessageType(messageType);
        return DialogDisplayer.getDefault().notify(d).equals(accept);
    }
    
                        }
