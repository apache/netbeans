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

import org.netbeans.lib.profiler.classfile.ClassInfo;


/**
 * Base class, containing functionality to scan bytecodes in a single method.
 *
 * @author Misha Dmitriev
 */
public class SingleMethodScaner implements JavaClassConstants {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // The following array is re-used,to avoid excessive object allocations - which means that THIS CLASS IS NOT MULTITHREAD-SAFE!
    private static byte[] reusableBytecodes = new byte[100];

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected ClassInfo clazz;
    protected byte[] bytecodes; // Current updateable copy of bytecodes (what is in MethodInfo Code attribute between the code_length and the exception_table_length fields)
    protected byte[] origMethodInfo;
    protected int bytecodesLength;
    protected int bytecodesStartIdx;
    protected int methodIdx;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SingleMethodScaner() {
    }

    public SingleMethodScaner(ClassInfo clazz, int methodIdx) {
        setClassAndMethod(clazz, methodIdx);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public void setClassAndMethod(ClassInfo clazz, int methodIdx) {
        this.clazz = clazz;
        this.methodIdx = methodIdx;
        origMethodInfo = clazz.getMethodInfo(methodIdx);
        bytecodesStartIdx = clazz.getMethodBytecodeOffsetInMethodInfo(methodIdx);
        bytecodesLength = clazz.getMethodBytecodesLength(methodIdx);
        initBytecodesArray();
    }

    protected static int getU2(byte[] buf, int pos) {
        return ((buf[pos] & 0xFF) << 8) + (buf[pos + 1] & 0xFF);
    }

    protected static int getU4(byte[] buf, int pos) {
        return ((buf[pos] & 0xFF) << 24) + ((buf[pos + 1] & 0xFF) << 16) + ((buf[pos + 2] & 0xFF) << 8) + (buf[pos + 3] & 0xFF);
    }

    protected static int align(int n) {
        return (n + 3) & ~3;
    }

    protected int opcodeLength(int bci) {
        int ret;
        int opcode = bytecodes[bci] & 0xFF;

        try {
            ret = opc_length[opcode];
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("*** JFluid warning: unknown opcode: " + opcode + " detected at offset " + bci // NOI18N
                               + "in class/method " + clazz.getName() + "." + clazz.getMethodName(methodIdx) // NOI18N
                               + clazz.getMethodSignature(methodIdx));

            ClassRewriter.saveToDisk(clazz.getName(), bytecodes);

            return 1;
        }

        if (ret != 0) {
            return ret;
        }

        if (opcode == opc_wide) {
            opcode = bytecodes[bci + 1] & 0xFF;

            if (((opcode >= opc_iload) && (opcode <= opc_aload)) || ((opcode >= opc_istore) && (opcode <= opc_astore))
                    || (opcode == opc_ret)) {
                return 4;
            } else if (opcode == opc_iinc) {
                return 6;
            } else {
                return 2;
            }
        } else {
            int pad = align(bci + 1) - (bci + 1);

            switch (opcode) {
                case opc_tableswitch: {
                    int lo = getInt(bci + 1 + pad + (4 * 1));
                    int hi = getInt(bci + 1 + pad + (4 * 2));
                    int n = hi - lo + 1;

                    return 1 + pad + (4 * (3 + n));
                }
                case opc_lookupswitch: {
                    int npairs = getInt(bci + 1 + pad + (4 * 1));

                    return 1 + pad + (4 * (2 + (2 * npairs)));
                }
            }
        }

        System.err.println("*** Profiler Engine: error - should not reach here in opcodeLength()"); // NOI18N

        return 0;
    }

    protected static void putByte(byte[] buf, int pos, int value) {
        buf[pos] = (byte) (value & 0xFF);
    }

    protected static void putU2(byte[] buf, int pos, int value) {
        buf[pos] = (byte) ((value >> 8) & 0xFF);
        buf[pos + 1] = (byte) (value & 0xFF);
    }

    protected static void putU4(byte[] buf, int pos, int value) {
        buf[pos] = (byte) ((value >> 24) & 0xFF);
        buf[pos + 1] = (byte) ((value >> 16) & 0xFF);
        buf[pos + 2] = (byte) ((value >> 8) & 0xFF);
        buf[pos + 3] = (byte) (value & 0xFF);
    }

    protected int getByte(int pos) {
        return (bytecodes[pos] & 0xFF);
    }

    protected int getInt(int pos) {
        return getU4(pos);
    }

    protected short getShort(int pos) {
        return (short) (((bytecodes[pos] & 0xFF) << 8) + (bytecodes[pos + 1] & 0xFF));
    }

    protected int getU2(int pos) {
        return ((bytecodes[pos] & 0xFF) << 8) + (bytecodes[pos + 1] & 0xFF);
    }

    protected int getU4(int pos) {
        return ((bytecodes[pos] & 0xFF) << 24) + ((bytecodes[pos + 1] & 0xFF) << 16) + ((bytecodes[pos + 2] & 0xFF) << 8)
               + (bytecodes[pos + 3] & 0xFF);
    }

    protected void initBytecodesArray() {
        if (reusableBytecodes.length < (bytecodesLength * 8)) {
            reusableBytecodes = new byte[bytecodesLength * 8];
        }

        System.arraycopy(origMethodInfo, bytecodesStartIdx, reusableBytecodes, 0, bytecodesLength);
        bytecodes = reusableBytecodes;
    }

    protected void putInt(int pos, int value) {
        putU4(pos, value);
    }

    protected void putShort(int pos, short value) {
        bytecodes[pos] = (byte) ((value >> 8) & 0xFF);
        bytecodes[pos + 1] = (byte) (value & 0xFF);
    }

    protected void putU4(int pos, int value) {
        bytecodes[pos] = (byte) ((value >> 24) & 0xFF);
        bytecodes[pos + 1] = (byte) ((value >> 16) & 0xFF);
        bytecodes[pos + 2] = (byte) ((value >> 8) & 0xFF);
        bytecodes[pos + 3] = (byte) (value & 0xFF);
    }
}
