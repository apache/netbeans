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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.DataFilesProviderImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * @author Jan Lahoda
 */
public final class DefaultSuiteProjectOperationsImplementation {
    
    private static final Logger LOG = Logger.getLogger(DefaultSuiteProjectOperationsImplementation.class.getName());
    
    //fractions how many time will be spent in some phases of the move and copy operation
    //the rename and delete operation use a different approach:
    static final int    MAX_WORK = 100;
    
    private DefaultSuiteProjectOperationsImplementation() {
    }
    
    private static String getDisplayName(Project project) {
        return ProjectUtils.getInformation(project).getDisplayName();
    }
 
    //<editor-fold defaultstate="collapsed" desc="Delete Operation">
    /**
     * @return true if success
     */
    private static void performDelete(Project project, List<FileObject> toDelete, Map<NbModuleProject,List<FileObject>> subModulesFilesToDelete ,ProgressHandle handle) throws Exception {
        try {
            int toDeleteSubModulesSize = 0;
            if(subModulesFilesToDelete != null) {
                for(Map.Entry<NbModuleProject, List<FileObject>> entryIter:subModulesFilesToDelete.entrySet()) {
                    toDeleteSubModulesSize += entryIter.getValue().size() + 1;
                }
            }
            handle.start(toDelete.size() + toDeleteSubModulesSize + 1 /*clean*/);
            
            int done = 0;
            
            done = performDeleteOnProject(project, toDelete, handle, done);
            
            if(subModulesFilesToDelete != null) {
                for(Map.Entry<NbModuleProject, List<FileObject>> entryIter:subModulesFilesToDelete.entrySet()) {
                    done = performDeleteOnProject(entryIter.getKey(), entryIter.getValue(), handle, done);
                }
            }
            
            handle.finish();
            
        } catch (Exception e) {
            String displayName = getDisplayName(project);
            String message     = NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "LBL_Project_cannot_be_deleted", displayName);

            Exceptions.attachLocalizedMessage(e, message);
            throw e;
        }
    }
    
    private static int performDeleteOnProject(Project project, List<FileObject> toDelete ,ProgressHandle handle, int done) throws IOException {
        
            handle.progress(NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "LBL_Progress_Cleaning_Project"));
            
            ProjectOperations.notifyDeleting(project);
            
            handle.progress(++done);
            
            for (FileObject f : toDelete) {
                handle.progress(NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "LBL_Progress_Deleting_File", FileUtil.getFileDisplayName(f)));
                
                if (f != null && f.isValid()) {
                    f.delete();
                }
                
                handle.progress(++done);
            }
            
            FileObject projectFolder = project.getProjectDirectory();
            projectFolder.refresh(); // #190983
            
            if (!projectFolder.isValid()) {
                LOG.log(Level.WARNING, "invalid project folder: {0}", projectFolder);
            } else if (projectFolder.getChildren().length == 0) {
                projectFolder.delete();
            } else {
                LOG.log(Level.WARNING, "project folder {0} was not empty: {1}", new Object[] {projectFolder, Arrays.asList(projectFolder.getChildren())});
            }
            ProjectOperations.notifyDeleted(project);
            return done;
    }
    
    public static void deleteProject(final SuiteProject project) {
        deleteProject(project, new GUIUserInputHandler());
    }
    
    static void deleteProject(final SuiteProject project, UserInputHandler handler) {
        String displayName = getDisplayName(project);
        FileObject projectFolder = project.getProjectDirectory();
        
        LOG.log(Level.FINE, "delete started: {0}", displayName);
        
        final List<FileObject> metadataFiles = ProjectOperations.getMetadataFiles(project);
        final List<FileObject> dataFiles = ProjectOperations.getDataFiles(project);
        final List<FileObject> allFiles = new ArrayList<FileObject>();
        
        allFiles.addAll(metadataFiles);
        allFiles.addAll(dataFiles);
        
        for (Iterator<FileObject> i = allFiles.iterator(); i.hasNext(); ) {
            FileObject f = i.next();
            if (!FileUtil.isParentOf(projectFolder, f)) {
                if (projectFolder.equals(f)) {
                    // sources == project directory
                    continue;
                }
                i.remove();
            }
        }
        
        
        final SubprojectProvider suiteProvider = project.getLookup().lookup(SubprojectProvider.class);
        final Set<Project> subProjects = (Set<Project>) suiteProvider.getSubprojects();
        final Set<NbModuleProject> subModules = new HashSet<NbModuleProject>();
        for(Project prjIter:subProjects) {
            NbModuleProject nbModulePrj = prjIter.getLookup().lookup(NbModuleProject.class);
            if(nbModulePrj != null) {
                subModules.add(nbModulePrj);
            }
        }
        final Map<NbModuleProject,List<FileObject>> subModulesDataFiles = getSubModulesDataFiles(subModules);
        
        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "LBL_Delete_Project_Caption"));
        final DefaultSuiteProjectDeletePanel deletePanel = new DefaultSuiteProjectDeletePanel(handle, displayName, FileUtil.getFileDisplayName(projectFolder), !subModulesDataFiles.isEmpty(), !subModules.isEmpty());
        
        String caption = NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "LBL_Delete_Project_Caption");
        
        handler.showConfirmationDialog(deletePanel, project, caption, "Yes_Button", "No_Button", true, new Executor() { // NOI18N
            public @Override void execute() throws Exception {
                deletePanel.addProgressBar();
                
                close(project);
                
                Map<NbModuleProject,List<FileObject>> subModulesAllFiles = null;
                Map<NbModuleProject,List<FileObject>> subModulesMetadataFiles = null;
                
                if(deletePanel.isDeleteModules()) {
                    for(NbModuleProject modulePrjIter:subModules) {
                        close(modulePrjIter);
                    }
                    subModulesAllFiles = new HashMap<NbModuleProject, List<FileObject>>();
                    subModulesAllFiles.putAll(subModulesDataFiles);
                    subModulesMetadataFiles = getSubModulesMetadataFiles(subModules, subModulesAllFiles);
                } 
                
                if (deletePanel.isDeleteSources()) {
                    performDelete(project, allFiles, subModulesAllFiles, handle);
                } else {
                    performDelete(project, metadataFiles, subModulesMetadataFiles, handle);
                }
                deletePanel.removeProgressBar();
            }
        });
        
        LOG.log(Level.FINE, "delete done: {0}", displayName);
    }
    
    /**Return list of files that are considered metadata files and folders for the all submodules of given project.
     * Returns meaningful values only if some of the <code>is*Supported</code> methods
     * return <code>true</code>.
     *
     * @param subModules submodules of project to test
     * @return list of metadata files/folders
     * @see DataFilesProviderImplementation#getMetadataFiles
     */
    private static Map<NbModuleProject,List<FileObject>> getSubModulesMetadataFiles(Set<NbModuleProject> subModules, Map<NbModuleProject,List<FileObject>> subModulesAllFiles) {
        Map<NbModuleProject,List<FileObject>> result = new HashMap<NbModuleProject, List<FileObject>>();
        List<FileObject> files;
        
        for(NbModuleProject modulePrjIter:subModules){
            files = new ArrayList<FileObject>();
            for (DataFilesProviderImplementation i : modulePrjIter.getLookup().lookupAll(DataFilesProviderImplementation.class)) {
                files.addAll(i.getMetadataFiles());
                assert !files.contains(null) : "Nulls in " + result + " from " + i;
            }
            FileObject subModuleProjectFolder = modulePrjIter.getProjectDirectory();
            for (Iterator<FileObject> i = files.iterator(); i.hasNext(); ) {
                FileObject f = i.next();
                if (!FileUtil.isParentOf(subModuleProjectFolder, f)) {
                    if (subModuleProjectFolder.equals(f)) {
                        // sources == project directory
                        continue;
                    }
                    i.remove();
                }
            }
            result.put(modulePrjIter, files);
            subModulesAllFiles.get(modulePrjIter).addAll(files);
        }
        
        return result;
    } 
    
    /**Return list of files that are considered source files and folders for the all submodules of given project.
     * Returns meaningful values only if some of the <code>is*Supported</code> methods
     * return <code>true</code>.
     *
     * @param subModules submodules of project to test
     * @return list of data files/folders
     * @see DataFilesProviderImplementation#getDataFiles
     */
    private static Map<NbModuleProject,List<FileObject>> getSubModulesDataFiles(Set<NbModuleProject> subModules) {
        Map<NbModuleProject,List<FileObject>> result = new HashMap<NbModuleProject, List<FileObject>>();
        List<FileObject> files;
        
        for(NbModuleProject modulePrjIter:subModules){
            files = new ArrayList<FileObject>();
            for (DataFilesProviderImplementation i : modulePrjIter.getLookup().lookupAll(DataFilesProviderImplementation.class)) {
                files.addAll(i.getDataFiles());
                assert !files.contains(null) : "Nulls in " + result + " from " + i;
            }
            FileObject subModuleProjectFolder = modulePrjIter.getProjectDirectory();
            for (Iterator<FileObject> i = files.iterator(); i.hasNext(); ) {
                FileObject f = i.next();
                if (!FileUtil.isParentOf(subModuleProjectFolder, f)) {
                    if (subModuleProjectFolder.equals(f)) {
                        // sources == project directory
                        continue;
                    }
                    i.remove();
                }
            }
            result.put(modulePrjIter, files);
        }
        
        return result;
    }
    
    static interface UserInputHandler {
        void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor);
    }
    
    private static final class GUIUserInputHandler implements UserInputHandler {
        
        public @Override void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor) {
            DefaultSuiteProjectOperationsImplementation.showConfirmationDialog(panel, project, caption, confirmButton, cancelButton, doSetMessageType, executor);
        }
        
    }
    //</editor-fold>
    
    private static JComponent wrapPanel(JComponent component) {
        component.setBorder(new EmptyBorder(12, 12, 12, 12));
        
        return component;
    }
    
    private static void showConfirmationDialog(final JComponent panel, Project project, String caption, String confirmButton, String cancelButton, boolean doSetMessageType, final Executor executor) {
        final JButton confirm = new JButton();
        Mnemonics.setLocalizedText(confirm, NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "LBL_" + confirmButton));
        final JButton cancel  = new JButton(cancelButton == null ?
              NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "LBL_Cancel_Button")
            : NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "LBL_" + cancelButton));
        
        confirm.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "ACSD_" + confirmButton));
        cancel.getAccessibleContext().setAccessibleDescription(cancelButton == null ?
              NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "ACSD_Cancel_Button")
            : NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "ACSD_" + cancelButton));
        
        assert panel instanceof InvalidablePanel;
        
        ((InvalidablePanel) panel).addChangeListener(new ChangeListener() {
            public @Override void stateChanged(ChangeEvent e) {
                confirm.setEnabled(((InvalidablePanel) panel).isPanelValid());
            }
        });
        
        confirm.setEnabled(((InvalidablePanel) panel).isPanelValid());
        
        final Dialog[] dialog = new Dialog[1];
        
        DialogDescriptor dd = new DialogDescriptor(doSetMessageType ? panel : wrapPanel(panel), caption, true, new Object[] {confirm, cancel}, cancelButton != null ? cancel : confirm, DialogDescriptor.DEFAULT_ALIGN, null, new ActionListener() {
            private boolean operationRunning;
            public @Override void actionPerformed(ActionEvent e) {
                //#65634: making sure that the user cannot close the dialog before the operation is finished:
                if (operationRunning) {
                    return ;
                }
                
                if (dialog[0] instanceof JDialog) {
                    ((JDialog) dialog[0]).getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).remove(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
                    ((JDialog) dialog[0]).setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                }
                
                operationRunning = true;
		
                if (e.getSource() == confirm) {
                    confirm.setEnabled(false);
                    cancel.setEnabled(false);
                    ((InvalidablePanel) panel).showProgress();
                    
                    Component findParent = panel;
                    
                    while (findParent != null && !(findParent instanceof Window)) {
                        findParent = findParent.getParent();
                    }
                    
                    if (findParent != null) {
                        ((Window) findParent).pack();
                    }
                    
                    RequestProcessor.getDefault().post(new Runnable() {
                        public @Override void run() {
                            final AtomicReference<Throwable> e = new AtomicReference<Throwable>();
                            try {
                                executor.execute();
                            } catch (Throwable ex) {
                                e.set(ex);
                                if (ex instanceof ThreadDeath) {
                                    throw (ThreadDeath)ex;
                                }
                            } finally {                            
                                SwingUtilities.invokeLater(new Runnable() {
                                    public @Override void run() {
                                        dialog[0].setVisible(false);
                                        if (e.get() != null) {
                                            LOG.log(Level.WARNING, null, e.get());
                                        }
                                    }
                                });
                            }
                        }
                    });
                } else {
                    dialog[0].setVisible(false);
                }
            }
        });
        
        if (doSetMessageType) {
            dd.setMessageType(NotifyDescriptor.QUESTION_MESSAGE);
        }
        
        dd.setClosingOptions(new Object[0]);
        
        dialog[0] = DialogDisplayer.getDefault().createDialog(dd);
        
        dialog[0].setVisible(true);
        
        dialog[0].dispose();
        dialog[0] = null;
    }
    
    static @CheckForNull String computeError(@NullAllowed File location, String projectNameText, boolean pureRename) {
        return computeError(location, projectNameText, null, pureRename);
    }
    
    static @CheckForNull String computeError(@NullAllowed File location, String projectNameText, String projectFolderText, boolean pureRename) {
        if (projectNameText.length() == 0) {
            return NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "ERR_Project_Name_Must_Entered");
        }
        if (projectNameText.indexOf('/') != -1 || projectNameText.indexOf('\\') != -1) {
            return NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "ERR_Not_Valid_Filename", projectNameText);
        }

        if (location == null) {
            return null; // #199241: skip other checks for remote projects
        }

        File parent = location;
        if (!location.exists()) {
            //if some dirs in teh chain are not created, consider it ok.
            parent = location.getParentFile();
            while (parent != null && !parent.exists()) {
                parent = parent.getParentFile();
            }
            if (parent == null) {
                return NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "ERR_Location_Does_Not_Exist");
            }
        }
        
        if (!parent.canWrite()) {
            return NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "ERR_Location_Read_Only");
        }
        
        File projectFolderFile = null;
        if (projectFolderText == null) {
            projectFolderFile = new File(location, projectNameText);
        } else {
            projectFolderFile = new File(projectFolderText);
        }
        
        if (projectFolderFile.exists() && !pureRename) {
            return NbBundle.getMessage(DefaultSuiteProjectOperationsImplementation.class, "ERR_Project_Folder_Exists");
        }
        
        return null;
    }
    
    private static void close(final Project prj) {
        LifecycleManager.getDefault().saveAll();
        OpenProjects.getDefault().close(new Project[] {prj});
    }
    
    static interface Executor {
        public void execute() throws Exception;
    }
    
    public static interface InvalidablePanel {
        public void addChangeListener(ChangeListener l);
        public void removeChangeListener(ChangeListener l);
        public boolean isPanelValid();
        public void showProgress();
    }
    //</editor-fold>
    
}
