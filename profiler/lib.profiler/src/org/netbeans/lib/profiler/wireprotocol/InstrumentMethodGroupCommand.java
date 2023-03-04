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
 * Request from client to back end to instrument a group of TA methods.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class InstrumentMethodGroupCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private InstrumentMethodGroupData b;
    private int instrType;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** 1.5-style RedefineClasses() instrumentation constructor */
    public InstrumentMethodGroupCommand(int instrType, String[] instrMethodClasses, int[] instrMethodClassLoaderIds,
                                        byte[][] replacementClassFileBytes, boolean[] instrMethodLeaf, int addInfo) {
        super(INSTRUMENT_METHOD_GROUP);
        this.instrType = instrType;
        b = new InstrumentMethodGroupData(instrMethodClasses, instrMethodClassLoaderIds, replacementClassFileBytes,
                                          instrMethodLeaf, addInfo);
    }

    /** This is used just to send "empty" commands, meaning no methods are instrumented */
    public InstrumentMethodGroupCommand(Object dummy) {
        super(INSTRUMENT_METHOD_GROUP);
        instrType = -1;
    }

    // Custom serializaion support
    InstrumentMethodGroupCommand() {
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
        return instrType == -1;
    }

    public boolean[] getInstrMethodLeaf() {
        return b.instrMethodLeaf;
    }

    public int getInstrType() {
        return instrType;
    }

    public String[] getMethodClasses() {
        return b.instrMethodClasses;
    }

    public byte[][] getReplacementClassFileBytes() {
        return b.replacementClassFileBytes;
    }

    public void dump() {
        System.err.print("-- InstrumentMethodGroupCommand: "); // NOI18N

        if (b != null) {
            b.dump();
        }
    }

    // ------------------------ Debugging -------------------------
    public String toString() {
        return "InstrumentMethodGroupCommand " + ((b != null) ? b.toString() : "null"); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        instrType = in.readInt();

        if (!isEmpty()) {
            b.readObject(in);
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(instrType);

        if (!isEmpty()) {
            b.writeObject(out);
        }
    }
}
