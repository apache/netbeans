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


/**
 * Basic support for adding entries to a class's constant pool.
 * This class allows one to create a semi-prepared chunk of bytes (class PackedCPoolFragment) representing entries that
 * should be added to the constant pool of an arbitrary class. Then, by passing a PackedCPoolFragment to the constructor
 * of ConstantPoolExtension, one obtains a relocated (i.e. with indices adjusted for a concrete class' existing constant pool)
 * copy of added constant pool.
 *
 * @author Misha Dmitriev
 */
public class ConstantPoolExtension implements JavaClassConstants {
    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    //------------------------------------------ Helper classes -------------------------------------------------
    public static class CPEntry {
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        String utf8;
        byte tag;
        int index1;
        int index2;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public CPEntry(int tag) {
            this.tag = (byte) tag;
        }

        public CPEntry(String utf8) {
            this.tag = CONSTANT_Utf8;
            this.utf8 = utf8;
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public void setIndex1(int idx) {
            index1 = idx;
        }

        public void setIndex2(int idx) {
            index2 = idx;
        }
    }

    /**
     * Represents an array of bytes containing constant pool contents to append to the real constant pool, in the unrelocated
     * form. Entries in this cpool fragment may point either to other entries within the same cpool fragment (internalIndicies
     * array), or to entries in one other cpool fragment (externalIndices array). In either case indices are relative to the
     * origin of the corresponding cpool fragment. By calling getRelocatedCPoolBytes(intBaseIndex, extBaseIndex), one gets the real
     * constant pool added contents, with all indices relocated, i.e. pointing at correct absolute locations in the concrete class'
     * constant pool.
     */
    public static class PackedCPFragment {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static byte[] tmpBytes = new byte[64];

        //~ Instance fields ------------------------------------------------------------------------------------------------------

