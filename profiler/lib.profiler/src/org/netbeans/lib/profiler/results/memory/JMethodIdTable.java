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

import org.netbeans.lib.profiler.ProfilerClient;
import org.netbeans.lib.profiler.client.ClientUtils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;


/**
 * This class maps jmethodIds to (clazz, methodIdx) pairs
 *
 * @author Misha Dmitriev
 */
public class JMethodIdTable {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    public static class JMethodIdTableEntry {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        public String className;
        public String methodName;
        public String methodSig;
        public transient boolean isNative;
        int methodId;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        JMethodIdTableEntry(int methodId) {
            this.methodId = methodId;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static String NATIVE_SUFFIX = "[native]";   // NOI18N
    private static JMethodIdTable defaultTable;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private JMethodIdTableEntry[] entries;
    private boolean staticTable = false;
    private int incompleteEntries;
    private int nElements;
    private int size;
    private int threshold;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public JMethodIdTable() {
        size = 97;
        threshold = (size * 3) / 4;
        nElements = 0;
        entries = new JMethodIdTableEntry[size];
    }

    public JMethodIdTable(JMethodIdTable otherTable) {
        staticTable = true;
        threshold = otherTable.nElements + 1;
        size = (threshold * 4) / 3 ;
        nElements = 0;
        entries = new JMethodIdTableEntry[size];
        
        for (int i = 0; i < otherTable.entries.length; i++) {
            JMethodIdTableEntry entry = otherTable.entries[i];
            
            if (entry != null) {
                addEntry(entry.methodId, entry.className, entry.methodName, entry.methodSig, entry.isNative);
            }
        }
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static synchronized JMethodIdTable getDefault() {
        if (defaultTable == null) {
            defaultTable = new JMethodIdTable();
        }

        return defaultTable;
    }

    public static synchronized void reset() {
        defaultTable = null;
    }

    public synchronized String debug() {
        if (entries == null) {
            return "Entries = null, size = " + size + ", nElements = " + nElements + ", threshold = " // NOI18N
                   + threshold + ", incompleteEntries = " + incompleteEntries; // NOI18N
        } else {
            return "Entries.length = " + entries.length + ", size = " + size + ", nElements = " + nElements // NOI18N
                   + ", threshold = " + threshold + ", incompleteEntries = " + incompleteEntries; // NOI18N
        }
    }

    public synchronized void readFromStream(DataInputStream in) throws IOException {
        size = in.readInt();
        nElements = in.readInt();
        threshold = in.readInt();

        entries = new JMethodIdTableEntry[size];

        int count = in.readInt();

        for (int i = 0; i < count; i++) {
            int methodId = in.readInt();
            String className = in.readUTF();
            String methodName = in.readUTF();
            String methodSig = in.readUTF();
            boolean isNative = false;
            
            if (methodName.endsWith(NATIVE_SUFFIX)) {
                methodName = methodName.substring(0, methodName.length() - NATIVE_SUFFIX.length());
                isNative = true;
            }
            addEntry(methodId, className, methodName, methodSig, isNative);
        }
    }

    public synchronized void writeToStream(DataOutputStream out) throws IOException {
        out.writeInt(size);
        out.writeInt(nElements);
        out.writeInt(threshold);

        int count = 0;

        for (int i = 0; i < entries.length; i++) {
            if (entries[i] != null) {
                count++;
            }
        }

        out.writeInt(count);

        for (int i = 0; i < entries.length; i++) {
            JMethodIdTableEntry entry = entries[i];
            
            if (entry != null) {
                out.writeInt(entry.methodId);
                out.writeUTF(entry.className);
                out.writeUTF(entry.isNative ? entry.methodName.concat(NATIVE_SUFFIX) : entry.methodName);
                out.writeUTF(entry.methodSig);
            }
        }
    }

    public synchronized JMethodIdTableEntry getEntry(int methodId) {
        int pos = hash(methodId) % size;

        while ((entries[pos] != null) && (entries[pos].methodId != methodId)) {
            pos = (pos + 1) % size;
        }

        return entries[pos];
    }

    public synchronized void getNamesForMethodIds(ProfilerClient profilerClient)
                                    throws ClientUtils.TargetAppOrVMTerminated {
        if (staticTable) {
            throw new IllegalStateException("Attempt to update snapshot JMethodIdTable"); // NOI18N
        }

        if (incompleteEntries == 0) {
            return;
        }

        int[] missingNameMethodIds = new int[incompleteEntries];
        int idx = 0;

        for (int i = 0; i < entries.length; i++) {
            if (entries[i] == null) {
                continue;
            }

            if (entries[i].className == null) {
                missingNameMethodIds[idx++] = entries[i].methodId;
            }
        }

        String[][] methodClassNameAndSig = profilerClient.getMethodNamesForJMethodIds(missingNameMethodIds);

        for (int i = 0; i < missingNameMethodIds.length; i++) {
            completeEntry(missingNameMethodIds[i], methodClassNameAndSig[0][i], methodClassNameAndSig[1][i],
                          methodClassNameAndSig[2][i], getBoolean(methodClassNameAndSig[3][i]));
        }

        incompleteEntries = 0;
    }

    void addEntry(int methodId, String className, String methodName, String methodSig, boolean isNative) {
        checkMethodId(methodId);
        completeEntry(methodId, className, methodName, methodSig, isNative);
    }

    public synchronized void checkMethodId(int methodId) {
        int pos = hash(methodId) % size;

        while (entries[pos] != null) {
            if (entries[pos].methodId == methodId) {
                return;
            }

            pos = (pos + 1) % size;
        }

        if (nElements < threshold) {
            entries[pos] = new JMethodIdTableEntry(methodId);
            nElements++;
            incompleteEntries++;

            return;
        } else {
            growTable();
            checkMethodId(methodId);
        }
    }

    private synchronized void completeEntry(int methodId, String className, String methodName, String methodSig, boolean isNative) {
        int pos = hash(methodId) % size;

        while (entries[pos].methodId != methodId) {
            pos = (pos + 1) % size;
        }

        entries[pos].className = className;
        entries[pos].methodName = methodName;
        entries[pos].methodSig = methodSig;
        entries[pos].isNative = isNative;
    }

    private void growTable() {
        JMethodIdTableEntry[] oldEntries = entries;
        size = (size * 2) + 1;
        threshold = (size * 3) / 4;
        entries = new JMethodIdTableEntry[size];

        for (int i = 0; i < oldEntries.length; i++) {
            if (oldEntries[i] != null) {
                int pos = hash(oldEntries[i].methodId) % size;

                while (entries[pos] != null) {
                    pos = (pos + 1) % size;
                }

                entries[pos] = oldEntries[i];
            }
        }
    }

    private int hash(int x) {
        return ((x >> 2) * 123457) & 0xFFFFFFF;
    }
    
    private boolean getBoolean(String boolStr) {
        return "1".equals(boolStr);       // NOI18N
    }
}
