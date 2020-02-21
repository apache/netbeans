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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils.ExitStatus;
import org.netbeans.modules.remote.api.RemoteFile;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remote.ui.support.RemoteLogger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * This class is used for filechooser only!
 * This is not a full implementation of File - only those
 * methods are supported that are used for file browsing...
 *
 */
public final class FileObjectBasedFile extends File implements RemoteFile{

    private final ExecutionEnvironment env;
    private FileObject fo;
    /** to be used ONLY when fo is null !!! */
    private final String path;
    private File[] NO_CHILDREN = new File[0];
    private final Object lock = new Object();
    private boolean refreshed = false;
    private final Factory factory;

    /*package*/ final static class Factory {

        private final Object lock = new Object();
        private final Map<String, FileObjectBasedFile> files = new HashMap<>();

        /*package*/ FileObjectBasedFile create(ExecutionEnvironment env, String path) {
            FileObjectBasedFile file;
            synchronized (lock) {
                file = files.get(path);
                if (file == null) {
                    file = new FileObjectBasedFile(env, path, this);
                    files.put(path, file);
                }
            }
            return file;
        }

        /*package*/ FileObjectBasedFile create(ExecutionEnvironment env, FileObject fo) {
            FileObjectBasedFile file;
            synchronized (lock) {
                file = files.get(fo.getPath());
                if (file == null || file.fo == null) {
                    file = new FileObjectBasedFile(env, fo, this);
                    files.put(fo.getPath(), file);
                }
            }
            return file;
        }

        private void notifyDeleted(String path) {
            synchronized (lock) {
                files.remove(path);
            }
        }

        private void notifyRenamed(FileObjectBasedFile file, String oldPath) {
            synchronized (lock) {
                files.remove(oldPath);
                files.put(file.getPath(), file);
            }
        }
    }

    private FileObjectBasedFile(ExecutionEnvironment env, String path, Factory factory) {
        super(path);
        RemoteLogger.assertTrue(path != null, "Path should not be null"); //NOI18N
        this.fo = null;
        this.path = toUnix(super.getPath());
        this.env = env;
        this.factory = factory;
    }

    private FileObjectBasedFile(ExecutionEnvironment env, FileObject fo, Factory factory) {
        super(fo == null || "".equals(fo.getPath()) ? "/" : fo.getPath()); // NOI18N
        this.fo = fo;
        // super.getPath() changes slashes and can lead to #186521 Wrong path returned by remote file chooser
        this.path = (fo == null || "".equals(fo.getPath())) ? "/" : fo.getPath(); // NOI18N
        this.env = env;
        this.factory = factory;
    }

    public FileObject getFileObject() {
        return fo;
    }

    @Override
    public boolean isDirectory() {
        return fo == null ? false : fo.isFolder();
    }

