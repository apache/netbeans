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
import java.io.DataOutputStream;
import java.io.IOException;
import static org.netbeans.lib.profiler.heap.HprofHeap.*;

/**
 *
 * @author Tomas Hurka
 */
class TagBounds {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    final int tag;
    final long startOffset;
    long endOffset;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    TagBounds(int t, long start, long end) {
        tag = t;
        startOffset = start;
        endOffset = end;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    TagBounds union(TagBounds otherTagBounds) {
        if (otherTagBounds == null) {
            return this;
        }

        long start = Math.min(startOffset, otherTagBounds.startOffset);
        long end = Math.max(endOffset, otherTagBounds.endOffset);

        return new TagBounds(-1, start, end);
    }

    //---- Serialization support
    void writeToStream(DataOutputStream out) throws IOException {
        out.writeInt(tag);
        out.writeLong(startOffset);
        out.writeLong(endOffset);
    }

    TagBounds(DataInputStream dis) throws IOException {
        tag = dis.readInt();
        startOffset = dis.readLong();
        endOffset = dis.readLong();
    }
    
    static void writeToStream(TagBounds[] bounds, DataOutputStream out) throws IOException {
        int tags = 0;
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i] != null) {
                tags++;
            }
        }
        out.writeInt(tags);
        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i] != null) {
                bounds[i].writeToStream(out);
            }
        }
    }

    static void readFromStream(DataInputStream dis, HprofHeap heap, TagBounds[] heapTagBounds) throws IOException {
        int tags = dis.readInt();
        for (int i = 0; i<tags; i++) {
            int tag = dis.readInt();
            long startOffset = dis.readLong();
            long endOffset = dis.readLong();
            TagBounds newBounds;
                        
            if (tag == LOAD_CLASS) {
                newBounds = new LoadClassSegment(heap, startOffset, endOffset);
            } else if (tag == STRING) {
                newBounds = new StringSegment(heap, startOffset, endOffset);
            } else if (tag == STACK_TRACE) {
                newBounds = new StackTraceSegment(heap, startOffset, endOffset);
            } else if (tag == STACK_FRAME) {
                newBounds = new StackFrameSegment(heap, startOffset, endOffset);
            } else if (tag == CLASS_DUMP) {
                newBounds = new ClassDumpSegment(heap, startOffset, endOffset, dis);
            } else {
                newBounds = new TagBounds(tag, startOffset, endOffset);
            }
            heapTagBounds[newBounds.tag] = newBounds;
        }
    }
}
