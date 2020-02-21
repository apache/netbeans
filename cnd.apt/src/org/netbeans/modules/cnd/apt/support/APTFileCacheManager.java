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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.apt.support;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.apt.impl.support.APTHandlersSupportImpl;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler.State;
import org.openide.filesystems.FileSystem;
import org.openide.util.Parameters;

/**
 *
 */
public final class APTFileCacheManager {

    private APTFileCacheManager() {
    }

    private static Map<FileSystem, APTFileCacheManager> managers = new WeakHashMap<FileSystem, APTFileCacheManager>();
    private static final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private static final java.util.concurrent.locks.Lock rLock;
    private static final java.util.concurrent.locks.Lock wLock;
    static {
        rLock = rwLock.readLock();
        wLock = rwLock.writeLock();
    }

    public static APTFileCacheManager getInstance(FileSystem fs) {
        Parameters.notNull("null file system", fs); //NOI18N
        APTFileCacheManager manager;
        rLock.lock();
        try {
            manager = managers.get(fs);
        } finally {
            rLock.unlock();
        }
        if (manager == null) {
            wLock.lock();
            try {
                manager = managers.get(fs);
                if (manager == null) {
                    manager = new APTFileCacheManager();
                    managers.put(fs, manager);
                }
            } finally {
                wLock.unlock();
            }
        }
        return manager;
    }

    private ConcurrentMap<CharSequence, Reference<ConcurrentMap<APTIncludeHandler.State, APTFileCacheEntry>>> file2AptCacheRef = new ConcurrentHashMap<CharSequence, Reference<ConcurrentMap<APTIncludeHandler.State, APTFileCacheEntry>>>();
    private static final class Lock {}
    private final Object aptCachesLock = new Lock();

    @org.netbeans.api.annotations.common.SuppressWarnings("DLS")
    private ConcurrentMap<APTIncludeHandler.State, APTFileCacheEntry> getAPTCache(CharSequence file, Boolean createAndClean) {
        if (createAndClean == null) {
            Reference<ConcurrentMap<State, APTFileCacheEntry>> removed = file2AptCacheRef.remove(file);
            return null;
        }
        Reference<ConcurrentMap<State, APTFileCacheEntry>> ref2fileCache = file2AptCacheRef.get(file);
        ConcurrentMap<State, APTFileCacheEntry> out = ref2fileCache == null ? null : ref2fileCache.get();
        if (out == null) {
            out = new ConcurrentHashMap<State, APTFileCacheEntry>();
            if (APTTraceFlags.USE_SOFT_APT_CACHE) {
                ref2fileCache = new SoftReference<ConcurrentMap<State, APTFileCacheEntry>>(out);
            } else {
                ref2fileCache = new WeakReference<ConcurrentMap<State, APTFileCacheEntry>>(out);
            }
            Reference<ConcurrentMap<State, APTFileCacheEntry>> prev = file2AptCacheRef.putIfAbsent(file, ref2fileCache);
            if (prev != null) {
                ConcurrentMap<State, APTFileCacheEntry> prevCache = prev.get();
                if (prevCache != null) {
                    out = prevCache;
                } else {
                    synchronized (aptCachesLock) {
                        file2AptCacheRef.remove(file, prev);
                        boolean add = false;
                        prev = file2AptCacheRef.putIfAbsent(file, ref2fileCache);
                        if (prev != null) {
                            prevCache = prev.get();
                            if (prevCache != null) {
                                out = prevCache;
                            } else {
                                add = true;
                            }
                        }
                        if (add) {
                            file2AptCacheRef.put(file, ref2fileCache);
                        }
                    }
                }
            }
        }
        assert out != null;
        if (Boolean.TRUE.equals(createAndClean)) {
            out.clear();
        }
        return out;
    }

    /**
     *
     * @param file
     * @param preprocHandler
     * @param createExclusiveIfAbsent pass null if only interested in existing entry,
     *          Boolean.TRUE means to create non-concurrent entry
     *          Boolean.FALSE menas to create concurrent entry and remember it in cache
     * @return
     */
    public APTFileCacheEntry getEntry(CharSequence file, APTPreprocHandler.State ppState, Boolean createExclusiveIfAbsent) {
        APTIncludeHandler.State key = getKey(ppState);
        ConcurrentMap<APTIncludeHandler.State, APTFileCacheEntry> cache = getAPTCache(file, Boolean.FALSE);
        APTFileCacheEntry out = cache.get(key);
        if (createExclusiveIfAbsent != null) {
            if (out == null) {
                if (Boolean.TRUE.equals(createExclusiveIfAbsent)) {
                    out = APTFileCacheEntry.createSerialEntry(file);
                } else {
                    // we do remember concurrent entries
                    out = APTFileCacheEntry.createConcurrentEntry(file);
                    APTFileCacheEntry prev = cache.putIfAbsent(key, out);
                    if (prev != null) {
                        out = prev;
                    }
                }
            } else {
                if (Boolean.TRUE.equals(createExclusiveIfAbsent)) {
                    out = APTFileCacheEntry.toReadOnly(out);
                }
                if (APTTraceFlags.TRACE_APT_CACHE) {
                    System.err.printf("APT CACHE for %s%nsize %d, key: %s%ncache state:%s%n", file, cache.size(), "", "");
                }
            }
        } else if (out != null) {
            out = APTFileCacheEntry.toReadOnly(out);
        }
        assert createExclusiveIfAbsent == null || out != null;
        return out;
    }

    public void setAPTCacheEntry(CharSequence absPath, APTPreprocHandler.State ppState, APTFileCacheEntry entry, boolean cleanOthers) {
        if (entry != null) {
            ConcurrentMap<APTIncludeHandler.State, APTFileCacheEntry> cache = getAPTCache(absPath, cleanOthers ? Boolean.TRUE : Boolean.FALSE);
            APTIncludeHandler.State key = getKey(ppState);
            cache.put(key, APTFileCacheEntry.toCachable(entry));
        }
    }

    public void invalidate(CharSequence absPath) {
        ConcurrentMap<State, APTFileCacheEntry> fileEntry = getAPTCache(absPath, null);
        if (fileEntry != null) {
            fileEntry.clear();
        }
    }

    public static void invalidateAll() {
        wLock.lock();
        try {
            for (APTFileCacheManager manager : managers.values()) {
                manager.file2AptCacheRef.clear();
            }
            managers.clear();
        } finally {
            wLock.unlock();
        }
    }

    public static void close() {
        invalidateAll();
    }

    private static State getKey(APTPreprocHandler.State ppState) {
        return APTHandlersSupportImpl.extractIncludeState(ppState);
    }
}
