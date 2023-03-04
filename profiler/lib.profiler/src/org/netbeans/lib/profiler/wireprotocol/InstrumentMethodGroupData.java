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
 * Conceptually, the base class for both InstrumentMethodGroupResponse and InstrumentMethodGroupCommand. However, we have to use
 * an instance of this class in each of the above, plus some delegation, instead of normal inheritance, since the above classes
 * have to extend Response and Command, respectively.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 */
public class InstrumentMethodGroupData {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected int[] instrMethodClassLoaderIds;
    protected String[] instrMethodClasses;
    protected boolean[] instrMethodLeaf;
    protected byte[][] replacementClassFileBytes;
    protected int addInfo;
    protected int nClasses;
    protected int nMethods;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** 1.5-style RedefineClasses() instrumentation constructor */
    public InstrumentMethodGroupData(String[] instrMethodClasses, int[] instrMethodClassLoaderIds,
                                     byte[][] replacementClassFileBytes, boolean[] instrMethodLeaf, int addInfo) {
        nClasses = instrMethodClasses.length;
        nMethods = (instrMethodLeaf != null) ? instrMethodLeaf.length : 0;
        this.instrMethodClasses = instrMethodClasses;
        this.instrMethodClassLoaderIds = instrMethodClassLoaderIds;
        this.replacementClassFileBytes = replacementClassFileBytes;
        this.addInfo = addInfo;
    }

    // Custom serializaion support
    InstrumentMethodGroupData() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getAddInfo() {
        return addInfo;
    }

    public int[] getClassLoaderIds() {
        return instrMethodClassLoaderIds;
    }

    public boolean[] getInstrMethodLeaf() {
        return instrMethodLeaf;
    }

    public String[] getMethodClasses() {
        return instrMethodClasses;
    }

    public int getNClasses() {
        return nClasses;
    }

    public int getNMethods() {
        return nMethods;
    }

    public byte[][] getReplacementClassFileBytes() {
        return replacementClassFileBytes;
    }

    public void dump() {
        if (instrMethodClasses == null) {
            System.err.println("0 classes --"); // NOI18N

            return;
        } else {
            if (instrMethodClasses[0].startsWith("*FAKE")) { // NOI18N
                System.err.println("Fake InstrMethodGroupBase --"); // NOI18N

                return;
            }

            System.err.println(nClasses + " classes, " + nMethods + " methods --"); // NOI18N
        }

        int idx = 0;

        for (int i = 0; i < nClasses; i++) {
            System.err.print("--Class " + instrMethodClasses[i] + "," + instrMethodClassLoaderIds[i]); // NOI18N
            System.err.println();
        }
    }

    // ------------------------ Debugging -------------------------
    public String toString() {
        return ((instrMethodClasses != null) ? (instrMethodClasses.length) : 0) + " classes."; // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        nClasses = in.readInt();

        if (nClasses == 0) {
            return;
        }

        if ((instrMethodClasses == null) || (nClasses > instrMethodClasses.length)) {
            instrMethodClasses = new String[nClasses];
            instrMethodClassLoaderIds = new int[nClasses];
        }

        for (int i = 0; i < nClasses; i++) {
            instrMethodClasses[i] = in.readUTF();
            instrMethodClassLoaderIds[i] = in.readInt();
        }

        nMethods = in.readInt();

        int code = in.read();

        if (code != 0) {
            if ((instrMethodLeaf == null) || (nMethods > instrMethodLeaf.length)) {
                instrMethodLeaf = new boolean[nMethods];
            }

            for (int i = 0; i < nMethods; i++) {
                instrMethodLeaf[i] = in.readBoolean();
            }
        } else {
            instrMethodLeaf = null;
        }

        addInfo = in.readInt();

        if ((replacementClassFileBytes == null) || (nClasses > replacementClassFileBytes.length)) {
            replacementClassFileBytes = new byte[nClasses][];
        }

        for (int i = 0; i < nClasses; i++) {
            int len = in.readInt();

            if (len > 0) {
                replacementClassFileBytes[i] = new byte[len];
                in.readFully(replacementClassFileBytes[i]);
            }
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        if (instrMethodClasses == null) {
            out.writeInt(0);

            return;
        }

        out.writeInt(nClasses);

        for (int i = 0; i < nClasses; i++) {
            out.writeUTF(instrMethodClasses[i]);
            out.writeInt(instrMethodClassLoaderIds[i]);
        }

        out.writeInt(nMethods);

        if (instrMethodLeaf != null) {
            out.write(1);

            for (int i = 0; i < nMethods; i++) {
                out.writeBoolean(instrMethodLeaf[i]);
            }
        } else {
            out.write(0);
        }

        out.writeInt(addInfo);

        for (int i = 0; i < nClasses; i++) {
            if (replacementClassFileBytes[i] == null) {
                out.writeInt(0);
            } else {
                out.writeInt(replacementClassFileBytes[i].length);
                out.write(replacementClassFileBytes[i]);
            }
        }

        instrMethodClasses = null;
        instrMethodClassLoaderIds = null;
        instrMethodLeaf = null;
        replacementClassFileBytes = null;
    }
}
