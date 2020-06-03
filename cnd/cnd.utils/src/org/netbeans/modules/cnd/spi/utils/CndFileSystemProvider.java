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

package org.netbeans.modules.cnd.spi.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 */
public abstract class CndFileSystemProvider {

    private static final CndFileSystemProvider DEFAULT = new DefaultProvider();
    private static FileSystem rootFileSystem = null;

    public static final class CndStatInfo {

        public final long inode;
        public final long device;

        private CndStatInfo(long device, long inode) {
            this.inode = inode;
            this.device = device;
        }
        
        public static CndStatInfo create(long  device, long inode) {
            return new CndStatInfo(device, inode);
        }

        public static CndStatInfo createInvalid() {
            return new CndStatInfo(0, 0);
        }

        public boolean isValid() {
            return inode > 0;
        }

        @Override
        public String toString() {
            return "CndStatInfo(" + "dev=" + device + ",ino=" + device + ')'; //NOI18N
        }
    }

    public static class FileInfo {
        public final String absolutePath;
        public final boolean directory;
        public final boolean file;
        public FileInfo(String absolutePath, boolean directory, boolean file) {
            this.absolutePath = absolutePath;
            this.directory = directory;
            this.file = file;
        }

        @Override
        public String toString() {
            return "FileInfo{" + "absolutePath=" + absolutePath + ",directory=" + directory + ",file=" + file +'}';//NOI18N
        }
    }

    public interface CndFileSystemProblemListener {
        void problemOccurred(FSPath fsPath);
        void recovered(FileSystem fileSystem);
    }

    private static CndFileSystemProvider getDefault() {
        return DEFAULT;
    }

    public static void addFileSystemProblemListener(CndFileSystemProblemListener listener, FileSystem fileSystem) {
        getDefault().addFileSystemProblemListenerImpl(listener, fileSystem);
    }
    
    public static void removeFileSystemProblemListener(CndFileSystemProblemListener listener, FileSystem fileSystem) {
        getDefault().removeFileSystemProblemListenerImpl(listener, fileSystem);
    }

    /** restricted access - in fact to kind of friend class CndFileSystemProviderHelper) */
    protected static void addFileSystemProblemListener(CndFileSystemProblemListener listener) {
        getDefault().addFileSystemProblemListenerImpl(listener);
    }

    /** restricted access - in fact to kind of friend class CndFileSystemProviderHelper) */
    protected static void fireFileSystemProblemOccurred(FSPath fSPath) {
        getDefault().fireFileSystemProblemOccurredImpl(fSPath);
    }

    public static File toFile(FileObject fileObject) {
        // TODO: do we still need this?
        File file = FileUtil.toFile(fileObject);
        if (file == null && fileObject != null && !fileObject.isValid()) {
            file = new File(fileObject.getPath());
        }
        return file;
    }

    public static InputStream getInputStream(FileObject fo, int maxSize) throws IOException {
        return getDefault().getInputStreamImpl(fo, maxSize);
    }

    /**
     * JFileChooser works in the term of files.
     * For such "perverted" files FileUtil.toFileObject won't work.
     * @param file
     * @return 
     */
    public static FileObject toFileObject(File file) {
        return getDefault().toFileObjectImpl(file);
    }    

    /**
     * Returns inode and device of the given file, if possible
     */
    public static CndStatInfo getStatInfo(FileObject fo) {
        return getDefault().getStatInfoImpl(fo);
    }

    public static final boolean isRemote(FileSystem fs) {
        return getDefault().isRemoteImpl(fs);
    }

    public static Boolean exists(CharSequence path) {
        return getDefault().existsImpl(path);
    }

    public static Boolean canRead(CharSequence path) {
        return getDefault().canReadImpl(path);
    }

    public static FileInfo[] getChildInfo(CharSequence path) {
        return getDefault().getChildInfoImpl(path);
    }

    public static FileObject toFileObject(CharSequence absPath) {
        FileObject result = getDefault().toFileObjectImpl(absPath);
        CndUtils.assertNotNull(result, "Null file object for ", absPath); //NOI18N
        return result;
    }

