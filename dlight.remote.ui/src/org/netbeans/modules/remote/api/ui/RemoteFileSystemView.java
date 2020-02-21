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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.remote.api.ui;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.MessageFormat;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.ui.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;

/**
 *
 */
/*package*/ final class RemoteFileSystemView extends FileSystemView {

    public static final String LOADING_STATUS = "ls"; //  NOI18N
    private final FileSystem fs;
    private final PropertyChangeSupport changeSupport;
    private final ExecutionEnvironment env;
    private static final String newFolderString =
            UIManager.getString("FileChooser.other.newFolder");//  NOI18N
    private static final String newFolderNextString  =
            UIManager.getString("FileChooser.other.newFolder.subsequent");//  NOI18N

    private final FileObjectBasedFile.Factory factory;

    public RemoteFileSystemView(final String root, final ExecutionEnvironment execEnv) {
        this.env = execEnv;
        fs = FileSystemProvider.getFileSystem(execEnv, root);
        assert (fs != null);
        changeSupport = new PropertyChangeSupport(this);
        factory = new FileObjectBasedFile.Factory();
    }

    public FileObject getFSRoot() {
        return fs.getRoot();
    }

    @Override
    public File createFileObject(String path) {
        if (RemoteLogger.getInstance().isLoggable(Level.FINEST)) {
            if (SwingUtilities.isEventDispatchThread()) {
                RemoteLogger.finest(new IllegalStateException("RFSV: creating file in EDT " + path)); //NOI18N
            } else {
                RemoteLogger.getInstance().log(Level.FINEST, "RFSV: creating file for {0}", path);
            }
        }
        RemoteLogger.getInstance().log(Level.FINEST, "RFSV: creating file for {0}", path);
        if (!path.isEmpty() && path.charAt(0) != '/') {
            return factory.create(env, path);
        }
        
        FileObject fo = fs.findResource(path);
        if (fo == null || !fo.isValid()) {
            RemoteLogger.getInstance().log(Level.FINEST, "Null file object for {0}", path);
            return factory.create(env, path);
        } else {
            return factory.create(env, fo);
        }
    }

    public FileObjectBasedFile.Factory getFactory() {
        return factory;
    }

    @Override
    public File createFileObject(File dir, String filename) {
        String parent = dir == null ? fs.getRoot().getPath() : dir.getPath();
        if (isAbsolute(filename)) {
            return createFileObject(filename);
        } else {
            return createFileObject(parent + "/" + filename); // NOI18N
        }
    }

    private static boolean isAbsolute(String fileName) {
        return fileName.length() > 0 && fileName.charAt(0) == '/';
    }

    @Override
    public File[] getRoots() {
        return new File[]{factory.create(env, fs.getRoot())};
    }

    @Override
    public String getSystemDisplayName(File f) {
        return "".equals(f.getName()) ? "/" : f.getName();// NOI18N
    }

    @Override
    public File getDefaultDirectory() {
        return factory.create(env, fs.getRoot());
    }

    @Override
    public File getHomeDirectory() {
        try {
            changeSupport.firePropertyChange(LOADING_STATUS, null, "${HOME}"); // NOI18N
            if (!(HostInfoUtils.isHostInfoAvailable(env) && ConnectionManager.getInstance().isConnectedTo(env))) {
                return getDefaultDirectory();
            }
            HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
            FileObject fo = fs.findResource(hostInfo.getUserDir());
            return (fo == null) ? factory.create(env, fs.getRoot()) : factory.create(env, fo);
        } catch (IOException ex) {
            RemoteLogger.finest(ex);
        } catch (CancellationException ex) {
            // never report cancellation exception
        } finally {
            changeSupport.firePropertyChange(LOADING_STATUS, "${HOME}", null); // NOI18N
        }
        return getDefaultDirectory();
    }

    @Override
    public boolean isFileSystem(File f) {
        return true;
    }
    
    @Override
    public File getParentDirectory(File dir) {
        if (dir == null) {
            return null;
        }
        File parentFile = dir.getParentFile();
        return parentFile == null ? null : createFileObject(parentFile.getPath());
    }
   
    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        changeSupport.firePropertyChange(LOADING_STATUS, null, dir.getAbsolutePath());
        try {
            if (!(dir instanceof FileObjectBasedFile)) {
                dir = factory.create(env, fs.findResource(dir.getAbsolutePath()));
            }
            FileObjectBasedFile rdir = (FileObjectBasedFile) dir;
            File[] result = null;

            if (dir.canRead()) {
                if (useFileHiding) {
                    result = rdir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return ! name.startsWith("."); // NOI18N
                        }
                    });
                } else {
                    result = rdir.listFiles();
                }
            }
            return (result == null) ? new File[0] : result;
        } finally {
            changeSupport.firePropertyChange(LOADING_STATUS, dir.getAbsolutePath(), null);
        }
    }

    /**
     * Creates a new folder with a default folder name.
     */
    @Override
    public File createNewFolder(File containingDir) throws IOException {
        if (containingDir == null) {
	    throw new IOException("Containing directory is null:");//  NOI18N
	}
	File newFolder;
	// Unix - using OpenWindows' default folder name. Can't find one for Motif/CDE.
	newFolder = createFileObject(containingDir, newFolderString);
	int i = 1;
	while (newFolder.exists() && (i < 100)) {
	    newFolder = createFileObject(containingDir, MessageFormat.format(
                    newFolderNextString, new Object[]{new Integer(i)}));
	    i++;
	}

        if (newFolder.exists()) {
	    throw new IOException("Directory already exists:" + newFolder.getAbsolutePath());//  NOI18N
	} else {
	    newFolder.mkdirs();
	}
	return newFolder;
    }

    @Override
    protected File createFileSystemRoot(File f) {
        return factory.create(env, fs.getRoot());
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    public Icon getSystemIcon(File f) {
        return UIManager.getIcon(f == null || f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");//NOI18N
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return env;
    }
}
