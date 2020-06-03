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

package org.netbeans.modules.cnd.utils.cache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.spi.utils.CndFileExistSensitiveCache;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.netbeans.modules.dlight.libs.common.PerformanceLogger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * some file utilities used by CND modules due to performance reasons or other
 */
public final class CndFileUtils {
    private static final String CASE_SENSITIVE_SYSTEM_PROP = "cnd.case.sensitive.fs"; // NOI18N
    private static final boolean TRUE_CASE_SENSITIVE_SYSTEM;
    private static final FileChangeListener FSL = new FSListener();
    private static final FSProblemListener FSPL = new FSProblemListener();
    private static final FileSystem fileFileSystem;
    public static final int FS_TIME_OUT = 30;
    public static final String LS_FOLDER_UTILS_PERFORMANCE_EVENT = "LS_FOLDER_UTILS_PERFORMANCE_EVENT"; //NOI18N
    public static final String READ_FILE_PERFORMANCE_EVENT = "READ_FILE_PERFORMANCE_EVENT"; //NOI18N
    static {
        FileSystem afileFileSystem = null;
        File tmpDirFile = new File(System.getProperty("java.io.tmpdir")); //NOI18N
        tmpDirFile = FileUtil.normalizeFile(tmpDirFile);
        FileObject tmpDirFo = FileUtil.toFileObject(tmpDirFile); // File SIC!
        if (tmpDirFo != null) {
            try {
                afileFileSystem = tmpDirFo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            tmpDirFile = new File(System.getProperty("netbeans.user")); //NOI18N
            tmpDirFile = FileUtil.normalizeFile(tmpDirFile);
            tmpDirFo = FileUtil.toFileObject(tmpDirFile);
            if (tmpDirFo != null) {
                try {
                    afileFileSystem = tmpDirFo.getFileSystem();
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (afileFileSystem == null) {
            afileFileSystem = InvalidFileObjectSupport.getDummyFileSystem();
            Exceptions.printStackTrace(new Exception("Cannot get local file system")); //NOI18N
        }
        fileFileSystem = afileFileSystem;
    }

    private CndFileUtils() {
    }

    static {
        boolean caseSenstive;
        boolean explicitlySet = false;
        String property = System.getProperty(CASE_SENSITIVE_SYSTEM_PROP);
        try {
            if (property != null) {
              caseSenstive = Boolean.parseBoolean(property);
              explicitlySet = true;
            } else {
                File tmpFile = File.createTempFile("CaseSensitiveFile", ".check"); // NOI18N
                String absPath = tmpFile.getAbsolutePath();
                absPath = absPath.toUpperCase();
                caseSenstive = !new File(absPath).exists();
                tmpFile.delete();
            }
        } catch (Throwable ex) {
            caseSenstive = Utilities.isUnix() && !Utilities.isMac();
        }
        TRUE_CASE_SENSITIVE_SYSTEM = caseSenstive;
        if (!CndUtils.isUnitTestMode()) {
            String msg = (TRUE_CASE_SENSITIVE_SYSTEM ? "case-sensitive" : "case-insensitive"); // NOI18N
            if (explicitlySet) {
              msg += " (explicitly set by " + CASE_SENSITIVE_SYSTEM_PROP + "=" + property + ")"; // NOI18N
            }
            Logger.getLogger(CndFileUtils.class.getName()).log(Level.INFO, "C/C++ File Utils: using {0} file system", msg); //NOI18N
        }        
        CndFileSystemProvider.addFileChangeListener(FSL);
        CndFileSystemProviderHelper.addFileSystemProblemListener(FSPL);
    }

    public static boolean isSystemCaseSensitive() {
        return TRUE_CASE_SENSITIVE_SYSTEM;
    }

    public static boolean areFilenamesEqual(String firstFile, String secondFile) {
        return isSystemCaseSensitive() ? firstFile.equals(secondFile) : firstFile.equalsIgnoreCase(secondFile);
    }

    public static void clearFileExistenceCache() {
        try {
            maRefLock.lock();
            for(Reference<ConcurrentMap<String, Flags>> mapRef : maps.values()) {
                mapRef.clear();
            }
            maps.clear();
        } finally {
            maRefLock.unlock();
        }
        for (CndFileExistSensitiveCache cache : getCaches()) {
            cache.invalidateAll();
        }
    }

    public static InputStream getInputStream(FileObject fo, int maxSize) throws IOException {
        return CndFileSystemProvider.getInputStream(fo, maxSize);
    }

    /**
     * normalize file
     * @param file
     * @return
     */
    public static File normalizeFile(File file) {
        CndUtils.assertAbsoluteFileInConsole(file, "Is it OK to normalize not absolute file? [" + file + "] during this session it is [" + file.getAbsolutePath() + "] but will be different if start IDE from another folder"); //NOI18N
        String path = file.getPath();
        String normPath = normalizeAbsolutePath(file.getAbsolutePath());
        return path.equals(normPath) ? file : new File(normPath);
    }

    public static File toFile(FileObject fileObject) {
        return CndFileSystemProvider.toFile(fileObject);
    }

    public static FileObject toFileObject(File file) {
        return CndFileSystemProvider.toFileObject(file);
    }

    private static final ConcurrentHashMap<FileSystem,ConcurrentHashMap<CharSequence,FileObject>> foCache = new ConcurrentHashMap<FileSystem, ConcurrentHashMap<CharSequence, FileObject>>();
    
    public static FileObject toFileObject(FileSystem fs, CharSequence absolutePath) {
        CndUtils.assertTrueInConsole(absolutePath != null, "null path ", fs);
        ConcurrentHashMap<CharSequence, FileObject> map = getFSFileObjectCache(fs);
        FileObject res = map.get(absolutePath);
        if (res == null || !res.isValid()) {
            res = toFileObjectImpl(fs, absolutePath);
            if (res != null && res.isValid()) {
                map.put(absolutePath, res);
            }
        }
        return res;
    }

    private static ConcurrentHashMap<CharSequence, FileObject> getFSFileObjectCache(FileSystem fs) {
        ConcurrentHashMap<CharSequence, FileObject> map = foCache.get(fs);
        if (map == null) {
            map = new ConcurrentHashMap<CharSequence, FileObject>();
            ConcurrentHashMap<CharSequence, FileObject> old = foCache.putIfAbsent(fs, map);
            if (old != null) {
                map = old;
            }
        }
        return map;
    }
    
    private static FileObject toFileObjectImpl(FileSystem fs, CharSequence absolutePath) {
        if (isLocalFileSystem(fs)) {
//            FileObject fo = FileUtil.toFileObject(new File(absolutePath.toString()));
//            if (fo == null) {
//                try {
//                    // sync if needed
//                    FileObject fo2 = CndFileSystemProvider.toFileObject(absolutePath);
//                    if (fo2 != null && !isLocalFileSystem(fo2.getFileSystem()) && fo2.isData()) {
//                        try {
//                            fo2.asBytes();
//                        } catch (IOException ex) {
////                            Exceptions.printStackTrace(ex);
//                        }
//                    }
//                    absolutePath = CndFileSystemProvider.getCaseInsensitivePath(absolutePath);
//                    File file = new File(absolutePath.toString());
//                    FileUtil.refreshFor(file);
//                    fo = FileUtil.toFileObject(file);
//                    if (fo == null) {
//                        fo = fo2;
//                    }
//                } catch (FileStateInvalidException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//            return fo;
            return toFileObject(absolutePath);
        } else {
            return fs.findResource(absolutePath.toString());
        }
    }
    
    public static FileObject toFileObject(CharSequence absoluteLocalPath) {
        return CndFileSystemProvider.toFileObject(absoluteLocalPath);
    }

    public static String getCanonicalPath(CharSequence path) throws IOException {
        return new File(path.toString()).getCanonicalPath(); // XXX:fullRemote conversion - delegate to provider!
    }

    public static FileObject getCanonicalFileObject(FileObject fo) throws IOException {
        Parameters.notNull("FileObject", fo); //NOI18N
        return CndFileSystemProvider.getCanonicalFileObject(fo);
    }
    
    public static String getCanonicalPath(FileObject fo) throws IOException {
        return CndFileSystemProvider.getCanonicalPath(fo);
    }

    public static boolean isValidLocalFile(String absolutePath) {
        if (CndPathUtilities.isPathAbsolute(absolutePath)) {
            return new File(absolutePath).exists();
        } else {
            return false;
        }
    }

    public static boolean isValidLocalFile(String base, String name) {
        if (CndPathUtilities.isPathAbsolute(base)) {
            return new File(base, name).exists();
        } else {
            return false;
        }
    }

    public static boolean isValidLocalFile(File base, String name) {
        if (CndPathUtilities.isPathAbsolute(base.getPath())) {
            return new File(base, name).exists();
        } else {
            return false;
        }
    }

    public static File createLocalFile(String absolutePath) {
        Parameters.notNull("null path", absolutePath); //NOI18N
        CndUtils.assertAbsolutePathInConsole(absolutePath);
        return new File(absolutePath);
    }

    public static File createLocalFile(File base,  String path) {
        Parameters.notNull("null base file", base); //NOI18N
        CndUtils.assertAbsoluteFileInConsole(base); //NOI18N
        Parameters.notNull("null path", path); //NOI18N
        return new File(base, path);
    }

    public static File createLocalFile(String base,  String path) {
        Parameters.notNull("null base file", base); //NOI18N
        CndUtils.assertAbsolutePathInConsole(base);
        Parameters.notNull("null path", path); //NOI18N
        return new File(base, path);
    }

    public static File createLocalFile(URI uri) {
        File file = Utilities.toFile(uri);
        CndUtils.assertAbsoluteFileInConsole(file); //NOI18N
        return file;
    }

    public static String normalizePath(FileObject fo) {
        try {
            return normalizeAbsolutePath(fo.getFileSystem(), fo.getPath());
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
            return fo.getPath();
        }
    }

    public static String normalizeAbsolutePath(FileSystem fs, String path) {
        return CndFileSystemProvider.normalizeAbsolutePath(fs, path);
    }
    
    /**
     * normalize LOCAL absolute paths
     * @param path
     * @return
     */
    public static String normalizeAbsolutePath(String path) {
        CndUtils.assertAbsolutePathInConsole(path, "path for normalization must be absolute"); //NOI18N
        // TODO: this should be probably rewritten in a more elegant way
        if (path.startsWith("/") && Utilities.isWindows()) { // NOI18N
            return PathUtilities.normalizeUnixPath(path);
        }
        boolean caseSensitive = isSystemCaseSensitive();
        if (!caseSensitive) {
            if (Utilities.isWindows()) {
                path = path.replace('\\', '/');
            }
        }
        String normalized;
        // small optimization for true case sensitive OSs
        if (!caseSensitive || (path.endsWith("/.") || path.endsWith("\\.") || path.contains("..") || path.contains("./") || path.contains(".\\"))) { // NOI18N
            normalized = FileUtil.normalizeFile(new File(path)).getAbsolutePath();
        } else {
            normalized = path;
        }        
        return normalized;
    }

    public static boolean exists(File file) {
        return getFlags(getLocalFileSystem(), file.getAbsolutePath(), true).exist;
    }

    public static boolean exists(FileSystem fs, String absolutePath) {
        return getFlags(fs, absolutePath, true).exist;
    }

    public static boolean isExistingFile(String filePath) {
        return isExistingFile(getLocalFileSystem(), filePath);
    }
    
    /**
     * Tests whether the file exists and not directory. One of file or filePath
     * must be not null
     * @param fs file system
     * @param filePath
     * @return
     */
    public static boolean isExistingFile(FileSystem fs, String filePath) {
        Flags flags = getFlags(fs, filePath, true);
        return flags.exist && !flags.directory;
    }

    public static boolean isExistingDirectory(FileSystem fs, String filePath) {
        Flags flags = getFlags(fs, filePath, false);
        return flags.exist && flags.directory;
    }

    public static FileObject urlToFileObject(CharSequence url) {
        return CndFileSystemProvider.urlToFileObject(url);
    }

    public static FileSystem urlToFileSystem(CharSequence url) {
        return CndFileSystemProvider.urlToFileSystem(url);
    }
    
    public static CharSequence fileObjectToUrl(FileObject fileObject) {
        return CndFileSystemProvider.fileObjectToUrl(fileObject);
    }

   /** just to speed it up, since Utilities.isWindows will get string property, test equals, etc */
    private static final boolean isWindows;
    private static final String  windowsDrive;
    private static final String  windowsPathSeparator = "\\"; // NOI18N
    private static final String  UNC_PREFIX = "\\\\"; // NOI18N
    static {
        isWindows = Utilities.isWindows();
        if (isWindows) {
            String disk = new File(windowsPathSeparator).getAbsolutePath(); 
            if (!disk.endsWith(windowsPathSeparator)) { 
                assert disk.endsWith(":") : "unexpected disk name " + disk;
                disk += windowsPathSeparator;
            }
            windowsDrive = disk;
        } else {
            windowsDrive = "";// NOI18N
        }
    }
   
   // expensive check
   private static final boolean ASSERT_INDEXED_NOTFOUND = Boolean.getBoolean("cnd.modelimpl.assert.notfound"); // NOI18N

    private static Flags getFlags(FileSystem fs, String absolutePath, boolean indexParentFolder) {
        assert fs != null;
        assert absolutePath != null;
        if (CndUtils.isDebugMode()) {
            CndUtils.assertTrueInConsole(!absolutePath.contains("var/cache/remote-files"), "trying to get access to " + absolutePath);  //NOI18N
        }
        if (isWindows && isLocalFileSystem(fs)) {
            absolutePath = absolutePath.replace('/', '\\');
            // append disk drive on windows for files like "/ws/full/path"
            if (absolutePath.startsWith("\\")) { //NOI18N
                // but leave UNC file paths as is: "\\USER-PC\Users\admin\Quote_1\iostream"
                if (!absolutePath.startsWith(UNC_PREFIX)) {
                  absolutePath = windowsDrive + absolutePath.substring(1);
                }
            }
        }
        absolutePath = changeStringCaseIfNeeded(fs, absolutePath);
        Flags exists;
        ConcurrentMap<String, Flags> files = getFilesMap(fs);
        exists = files.get(absolutePath);
        if (exists == null) {
            String parent = CndPathUtilities.getDirName(absolutePath);
            if (parent != null) {
                Flags parentDirFlags = files.get(parent);
                if (parentDirFlags == null || parentDirFlags == Flags.DIRECTORY) {
                    if (parentDirFlags == null) {
                        // not yet checked
                        parentDirFlags = Flags.get(fs, parent);
                        files.putIfAbsent(parent, parentDirFlags);
                    }
                    if (parentDirFlags == Flags.NOT_FOUND || parentDirFlags == Flags.BROKEN_LINK || parentDirFlags == Flags.FILE) {
                        // no need to check non existing file
                        exists = Flags.NOT_FOUND;
//                        files.put(path, exists);
                    } else if (parentDirFlags == Flags.NOT_FOUND_CONNECTION_ISSUE) {
                        exists = Flags.NOT_FOUND_CONNECTION_ISSUE;
                    } else if (indexParentFolder) {
                        assert (parentDirFlags == Flags.DIRECTORY) : "must be DIRECTORY but was " + parentDirFlags; // NOI18N
                        // let's index not indexed directory
                        index(fs, parent, files);
                        exists = files.get(absolutePath);
                        if (exists == null) {
                            // if we're inside INDEXED_DIRECTORY then the file does not exist
                            exists = Flags.NOT_FOUND;
                            // does not make sense to assert here, file may appear after parent folder index but before this assert, see bug 2322294
//                            if (ASSERT_INDEXED_NOTFOUND) {
//                                assert Flags.get(fs, absolutePath) == Flags.NOT_FOUND : absolutePath + " exists, but reported as NOT_FOUND"; //NOI18N
//                            }
                        }
                    }
                } else {
                    if (parentDirFlags == Flags.INDEXED_DIRECTORY) {
                        // if we're inside INDEXED_DIRECTORY then the file does not exist
                        exists = Flags.NOT_FOUND;
                        if (ASSERT_INDEXED_NOTFOUND) {
                            assert Flags.get(fs, absolutePath) == Flags.NOT_FOUND : absolutePath + " exists, but reported as NOT_FOUND"; //NOI18N
                        }
                    } else if (parentDirFlags == Flags.NOT_FOUND || parentDirFlags == Flags.BROKEN_LINK) {
                        // no need to check non existing file
                        exists = Flags.NOT_FOUND;
                    } else if (parentDirFlags == Flags.NOT_FOUND_CONNECTION_ISSUE) {
                        exists = Flags.NOT_FOUND_CONNECTION_ISSUE;
                    } else {
                        // may be our parent was indexed in parallel thread
                        exists = files.get(absolutePath);
                    }
                }
            }
            if (exists == null) {
                exists = Flags.get(fs, absolutePath);
                files.putIfAbsent(absolutePath, exists);
            }
            if (exists == Flags.DIRECTORY) {
                // let's index not indexed directory
                index(fs, absolutePath, files);
            }
        } else {
            if (exists == Flags.NOT_FOUND_CONNECTION_ISSUE) {
                CndFileSystemProviderHelper.fireFileSystemProblemOccurred(new FSPath(fs, absolutePath));
            }
            //hits ++;
        }
        return exists;
    }

    public static boolean isRemoteFileSystem(FileSystem fs) {
        return ! isLocalFileSystem(fs);
    }

    public static boolean isRemoteFileSystem(FileObject fo) {
        return ! isLocalFileSystem(fo);
    }

    public static boolean isLocalFileSystem(FileSystem fs) {
        return fs == getLocalFileSystem();
    }

    public static boolean isLocalFileSystem(FileObject fo) {
        try {
            return fo.getFileSystem() == getLocalFileSystem();
        } catch (FileStateInvalidException ex) {
            return false;
        }
    }

    private static void index(FileSystem fs, String path, ConcurrentMap<String, Flags> files) {        
        if (isLocalFileSystem(fs)) {
            File file = new File(path);
            if (CndFileSystemProvider.canRead(path)) {
                CndFileSystemProvider.FileInfo[] listFiles = listFilesImpl(file);
                for (CndFileSystemProvider.FileInfo curFile : listFiles) {
                    String absPath = changeStringCaseIfNeeded(fs, curFile.absolutePath);
                    if (isWindows) { //  isLocalFS(fs) checked above
                        absPath = absPath.replace('/', '\\');
                    }
                    if (curFile.directory) {
                        files.putIfAbsent(absPath, Flags.DIRECTORY);
                    } else if (curFile.file) {
                        files.put(absPath, Flags.FILE);
                    } else {
                        // broken link
                        files.put(absPath, Flags.BROKEN_LINK);
                    }
                }
            }        
        } else {
            FileObject file = fs.findResource(path);            
            if (file != null && file.isFolder() && file.canRead()) {
                final char fileSeparatorChar = getFileSeparatorChar(fs);            
                FileObject[] children;
                fileSystemError.set(Boolean.FALSE);
                children = file.getChildren();
                if (fileSystemError.get()) {
                    files.put(path, Flags.NOT_FOUND_CONNECTION_ISSUE);
                    return;
                }
                for (FileObject child : children) {
                    //we do concat as we need to index relative path not absolute ones
                    String absPath = path + fileSeparatorChar + child.getNameExt();
                    if (child.isFolder()) {
                        files.putIfAbsent(absPath, Flags.DIRECTORY);
                    } else if (child.isData()) {
                        files.put(absPath, Flags.FILE);
                    } else {
                        // broken link
                        files.put(absPath, Flags.BROKEN_LINK);
                    }
                }
            }
        }
        // path is already converted into correct case
        assert changeStringCaseIfNeeded(fs, path).equals(path);
        files.put(path, Flags.INDEXED_DIRECTORY);
    }

    private static String changeStringCaseIfNeeded(FileSystem fs, String path) {
        if (isLocalFileSystem(fs)) {
            if (CndFileUtils.isSystemCaseSensitive()) {
                return path;
            } else {
                return path.toLowerCase();
            }
        } else {
            return path; // remote is always case sensitive
        }
    }

//    public static String getHitRate() {
//	return "" + hits + "/" + calls; // NOI18N
//    }
//    private static int calls = 0;
//    private static int hits = 0;

    private static ConcurrentMap<String, Flags> getFilesMap(FileSystem fs) {
        ConcurrentMap<String, Flags> map;
        if (CndTraceFlags.L1_CACHE_FILE_UTILS) {
            L1Cache aCache = l1Cache;
            if (aCache != null) {
                map = aCache.get(fs);
                if (map != null) {
                    return map;
                }
            }
        }
        try {
            maRefLock.lock();
            Reference<ConcurrentMap<String, Flags>> mapRef = maps.get(fs);
            if (mapRef == null || (map = mapRef.get()) == null) {
                map = new ConcurrentHashMap<String, Flags>();
                mapRef = new SoftReference<ConcurrentMap<String, Flags>>(map);
                maps.put(fs, mapRef);
                if (CndTraceFlags.L1_CACHE_FILE_UTILS) {
                    l1Cache = new L1Cache(fs, mapRef);
                }
            }
        } finally {
            maRefLock.unlock();
        }
        return map;
    }

    private static L1Cache l1Cache;
    private final static class L1Cache {
        private final FileSystem fs;
        private final Reference<ConcurrentMap<String, Flags>> mapRef;
        private L1Cache(FileSystem fs, Reference<ConcurrentMap<String, Flags>> mapRef) {
            this.fs = fs;
            this.mapRef = mapRef;
        }
        private ConcurrentMap<String, Flags> get(FileSystem fs) {
            if (this.fs == fs) {
                return mapRef.get();
            }
            return null;
        }
    }

    private static CndFileSystemProvider.FileInfo[] listFilesImpl(File file) {
        CndFileSystemProvider.FileInfo[] info = CndFileSystemProvider.getChildInfo(file.getAbsolutePath());
        if (info == null) {
            PerformanceLogger.PerformaceAction lsPerformanceEvent = PerformanceLogger.getLogger().start(CndFileUtils.LS_FOLDER_UTILS_PERFORMANCE_EVENT, file);
            File[] children = null;
            try { 
                lsPerformanceEvent.setTimeOut(CndFileUtils.FS_TIME_OUT);
                children = file.listFiles();
                if (children != null) {
                    info = new CndFileSystemProvider.FileInfo[children.length];
                    for (int i = 0; i < children.length; i++) {
                        info[i] = new CndFileSystemProvider.FileInfo(children[i].getAbsolutePath(), children[i].isDirectory(), children[i].isFile());
                    }
                } else {
                    info = new CndFileSystemProvider.FileInfo[0];
                }
            } finally {
                lsPerformanceEvent.log(children == null ? 0 : children.length);
            }
        }
        return info;
    }
    
    public static FileSystem getLocalFileSystem() {
        return fileFileSystem;
    }
    
    public static char getFileSeparatorChar(FileSystem fs) {
        if (isLocalFileSystem(fs)) {
            return File.separatorChar;
        } else {
            return '/'; //NOI18N
        }
    }

    public static List<String> toPathList(Collection<FSPath> paths) {
        if (paths != null && paths.size() > 0) {
            List<String> result =  new ArrayList<String>(paths.size());
            for (FSPath fSPath : paths) {
                result.add(fSPath.getPath());
            }
            return result;
        }
        return Collections.<String>emptyList();
    }

    public static List<FSPath> toFSPathList(FileSystem fileSystem, Collection<String> paths) {
        if (paths != null && paths.size() > 0) {
            List<FSPath> result = new ArrayList<FSPath>(paths.size());
            for (String path : paths) {
                result.add(new FSPath(fileSystem, path));
            }
            return result;
        }
        return Collections.<FSPath>emptyList();
    }

    public static FileSystem decodeFileSystem(CharSequence stringRepresentation) throws IOException {
        FileObject rootFileObject = CndFileUtils.urlToFileObject(stringRepresentation);
        assert (rootFileObject != null) : "Restored null file object for URL " + stringRepresentation;
        return rootFileObject.getFileSystem();
    }

    public static CharSequence codeFileSystem(FileSystem fs) throws IOException {
        CharSequence rootUrl = CharSequences.create(CndFileUtils.fileObjectToUrl(fs.getRoot()));
        return rootUrl;
    }

    private static final Lock maRefLock = new ReentrantLock();

    private static final Map<FileSystem, Reference<ConcurrentMap<String, Flags>>> maps = 
            new WeakHashMap<FileSystem, Reference<ConcurrentMap<String, Flags>>>();

    private static final ThreadLocal<Boolean> fileSystemError = new ThreadLocal<>();
    
    private final static class Flags {

        private final boolean exist;
        private final boolean directory;
        private Flags(boolean exist, boolean directory){
            this.exist = exist;
            this.directory = directory;
        }
        private static final Flags FILE = new Flags(true,false);
        private static final Flags DIRECTORY = new Flags(true,true);
        private static final Flags INDEXED_DIRECTORY = new Flags(true,true);
        private static final Flags NOT_FOUND = new Flags(false,true);
        private static final Flags NOT_FOUND_CONNECTION_ISSUE = new Flags(false,true);
        private static final Flags BROKEN_LINK = new Flags(false, false);
        
        private static Flags get(FileSystem fs, String absPath) {
            FileObject fo;
            fileSystemError.set(Boolean.FALSE);
            if (isLocalFileSystem(fs)) {
                absPath = FileUtil.normalizePath(absPath);
                fo = CndFileSystemProvider.toFileObject(absPath);                
            } else {
                fo = fs.findResource(absPath);
            }
            if (fo != null && fo.isValid()) {
                if (fo.isFolder()) {
                    return DIRECTORY;
                } else {
                    assert fo.isData() : "not a file " + fo;
                    return FILE;
                }
            } else {
                return fileSystemError.get() ? NOT_FOUND_CONNECTION_ISSUE : NOT_FOUND;
            }
        }

        @Override
        public String toString() {
            if (this == NOT_FOUND) {
                return "NOT_FOUND"; // NOI18N
            } else if (this == NOT_FOUND_CONNECTION_ISSUE) {
                return "NOT_FOUND_CONNECTION_ISSUE"; // NOI18N
            } else if (this == INDEXED_DIRECTORY) {
                return "INDEXED_DIRECTORY"; // NOI18N
            } else if (this == DIRECTORY) {
                return "DIRECTORY"; // NOI18N
            } else if (this == FILE) {
                return "FILE"; // NOI18N
            } else if (this == BROKEN_LINK) {
                return "BROKEN_LINK"; // NOI18N
            } else {
                return "UNKNOWN"; // NOI18N
            }
        }

    }
    
    private static final class FSProblemListener implements CndFileSystemProvider.CndFileSystemProblemListener {

        @Override
        public void problemOccurred(FSPath fsPath) {
            fileSystemError.set(Boolean.TRUE);
        }

        @Override
        public void recovered(FileSystem fileSystem) {
        }        
    }
    
    private static class CndFileSystemProviderHelper {

        private CndFileSystemProviderHelper() {
        }

        private static abstract class FakeProvider extends CndFileSystemProvider {

            public static void addProblemListener(CndFileSystemProvider.CndFileSystemProblemListener listener) {
                CndFileSystemProvider.addFileSystemProblemListener(listener);
            }

            protected static void fireProblem(FSPath fSPath) {
                CndFileSystemProvider.fireFileSystemProblemOccurred(fSPath);
            }
        }

        public static void addFileSystemProblemListener(CndFileSystemProvider.CndFileSystemProblemListener listener) {
            FakeProvider.addProblemListener(listener);
        }

        public static void fireFileSystemProblemOccurred(FSPath fSPath) {
            FakeProvider.fireProblem(fSPath);
        }
    }
    

    private static final class FSListener implements FileChangeListener {

        private FSListener() {
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {            
            String path = checkSeparators(fe.getFile());
            String absPath = changeStringCaseIfNeeded(getFileSystem(fe), path);
            FileSystem fs = getFileSystem(fe);
            if (getFilesMap(fs).put(absPath, Flags.DIRECTORY) != null) {
                // If there was something in the map already - invalidate it
                invalidateFile(fs, path, absPath);
            }
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            String path = checkSeparators(fe.getFile());
            String absPath = changeStringCaseIfNeeded(getFileSystem(fe), path);
            FileSystem fs = getFileSystem(fe);
            if (getFilesMap(fs).put(absPath, Flags.FILE) != null) {
                // If there was something in the map already - invalidate it
                invalidateFile(fs, path, absPath);
            }
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            final FSPath file = FSPath.toFSPath(fe.getFile());
            cleanCachesImpl(file);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            final FSPath newFile = FSPath.toFSPath(fe.getFile());
            cleanCachesImpl(newFile);
            
            final FSPath parent = newFile.getParent();
            // update info about old file as well
            if (parent != null) {
                final String ext = fe.getExt();
                final String oldName = (ext.length() == 0) ? fe.getName() : (fe.getName() + "." + ext); // NOI18N
                final FSPath oldFile = parent.getChild(oldName);
                cleanCachesImpl(oldFile);
            }
        }

        @Override
        public void fileChanged(FileEvent fe) {
            // no update
        }
        
        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // no update
        }
        
        private String checkSeparators(FileObject fo) {
            return checkSeparators(FSPath.toFSPath(fo));
        }
        
        private String checkSeparators(FSPath path) {
            String absPath = path.getPath();
            if (isWindows && isLocalFileSystem(path.getFileSystem())) {
                absPath = absPath.replace('/', '\\');
            }
            return absPath;
        }
        
        private FileSystem getFileSystem(FileEvent fe) {
            return getFileSystem(fe.getFile());
        }
        
        private FileSystem getFileSystem(FileObject fo) {
            try {
                return fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                throw new IllegalStateException(ex);
            }
        }

        private static void invalidateFile(FileSystem fileSystem, String file, String localPathOrRemoteUrl) {
            for (CndFileExistSensitiveCache cache : getCaches()) {
                cache.invalidateFile(fileSystem, file);
                cache.invalidateFile(fileSystem, localPathOrRemoteUrl);
            }
        }

        private void cleanCachesImpl(FSPath fsPath) {
            String file = checkSeparators(fsPath);
            String absPath = changeStringCaseIfNeeded(fsPath.getFileSystem(), file);
            Flags removed = getFilesMap(getLocalFileSystem()).remove(absPath);
            if (TRACE_EXTERNAL_CHANGES) {
                System.err.printf("clean cache for %s->%s\n", absPath, removed);
            }
            getFSFileObjectCache(fsPath.getFileSystem()).remove(fsPath.getPath());
            invalidateFile(fsPath.getFileSystem(), file, absPath);
        }
    }
    private static final boolean TRACE_EXTERNAL_CHANGES = Boolean.getBoolean("cnd.modelimpl.trace.external.changes"); // NOI18N
    private static volatile Collection<? extends CndFileExistSensitiveCache> listeners;
    private static Collection<? extends CndFileExistSensitiveCache> getCaches() {
        if (listeners == null) {
             listeners = Lookup.getDefault().lookupAll(CndFileExistSensitiveCache.class);
        }
        return listeners;
    }
}
