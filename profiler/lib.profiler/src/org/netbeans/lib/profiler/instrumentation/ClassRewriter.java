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

package org.netbeans.lib.profiler.instrumentation;

import org.netbeans.lib.profiler.global.Platform;
import org.netbeans.lib.profiler.classfile.DynamicClassInfo;
import org.netbeans.lib.profiler.utils.MiscUtils;
import java.io.IOException;


/**
 * This class contains functionality to rewrite a given complete class file, replacing given
 * methodinfos and appending the constant pool.
 *
 * @author  Misha Dmitriev
 */
public class ClassRewriter {
    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static byte[] rewriteClassFile(DynamicClassInfo clazz, byte[][] replacementMethodInfos, int nAddedCPEntries,
                                          byte[] addedCPContents) {
        // Now assemble a new class file. First get original class file bytes.
        byte[] origBytes = null;

        try {
            origBytes = clazz.getClassFileBytes();
        } catch (IOException ex) {
            // Shouldn't happen, so a message just in case
            MiscUtils.internalError("ClassRewriter: can't get original class file bytes for class " + clazz.getName()
                                    + "\nIOException message = " + ex.getMessage()); // NOI18N
        }

        // Compute new class file length.
        int newLen = origBytes.length;

        // First add new constant pool size
        newLen += addedCPContents.length;

        // Now add differences between new and old method sizes
        int nMethods = clazz.getMethodNames().length;

        for (int i = 0; i < nMethods; i++) {
            if (replacementMethodInfos[i] != null) {
                newLen += (replacementMethodInfos[i].length - clazz.getOrigMethodInfoLength(i));
            }
        }

        byte[] res = new byte[newLen];

        // Copy over unchanged contents from old class file, copy/add changed contents, and adjust some counts
        int destPos = 0;
        // Copy preamble and original constant pool unchanged
        System.arraycopy(origBytes, 0, res, destPos, clazz.getOrigIntermediateDataStartOfs());
        destPos += clazz.getOrigIntermediateDataStartOfs();
        // Copy our new constant pool extension
        System.arraycopy(addedCPContents, 0, res, destPos, addedCPContents.length);
        destPos += addedCPContents.length;

        // Adjust the cpool count
        int newCPCount = clazz.getOrigCPoolCount() + nAddedCPEntries;
        int pos = clazz.getOrigCPoolStartOfs();
        res[pos] = (byte) ((newCPCount >> 8) & 255);
        res[pos + 1] = (byte) (newCPCount & 255);

        // Copy intermediate data and fields unchanged
        int count = clazz.getOrigMethodsStartOfs() - clazz.getOrigIntermediateDataStartOfs();
        System.arraycopy(origBytes, clazz.getOrigIntermediateDataStartOfs(), res, destPos, count);
        destPos += count;

        // Now copy all new methodInfos. First write the method count
        res[destPos] = (byte) ((nMethods >> 8) & 255);
        res[destPos + 1] = (byte) (nMethods & 255);
        destPos += 2;

        // Write methodInfos
        for (int i = 0; i < nMethods; i++) {
            if (replacementMethodInfos[i] != null) {
                System.arraycopy(replacementMethodInfos[i], 0, res, destPos, replacementMethodInfos[i].length);
                destPos += replacementMethodInfos[i].length;
            } else {
                byte[] origMethodInfo = clazz.getOrigMethodInfo(i);
                System.arraycopy(origMethodInfo, 0, res, destPos, origMethodInfo.length);
                destPos += origMethodInfo.length;
            }
        }

        // Copy what remains - class attributes
        count = origBytes.length - clazz.getOrigAttrsStartOfs();
        System.arraycopy(origBytes, clazz.getOrigAttrsStartOfs(), res, destPos, count);
        clazz.resetTables();

        // For debugging
        //if (clazz.getName().equals("profilertestapp/Main")) {
        //  saveClassFileToDisk(clazz, res);
        //}
        return res;
    }

    public static void saveToDisk(String name, byte[] classBytes) {
        if (Platform.getJDKVersionNumber() == Platform.JDK_CVM) {
            // No room on device to do this
            return;
        }
        name = name.replace('/', '_'); // NOI18N

        try {
            System.err.print("*** Gonna save bytecode " + name + " to disk... "); // NOI18N

            java.io.OutputStream out = new java.io.FileOutputStream(new java.io.File(name + ".class")); // NOI18N
            try {
                out.write(classBytes);
            } finally {
                out.close();
            }
            System.err.println("done"); // NOI18N
        } catch (Exception ex) {
            System.err.println("*** In RecursiveMethodInstrumentor.saveClassFileToDisk caught ex = " + ex); // NOI18N
        }
    }

    private static void saveClassFileToDisk(DynamicClassInfo clazz, byte[] replacementClassFile) {
        saveToDisk(clazz.getName(), replacementClassFile);
    }
}
