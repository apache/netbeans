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

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.APTDriverImpl;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.openide.filesystems.FileSystem;

/**
 * Thread safe driver to obtain APT for the file.
 * Wait till APT for file will be created.
 */
public final class APTDriver {
    
    private static final Map<FileSystem, APTDriverImpl> drivers = new WeakHashMap<FileSystem, APTDriverImpl>();
    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static final Lock rLock;
    private static final Lock wLock;
    static {
        rLock = rwLock.readLock();
        wLock = rwLock.writeLock();
    }

    /** Creates a new instance of APTCreator */
    private APTDriver() {
    }
    
    private static APTDriverImpl getInstance(APTFileBuffer buffer) {
        FileSystem fs = buffer.getFileSystem();
        APTDriverImpl impl;
        rLock.lock();
        try {
            impl = drivers.get(fs);
        } finally {
            rLock.unlock();
        }
        if (impl == null) {
            wLock.lock();
            try {
                impl = drivers.get(fs);
                if (impl == null) {
                    impl = new APTDriverImpl();
                    drivers.put(fs, impl);
                }
            } finally {
                wLock.unlock();
            }
        }
        return impl;
    }
    
    public static APTFile.Kind langFlavorToAPTFileKind(String lang) {
        return langFlavorToAPTFileKind(lang, APTLanguageSupport.FLAVOR_UNKNOWN);
    }
    public static APTFile.Kind langFlavorToAPTFileKind(String lang, String flavor) {
        // flavor is important only for Fortran
        // for C and C++ we need the same output, because created APT is reused by both language contexts
        if(lang.equalsIgnoreCase(APTLanguageSupport.FORTRAN)) {
            if(flavor.equalsIgnoreCase(APTLanguageSupport.FLAVOR_FORTRAN_FREE)) {
                return APTFile.Kind.FORTRAN_FREE;
            } else {
                return APTFile.Kind.FORTRAN_FIXED;
            }
        } else {
            // for C and C++ we use C++ mode when lex source files
            // because i.e. created APT is reused by both language contexts
            return APTFile.Kind.C_CPP;
        }        
    }

    public static APTFile findAPTLight(APTFileBuffer buffer, APTFile.Kind aptKind) throws IOException {
        assert !APTTraceFlags.USE_CLANK;
        APTFile out = null;
        if (buffer instanceof APTFileCache) {
            out = ((APTFileCache)buffer).getCachedAPTLight();
        }
        if (out == null) {
            out = getInstance(buffer).findAPT(buffer, false, aptKind);
        }
        return out;
    }
    
    public static APTFile findAPT(APTFileBuffer buffer, APTFile.Kind aptKind) throws IOException {
        assert !APTTraceFlags.USE_CLANK;
        APTFile out = null;
        if (buffer instanceof APTFileCache) {
            out = ((APTFileCache) buffer).getCachedAPT();
        }
        if (out == null) {
            out = getInstance(buffer).findAPT(buffer, true, aptKind);
        }
        return out;
    }
    
    public static void invalidateAPT(APTFileBuffer buffer) {
        if (buffer instanceof APTFileCache) {
            ((APTFileCache) buffer).invalidate();
        }
        getInstance(buffer).invalidateAPT(buffer);
    }
    
    public static void invalidateAll() {
        wLock.lock();
        try {
            for (APTDriverImpl driver : drivers.values()) {
                driver.invalidateAll();
            }
            drivers.clear();
        } finally {
            wLock.unlock();
        }
    }
    
    public static void close() {
        wLock.lock();
        try {
            for (APTDriverImpl driver : drivers.values()) {
                driver.close();
            }
            drivers.clear();
        } finally {
            wLock.unlock();
        }      
    }
    
    public static void dumpStatistics() {
        wLock.lock();
        try {
            for (APTDriverImpl driver : drivers.values()) {
                driver.traceActivity();
            }
        } finally {
            wLock.unlock();
        }
    }
}
