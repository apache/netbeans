/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * VerificationTypeInfo structure, which is defined as a C-like union
 * in the Java Virtual Machine Specification, section 4.8.4, and is
 * used to define stack map frame structures.  To map this union to Java
 * classes, this class is abstract and has a separate public subclass for each
 * union member.  The verification type can be determined either by the
 * its <code>frame_type</code> or using an instanceof test.
 *
 * @author tball
 */
public abstract class VerificationTypeInfo {
    private int tag;
    
    /** Verification type <code>top</code>. */
    public static final int ITEM_Top = 0;
    /** Verification type <code>int</code>. */
    public static final int ITEM_Integer = 1;
    /** Verification type <code>float</code>. */
    public static final int ITEM_Float = 2;
    /** Verification type <code>double</code>. */
    public static final int ITEM_Double = 3;
    /** Verification type <code>long</code>. */
    public static final int ITEM_Long = 4;
    /** Verification type <code>null</code>. */
    public static final int ITEM_Null = 5;
    /** Verification type <code>uninitializedThis</code>. */
    public static final int ITEM_UninitializedThis = 6;
    /** Verification type <code>object</code>. */
    public static final int ITEM_Object = 7;
    /** Verification type <code>uninitialized</code>. */
    public static final int ITEM_Uninitialized = 8;
    
    static VerificationTypeInfo loadVerificationTypeInfo(DataInputStream in, ConstantPool pool) 
      throws IOException {
        int tag = in.readUnsignedByte();
        switch (tag) {
            case ITEM_Top: return new TopVariableInfo();
            case ITEM_Integer: return new IntegerVariableInfo();
            case ITEM_Float: return new FloatVariableInfo();
            case ITEM_Long: return new LongVariableInfo();
            case ITEM_Double: return new DoubleVariableInfo();
            case ITEM_Null: return new NullVariableInfo();
            case ITEM_UninitializedThis: return new UninitializedThisVariableInfo();
            case ITEM_Object: {
                int cpool_index = in.readUnsignedShort();
                return new ObjectVariableInfo(pool.get(cpool_index));
            }
            case ITEM_Uninitialized: {
                int offset = in.readUnsignedShort();
                return new UninitializedVariableInfo(offset);
            }
            default:
                throw new InvalidClassFormatException("invalid verification_type_info tag: " + tag);
        }
    }
    
    /** Creates a new instance of VerificationTypeInfo */
    VerificationTypeInfo(int tag) {
        this.tag = tag;
    }

    /**
     * Returns the structure's tag, which specifies its type.  This tag is a
     * value between 0 and 8, as defined by the <code>ITEM_*</code> constants
     * in this class.  (When Java 5 is the minimum JVM for NetBeans, these
     * constants can be replaced with an enum.)
     * @return structure tag
     */
    public int getTag() {
        return tag;
    }
    
    /**
     * A Top_variable_info type, which indicates that the local variable has 
     * the verification type <code>top</code>.
     */
    public static final class TopVariableInfo extends VerificationTypeInfo {
        TopVariableInfo() {
            super(ITEM_Top);
        }
    }
    
    /**
     * A Integer_variable_info type, which indicates that the location 
     * contains the verification type <code>int</code>.
     */
    public static final class IntegerVariableInfo extends VerificationTypeInfo {
        IntegerVariableInfo() {
            super(ITEM_Integer);
        }
    }
    
    /**
     * A Float_variable_info type, which indicates that the location contains 
     * the verification type <code>float</code>.
     */
    public static final class FloatVariableInfo extends VerificationTypeInfo {
        FloatVariableInfo() {
            super(ITEM_Float);
        }
    }
    
    /**
     * A Long_variable_info type, which indicates that the location contains 
     * the verification type <code>long</code>.
     */
    public static final class LongVariableInfo extends VerificationTypeInfo {
        LongVariableInfo() {
            super(ITEM_Long);
        }
    }
    
    /**
     * A Double_variable_info type, which indicates that the location contains 
     * the verification type <code>double</code>.
     */
    public static final class DoubleVariableInfo extends VerificationTypeInfo {
        DoubleVariableInfo() {
            super(ITEM_Double);
        }
    }
    
    /**
     * A Null_variable_info type, which indicates that the location contains 
     * the verification type <code>null</code>.
     */
    public static final class NullVariableInfo extends VerificationTypeInfo {
        NullVariableInfo() {
            super(ITEM_Null);
        }
    }
    
    /**
     * A UninitializedThis_variable_info type, which indicates that the location contains 
     * the verification type <code>uninitializedThis</code>.
     */
    public static final class UninitializedThisVariableInfo extends VerificationTypeInfo {
        UninitializedThisVariableInfo() {
            super(ITEM_UninitializedThis);
        }
    }
    
    /**
     * An Object_variable_info type, which indicates that the location 
     * contains an instance of the class referenced by the constant pool entry.
     */
    public static final class ObjectVariableInfo extends VerificationTypeInfo {
        CPEntry cpEntry;
        ObjectVariableInfo(CPEntry entry) {
            super(ITEM_Object);
            cpEntry = entry;
        }
        
        /**
         * Returns the constant pool entry which initializes this variables.
         * @return the constant poll entry which initializes this variables
         */
        public CPEntry getConstantPoolEntry() {
            return cpEntry;
        }
    }
    
    /**
     * An Uninitialized_variable_info type, which indicates that the location 
     * contains the verification type <code>uninitialized(<i>offset</i>)</code>. 
     */
    public static final class UninitializedVariableInfo extends VerificationTypeInfo {
        int offset;
        UninitializedVariableInfo(int offset) {
            super(ITEM_Object);
            this.offset = offset;
        }
        
        /**
         * Returns  the offset of the new instruction that created 
         * the object being stored in the location.
         * @return the offset of the new instruction that created the object 
         * being stored in the location
         */
        public int getOffset() {
            return offset;
        }
    }
}
