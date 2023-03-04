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

package org.netbeans.modules.openfile;

import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileChooserBuilder.SelectionApprover;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.UserCancelException;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action which allows user open file from disk. It is installed
 * in Menu | File | Open file... .
 *
 * @author Jesse Glick
 * @author Marian Petras
 */
@ActionRegistration(
    displayName="#LBL_openFile", 
    iconBase="org/netbeans/modules/openfile/openFile.png",
    iconInMenu=false
)
@ActionID(category="System", id="org.netbeans.modules.openfile.OpenFileAction")
@ActionReference(path="Menu/File", position=800)
public class OpenFileAction implements ActionListener {

    /** stores the last current directory of the file chooser */
    private static File currentDirectory = null;

    /** stores tha last used file filter */
    private static String currentFileFilter = null;

    public OpenFileAction() {
    }

    private HelpCtx getHelpCtx() {
        return new HelpCtx(OpenFileAction.class);
    }

    /**
     * Creates and initializes a file chooser.
     *
     * @return  the initialized file chooser
     */
    protected JFileChooser prepareFileChooser() {
        FileChooserBuilder fcb = new FileChooserBuilder(OpenFileAction.class);
        fcb.setSelectionApprover(new OpenFileSelectionApprover());
        fcb.setFilesOnly(true);
        fcb.addDefaultFileFilters();
        for (OpenFileDialogFilter filter :
                Lookup.getDefault().lookupAll(OpenFileDialogFilter.class)) {
            fcb.addFileFilter(filter);
        }
        JFileChooser chooser = fcb.createFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.getCurrentDirectory().listFiles(); //preload
        chooser.setCurrentDirectory(getCurrentDirectory());
        if (currentFileFilter != null) {
            for (FileFilter ff : chooser.getChoosableFileFilters()) {
                if (currentFileFilter.equals(ff.getDescription())) {
                    chooser.setFileFilter(ff);
                    break;
                }
            }
        }
        HelpCtx.setHelpIDString(chooser, getHelpCtx().getHelpID());
        return chooser;
    }
    
    /**
     * Displays the specified file chooser and returns a list of selected files.
     *
     * @param  chooser  file chooser to display
     * @return  array of selected files,
     * @exception  org.openide.util.UserCancelException
     *                     if the user cancelled the operation
     */
    public static File[] chooseFilesToOpen(JFileChooser chooser)
            throws UserCancelException {
        File[] files;
        do {
            int selectedOption = chooser.showOpenDialog(
                WindowManager.getDefault().getMainWindow());
            
            if (selectedOption != JFileChooser.APPROVE_OPTION) {
                throw new UserCancelException();
            }
            files = chooser.getSelectedFiles();
        } while (files.length == 0);
        return files;
    }

    private static boolean running;
    /**
     * {@inheritDoc} Displays a file chooser dialog
     * and opens the selected files.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            return;
        }
        try {
            running = true;
            JFileChooser chooser = prepareFileChooser();
            File[] files;
            try {
                if( Boolean.getBoolean("nb.native.filechooser") ) { //NOI18N
                    String oldFileDialogProp = System.getProperty("apple.awt.fileDialogForDirectories"); //NOI18N
                    System.setProperty("apple.awt.fileDialogForDirectories", "false"); //NOI18N
                    FileDialog fileDialog = new FileDialog(WindowManager.getDefault().getMainWindow());
                    fileDialog.setMode(FileDialog.LOAD);
                    fileDialog.setDirectory(getCurrentDirectory().getAbsolutePath());
                    fileDialog.setTitle(chooser.getDialogTitle());
                    fileDialog.setVisible(true);
                    if( null != oldFileDialogProp ) {
                        System.setProperty("apple.awt.fileDialogForDirectories", oldFileDialogProp); //NOI18N
                    } else {
                        System.clearProperty("apple.awt.fileDialogForDirectories"); //NOI18N
                    }

                    if( fileDialog.getDirectory() != null && fileDialog.getFile() != null ) {
                        String selFile = fileDialog.getFile();
                        File dir = new File( fileDialog.getDirectory() );
                        files = new File[] { new File( dir, selFile ) };
                        currentDirectory = dir;
                    } else {
                        throw new UserCancelException();
                    }
                } else {
                    files = chooseFilesToOpen(chooser);
                    currentDirectory = chooser.getCurrentDirectory();
                    if (chooser.getFileFilter() != null) { // #227187
                        currentFileFilter =
                                chooser.getFileFilter().getDescription();
                    }
                }
            } catch (UserCancelException ex) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                OpenFile.openFile(files[i], -1);
            }
        } finally {
            running = false;
        }
    }
    
    private static File getCurrentDirectory() {
        if (Boolean.getBoolean("netbeans.openfile.197063")) {
            // Prefer to open from parent of active editor, if any.
            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated != null && WindowManager.getDefault().isOpenedEditorTopComponent(activated)) {
                DataObject d = activated.getLookup().lookup(DataObject.class);
                if (d != null) {
                    File f = FileUtil.toFile(d.getPrimaryFile());
                    if (f != null) {
                        return f.getParentFile();
                    }
                }
            }
        }
        // Otherwise, use last-selected directory, if any.
        if(currentDirectory != null && currentDirectory.exists()) {
            return currentDirectory;
        }
        // Fall back to default location ($HOME or similar).
        currentDirectory =
                FileSystemView.getFileSystemView().getDefaultDirectory();
        return currentDirectory;
    }

    private static class OpenFileSelectionApprover
            implements SelectionApprover {

        @Override
        public boolean approve(File[] selectedFiles) {
            /* check the files: */
            List<String> errorMsgs = null;
            for (int i = 0; i < selectedFiles.length; i++) {
                String msgPatternRef = null;
                File file = selectedFiles[i];

                if (!file.exists()) {
                    msgPatternRef = "MSG_FileDoesNotExist";             //NOI18N
                } else if (file.isDirectory()) {
                    msgPatternRef = "MSG_FileIsADirectory";             //NOI18N
                } else if (!file.isFile()) {
                    msgPatternRef = "MSG_FileIsNotPlainFile";           //NOI18N
                }
                if (msgPatternRef == null) {
                    continue;
                }
                if (errorMsgs == null) {
                    errorMsgs = new ArrayList<String>(selectedFiles.length - i);
                }
                errorMsgs.add(NbBundle.getMessage(OpenFileAction.class,
                        msgPatternRef, file.getName()));
            }
            if (errorMsgs == null) {
                return true;
            } else {
                JPanel panel = new JPanel(
                        new GridLayout(errorMsgs.size(), 0, 0, 2));   //gaps
                for (String errMsg : errorMsgs) {
                    panel.add(new JLabel(errMsg));
                }
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(
                        panel, NotifyDescriptor.WARNING_MESSAGE));
                return false;
            }
        }
    }
}
