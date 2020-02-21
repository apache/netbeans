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

package org.netbeans.modules.cnd.apt.support;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.cnd.apt.impl.support.ResolverResultsCache;
import org.netbeans.modules.cnd.apt.impl.support.SupportAPIAccessor;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;

/**
 *
 */
public final class IncludeDirEntry {
    static {
        SupportAPIAccessor.register(new AccessorImpl());
    }

    private static final int MANAGER_DEFAULT_CAPACITY;
    private static final int MANAGER_DEFAULT_SLICED_NUMBER;
    static {
        int nrProc = CndUtils.getConcurrencyLevel();
        if (nrProc <= 4) {
            MANAGER_DEFAULT_SLICED_NUMBER = 32;
            MANAGER_DEFAULT_CAPACITY = 512;
        } else {
            MANAGER_DEFAULT_SLICED_NUMBER = 128;
            MANAGER_DEFAULT_CAPACITY = 128;
        }
    }
    private static final IncludeDirStorage storage = new IncludeDirStorage(MANAGER_DEFAULT_SLICED_NUMBER, MANAGER_DEFAULT_CAPACITY);

    private volatile Boolean exists;
    private final boolean isFramework;
    private final boolean ignoreSysRoot;
    private final CharSequence asCharSeq;
    private final FileSystem fileSystem;
    private final int hashCode;

    private IncludeDirEntry(boolean exists, boolean framework, boolean ignoreSysRoot, FileSystem fileSystem, CharSequence asCharSeq, int hashCode) {
        this.exists = exists;
        this.isFramework = framework;
        this.ignoreSysRoot = ignoreSysRoot;
        this.fileSystem = fileSystem;
        this.asCharSeq = asCharSeq;
        this.hashCode = hashCode;
    }

