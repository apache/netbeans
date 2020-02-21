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
