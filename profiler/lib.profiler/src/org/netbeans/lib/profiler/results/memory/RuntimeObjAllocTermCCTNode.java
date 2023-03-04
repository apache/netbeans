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

package org.netbeans.lib.profiler.results.memory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * A terminal node used in Object Allocation Profiling Calling Context Tree (CCT).
 * Contains the information gathered during object allocation profiling, which can be calculated  for intermediate nodes
 * if known for terminal nodes.
 * <p/>
 * Normally used as a leaf, except in case there are multiple same paths in the tree with different length
 * <p/>
 * The information in TermCCTNode represents all objects of the same type allocated using same call path.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class RuntimeObjAllocTermCCTNode extends RuntimeMemoryCCTNode {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    public long nCalls; // # of invocations
    public long totalObjSize; // object size in Bytes

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public RuntimeObjAllocTermCCTNode(int methodId) {
        super(methodId);
    }

    protected RuntimeObjAllocTermCCTNode() {
    } // only for I/O

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getType() {
        return TYPE_RuntimeObjAllocTermCCTNode;
    }

    public void readFromStream(DataInputStream in) throws IOException {
        super.readFromStream(in);
        nCalls = in.readLong();
        totalObjSize = in.readLong();
    }

    public void updateForNewObject(long objSize) {
        nCalls++;
        totalObjSize += objSize;
    }

    public void updateForRemovedObject(long objSize) {
        totalObjSize -= objSize;
    }

    public void writeToStream(DataOutputStream out) throws IOException {
        super.writeToStream(out);
        out.writeLong(nCalls);
        out.writeLong(totalObjSize);
    }
}
