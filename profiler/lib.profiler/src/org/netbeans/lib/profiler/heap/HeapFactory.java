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

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * This is factory class for creating {@link Heap} from the file in Hprof dump format.
 * @author Tomas Hurka
 */
public class HeapFactory {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * this factory method creates {@link Heap} from a memory dump file in Hprof format.
     * <br>
     * Speed: slow
     * @param heapDump file which contains memory dump
     * @return implementation of {@link Heap} corresponding to the memory dump
     * passed in heapDump parameter
     * @throws java.io.FileNotFoundException if heapDump file does not exist
     * @throws java.io.IOException if I/O error occurred while accessing heapDump file
     */
    public static Heap createHeap(java.io.File heapDump) throws FileNotFoundException, IOException {
        return createHeap(heapDump, 0);
    }

    /**
     * this factory method creates {@link Heap} from a memory dump file in Hprof format.
     * If the memory dump file contains more than one dump, parameter segment is used to
     * select particular dump.
     * <br>
     * Speed: slow
     * @return implementation of {@link Heap} corresponding to the memory dump
     * passed in heapDump parameter
     * @param segment select corresponding dump from multi-dump file
     * @param heapDump file which contains memory dump
     * @throws java.io.FileNotFoundException if heapDump file does not exist
     * @throws java.io.IOException if I/O error occurred while accessing heapDump file
     */
    public static Heap createHeap(java.io.File heapDump, int segment)
                           throws FileNotFoundException, IOException {
        File hd = JavaIoFile.IO.newFile(heapDump);
        CacheDirectory cacheDir = CacheDirectory.getHeapDumpCacheDirectory(JavaIoFile.IO, hd, segment);
        if (!cacheDir.isTemporary()) {
            File savedDump = cacheDir.getHeapDumpAuxFile();

            if (savedDump.exists() && savedDump.isFile() && savedDump.canRead()) {
                try {
                    return loadHeap(cacheDir);
                } catch (IOException ex) {
                    Systems.printStackTrace("Loading heap dump "+heapDump+" from cache failed.", ex);
                }
            }
        }
        return new HprofHeap(hd, segment, cacheDir);

    }

    /** Factory method for processing heap dumps in memory. When heap data
     * aren't available on disk, but only in memory, create a {@link ByteBuffer}
     * from them and use this factory method to create their {@link Heap}
     * representation.
     * 
     * @param buffer data representing the heap dump
     * @param segment select corresponding dump from multi-dump file
     * @return implementation of {@link Heap} corresponding to the memory dump
     * passed in the {@code buffer} parameter
     * @throws IOException if the access to the buffer fails or data are corrupted
     * @since 1.122
     */
    public static Heap createHeap(ByteBuffer buffer, int segment) throws IOException {
        return new HprofHeap(buffer, segment, new CacheDirectory(JavaIoFile.IO, null));
    }
    
    static Heap loadHeap(CacheDirectory cacheDir)
                           throws FileNotFoundException, IOException {
        File savedDump = cacheDir.getHeapDumpAuxFile();
        DataInputStream dis = savedDump.newDataInputStream(64*1024);
        Heap heap = new HprofHeap(dis, cacheDir);
        dis.close();
        return heap;
    }
    
}