    @Override
    public boolean isAbsolute() {
        String p = getPath();
        if (p.length() != 0) {
            return p.charAt(0) == '/';
        } else {
            if (fo != null) {
                // the path is empty => it is absolute only if it is root
                return fo.getParent() == null;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileObjectBasedFile other = (FileObjectBasedFile) obj;
        if (this.env != other.env && (this.env == null || !this.env.equals(other.env))) {
            return false;
        }
        if (this.fo != other.fo && (this.fo == null || !this.fo.equals(other.fo))) {
            return false;
        }
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.env != null ? this.env.hashCode() : 0);
        hash = 17 * hash + (this.fo != null ? this.fo.hashCode() : 0);
        hash = 17 * hash + (this.path != null ? this.path.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean exists() {
        return fo == null ? false : fo.isValid();
    }

    @Override
    public boolean renameTo(File dest) {
        String oldPath = getPath();
        boolean success = renameImpl(dest);
        if (success) {
            factory.notifyRenamed(this, oldPath);
        }
        return success;
    }

    private boolean renameImpl(File dest) {
        if (fo == null) {
            fo = FileSystemProvider.getFileObject(env, path);
        }
        if (fo == null) {
            Future<Integer> result = renameTo(env, getPath(), dest.getPath(), new StringWriter());
            try {
                return result.get() == 0;
            } catch (InterruptedException | ExecutionException ex) {
            }
            return false;
        } else {
            String name = dest.getName();
            String ext = "";
            int pos = name.lastIndexOf('.');
            if (pos > 0) {
                ext = name.substring(pos + 1);
                name = name.substring(0, pos);                        
            }
            try {
                FileLock aLock = fo.lock();
                try {
                    fo.rename(aLock, name, ext);
                    return true;
                } finally {
                    aLock.releaseLock();
                }
            } catch (IOException ex) {
                return false;
            }
        }
    }
    
    @Override
    public boolean mkdirs() {
        if (fo == null) {
            try {
                fo = FileUtil.createFolder(FileSystemProvider.getFileSystem(env).getRoot(), path);
            } catch (IOException ex) {
                ex.printStackTrace(System.err);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mkdir() {
        return mkdirs();
    }

    @Override
    public boolean canWrite() {
       return (fo == null) ? false : fo.canWrite();
    }

    @Override
    public boolean canRead() {
       return (fo == null) ? false : fo.canRead();
    }

    @Override
    public String getPath() {
        return fo == null ? path : fo.getPath();
    }

    @Override
    public File getParentFile() {
        if (fo == null) {
            String p = getPath();
            if (p != null) { // paranoia
                String parentPath = PathUtilities.getDirName(p);
                if (parentPath != null && parentPath.length() > 0) {
                    final FileObject parentFO = FileSystemProvider.getFileObject(env, parentPath);
                    if (parentFO == null) {
                        return factory.create(env, parentPath);
                    } else {
                        return factory.create(env, parentFO);
                    }
                }
            }
            return null;
        }
        FileObject parent = fo.getParent();
        return parent == null ? null : factory.create(env, parent);
    }
    
    @Override
    public String getName() {
        return (fo == null) ? PathUtilities.getBaseName(path): fo.getNameExt();
    }

    @Override
    public boolean createNewFile() throws IOException {
        return super.createNewFile();
    }

    @Override
    public boolean delete() {
        boolean success = deleteImpl();
        String p = getPath();
        if (success) {
            factory.notifyDeleted(p);
        }
        return success;
    }

    private boolean deleteImpl() {
        if (fo == null) {
            fo = FileSystemProvider.getFileObject(env, path);
        }
        if (fo != null) {
            try {
                fo.delete();
                return true;
            } catch (IOException ex) {
                return false;
            }
        }
        return false;
    }
    
    @Override
    public String getParent() {
	int index = path.lastIndexOf('/');
        if (index < 0 || (index == 0 && path.length() == 1) ) {
            return null;
        } else {
            return path.substring(0, index);
        }
    }

    @Override
    public boolean isFile() {
        return !isDirectory();
    }

    @Override
    public String getAbsolutePath() {
        if (fo != null) {
            return fo.getPath();
        } else {
            if (isAbsolute(path)) {
                return path;
            } else {
                return toUnix(super.getAbsolutePath());
            }
        }
    }

    private static boolean isAbsolute(String fileName) {
        return fileName.length() > 0 && fileName.charAt(0) == '/';
    }

    private static String toUnix(String path) {
        if (path != null && Utilities.isWindows()) {
            path = path.replace('\\', '/'); // NOI18N
            while (path.startsWith("//")) { // NOI18N
                path = path.substring(1);
            }
        }
        return path;
    }

    @Override
    public File[] listFiles() {
        return listFiles((FilenameFilter) null);
    }

    @Override
    public File[] listFiles(FilenameFilter filter) {
        if (fo == null) {
            return NO_CHILDREN;
        }

        FileObject[] children = getChilfdrenFO();

        List<File> res = new ArrayList<File>(children.length);
        for (FileObject child : children) {
            if (filter == null || filter.accept(this, child.getNameExt())) {
                res.add(factory.create(env, child));
            }
        }
        return res.toArray(new File[res.size()]);
    }

    private FileObject[] getChilfdrenFO() {
        synchronized (lock) {
            if (!refreshed) {
                FileSystemProvider.refresh(fo, false);
                refreshed = true;
            }
            return fo.getChildren();
        }
    }

    @Override
    public File getCanonicalFile() throws IOException {
        return this;
    }

    @Override
    public long length() {
        if (fo != null) {
            return fo.getSize();
        }
        return super.length();
    }

    @Override
    public long lastModified() {
        if (fo != null) {
            return fo.lastModified().getTime();
        }
        return super.lastModified();
    }


    private static final String PREFIX = "NATIVEEXECUTOR: "; // NOI18N
    private static final RequestProcessor processor = new RequestProcessor(PREFIX, 50);

    public static Future<Integer> renameTo(final ExecutionEnvironment execEnv, final String sourceDir,
            final String destDir, final Writer error) {
        final FutureTask<Integer> ftask = new FutureTask<Integer>(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                ExitStatus result = ProcessUtils.execute(execEnv, "mv", sourceDir, destDir); // NOI18N

                if (!result.isOK() && error != null) {
                    error.write(result.getErrorString());
                    error.flush();
            }

                return result.exitCode;
            }
        });


        processor.post(ftask);
        return ftask;
    }
}
