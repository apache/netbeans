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
package org.netbeans.modules.cnd.utils.ui;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

public class FileChooser extends JFileChooser {

    private static File currentChooserFile = null;

    public FileChooser(String titleText, String buttonText, int mode, FileFilter[] filters, String feedFilePath, boolean useParent) {
        super(computeCurrentDir(feedFilePath, useParent));
        setFileHidingEnabled(false);
        setFileSelectionMode(mode);
        setDialogTitle(titleText);
        if (buttonText != null) {
            setApproveButtonText(buttonText);
        }
        if (filters != null) {
            for (int i = 0; i < filters.length; i++) {
                addChoosableFileFilter(filters[i]);
            }
            setFileFilter(filters[0]);
        }
        File parent = getCurrentDirectory();
        File file = FileChooser.getCurrentChooserFile();
        if (parent != null && file != null && parent.equals(file.getParentFile())) {
            setSelectedFile(file);
        }
    }

    private static File computeCurrentDir(String feedFilePath, boolean useParent) {
        File feedFilePathFile = null;
        if (feedFilePath != null && feedFilePath.length() > 0) {
            feedFilePathFile = new File(feedFilePath);
            // Does not canonize path, see Bug #216910
            //try {
            //    feedFilePathFile = feedFilePathFile.getCanonicalFile();
            //} catch (IOException e) {
            //}
        }
        if (feedFilePathFile != null && feedFilePathFile.exists()) {
            FileChooser.setCurrentChooserFile(feedFilePathFile);
        }
        if (FileChooser.getCurrentChooserFile() == null && feedFilePathFile == null) {
            feedFilePathFile = new File(getLastPath());
        }
        if (FileChooser.getCurrentChooserFile() == null && feedFilePathFile != null && 
            feedFilePathFile.getParentFile() != null && feedFilePathFile.getParentFile().exists()) {
            FileChooser.setCurrentChooserFile(feedFilePathFile.getParentFile());
            useParent = false;
        }
        if (FileChooser.getCurrentChooserFile() != null) {
            if (useParent) {
                if (FileChooser.getCurrentChooserFile() != null && FileChooser.getCurrentChooserFile().exists()) {
                    return FileChooser.getCurrentChooserFile().getParentFile();
                }
            } else {
                if (FileChooser.getCurrentChooserFile() != null && FileChooser.getCurrentChooserFile().exists()) {
                    return FileChooser.getCurrentChooserFile();
                }
            }
        } else {
            String sd = System.getProperty("spro.pwd"); // NOI18N
            if (sd != null) {
                File sdFile = new File(sd);
                if (sdFile.exists()) {
                    return wrapFileNoCanonicalize(sdFile);
                }
            }
        }
        return null;
    }

    private static String getLastPath(){
        String feed = System.getProperty("user.home"); // NOI18N
        if (feed == null) {
            feed = ""; // NOI18N
        }
        Preferences pref = NbPreferences.forModule(FileChooser.class);
        String res = pref.get("last-file", feed); // NOI18N
        File file = new File(res);
        if (!file.exists()) {
            if (!res.equals(feed)){
                res = feed;
            }
        }
        return res;
    }

    /** 
     * See bz#82821 for more details,
     * C/C++ file choosers do no respect nb.native.filechooser
     * now it will be supported  for local case, in remote case we will show file chooser 
     * in currently used L&F
     * 
     */ 
    @Override
    public int showDialog(Component parent, String approveButtonText) throws HeadlessException {
        if (Boolean.getBoolean("nb.native.filechooser")) { //NOI18N
            FileDialog fileDialog = createFileDialog(parent, getCurrentDirectory());
            if (null != fileDialog) {
                return showFileDialog(fileDialog, FileDialog.LOAD);
            }
        }
        return super.showDialog(parent, approveButtonText);
    }