    public static FileObject urlToFileObject(CharSequence url) {
        return getDefault().urlToFileObjectImpl(url);
    }
    
    public static FileSystem urlToFileSystem(CharSequence url) {
        return getDefault().urlToFileSystemImpl(url);
    }

    /**
     * This method is different from fileObjectToUrl.
     * On Windows for local file object returns true Windows path.
     * C:\path\to\file
     * @param fileObject
     * @return 
     */
    public static CharSequence toUrl(FSPath fsPath) {
        return getDefault().toUrlImpl(fsPath);
    }
    public static CharSequence toUrl(FileSystem fileSystem, CharSequence absPath) {
        return getDefault().toUrlImpl(fileSystem, absPath);
    }

    /**
     * This method is different from toUrl.
     * On Windows for local file object returns Unix-like path, but with drive letter,
     * C:/path/to/file
     * @param fileObject
     * @return 
     */
    public static CharSequence fileObjectToUrl(FileObject fileObject) {
        CharSequence result = getDefault().fileObjectToUrlImpl(fileObject);
        CndUtils.assertNotNull(result, "Null URL for file object ", fileObject); //NOI18N
        return result;
    }
    
    public static CharSequence getCanonicalPath(FileSystem fileSystem, CharSequence absPath) throws IOException {
        CndUtils.assertAbsolutePathInConsole(absPath.toString());
        return getDefault().getCanonicalPathImpl(fileSystem, absPath);
    }
    
    public static FileObject getCanonicalFileObject(FileObject fo) throws IOException {
        return getDefault().getCanonicalFileObjectImpl(fo);
    }
    
    public static String getCanonicalPath(FileObject fo) throws IOException {
        return getDefault().getCanonicalPathImpl(fo);        
    }
    
    public static String normalizeAbsolutePath(FileSystem fs, String absPath) {
        return getDefault().normalizeAbsolutePathImpl(fs, absPath);
    }
    
    public static boolean isAbsolute(FileSystem fs, String path) {
        return getDefault().isAbsoluteImpl(fs, path);
    }

    public static void addFileChangeListener(FileChangeListener listener) {
        getDefault().addFileChangeListenerImpl(listener);
    }