        int nEntries;
        private byte[] cpoolBytes; // Non-relocated cpool bytes
        private char[] externalIndices; // Positions of all u2 indices in the above array, that need to be adjusted
        private char[] internalIndices; // Positions of all u2 indices in the above array, that need to be adjusted

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        public PackedCPFragment(CPEntry[] entries) {
            nEntries = entries.length;

            // First compute the packed size and the number of indices that would need adjustment
            int packedSize = 0;
            int nIntIdx = 0;
            int nExtIdx = 0;

            for (int i = 0; i < nEntries; i++) {
                switch (entries[i].tag) {
                    case CONSTANT_Class:
                        packedSize += 3;

                        if (entries[i].index1 <= 0xFFFF) {
                            nIntIdx++;
                        } else {
                            nExtIdx++;
                        }

                        break;
                    case CONSTANT_Fieldref:
                    case CONSTANT_Methodref:
                    case CONSTANT_InterfaceMethodref:
                    case CONSTANT_NameAndType:
                        packedSize += 5;

                        if (entries[i].index1 <= 0xFFFF) {
                            nIntIdx++;
                        } else {
                            nExtIdx++;
                        }

                        if (entries[i].index2 <= 0xFFFF) {
                            nIntIdx++;
                        } else {
                            nExtIdx++;
                        }

                        break;
                    case CONSTANT_Utf8:
                        packedSize += (3 + entries[i].utf8.length());

                        break;
                    default:
                        System.err.println("*** PackedAddedCPool : unsupported constant!"); // NOI18N
                }
            }

            cpoolBytes = new byte[packedSize];
            internalIndices = new char[nIntIdx];
            externalIndices = new char[nExtIdx];

            int curPos = 0;
            nIntIdx = 0;
            nExtIdx = 0;

            for (int i = 0; i < nEntries; i++) {
                switch (entries[i].tag) {
                    case CONSTANT_Class:
                        cpoolBytes[curPos++] = entries[i].tag;

                        if (entries[i].index1 <= 0xFFFF) {
                            internalIndices[nIntIdx++] = (char) curPos;
                        } else {
                            externalIndices[nExtIdx++] = (char) curPos;
                        }

                        cpoolBytes[curPos++] = (byte) ((entries[i].index1 >> 8) & 0xFF);
                        cpoolBytes[curPos++] = (byte) ((entries[i].index1) & 0xFF);

                        break;
                    case CONSTANT_Fieldref:
                    case CONSTANT_Methodref:
                    case CONSTANT_InterfaceMethodref:
                    case CONSTANT_NameAndType:
                        cpoolBytes[curPos++] = entries[i].tag;

                        if (entries[i].index1 <= 0xFFFF) {
                            internalIndices[nIntIdx++] = (char) curPos;
                        } else {
                            externalIndices[nExtIdx++] = (char) curPos;
                        }

                        cpoolBytes[curPos++] = (byte) ((entries[i].index1 >> 8) & 0xFF);
                        cpoolBytes[curPos++] = (byte) ((entries[i].index1) & 0xFF);

                        if (entries[i].index2 <= 0xFFFF) {
                            internalIndices[nIntIdx++] = (char) curPos;
                        } else {
                            externalIndices[nExtIdx++] = (char) curPos;
                        }

                        cpoolBytes[curPos++] = (byte) ((entries[i].index2 >> 8) & 0xFF);
                        cpoolBytes[curPos++] = (byte) ((entries[i].index2) & 0xFF);

                        break;
                    case CONSTANT_Utf8:
                        cpoolBytes[curPos++] = entries[i].tag;

                        String utf8 = entries[i].utf8;
                        int len = utf8.length();
                        cpoolBytes[curPos++] = (byte) ((len >> 8) & 0xFF);
                        cpoolBytes[curPos++] = (byte) ((len) & 0xFF);

                        for (int j = 0; j < len; j++) {
                            tmpBytes[j] = (byte) utf8.charAt(j);
                        }

                        System.arraycopy(tmpBytes, 0, cpoolBytes, curPos, len);
                        curPos += len;

                        break;
                    default:
                        System.err.println("*** PackedAddedCPool : unsupported constant!"); // NOI18N
                }
            }
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        public byte[] getRelocatedCPBytes(int intBaseIndex, int extBaseIndex) {
            byte[] ret = new byte[cpoolBytes.length];
            System.arraycopy(cpoolBytes, 0, ret, 0, cpoolBytes.length);

            for (int i = 0; i < internalIndices.length; i++) {
                int pos = internalIndices[i];
                int value = getU2(ret, pos);
                value += intBaseIndex;
                putU2(ret, pos, value);
            }

            for (int i = 0; i < externalIndices.length; i++) {
                int pos = externalIndices[i];
                int value = getU2(ret, pos);
                value += extBaseIndex;
                putU2(ret, pos, value);
            }

            return ret;
        }
    }

    private static int getU2(byte[] buf, int pos) {
        return ((buf[pos] & 0xFF) << 8) + (buf[pos + 1] & 0xFF);
    }
    
    private static void putU2(byte[] buf, int pos, int value) {
        buf[pos] = (byte) ((value >> 8) & 0xFF);
        buf[pos + 1] = (byte) (value & 0xFF);
    }
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    protected byte[] addedCPContents;
    protected int nAddedEntries;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    //----------------------------------------- Protected methods ---------------------------------------------------

    /** Creates a ConstantPoolExtension containing properly relocated contents from pcp. */
    protected ConstantPoolExtension(PackedCPFragment pcp, int baseCPCount, int secondaryBaseCPCount) {
        addedCPContents = pcp.getRelocatedCPBytes(baseCPCount, secondaryBaseCPCount);
        nAddedEntries = pcp.nEntries;
    }

    protected ConstantPoolExtension() {
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public byte[] getConcatenatedContents(ConstantPoolExtension other) {
        if (this.addedCPContents == null) {
            return other.getContents();
        } else if (other.addedCPContents == null) {
            return this.getContents();
        } else {
            byte[] ret = new byte[this.addedCPContents.length + other.addedCPContents.length];
            System.arraycopy(this.addedCPContents, 0, ret, 0, this.addedCPContents.length);
            System.arraycopy(other.addedCPContents, 0, ret, this.addedCPContents.length, other.addedCPContents.length);

            return ret;
        }
    }

    public byte[] getContents() {
        return addedCPContents;
    }

    //-------------------------------------- Public interface --------------------------------------------------------
    public int getNEntries() {
        return nAddedEntries;
    }
}