    private FileDialog createFileDialog(Component parentComponent, File currentDirectory) {
        boolean dirsOnly = getFileSelectionMode() == DIRECTORIES_ONLY;
        if (!Boolean.getBoolean("nb.native.filechooser")) { //NOI18N
            return null;
        }
        if (dirsOnly && !Utilities.isMac()) {
            return null;
        }
        Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parentComponent);
        FileDialog fileDialog = new FileDialog(parentFrame);
        String dialogTitle = getDialogTitle();
        if (dialogTitle != null) {
            fileDialog.setTitle(dialogTitle);
        }
        if (null != currentDirectory) {
            fileDialog.setDirectory(currentDirectory.getAbsolutePath());
        }
        return fileDialog;
    }

    public int showFileDialog(FileDialog fileDialog, int mode) {
        String oldFileDialogProp = System.getProperty("apple.awt.fileDialogForDirectories"); //NOI18N
        boolean dirsOnly = getFileSelectionMode() == DIRECTORIES_ONLY;
        if (dirsOnly) {
            System.setProperty("apple.awt.fileDialogForDirectories", "true"); //NOI18N
        }
        fileDialog.setMode(mode);
        fileDialog.setVisible(true);
        if (dirsOnly) {
            if (null != oldFileDialogProp) {
                System.setProperty("apple.awt.fileDialogForDirectories", oldFileDialogProp); //NOI18N
            } else {
                System.clearProperty("apple.awt.fileDialogForDirectories"); //NOI18N
            }
        }
        if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
            setSelectedFile(new File(fileDialog.getDirectory(), fileDialog.getFile()));
            setSelectedFiles(new File[]{new File(fileDialog.getDirectory(), fileDialog.getFile())});            
            return JFileChooser.APPROVE_OPTION;
        }
        return JFileChooser.CANCEL_OPTION;
    }

    @Override
    public int showOpenDialog(Component parent) {
        int ret = super.showOpenDialog(parent);
        if (ret != CANCEL_OPTION) {
            if (getSelectedFile().exists()) {
                setCurrentChooserFile(getSelectedFile());
                Preferences pref = NbPreferences.forModule(FileChooser.class);
                pref.put("last-file", getCurrentChooserFile().getAbsolutePath()); // NOI18N
            }
        }
        return ret;
    }

    public static File getCurrentChooserFile() {
        return currentChooserFile;
    }

    public static void setCurrentChooserFile(File aCurrentChooserFile) {
        currentChooserFile = wrapFileNoCanonicalize(aCurrentChooserFile);
    }
    
    private static File wrapFileNoCanonicalize(File f) {
        if (f instanceof FileChooser.NonCanonicalizingFile) {
            return f;
        } else if (f != null) {
            return new FileChooser.NonCanonicalizingFile(f);
        } else {
            return null;
        }
    }

    private static File[] wrapFilesNoCanonicalize(File[] fs) {
        if (fs != null) {
            for (int i = 0; i < fs.length; i++) {
                fs[i] = wrapFileNoCanonicalize(fs[i]);
            }
        }

        return fs;
    }

    private static final class NonCanonicalizingFile extends File {

        public NonCanonicalizingFile(File orig) {
            this(orig.getPath());
        }

        private NonCanonicalizingFile(String path) {
            super(path);
        }

        private NonCanonicalizingFile(URI uri) {
            super(uri);
        }

        @Override
        public File getCanonicalFile() throws IOException {
            return wrapFileNoCanonicalize(FileUtil.normalizeFile(super.getAbsoluteFile()));
        }

        @Override
        public String getCanonicalPath() throws IOException {
            return FileUtil.normalizeFile(super.getAbsoluteFile()).getAbsolutePath();
        }

        @Override
        public File getParentFile() {
            return wrapFileNoCanonicalize(super.getParentFile());
        }

        @Override
        public File getAbsoluteFile() {
            return wrapFileNoCanonicalize(super.getAbsoluteFile());
        }

        @Override
        public File[] listFiles() {
            return wrapFilesNoCanonicalize(super.listFiles());
        }

        @Override
        public File[] listFiles(java.io.FileFilter filter) {
            return wrapFilesNoCanonicalize(super.listFiles(filter));
        }

        @Override
        public File[] listFiles(FilenameFilter filter) {
            return wrapFilesNoCanonicalize(super.listFiles(filter));
        }
    }    
}
