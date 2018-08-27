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

package org.netbeans.lib.profiler.heap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Tomas Hurka
 */
class CacheDirectory {
    
    private static final String DIR_EXT = ".hwcache";   // NOI18N
    private static final String DUMP_AUX_FILE = "NBProfiler.nphd";   // NOI18N
    
    private File cacheDirectory;
    
    static CacheDirectory getHeapDumpCacheDirectory(File heapDump) {
        String dumpName = heapDump.getName();
        File parent = heapDump.getParentFile();
        File dir = new File(parent, dumpName+DIR_EXT);
        return new CacheDirectory(dir);
    }
    
    CacheDirectory(File cacheDir) {
        cacheDirectory = cacheDir;
        if (cacheDir != null) {
            if (!cacheDir.exists()) {
                if (!cacheDir.mkdir()) {
                    cacheDirectory = null;
                }
            }
        }
        if (cacheDirectory != null) {
            assert cacheDirectory.isDirectory() && cacheDirectory.canRead() && cacheDirectory.canWrite();            
        }
    }
    
    File createTempFile(String prefix, String suffix) throws IOException {
        File newFile;
        
        if (isTemporary()) {
            newFile = File.createTempFile(prefix, suffix);
            newFile.deleteOnExit();
        } else {
            newFile = File.createTempFile(prefix, suffix, cacheDirectory);
        }
        return newFile;
    }
    
    File getHeapDumpAuxFile() {
        assert !isTemporary();
        return new File(cacheDirectory, DUMP_AUX_FILE);
    }
    
    boolean isTemporary() {
        return cacheDirectory == null;
    }

    File getCacheFile(String fileName) throws FileNotFoundException {
        File f = new File(fileName);
        if (isFileRW(f)) {
            return f;
        }
        // try to find file in cache directory
        f = new File(cacheDirectory, f.getName());
        if (isFileRW(f)) {
            return f;
        }
        throw new FileNotFoundException(fileName);
    }

    File getHeapFile(String fileName) throws FileNotFoundException {
        File f = new File(fileName);
        if (isFileR(f)) {
            return f;
        }
        // try to find heap dump file next to cache directory
        f = new File(cacheDirectory.getParentFile(), f.getName());
        if (isFileR(f)) {
            return f;
        }
        throw new FileNotFoundException(fileName);        
    }
    
    private static boolean isFileR(File f) {
        return f.exists() && f.isFile() && f.canRead();
    }
    
    private static boolean isFileRW(File f) {
        return isFileR(f) && f.canWrite();
    }

    private static boolean isLinux() {
        String osName = System.getProperty("os.name");  // NOI18N

        return osName.endsWith("Linux"); // NOI18N
    }
}
