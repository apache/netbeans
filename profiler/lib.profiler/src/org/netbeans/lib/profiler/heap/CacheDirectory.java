/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.heap;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Tomas Hurka
 */
class CacheDirectory {
    
    private static final String DIR_EXT = ".nbcache";   // NOI18N
    private static final String DUMP_AUX_FILE = "NBProfiler.nphd";   // NOI18N
    
    private final File.Factory io;
    private File cacheDirectory;
    
    static CacheDirectory getHeapDumpCacheDirectory(File.Factory io, File heapDump, int segment) {
        String dumpName = heapDump.getName();
        if (segment != 0) {
            dumpName += "_" + segment;
        }
        File parent = heapDump.getParentFile();
        File dir = io.newFile(parent, dumpName + DIR_EXT);
        return new CacheDirectory(io, dir);
    }
    
    CacheDirectory(File.Factory io, File cacheDir) {
        io.getClass();
        this.io = io;
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
            newFile = io.createTempFile(prefix, suffix, null);
            newFile.deleteOnExit();
        } else {
            newFile = io.createTempFile(prefix, suffix, cacheDirectory);
        }
        return newFile;
    }
    
    File getHeapDumpAuxFile() {
        assert !isTemporary();
        return io.newFile(cacheDirectory, DUMP_AUX_FILE);
    }
    
    boolean isTemporary() {
        return cacheDirectory == null;
    }

    File getCacheFile(String fileName) throws FileNotFoundException {
        File f = io.newFile(fileName);
        if (isFileRW(f)) {
            return f;
        }
        // try to find file in cache directory
        f = io.newFile(cacheDirectory, f.getName());
        if (isFileRW(f)) {
            return f;
        }
        throw new FileNotFoundException(fileName);
    }

    File getHeapFile(String fileName) throws FileNotFoundException {
        File f = io.newFile(fileName);
        if (isFileR(f)) {
            return f;
        }
        // try to find heap dump file next to cache directory
        f = io.newFile(cacheDirectory.getParentFile(), f.getName());
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
}
