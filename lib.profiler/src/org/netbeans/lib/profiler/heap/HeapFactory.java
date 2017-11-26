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
    public static Heap createHeap(File heapDump) throws FileNotFoundException, IOException {
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
    public static Heap createHeap(File heapDump, int segment)
                           throws FileNotFoundException, IOException {
        return new HprofHeap(heapDump, segment);
    }
}
