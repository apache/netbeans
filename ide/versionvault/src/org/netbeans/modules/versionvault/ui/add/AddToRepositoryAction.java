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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.add;

import org.openide.util.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.*;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.modules.versionvault.ui.wizard.AbstractStep;
import org.netbeans.modules.versionvault.ui.wizard.RepositoryStep;
import org.netbeans.modules.versionvault.ui.wizard.Repository;
import org.netbeans.modules.versionvault.ui.selectors.ModuleSelector;
import org.netbeans.modules.versionvault.util.ProgressSupport;
import org.netbeans.modules.versionvault.client.Clearfsimport;
import org.netbeans.modules.versionvault.client.NotificationListener;
import org.netbeans.modules.versionvault.*;
import org.netbeans.modules.versionvault.ui.checkin.CheckinOptions;
import org.netbeans.modules.versionvault.ClearcaseException;
import org.netbeans.modules.versionvault.FileStatusCache;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.versioning.util.CommandReport;
import org.netbeans.modules.versioning.util.Utils;


import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.io.*;
import java.util.*;
import java.text.MessageFormat;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Imports folder into Clearcase repository. It's enabled on Nodes that represent:
 * <ul>
 * <li>project root directory, parent of all necessary
 * project data and metadata.
 * <li>folders that are not a part on any project
 * </ul>
 * It's minimalitics attempt to assure
 * that the project can be reopend after checkout.
 * It also simplifies implemenattion avoiding huge
 * import mapping wizard for projects with external
 * data folders.
 *
 *
 * @author Petr Kuzel, Ramin Moazeni
 */
public final class AddToRepositoryAction extends AbstractAction implements ChangeListener, NotificationListener {

    private WizardDescriptor wizard;
    public static boolean  canBeCheckedOut = false;
    private WizardDescriptor.Iterator wizardIterator;

    private RepositoryStep repositoryStep;
    private Repository repository;
    private ImportStep importStep;
    
    private final VCSContext context;

    private CheckinOptions[] checkinOptions;
    private File [] files2;
    
    public static String vobTag;
    
    private static int ALLOW_IMPORT = 
            FileInformation.STATUS_NOTVERSIONED_NOTMANAGED | 
            FileInformation.STATUS_UNKNOWN;
    
    public AddToRepositoryAction(String name, VCSContext context) {
        super(name);
        this.context = context;
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    @Override
    public boolean isEnabled() {
        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();
        Set<File> roots = context.getRootFiles();
        for (File file : roots) {
            if( (cache.getInfo(file).getStatus() & ALLOW_IMPORT) == 0 ) {
                return false;
            }                
        }
        return true;
    }
    public String getName() {
        return NbBundle.getMessage(AddToRepositoryAction.class, "BK0006");
    }

    public void actionPerformed(ActionEvent e) {
        // TODO: only import one folder? seems most reasonable
        final File importDirectory = context.getRootFiles().iterator().next();
             
        if (importDirectory != null) {

            String prefRoot = NbBundle.getMessage(AddToRepositoryAction.class, "BK0008");
            String prefModule = importDirectory.getName();
                                        
            wizardIterator = panelIterator(prefRoot, prefModule, importDirectory.getAbsolutePath());
            wizard = new WizardDescriptor(wizardIterator);
            wizard.putProperty(WizardDescriptor.PROP_CONTENT_DATA,  // NOI18N
                    new String[] {
                        NbBundle.getMessage(AddToRepositoryAction.class, "BK0015"),
                        NbBundle.getMessage(AddToRepositoryAction.class, "BK0014")
                    }
            );
            wizard.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);  // NOI18N
            wizard.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);  // NOI18N
            wizard.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);  // NOI18N
            wizard.setTitleFormat(new MessageFormat("{0}"));  // NOI18N
            String title = NbBundle.getMessage(AddToRepositoryAction.class, "BK0007");
            wizard.setTitle(title);

