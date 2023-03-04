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

package org.netbeans.modules.pdf;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Permits a PDF file to be opened in an external viewer.
 *
 * @author Jesse Glick
 * @author Marian Petras
 */
class PDFOpenSupport implements OpenCookie {

    private static final Logger LOG = Logger.getLogger(
            PDFOpenSupport.class.getName());
    static final String FILENAME_PLACEHOLDER = "$file";                 //NOI18N
    private static final String DATA_FOLDER =
            "org-netbeans-modules-pdf";                                 //NOI18N
    private static final String DATA_FILE = "cmd.data";                 //NOI18N
    private static final String CMD_ATTR = "cmd";                       //NOI18N

    private File f;
    private DataObject dObj;
    
    /**
     * @exception  java.lang.IllegalArgumentException
     *             if the specified file does not exist or is not a plain file
     */
    public PDFOpenSupport(File f) {
        this.f = f;
        try {
            this.dObj = DataObject.find(FileUtil.toFileObject(f));
        } catch (DataObjectNotFoundException ex) {
            LOG.log(Level.INFO, null, ex);
        }
    }

    public PDFOpenSupport(DataObject dObj) {
        this.dObj = dObj;
        this.f = FileUtil.toFile(dObj.getPrimaryFile());
    }

    @Override
    public void open() {
        if(dObj != null){
            f = FileUtil.toFile(dObj.getPrimaryFile());
        }
        try {
            Desktop.getDesktop().open(f);
        } catch (Exception ex) {
            LOG.log(Level.INFO, "java.awt.Desktop.open() failed.", ex); //NOI18N
            openFallback(f);
        }
    }

    /**
     * Open - alternative fallback method.
     *
     * Used only if Desktop is not available or has failed to open the file.
     *
     * @param file PDF file to open.
     */
    private void openFallback(File file) {
        String customCommand = getCustomCommand();
        if (customCommand == null || customCommand.isEmpty()) {
            askForCustomCommand(file);
        } else if (customCommand.indexOf(FILENAME_PLACEHOLDER) == -1) {
            askForCustomCommand(file);
        } else {
            try {
                openWithCustomCommand(customCommand, file);
            } catch (Exception e) {
                askForCustomCommand(file);
            }
        }
    }

    /**
     * Retrieve stored custom command from System FileSystem.
     *
     * @return Stored command, or null if not available.
     */
    private String getCustomCommand() {

        FileObject root = FileUtil.getConfigRoot();
        FileObject d = root.getFileObject(DATA_FOLDER
                + "/" + DATA_FILE);                                     //NOI18N
        if (d != null) {
            return (String) d.getAttribute(CMD_ATTR);
        } else {
            return null;
        }
    }

    /**
     * Store custom command to System Filesystem.
     *
     * @param customCommand Command to store.
     */
    private void storeCustomCommand(String customCommand) throws IOException {
        FileObject root = FileUtil.getConfigRoot();
        FileObject folder = root.getFileObject(DATA_FOLDER);
        if (folder == null) {
            folder = root.createFolder(DATA_FOLDER);
        }
        FileObject data = folder.getFileObject(DATA_FILE);
        if (data == null) {
            data = folder.createData(DATA_FILE);
        }
        data.setAttribute(CMD_ATTR, customCommand);
    }

    /**
     * Opens a dialog to specify command for opening PDF files.
     *
     * If OK button is clicked and specified command is invalid, the dialog
     * appears again.
     */
    private void askForCustomCommand(File f) {
        SetCommandForm scf = new SetCommandForm(getCustomCommand());
        DialogDescriptor desc = new DialogDescriptor(scf,
                NbBundle.getMessage(
                PDFOpenSupport.class, "SetCommandDialog.title"));       //NOI18N

        Object result = DialogDisplayer.getDefault().notify(desc);

        if (result == DialogDescriptor.OK_OPTION) {
            String command = scf.getCommand();
            try {
                storeCustomCommand(command);
                try {
                    openWithCustomCommand(command, f);
                } catch (Exception e) {
                    askForCustomCommand(f);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Open PDF with a custom command.
     *
     * @param command Command and arguments to open PDF files. On of arguments
     * should equal string $file, to be replaced with actual file path.
     * @param file file to open in a PDF viewer.
     */
    private void openWithCustomCommand(String command, File file)
            throws IOException {
        String[] cmds = command.split(" ");                             //NOI18N
        for (int i = 0; i < cmds.length; i++) {
            if (FILENAME_PLACEHOLDER.equals(cmds[i])) {
                cmds[i] = file.getAbsolutePath();
            }
        }
        Runtime.getRuntime().exec(cmds);
    }
}
