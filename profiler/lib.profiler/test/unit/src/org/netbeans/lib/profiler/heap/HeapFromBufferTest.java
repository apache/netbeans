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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.junit.Before;

public class HeapFromBufferTest extends HeapTest {
    public HeapFromBufferTest() {
    }

    @Before
    public void setUp() throws IOException, URISyntaxException {
        URL url = getClass().getResource("heap_dump.bin");
        File heapFile = new File(url.toURI());
        ByteBuffer buffer = ByteBuffer.allocate((int) heapFile.length());
        try (FileChannel ch = new FileInputStream(heapFile).getChannel()) {
            while (buffer.remaining() > 0) {
                int len = ch.read(buffer);
                if (len == -1) {
                    break;
                }
            }
        }
        heap = HeapFactory.createHeap(buffer, 0);
    }

}