    public static void addFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path) {
        getDefault().addFileChangeListenerImpl(listener, fileSystem, path);
    }

    public static void removeFileChangeListener(FileChangeListener listener) {
        getDefault().removeFileChangeListenerImpl(listener);
    }

    public static void removeFileChangeListener(FileChangeListener listener, FileSystem fileSystem, String path) {
        getDefault().removeFileChangeListenerImpl(listener, fileSystem, path);
    }
    
    public static FileSystem getLocalFileSystem() {
        return DefaultProvider.getRootFileSystem();
    }
    
    public static boolean isWindows(FileSystem fs) {
        return (fs == null) ? Utilities.isWindows() : getDefault().isWindowsImpl(fs);
    }
    
    public static boolean isMacOS(FileSystem fs) {
        return (fs == null) ? Utilities.isMac() : getDefault().isMacOSImpl(fs);
    }

    /**
     * Checks whether the file specified by path exists or not
     * @param path
     * @return Boolean.TRUE if the file belongs to this provider file system and exists,
     * Boolean.FALSE if the file belongs to this provider file system and does NOT exist,
     * or NULL if the file does not belong to this provider file system
     */
    protected abstract Boolean existsImpl(CharSequence path);
    protected abstract Boolean canReadImpl(CharSequence path);
    protected abstract FileInfo[] getChildInfoImpl(CharSequence path);
    protected abstract InputStream getInputStreamImpl(FileObject fo, int maxSize) throws IOException;

    /** a bridge from cnd.utils to dlight.remote */
    protected abstract FileObject toFileObjectImpl(CharSequence absPath);

    protected abstract CharSequence fileObjectToUrlImpl(FileObject fileObject);
    protected abstract CharSequence toUrlImpl(FSPath fSPath);
    protected abstract CharSequence toUrlImpl(FileSystem fileSystem, CharSequence absPath);
    protected abstract FileObject urlToFileObjectImpl(CharSequence url);
    protected abstract FileSystem urlToFileSystemImpl(CharSequence url);
    protected abstract FileObject toFileObjectImpl(File file);

    protected abstract CharSequence getCanonicalPathImpl(FileSystem fileSystem, CharSequence absPath) throws IOException;
    protected abstract FileObject getCanonicalFileObjectImpl(FileObject fo) throws IOException;
    protected abstract String getCanonicalPathImpl(FileObject fo) throws IOException;
    
    protected abstract String normalizeAbsolutePathImpl(FileSystem fs, String absPath);
    
    protected abstract boolean addFileChangeListenerImpl(FileChangeListener listener);
    protected abstract boolean removeFileChangeListenerImpl(FileChangeListener listener);
    
    protected abstract boolean addFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path);    
    protected abstract boolean removeFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path);    

    protected abstract void removeFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem);
    protected abstract void addFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem);
    
    protected abstract void addFileSystemProblemListenerImpl(CndFileSystemProblemListener listener);
    protected abstract void fireFileSystemProblemOccurredImpl(FSPath fsPath);

    protected abstract boolean isAbsoluteImpl(FileSystem fs, String path);

    protected abstract boolean isMacOSImpl(FileSystem fs);
    protected abstract boolean isWindowsImpl(FileSystem fs);

    protected abstract boolean isRemoteImpl(FileSystem fs);
    protected abstract CndStatInfo getStatInfoImpl(FileObject fo);

    private static class DefaultProvider extends CndFileSystemProvider {
        private static final String FILE_PROTOCOL_PREFIX = "file:"; // NOI18N

        private final CndFileSystemProvider[] cache;

        DefaultProvider() {
            Collection<? extends CndFileSystemProvider> instances =
                    Lookup.getDefault().lookupAll(CndFileSystemProvider.class);
            cache = instances.toArray(new CndFileSystemProvider[instances.size()]);
            CndUtils.assertTrueInConsole(cache.length > 0, "CndFileSystemProvider NOT FOUND"); // NOI18N
        }

        @Override
        public FileObject toFileObjectImpl(CharSequence absPath) {
            FileObject  fo;
            for (CndFileSystemProvider provider : cache) {
                fo = provider.toFileObjectImpl(absPath);
                if (fo != null) {
                    return fo;
                }
            }
            // not cnd specific file => use default file system conversion
            File file = new File(FileUtil.normalizePath(absPath.toString()));
            fo = FileUtil.toFileObject(file);
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(file);
            }
            return fo;
        }

        @Override
        protected FileObject toFileObjectImpl(File file) {
            FileObject fo;
            for (CndFileSystemProvider provider : cache) {
                fo = provider.toFileObjectImpl(file);
                if (fo != null) {
                    return fo;
                }
            }
            fo = FileUtil.toFileObject(file);
            if (fo == null) {
                fo = InvalidFileObjectSupport.getInvalidFileObject(file);
            }
            return fo;
        }

        @Override
        protected Boolean canReadImpl(CharSequence path) {
            for (CndFileSystemProvider provider : cache) {
                Boolean result = provider.canReadImpl(path);
                if (result != null) {
                    return result;
                }
            }
            return new File(path.toString()).canRead();
        }

        @Override
        protected Boolean existsImpl(CharSequence path) {
            for (CndFileSystemProvider provider : cache) {
                Boolean result = provider.existsImpl(path);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        @Override
        protected FileInfo[] getChildInfoImpl(CharSequence path) {
            for (CndFileSystemProvider provider : cache) {
                FileInfo[] result = provider.getChildInfoImpl(path);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }

        @Override
        protected FileObject urlToFileObjectImpl(CharSequence url) {
            for (CndFileSystemProvider provider : cache) {
                FileObject fo = provider.urlToFileObjectImpl(url);
                if (fo != null) {
                    return fo;
                }
            }
            String path = url.toString();
            File file;
            if (path.startsWith(FILE_PROTOCOL_PREFIX)) {
                try {
                    URL u = new URL(path);
                    file = FileUtil.normalizeFile(new File(u.toURI()));
                } catch (IllegalArgumentException ex) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}:\n{1}", new Object[]{path, ex.getLocalizedMessage()});
                    return null;
                } catch (URISyntaxException ex) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}:\n{1}", new Object[]{path, ex.getLocalizedMessage()});
                    return null;
                } catch (MalformedURLException ex) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}:\n{1}", new Object[]{path, ex.getLocalizedMessage()});
                    return null;
                }       
            } else {
                file = new File(FileUtil.normalizePath(path));
            }
            return FileUtil.toFileObject(file);
        }
        
        @Override
        protected FileSystem urlToFileSystemImpl(CharSequence url) {
            for (CndFileSystemProvider provider : cache) {
                FileSystem fs = provider.urlToFileSystemImpl(url);
                if (fs != null) {
                    return fs;
                }
            }
            if (url.length() == 0) {
                return getRootFileSystem();
            }
            String path = url.toString();
            File file;
            if (path.startsWith(FILE_PROTOCOL_PREFIX)) {
                try {
                    URL u = new URL(path);
                    file = FileUtil.normalizeFile(new File(u.toURI()));
                } catch (IllegalArgumentException ex) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}:\n{1}", new Object[]{path, ex.getLocalizedMessage()});
                    return null;
                } catch (URISyntaxException ex) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}:\n{1}", new Object[]{path, ex.getLocalizedMessage()});
                    return null;
                } catch (MalformedURLException ex) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}:\n{1}", new Object[]{path, ex.getLocalizedMessage()});
                    return null;
                }       
            } else {
                file = new File(FileUtil.normalizePath(path));
            }
            try {
                FileObject fo = FileUtil.toFileObject(file);
                if (fo == null) {
                    CndUtils.getLogger().log(Level.WARNING, "CndFileSystemProvider.urlToFileObjectImpl can not convert {0}", path);
                    return null;
                }
                return fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                return null;
            }
        }
        
        private static FileSystem getRootFileSystem() {
            if (rootFileSystem == null) {
                File tmpFile = null;
                try {
                    tmpFile = File.createTempFile("NetBeans", ".tmp"); //NOI18N
                    tmpFile = FileUtil.normalizeFile(tmpFile);
                    FileObject fo = FileUtil.toFileObject(tmpFile.getParentFile());
                    rootFileSystem = fo.getFileSystem();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    if (tmpFile != null) {
                        tmpFile.delete();
                    }
                }
            }
            return rootFileSystem;
        }

        @Override
        protected CharSequence fileObjectToUrlImpl(FileObject fileObject) {
            for (CndFileSystemProvider provider : cache) {
                CharSequence path = provider.fileObjectToUrlImpl(fileObject);
                if (path != null) {
                    return path;
                }
            }
            return fileObject.getPath();
        }

        @Override
        protected CharSequence toUrlImpl(FSPath fSPath) {
            for (CndFileSystemProvider provider : cache) {
                CharSequence url = provider.toUrlImpl(fSPath);
                if (url != null) {
                    return url;
                }
            }
            return fSPath.getPath();
        }

        @Override
        protected CharSequence toUrlImpl(FileSystem fileSystem, CharSequence absPath) {
            for (CndFileSystemProvider provider : cache) {
                CharSequence url = provider.toUrlImpl(fileSystem, absPath);
                if (url != null) {
                    return url;
                }
            }
            return absPath;
        }
        
        @Override
        protected CharSequence getCanonicalPathImpl(FileSystem fileSystem, CharSequence absPath) throws IOException {
            for (CndFileSystemProvider provider : cache) {
                CharSequence canonical = provider.getCanonicalPathImpl(fileSystem, absPath);
                if (canonical != null) {
                    return canonical;
                }
            }
            return absPath;
        }

        @Override
        protected FileObject getCanonicalFileObjectImpl(FileObject fo) throws IOException {
            for (CndFileSystemProvider provider : cache) {
                FileObject canonical = provider.getCanonicalFileObjectImpl(fo);
                if (canonical != null) {
                    return canonical;
                }
            }
            return fo;
        }

        @Override
        protected String getCanonicalPathImpl(FileObject fo) throws IOException {
            for (CndFileSystemProvider provider : cache) {
                String canonical = provider.getCanonicalPathImpl(fo);
                if (canonical != null) {
                    return canonical;
                }
            }
            return fo.getPath();
        }

        @Override
        protected String normalizeAbsolutePathImpl(FileSystem fs, String absPath) {
            for (CndFileSystemProvider provider : cache) {
                String normalized = provider.normalizeAbsolutePathImpl(fs, absPath);
                if (normalized != null) {
                    return normalized;
                }
            }
            return absPath;
        }

        @Override
        protected boolean addFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path) {
            for (CndFileSystemProvider provider : cache) {
                if (provider.addFileChangeListenerImpl(listener, fileSystem, path)) {
                    return true;
                }
            }
            if (CndFileUtils.isLocalFileSystem(fileSystem)) {
                FileUtil.addFileChangeListener(listener, FileUtil.normalizeFile(new File(path)));
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected boolean removeFileChangeListenerImpl(FileChangeListener listener, FileSystem fileSystem, String path) {
            for (CndFileSystemProvider provider : cache) {
                if (provider.removeFileChangeListenerImpl(listener, fileSystem, path)) {
                    return true;
                }
            }
            if (CndFileUtils.isLocalFileSystem(fileSystem)) {
                FileUtil.removeFileChangeListener(listener, FileUtil.normalizeFile(new File(path)));
                return true;
            } else {
                return false;
            }
        }

        @Override
        protected boolean addFileChangeListenerImpl(FileChangeListener listener) {
            for (CndFileSystemProvider provider : cache) {
                provider.addFileChangeListenerImpl(listener);
            }
            return true;
        }

        @Override
        protected boolean removeFileChangeListenerImpl(FileChangeListener listener) {
            for (CndFileSystemProvider provider : cache) {
                provider.removeFileChangeListenerImpl(listener);
            }
            return true;
        }

        @Override
        protected void addFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem) {
            for (CndFileSystemProvider provider : cache) {
                provider.addFileSystemProblemListenerImpl(listener, fileSystem);
            }
        }

        @Override
        protected void removeFileSystemProblemListenerImpl(CndFileSystemProblemListener listener, FileSystem fileSystem) {
            for (CndFileSystemProvider provider : cache) {
                provider.removeFileSystemProblemListenerImpl(listener, fileSystem);
            }
        }        
        
        @Override
        protected void addFileSystemProblemListenerImpl(CndFileSystemProblemListener listener) {
            for (CndFileSystemProvider provider : cache) {
                provider.addFileSystemProblemListenerImpl(listener);
            }
        }

        @Override
        protected void fireFileSystemProblemOccurredImpl(FSPath fsPath) {
            for (CndFileSystemProvider provider : cache) {
                provider.fireFileSystemProblemOccurredImpl(fsPath);
            }
        }

        @Override
        protected boolean isAbsoluteImpl(FileSystem fs, String path) {
            for (CndFileSystemProvider provider : cache) {
                return provider.isAbsoluteImpl(fs, path);
            }
            return CndPathUtilities.isAbsolute(path);
        }        

        @Override
        protected boolean isMacOSImpl(FileSystem fs) {
            for (CndFileSystemProvider provider : cache) {
                return provider.isMacOSImpl(fs);
            }
            return Utilities.isMac();
        }

        @Override
        protected boolean isWindowsImpl(FileSystem fs) {
            for (CndFileSystemProvider provider : cache) {
                return provider.isWindowsImpl(fs);
            }
            return Utilities.isWindows();
        }

        @Override
        protected InputStream getInputStreamImpl(FileObject fo, int maxSize) throws IOException {
            for (CndFileSystemProvider provider : cache) {
                return provider.getInputStreamImpl(fo, maxSize);
            }
            return fo.getInputStream();
        }

        @Override
        protected boolean isRemoteImpl(FileSystem fs) {
            for (CndFileSystemProvider provider : cache) {
                return provider.isRemoteImpl(fs);
            }
            return false;
        }

        @Override
        protected CndStatInfo getStatInfoImpl(FileObject fo) {
            for (CndFileSystemProvider provider : cache) {
                return provider.getStatInfoImpl(fo);
            }
            return CndStatInfo.createInvalid();
        }
    }
}
