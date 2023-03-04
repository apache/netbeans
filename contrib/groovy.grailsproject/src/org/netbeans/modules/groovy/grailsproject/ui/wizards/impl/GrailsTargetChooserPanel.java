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

package org.netbeans.modules.groovy.grailsproject.ui.wizards.impl;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author  Petr Hrebejk
 */
public final class GrailsTargetChooserPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private static final Logger LOGGER = Logger.getLogger(GrailsTargetChooserPanel.class.getName());
    private static final String FOLDER_TO_DELETE = "folderToDelete";    //NOI18N

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private GrailsTargetChooserPanelGUI gui;
    private WizardDescriptor.Panel<WizardDescriptor> bottomPanel;
    private WizardDescriptor wizard;

    private final Project project;
    private final SourceGroup folder;
    private final String suffix;

    public GrailsTargetChooserPanel(Project project, SourceGroup folder, WizardDescriptor.Panel<WizardDescriptor> bottomPanel, String suffix) {
        this.project = project;
        this.folder = folder;
        this.suffix = suffix;
        this.bottomPanel = bottomPanel;
        if (bottomPanel != null) {
            bottomPanel.addChangeListener(this);
        }
    }

    @Override
    public Component getComponent() {
        if (gui == null) {
            gui = new GrailsTargetChooserPanelGUI(project, folder, bottomPanel == null ? null : bottomPanel.getComponent());
            gui.addChangeListener(this);
        }
        return gui;
    }

    @Override
    public HelpCtx getHelp() {
        if (bottomPanel != null) {
            HelpCtx bottomHelp = bottomPanel.getHelp();
            if (bottomHelp != null) {
                return bottomHelp;
            }
        }

        //XXX
        return null;

    }

    @Override
    public boolean isValid() {
        if (gui == null) {
           setErrorMessage(null);
           return false;
        }
        if (gui.getTargetName() == null) {
            setInfoMessage("INFO_GrailsTargetChooser_ProvideArtifact");
            return false;
        } else if (!isValidTypeIdentifier(gui.getTargetName())) {
            setErrorMessage("ERR_GrailsTargetChooser_InvalidArtifactName");
            return false;
        } else if (!isValidPackageName(gui.getPackageName())) {
            setErrorMessage("ERR_GrailsTargetChooser_InvalidPackage");
            return false;
        }
        if (!isValidPackage(gui.getRootFolder(), gui.getPackageName())) {
            setErrorMessage("ERR_GrailsTargetChooser_InvalidFolder");
            return false;
        }

        // check if the file name can be created
        FileObject template = Templates.getTemplate(wizard);

        boolean returnValue = true;
        FileObject rootFolder = gui.getRootFolder();
        String errorMessage = canUseFileName(rootFolder, gui.getPackageFileName(), gui.getTargetName(),
                suffix != null ? suffix : "." + template.getExt()); // NOI18N
        if (gui != null) {
            wizard.getNotificationLineSupport().setErrorMessage(errorMessage);
        }
        if (errorMessage != null) {
            returnValue = false;
        }

        if (returnValue && gui.getPackageName().length() == 0) {
            //Only warning, display it only if everything else is OK.
            setWarningMessage("ERR_GrailsTargetChooser_DefaultPackage");
        }

        // this enables to display error messages from the bottom panel
        // Nevertheless, the previous error messages have bigger priorities
        if (returnValue && bottomPanel != null) {
           if (!bottomPanel.isValid()) {
               return false;
           }
        }

        return returnValue;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(e);
        }
    }

    @Override
    public void readSettings(WizardDescriptor wizard) {
        this.wizard = wizard;
        if (gui != null) {
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder(wizard);
            // Init values
            gui.initValues(Templates.getTemplate(wizard), preselectedFolder, suffix);
        }

        if (bottomPanel != null) {
            bottomPanel.readSettings(wizard);
        }

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        if (gui != null) {
            Object substitute = gui.getClientProperty("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                wizard.putProperty("NewFileWizard_Title", substitute); // NOI18N
            }
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wizard) {
        Object value = wizard.getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value)
                || WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if (isValid()) {
            if (bottomPanel != null) {
                bottomPanel.storeSettings(wizard);
            }
            Templates.setTargetFolder(wizard, getTargetFolderFromGUI(wizard));
            Templates.setTargetName(wizard, gui.getTargetName());
        }
        wizard.putProperty("NewFileWizard_Title", null); // NOI18N

        if (WizardDescriptor.FINISH_OPTION.equals(value)) {
            wizard.putProperty(FOLDER_TO_DELETE, null);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    // Private methods ---------------------------------------------------------

    private void setErrorMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setErrorMessage(NbBundle.getMessage(GrailsTargetChooserPanelGUI.class, key));
        }
    }

    private void setWarningMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setWarningMessage(NbBundle.getMessage(GrailsTargetChooserPanelGUI.class, key));
        }
    }

    private void setInfoMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setInformationMessage(NbBundle.getMessage(GrailsTargetChooserPanelGUI.class, key));
        }
    }

    private FileObject getTargetFolderFromGUI (WizardDescriptor wd) {
        assert gui != null;
        FileObject rootFolder = gui.getRootFolder();
        FileObject folder = null;
        String packageFileName = gui.getPackageFileName();
        folder = rootFolder.getFileObject(packageFileName);
        if (folder == null) {
            try {
                folder = rootFolder;
                StringTokenizer tk = new StringTokenizer(packageFileName, "/"); //NOI18N
                String name = null;
                while (tk.hasMoreTokens()) {
                    name = tk.nextToken();
                    FileObject fo = folder.getFileObject(name, "");   //NOI18N
                    if (fo == null) {
                        break;
                    }
                    folder = fo;
                }
                folder = folder.createFolder(name);
                FileObject toDelete = (FileObject) wd.getProperty(FOLDER_TO_DELETE);
                if (toDelete == null) {
                    wd.putProperty(FOLDER_TO_DELETE, folder);
                } else if (!toDelete.equals(folder)) {
                    toDelete.delete();
                    wd.putProperty(FOLDER_TO_DELETE, folder);
                }
                while (tk.hasMoreTokens()) {
                    name = tk.nextToken();
                    folder = folder.createFolder(name);
                }

            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }
        }
        return folder;
    }

    // Nice copy of useful methods (Taken from JavaModule)

    static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token)) {
                return false;
            }
            if (!Utilities.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPackage (FileObject root, final String path) {
        assert root != null;
        assert path != null;
        final StringTokenizer tk = new StringTokenizer(path, ".");   //NOI18N
        while (tk.hasMoreTokens()) {
            root = root.getFileObject(tk.nextToken());
            if (root == null) {
                return true;
            } else if (root.isData()) {
                return false;
            }
        }
        return true;
    }

    static boolean isValidTypeIdentifier(String ident) {
        if (ident == null || "".equals(ident) || !Utilities.isJavaIdentifier(ident)) {
            return false;
        } else {
            return true;
        }
    }

    // helper methods copied from project/ui/ProjectUtilities
    /** Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */
    public static final String canUseFileName(FileObject targetFolder, String folderName, String newObjectName, String extension) {
        String newObjectNameToDisplay = newObjectName;
        if (newObjectName != null) {
            newObjectName = newObjectName.replace('.', '/'); // NOI18N
        }
        if (extension != null && extension.length () > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(newObjectName);
            sb.append(extension);
            newObjectName = sb.toString();
        }

        if (extension != null && extension.length () > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(newObjectNameToDisplay);
            sb.append(extension);
            newObjectNameToDisplay = sb.toString();
        }

        String relFileName = folderName + "/" + newObjectName; // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage(GrailsTargetChooserPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }

        // target package should be writable
        File targetPackage = folderName != null ? new File(FileUtil.toFile (targetFolder), folderName) : FileUtil.toFile (targetFolder);
        if (targetPackage != null) {
            if (targetPackage.exists() && !targetPackage.canWrite()) {
                return NbBundle.getMessage(GrailsTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
            }
        } else if (!targetFolder.canWrite()) {
            return NbBundle.getMessage (GrailsTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }

        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage(GrailsTargetChooserPanel.class, "MSG_file_already_exist", newObjectNameToDisplay); // NOI18N
        }

        // all ok
        return null;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName) {
        boolean result = false;
        File fileForTargetFolder = FileUtil.toFile(targetFolder);
        if (fileForTargetFolder.exists()) {
            result = new File(fileForTargetFolder, relFileName).exists();
        } else {
            result = targetFolder.getFileObject(relFileName) != null;
        }

        return result;
    }
}