    public static IncludeDirEntry get(FSPath fsPath, boolean framework, boolean ignoreSysRoot) {
        FileSystem fs = fsPath.getFileSystem();
        String dir = fsPath.getPath();
        CndUtils.assertAbsolutePathInConsole(dir);
        CharSequence key = FilePathCache.getManager().getString(CndFileSystemProvider.toUrl(fs, dir));
        Map<CharSequence, IncludeDirEntry> delegate = storage.getDelegate(key);
        IncludeDirEntry out;
        synchronized (delegate) {
            out = delegate.get(key);
        }
        if (out == null) {
            // #196267 -  slow parsing in Full Remote
            // do expensive work out of sync block

            // FIXME XXX:FullRemote
            if (dir.contains(File.separatorChar + "remote-files" + File.separatorChar)) { //XXX:fullRemote //NOI18N
                fs = CndFileUtils.getLocalFileSystem();
            }
            boolean exists = CndFileUtils.isExistingDirectory(fs, dir);
            FileSystem entryFS = fs;
            if (exists) {
                FileObject fo = CndFileUtils.toFileObject(fs, dir);
                if (fo == null) {
                    exists = false;
                } else {
                    try {
                        entryFS = fo.getFileSystem();
                        dir = CndFileUtils.normalizePath(fo);
                    } catch (FileStateInvalidException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
            CharSequence asCharSeq = FilePathCache.getManager().getString(dir);
            // then go into sync block again
            boolean resetNonExistanceFlag = false;
            synchronized (delegate) {
                out = delegate.get(key);
                if (out == null) {
                    out = new IncludeDirEntry(exists, framework, ignoreSysRoot, entryFS, asCharSeq, key.hashCode());
                    delegate.put(key, out);
                } else {
                    resetNonExistanceFlag = true;
                }
            }
            if (resetNonExistanceFlag) {
                out.resetNonExistanceFlag();
            }
        } else {
            out.resetNonExistanceFlag();
        }
        return out;
    }

    /*package*/ void resetNonExistanceFlag() {
        if (exists == Boolean.FALSE) { // perhaps && !CndFileUtils.isLocalFileSystem(fileSystem) ??
            exists = CndFileUtils.isExistingDirectory(fileSystem, getPath());
        }
    }
    
    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IncludeDirEntry other = (IncludeDirEntry) obj;
        if (this.isFramework != other.isFramework) {
            return false;
        }
        if (this.asCharSeq != other.asCharSeq && (this.asCharSeq == null || !this.asCharSeq.equals(other.asCharSeq))) {
            return false;
        }
        if (this.fileSystem != other.fileSystem && (this.fileSystem == null || !this.fileSystem.equals(other.fileSystem))) {
            return false;
        }
        return true;
    }


    public CharSequence getAsSharedCharSequence() {
        return asCharSeq;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public boolean isFramework() {
        return isFramework;
    }

    public boolean ignoreSysRoot() {
        return ignoreSysRoot;
    }

    public boolean isExistingDirectory() {
        Boolean val = exists;
        if (val == null) {
            val = CndFileUtils.isExistingDirectory(fileSystem, getPath());
            exists = val;
        }
        return val;
    }

    public String getPath() {
        return asCharSeq.toString();
    }

    @Override
    public String toString() {
        Boolean val = exists;
        return (val == null ? "Not Initialized exist flag" : (val.booleanValue() ? "" : "NOT EXISTING ")) + fileSystem.getDisplayName() + ':' + asCharSeq; // NOI18N
    }

    private void invalidateDirExistence() {
        exists = null;
    }

    /*package*/static void disposeCache() {
        storage.dispose();
        ResolverResultsCache.clearCache();
    }

    /*package*/static void invalidateCache() {
        for (Map<CharSequence, IncludeDirEntry> map : storage.instances) {
            synchronized (map) {
                for (IncludeDirEntry includeDirEntry : map.values()) {
                    includeDirEntry.invalidateDirExistence();
                }
            }
        }
        ResolverResultsCache.clearCache();
    }

    /*package*/static void invalidateFileBasedCache(String file) {
        final CharSequence key = FilePathCache.getManager().getString(file);
        Map<CharSequence, IncludeDirEntry> delegate = storage.getDelegate(key);
        synchronized (delegate) {
            IncludeDirEntry prev = delegate.remove(key);
            if (prev != null) {
                prev.invalidateDirExistence();
            }
        }
        ResolverResultsCache.clearCache();
    }

    private static final class IncludeDirStorage {

        private final WeakHashMap<CharSequence, IncludeDirEntry>[] instances;
        private final int segmentMask; // mask

        private IncludeDirStorage(int sliceNumber, int initialCapacity) {
            // Find power-of-two sizes best matching arguments
            int ssize = 1;
            while (ssize < sliceNumber) {
                ssize <<= 1;
            }
            segmentMask = ssize - 1;
            @SuppressWarnings("unchecked")
            WeakHashMap<CharSequence, IncludeDirEntry>[] ar = new WeakHashMap[ssize];
            for (int i = 0; i < ar.length; i++) {
                ar[i] = new WeakHashMap<CharSequence, IncludeDirEntry>(initialCapacity);
            }
            instances = ar;
        }

        private Map<CharSequence, IncludeDirEntry> getDelegate(CharSequence key) {
            int index = key.hashCode() & segmentMask;
            return instances[index];
        }

        @SuppressWarnings("unchecked")
        public final IncludeDirEntry getSharedUID(CharSequence key) {
            return getDelegate(key).get(key);
        }

        public final void dispose() {
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].size() > 0) {
                    if (CndTraceFlags.TRACE_SLICE_DISTIBUTIONS) {
                        System.out.println("Include Dir Cache " + instances[i].size()); // NOI18N
                        Map<Class<?>, Integer> keyClasses = new HashMap<Class<?>, Integer>();
                        for (Object o : instances[i].keySet()) {
                            if (o != null) {
                                incCounter( keyClasses, o);
                            }
                        }
                        for (Map.Entry<Class<?>, Integer> e : keyClasses.entrySet()) {
                            System.out.println("   " + e.getValue() + " of " + e.getKey().getName()); // NOI18N
                        }
                    }
                    instances[i].clear();
                }
            }
        }

        private void incCounter(Map<Class<?>, Integer> uidClasses, Object o) {
            Integer num = uidClasses.get(o.getClass());
            if (num != null) {
                num = Integer.valueOf(num.intValue() + 1);
            } else {
                num = Integer.valueOf(1);
            }
            uidClasses.put(o.getClass(), num);
        }
    }
}
