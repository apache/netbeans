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
package org.netbeans.modules.remote.api.ui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.prefs.Preferences;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.FileChooserUI;
import org.netbeans.modules.remote.ui.support.RemoteLogger;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public final class FileChooserBuilder extends org.openide.filesystems.FileChooserBuilder {

    // TODO: think of a better name
    public abstract static class JFileChooserEx extends JFileChooser {
        private File curFile;
        private final AtomicReference<Callable<String>> defaultDirectoryRef= new AtomicReference<Callable<String>>();

        protected JFileChooserEx(Callable<String> currentDirectoryPath) {
            super((String)null);
            defaultDirectoryRef.set(currentDirectoryPath);
        }

        public JFileChooserEx(Callable<String> currentDirectoryPath, FileSystemView fsv) {
            super((String)null, fsv);
            defaultDirectoryRef.set(currentDirectoryPath);
        }
        

        public abstract void setCurrentDirectory(FileObject dir);
        public abstract FileObject getSelectedFileObject();
        public abstract FileObject[] getSelectedFileObjects();
        public abstract ExecutionEnvironment getExecutionEnvironment();
        
        /** 
         * Very close to getHomeDirectory, but isn't quite the same:
         * getHomePath()
         * - does not create file object, 
         * - does not fire event,
         * - is fast (in the case remote host info isn't avaliable, just returns  "/")
         * @return user home
         */
        protected abstract String getHomePath();

        /**
         * Used to determine whether "~" should be expanded to home directory
         * @return 
         */
        protected abstract boolean isUnix();

        protected abstract char getFileSeparatorChar();
        
        @Override
        public final File getCurrentDirectory() {
            return curFile;
        }        
        
        /*package*/  Callable<String> getAndClearDefaultDirectory() {
            return defaultDirectoryRef.getAndSet(null);
        }

        @Override
        public void setCurrentDirectory(File dir) {                        
            curFile = dir;
            getUI().rescanCurrentDirectory(this);
        }

        
        @Override
        public final void updateUI() {
            if (isAcceptAllFileFilterUsed()) {
                removeChoosableFileFilter(getAcceptAllFileFilter());
            }
            FileChooserUI fileChooserUI = new FileChooserUIImpl(this);
            if (getFileSystemView() == null) {
                // We were probably deserialized
                setFileSystemView(FileSystemView.getFileSystemView());
            }
            setUI(fileChooserUI);

            if(isAcceptAllFileFilterUsed()) {
                addChoosableFileFilter(getAcceptAllFileFilter());
            }
        }        
        
        
    }

    private static final String openDialogTitleTextKey = "FileChooser.openDialogTitleText"; // NOI18N
    private static final String saveDialogTitleTextKey = "FileChooser.saveDialogTitleText"; // NOI18N
    private static final String readOnlyKey = "FileChooser.readOnly"; // NOI18N

    private final ExecutionEnvironment env;
    private Preferences forModule;

    public FileChooserBuilder(ExecutionEnvironment env) {
        this(env, ExecutionEnvironmentFactory.toUniqueID(env));
    }
    
    public FileChooserBuilder(ExecutionEnvironment env, String dirKey) {
        super(dirKey);
        this.env = env;
    }

    public JFileChooserEx createFileChooser(Callable<String> selectedPath) {
        JFileChooserEx res;
        if (env.isLocal()) {
            res = new LocalFileChooserImpl(selectedPath);
        } else {
            String currentOpenTitle = UIManager.getString(openDialogTitleTextKey);
            String currentSaveTitle = UIManager.getString(saveDialogTitleTextKey);
            Boolean currentReadOnly = UIManager.getBoolean(readOnlyKey);

            UIManager.put(openDialogTitleTextKey, decorateTitle(currentOpenTitle, env));
            UIManager.put(saveDialogTitleTextKey, decorateTitle(currentSaveTitle, env));

            RemoteFileSystemView remoteFileSystemView = new RemoteFileSystemView("/", env); // NOI18N

            RemoteFileChooserImpl chooser = new RemoteFileChooserImpl(selectedPath, remoteFileSystemView, env, forModule);//NOI18N
            remoteFileSystemView.addPropertyChangeListener(chooser);
            chooser.setFileView(new CustomFileView(remoteFileSystemView));

            UIManager.put(openDialogTitleTextKey, currentOpenTitle);
            UIManager.put(saveDialogTitleTextKey, currentSaveTitle);
            UIManager.put(readOnlyKey, currentReadOnly);

            res = chooser;
        }
        res.setFileHidingEnabled(false);
        return res;
    }

    public JFileChooserEx createFileChooser() {
        return createFileChooser((String)null);
    }

    public JFileChooserEx createFileChooser(final String selectedPath) {
        return createFileChooser(new Callable<String>() {
            @Override
            public String call() throws Exception {
                 return selectedPath;
            }
        });

    }

    public FileChooserBuilder setPreferences(Preferences forModule) {
        this.forModule = forModule;
        return this;
    }

    private static String decorateTitle(String title, ExecutionEnvironment env) {
        return NbBundle.getMessage(FileChooserBuilder.class, "REMOTE_CHOOSER_TITLE", title, env.getDisplayName()); // NOI18N
    }

    private static class LocalFileChooserImpl extends JFileChooserEx {

        public LocalFileChooserImpl(Callable<String> selectedPath) {
            super(selectedPath);
        }

        @Override
        public File getSelectedFile() {
            File result =  super.getSelectedFile(); //To change body of generated methods, choose Tools | Templates.
            if (result == null) {
                return getCurrentDirectory();
            }
            return result;
        }
                        
        @Override
        public FileObject getSelectedFileObject() {
            File file = getSelectedFile();
            return (file == null) ? null : FileUtil.toFileObject(file);
        }

        @Override
        public FileObject[] getSelectedFileObjects() {
            File[] files = getSelectedFiles();
            if (files == null) {
                return null;
            } else {
                FileObject[] result = new FileObject[files.length];
                for (int i = 0; i < files.length; i++) {
                    result[i] = FileUtil.toFileObject(files[i]);
                }
                return result;
            }
        }

        @Override
        public ExecutionEnvironment getExecutionEnvironment() {
            return ExecutionEnvironmentFactory.getLocal();
        }

        @Override
        public String getHomePath() {
            return System.getProperty("user.home");
        }

        @Override
        public boolean isUnix() {
            return Utilities.isUnix();
        }

        @Override
        public char getFileSeparatorChar() {
            return File.separatorChar;
        }

        @Override
        public void setCurrentDirectory(FileObject dir) {
            if (dir != null && dir.isFolder()) {
                File file = FileUtil.toFile(dir);
                setCurrentDirectory(file);
            }
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
            if (getFileSelectionMode() == FILES_AND_DIRECTORIES) {
                //FileDialog does not support selection of files and directories
                return null;
            }
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
    }

    private static class RemoteFileChooserImpl extends JFileChooserEx
            implements PropertyChangeListener {
        private final Preferences forModule;
        private final ExecutionEnvironment env;

        public RemoteFileChooserImpl(Callable<String> currentDirectory, RemoteFileSystemView fsv, ExecutionEnvironment env, Preferences forModule) {
            super(currentDirectory, fsv);
            this.env = env;
            this.forModule = forModule;
        }
        
        @Override
        public File getSelectedFile() {
            File result =  super.getSelectedFile(); //To change body of generated methods, choose Tools | Templates.
            if (result == null) {
                return getCurrentDirectory();
            }
            return result;
        }        
           
        @Override
        public FileObject getSelectedFileObject() {
            File file = getSelectedFile();
            return (file instanceof FileObjectBasedFile) ? ((FileObjectBasedFile) file).getFileObject() : null;
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            setCursor(Cursor.getDefaultCursor());
        }
                
        @Override
        public FileObject[] getSelectedFileObjects() {
            File[] files = getSelectedFiles();
            if (files == null) {
                return null;
            } else {
                List<FileObject> result = new ArrayList<FileObject>(files.length);
                for (int i = 0; i < files.length; i++) {
                    if (files[i] instanceof FileObjectBasedFile) {
                        FileObject fo = ((FileObjectBasedFile) files[i]).getFileObject();
                        if (fo != null) {
                            result.add(fo);
                        } else {
                            RemoteLogger.getInstance().log(Level.FINEST, "Null file object for {0}", files[i].getAbsolutePath());
                        }
                    }
                }
                return result.toArray(new FileObject[result.size()]);
            }
        }

        @Override
        public ExecutionEnvironment getExecutionEnvironment() {
            return env;
        }
        
        @Override
        public String getHomePath() {
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    return HostInfoUtils.getHostInfo(env).getUserDir();
                } catch (IOException ex) {
                    RemoteLogger.finest(ex);
                } catch (ConnectionManager.CancellationException ex) {
                }
            }
            return "/"; //NOI18N
        }        

        @Override
        public boolean isUnix() {
            return true;
        }

        @Override
        public char getFileSeparatorChar() {
            return '/';
        }

        @Override
        protected void setup(FileSystemView view) {
            super.setup(view);
        }

        @Override
        public void approveSelection() {
            File selectedFile = getSelectedFile();
            if (selectedFile != null) {
                if (selectedFile.isDirectory() && getFileSelectionMode() == FILES_ONLY) {
                    setCurrentDirectory(getSelectedFile());
                } else {
                    super.approveSelection();
                }
            }
        }

        @Override
        public void setCurrentDirectory(File dir) {
            if (dir != null && !(dir instanceof FileObjectBasedFile) && env != null) {
                String path = dir.getPath().replace('\\', FileSystemProvider.getFileSeparatorChar(env)); //NOI18N
                FileObject fo = FileSystemProvider.getFileObject(env, path);
                if (fo != null) {
                    dir = getFileSystemView().getFactory().create(env, fo);
                }
            }
            super.setCurrentDirectory(dir);
        }

        @Override
        public void setCurrentDirectory(FileObject fo) {
            if (fo != null && fo.isFolder()) {
                File dir = getFileSystemView().getFactory().create(env, fo);
                super.setCurrentDirectory(dir);
            }
        }

        @Override
        protected void fireActionPerformed(String command) {
            super.fireActionPerformed(command);
        }

        @Override
        public void propertyChange(final PropertyChangeEvent evt) {
            if (RemoteFileSystemView.LOADING_STATUS.equals(evt.getPropertyName())) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        String file = (String) evt.getNewValue();
                        if (file == null) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        } else {
                            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        }
                    }
                });
            }
        }

        @Override
        public RemoteFileSystemView getFileSystemView() {
            return (RemoteFileSystemView) super.getFileSystemView();
        }

        @Override
        public void setDialogTitle(String dialogTitle) {
            super.setDialogTitle(decorateTitle(dialogTitle, getFileSystemView().getExecutionEnvironment()));
        }

        @Override
        public int showOpenDialog(Component parent) throws HeadlessException {
            int ret = super.showOpenDialog(parent);
            if (ret != CANCEL_OPTION) {
                if (getSelectedFile() != null) {
                    String path = getSelectedFile().getAbsolutePath();
                    if (forModule != null) {
                        String envID = ExecutionEnvironmentFactory.toUniqueID(env);
                        forModule.put("FileChooserPath"+envID, path); // NOI18N
                    }
                }
            }
            return ret;
        }
    }

    private static class CustomFileView extends FileView {
        final FileSystemView view;

        public CustomFileView(FileSystemView view) {
            this.view = view;
        }

        @Override
        public Icon getIcon(File f) {
            return view.getSystemIcon(f);
        }

        @Override
        public String getName(File f) {
            if (view.isRoot(f)) {
                return "/"; //NOI18N
            }
            return super.getName(f);
        }

        @Override
        public Boolean isTraversable(File f) {
            return f.isDirectory();
        }
    }

}
