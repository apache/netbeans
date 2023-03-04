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

package org.netbeans.lib.profiler.wireprotocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * Response from client to back end, that contains a group of TA methods to instrument.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class InstrumentMethodGroupResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private InstrumentMethodGroupData b;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** 1.5-style RedefineClasses() instrumentation constructor */
    public InstrumentMethodGroupResponse(String[] instrMethodClasses, int[] instrMethodClassLoaderIds,
                                         byte[][] replacementClassFileBytes, boolean[] instrMethodLeaf, int addInfo) {
        super(true, INSTRUMENT_METHOD_GROUP);
        b = new InstrumentMethodGroupData(instrMethodClasses, instrMethodClassLoaderIds, replacementClassFileBytes,
                                          instrMethodLeaf, addInfo);
    }

    /** This is used just to send "empty" responses, meaning no methods are instrumented */
    public InstrumentMethodGroupResponse(Object dummy) {
        super(false, INSTRUMENT_METHOD_GROUP);
    }

    // Custom serializaion support
    InstrumentMethodGroupResponse() {
        super(INSTRUMENT_METHOD_GROUP);
        b = new InstrumentMethodGroupData();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public InstrumentMethodGroupData getBase() {
        return b;
    }

    public int[] getClassLoaderIds() {
        return b.instrMethodClassLoaderIds;
    }

    public boolean isEmpty() {
        return !yes;
    }

    public boolean[] getInstrMethodLeaf() {
        return b.instrMethodLeaf;
    }

    public String[] getMethodClasses() {
        return b.instrMethodClasses;
    }

    public byte[][] getReplacementClassFileBytes() {
        return b.replacementClassFileBytes;
    }

    public void dump() {
        System.err.print("-- InstrumentMethodGroupResponse: "); // NOI18N
        b.dump();
    }

    // ------------------------ Debugging -------------------------
    public String toString() {
        return "InstrumentMethodGroupResponse " + ((b != null) ? b.toString() : "empty"); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        if (!isEmpty()) {
            b.readObject(in);
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        if (!isEmpty()) {
            b.writeObject(out);
        }
    }
}