            Object result = DialogDisplayer.getDefault().notify(wizard);
            if (result == DialogDescriptor.OK_OPTION) {
                FileObject projectFolder = FileUtil.toFileObject(importDirectory);
                if (projectFolder != null) {
                    try {
                        Project p = ProjectManager.getDefault().findProject(projectFolder);
                        if (p != null) {
                            closeProject(p);
                        }
                    } catch (IOException e1) {
                        final List<String> errors = new ArrayList<String>(100);
                        Logger.getLogger(AddToRepositoryAction.class.getName()).fine("AddToRepositoryAction: Could not close original project");
                        if (e1 != null) {
                            errors.add(e1.getMessage());
                            Utils.logWarn(this, e1);
                        }
                        report(NbBundle.getMessage(AddToRepositoryAction.class, "BK1014"), NbBundle.getMessage(AddToRepositoryAction.class, "BK1014"), errors, NotifyDescriptor.ERROR_MESSAGE); //NOI18N

                    }
                }
            }              
        }
    }

    /**
     * @return Map&lt;SvnFileNode, CommitOptions>
     */
    public Map<ClearcaseFileNode, CheckinOptions> getAddFiles(ClearcaseFileNode[] nodes) {
        Map<ClearcaseFileNode, CheckinOptions> ret = new HashMap<ClearcaseFileNode, CheckinOptions>(nodes.length);
       
        for (int i = 0; i < nodes.length; i++) {
            ret.put(nodes[i], checkinOptions[i]);
        }
        return ret;
    }
    
    
    private void async() {

        final String logMessage = importStep.getMessage();    //import message
        final String module = importStep.getModule();         //repository module
        final String folder = importStep.getFolder();         //folder to import
        final String selectedRoot = repositoryStep.getRepositoryFile().getFileUrl();

        final File dir = new File(folder);

        final File[] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
        if (files == null || files.length == 0) {
            ErrorManager.getDefault().notify(new ClearcaseException("No files selected. Please select a file for checkin."));
            return;
        }
   
        try {
            Clearfsimport cfs = new Clearfsimport();
            cfs.doRecursiveCheckin(selectedRoot, module, logMessage, dir);
            canBeCheckedOut = true;
            File newProject = new File(module + "/" + files[0].getName());
            FileObject projectFolder = FileUtil.toFileObject(newProject);
            if (projectFolder != null) {
                Project p = ProjectManager.getDefault().findProject(projectFolder);
                if (p != null) {
                    openProject(p);
                }
            }
        } catch (IOException e) {
            final List<String> errors = new ArrayList<String>(100);
            Logger.getLogger(AddToRepositoryAction.class.getName()).fine("AddToRepositoryAction: Could not open imported project");
            if (e != null) {
                errors.add(e.getMessage());
                Utils.logWarn(this, e);
            }
            report(NbBundle.getMessage(AddToRepositoryAction.class, "BK1015"), NbBundle.getMessage(AddToRepositoryAction.class, "BK1015"), errors, NotifyDescriptor.ERROR_MESSAGE); //NOI18N

        }
    }
    public boolean cancel() {
        return true;
    }

    private WizardDescriptor.Iterator panelIterator(String root, String module, String folder) {
        repositoryStep = new RepositoryStep(RepositoryStep.IMPORT_HELP_ID);
        repositoryStep.addChangeListener(this);
        importStep = new ImportStep(module, folder);
        importStep.addChangeListener(this);

        final WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[2];
        panels[0] = repositoryStep;
        panels[1] = importStep;

        WizardDescriptor.ArrayIterator ret = new WizardDescriptor.ArrayIterator(panels) {
            public WizardDescriptor.Panel current() {
                WizardDescriptor.Panel ret = super.current();
                for (int i = 0; i<panels.length; i++) {
                    if (panels[i] == ret) {
                        wizard.putProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));  // NOI18N
                    }
                }
                return ret;
            }
        };
        return ret;
    }

    private void report(String title, String prompt, List<String> messages, int type) {
        boolean emptyReport = true;
        for (String message : messages) {
            if (message != null && message.length() > 0) {
                emptyReport = false;
                break;
            }
        }
        if (emptyReport) {
            return;
        }
        CommandReport report = new CommandReport(prompt, messages);
        JButton ok = new JButton(NbBundle.getMessage(Clearfsimport.class, "CommandReport_OK")); //NOI18N

        NotifyDescriptor descriptor = new NotifyDescriptor(
                report,
                title,
                NotifyDescriptor.DEFAULT_OPTION,
                type,
                new Object[]{ok},
                ok);
        DialogDisplayer.getDefault().notify(descriptor);
    }
    private void setErrorMessage(String msg) {
        if (wizard != null) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, msg); // NOI18N
        }
    }

    public void stateChanged(ChangeEvent e) {
        AbstractStep step = (AbstractStep) wizardIterator.current();
        setErrorMessage(step.getErrorMessage());
    }

    class ImportStep extends AbstractStep implements ActionListener {
        private final String module;
        private final String folder;
        private ImportPanel importPanel;

        public ImportStep(String module, String folder) {
            this.module = module;
            this.folder = folder;
        }

        public HelpCtx getHelp() {
            return new HelpCtx(ImportStep.class);
        }

        protected JComponent createComponent() {
            vobTag = repositoryStep.getRepositoryFile().getFileUrl();
            
            importPanel = new ImportPanel();
            importPanel.folderTextField.setText(folder);
            importPanel.moduleTextField.setText(vobTag);
            // user input validation
            DocumentListener validation = new DocumentListener() {
                public void changedUpdate(DocumentEvent e) {
                }
                public void insertUpdate(DocumentEvent e) {
                    String s = checkInput(importPanel);
                    if (s == null) {
                        valid();
                    } else {
                        invalid(s);
                    }
                }
                public void removeUpdate(DocumentEvent e) {
                    String s = checkInput(importPanel);
                    if (s == null) {
                        valid();
                    } else {
                        invalid(s);
                    }
                }
            };
            importPanel.moduleTextField.getDocument().addDocumentListener(validation);
            importPanel.commentTextArea.getDocument().addDocumentListener(validation);
            importPanel.folderTextField.getDocument().addDocumentListener(validation);
            importPanel.folderButton.addActionListener(this);
            importPanel.moduleButton.addActionListener(this);

            String s = checkInput(importPanel);
            if (s == null) {
                valid();
            } else {
                invalid(s);
            }
    
            return importPanel;
        }

        protected void validateBeforeNext() {

            ProgressSupport ps = new ProgressSupport(Clearcase.getInstance().getClient().getRequestProcessor(), NbBundle.getMessage(AddToRepositoryAction.class, "Progress_Import")) { //NOI18N


                @Override
                protected void perform() {
                    String invalidMsg = null;
                    String folder = importStep.getFolder();         //folder to import

                    try {
                        if (!validateUserInput()) {
                            return;
                        }
                        invalid(null);

                        File dir = new File(folder);    //import directory
                        File[] files = context.getRootFiles().toArray(new File[context.getRootFiles().size()]);
                        if (files == null || files.length == 0) {
                            ErrorManager.getDefault().notify(new ClearcaseException("No files selected. Please select a file for checkin."));
                            return;
                        }
                        async();

                        if (isCanceled()) {
                            return;
                        }

                        // XXX this is ugly and expensive! the client should notify (onNotify()) the cache. find out why it doesn't work...
                        forceStatusRefresh(dir);
                        if (isCanceled()) {
                            return;
                        }
                      
                    }
                    catch (Exception e)
                    {
                        final List<String> errors = new ArrayList<String>(100);
                        Logger.getLogger(AddToRepositoryAction.class.getName()).fine("ImportStep: Import Action Failed.");
                        if (e != null) {
                            errors.add(e.getMessage());
                            Utils.logWarn(this, e);
                        }
                        report(NbBundle.getMessage(ImportStep.class, "MSG_Import_ActionFailed"), NbBundle.getMessage(ImportStep.class, "MSG_Import_ActionCanceled"), errors, NotifyDescriptor.ERROR_MESSAGE); //NOI18N

                    }
                    finally {

                        if (isCanceled()) {
                            valid(org.openide.util.NbBundle.getMessage(ImportStep.class, "MSG_Import_ActionCanceled")); // NOI18N

                        } else if (invalidMsg != null) {
                            valid(invalidMsg);
                        } else {
                            valid();
                        }
                    }
                }
            };
            ps.start();
        }

        public String getMessage() {
            return importPanel.commentTextArea.getText();
        }

        public String getModule() {
            return importPanel.moduleTextField.getText();
        }

        public String getFolder() {
            return importPanel.folderTextField.getText();
        }
        
        public String getImportMessage() {
            return importPanel.commentTextArea.getText();
        }
        /**
         * Returns file to be initaly used.
         * <ul>
         * <li>first is takes text in workTextField
         * <li>then recent project folder
         * <li>finally <tt>user.home</tt>
         * <ul>
         */
        private File defaultWorkingDirectory() {
            File defaultDir = null;
            String current = importPanel.folderTextField.getText();
            if (current != null && !(current.trim().equals(""))) {  // NOI18N
                File currentFile = new File(current);
                while (currentFile != null && currentFile.exists() == false) {
                    currentFile = currentFile.getParentFile();
                }
                if (currentFile != null) {
                    if (currentFile.isFile()) {
                        defaultDir = currentFile.getParentFile();
                    } else {
                        defaultDir = currentFile;
                    }
                }
            }

            if (defaultDir == null) {
                File projectFolder = ProjectChooser.getProjectsFolder();
                if (projectFolder.exists() && projectFolder.isDirectory()) {
                    defaultDir = projectFolder;
                }
            }

            if (defaultDir == null) {
                defaultDir = new File(System.getProperty("user.home"));  // NOI18N
            }

            return defaultDir;
        }

        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == importPanel.folderButton) {
                File defaultDir = defaultWorkingDirectory();
                JFileChooser fileChooser = new JFileChooser(defaultDir);
                fileChooser.setDialogTitle(NbBundle.getMessage(AddToRepositoryAction.class, "BK1017"));
                fileChooser.setMultiSelectionEnabled(false);
                javax.swing.filechooser.FileFilter[] old = fileChooser.getChoosableFileFilters();
                for (int i = 0; i < old.length; i++) {
                    javax.swing.filechooser.FileFilter fileFilter = old[i];
                    fileChooser.removeChoosableFileFilter(fileFilter);

                }
                fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
                    public boolean accept(File f) {
                        return f.isDirectory();
                    }
                    public String getDescription() {
                        return NbBundle.getMessage(AddToRepositoryAction.class, "BK1018");
                    }
                });
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showDialog(importPanel, NbBundle.getMessage(AddToRepositoryAction.class, "BK1019"));
                File f = fileChooser.getSelectedFile();
                if (f != null) {
                    importPanel.folderTextField.setText(f.getAbsolutePath());
                }
            } else if (e.getSource() == importPanel.moduleButton) {
                ModuleSelector selector = new ModuleSelector();
                vobTag = repositoryStep.getRepositoryFile().getFileUrl();
                                                
                String path = selector.selectRepositoryPath(vobTag);
                if (path != null) {
                    if (path.equals(""))
                    {
                        importPanel.moduleTextField.setText(vobTag);
                    }
                    else
                        importPanel.moduleTextField.setText(path);
                }
                    
            }
        }
        
        public boolean validateUserInput() {
            invalid(null);
            
            String text = importPanel.moduleTextField.getText().trim();
            if (text.length() == 0) {
                invalid(org.openide.util.NbBundle.getMessage(ImportStep.class, "BK2014")); // NOI18N
                return false;
            }
            
            text = importPanel.commentTextArea.getText().trim();
            boolean valid = text.length() > 0;
            if(valid) {
                valid();
            } else {
                invalid(org.openide.util.NbBundle.getMessage(ImportStep.class, "CTL_Import_MessageRequired")); // NOI18N
            }
            
            return valid;
        }
        
        private void forceStatusRefresh(File file) {
            if(!file.isFile()) {
                File[] files = file.listFiles();
                if(files == null) {
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    forceStatusRefresh(files[i]);
                }
            }
        }
    }

    private static String checkInput(ImportPanel importPanel) {
        boolean valid = true;
        
        File file = new File(importPanel.folderTextField.getText());
        valid &= file.isDirectory();
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0022");
        try {
            valid &= Clearcase.getInstance().isManaged(file) == false;
        } catch (Exception iox) {
            NotifyDescriptor.Exception e = new NotifyDescriptor.Exception(iox);
            DialogDisplayer.getDefault().notifyLater(e);
        }
        
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0023");

        valid &= importPanel.commentTextArea.getText().trim().length() > 0;
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0024");

        String module = importPanel.moduleTextField.getText().trim();
        valid &= module.length() > 0;
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0025");
        valid &= module.indexOf(" ") == -1;  // NOI18N // NOI18N
        valid &= ".".equals(module.trim()) == false;  // NOI18N
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0026");
      
        File moduleText = new File(importPanel.moduleTextField.getText());
        valid &= moduleText.isDirectory();
        if (!valid) return NbBundle.getMessage(AddToRepositoryAction.class, "BK0026");

        return null;
    }

    /**
     * @return false on Thread.interrupted i.e. user cancel.
     */
    private boolean prepareIgnore(File dir) throws IOException {
        File[] projectMeta = dir.listFiles();
        Set ignored = new HashSet();
        for (int i = 0; i < projectMeta.length; i++) {
            if (Thread.interrupted()) {
                return false;
            }
            File file = projectMeta[i];
            String name = file.getName();
            int sharability = SharabilityQuery.getSharability(file);
            if (sharability == SharabilityQuery.MIXED) {
                assert file.isDirectory() : file;
                prepareIgnore(file);
            }
        }
        return true;
    }

    protected boolean asynchronous() {
        return false;
    }
    
    public void setEditable(boolean bl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private void closeProject(Project p) {
        Project[] projects = new Project[]{p};
        OpenProjects.getDefault().close(projects);
    }
    private void openProject(Project p) {
        Project[] projects = new Project[]{p};
        OpenProjects.getDefault().open(projects, false);
    }
    
    public void commandStarted()        { /* boring */ }
    public void outputText(String line) { /* boring */ }
    public void errorText(String line)  { /* boring */ }
    public void commandFinished() {               
        org.netbeans.modules.versionvault.util.ClearcaseUtils.afterCommandRefresh(files2, false);        
    }    
    
}
