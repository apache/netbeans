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


/**
 *
 * @author Tomas Hurka
 */
class LoadClass extends HprofObject {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final LoadClassSegment loadClassSegment;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    LoadClass(LoadClassSegment segment, long offset) {
        super(offset);
        loadClassSegment = segment;
        assert getHprofBuffer().get(offset) == HprofHeap.LOAD_CLASS;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    long getClassObjectID() {
        return getHprofBuffer().getID(fileOffset + loadClassSegment.classIDOffset);
    }

    String getName() {
        return convertToName(getVMName());
    }

    long getNameID() {
        return getHprofBuffer().getID(fileOffset + loadClassSegment.nameStringIDOffset);
    }

    String getVMName() {
        StringSegment stringSegment = loadClassSegment.hprofHeap.getStringSegment();

        return stringSegment.getStringByID(getNameID());
    }

    private HprofByteBuffer getHprofBuffer() {
        return loadClassSegment.hprofHeap.dumpBuffer;
    }

    private static String convertToName(String vmName) {
        String name = vmName.replace('/', '.'); // NOI18N
        int i;

        for (i = 0; i < name.length(); i++) {
            if (name.charAt(i) != '[') { // NOI18N    // arrays
                break;
            }
        }

        if (i != 0) {
            name = name.substring(i);

            char firstChar = name.charAt(0);

            if (firstChar == 'L') { // NOI18N      // object array
                name = name.substring(1, name.length() - 1);
            } else {
                switch (firstChar) {
                    case 'C':
                        name = "char"; // NOI18N
                        break;
                    case 'B':
                        name = "byte"; // NOI18N
                        break;
                    case 'I':
                        name = "int"; // NOI18N
                        break;
                    case 'Z':
                        name = "boolean"; // NOI18N
                        break;
                    case 'F':
                        name = "float"; // NOI18N
                        break;
                    case 'D':
                        name = "double"; // NOI18N
                        break;
                    case 'S':
                        name = "short"; // NOI18N
                        break;
                    case 'J':
                        name = "long"; // NOI18N
                        break;
                }
            }

            for (; i > 0; i--) {
                name = name.concat("[]"); // NOI18N
            }
        }

        return name;
    }
}
