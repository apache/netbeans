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

package org.netbeans.modules.debugger.jpda.projects;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 * Structured representation of the class constant pool.
 *
 * @author Martin Entlicher
 */
public class ConstantPool {

    private static final byte TAG_UTF8 = 1;
    private static final byte TAG_INTEGER = 3;
    private static final byte TAG_FLOAT = 4;
    private static final byte TAG_LONG = 5;
    private static final byte TAG_DOUBLE = 6;
    private static final byte TAG_CLASS = 7;
    private static final byte TAG_STRING = 8;
    private static final byte TAG_FIELDREF = 9;
    private static final byte TAG_METHODREF = 10;
    private static final byte TAG_INTERFACEREF = 11;
    private static final byte TAG_NAMETYPE = 12;
    private static final byte TAG_METHODHANDLE = 15;
    private static final byte TAG_METHODTYPE = 16;
    private static final byte TAG_CONSTANTDYNAMIC = 17;
    private static final byte TAG_INVOKEDYNAMIC = 18;
    private static final byte TAG_MODULE = 19;
    private static final byte TAG_PACKAGE = 20;

    private final List<ConstantPool.Entry> entries;
    private final String description;

    private ConstantPool(List<ConstantPool.Entry> entries, String description) {
        this.entries = entries;
        this.description = description;
    }

    public ConstantPool.Entry getEntry(int index) {
        return entries.get(index);
    }

    /**
     * @param index Index to constant pool entries
     * @return method name
     * @throws IndexOutOfBoundsException when the constant pool size is smaller than index.
     */
    public String getMethodName(int index) {
        try {
        EntryFieldMethodRef methodRef = (EntryFieldMethodRef) entries.get(index);
        return ((EntryUTF8) entries.get(((EntryNameType) entries.get(methodRef.nameAndTypeIndex)).getNameIndex())).getUTF8();
        } catch (RuntimeException re) {
            throw Exceptions.attachMessage(re, description);
        }
    }

    /**
     * @param index Index to constant pool entries
     * @return method name
     * @throws IndexOutOfBoundsException when the constant pool size is smaller than index.
     */
    public String getMethodDescriptor(int index) {
        try {
        EntryFieldMethodRef methodRef = (EntryFieldMethodRef) entries.get(index);
        return ((EntryUTF8) entries.get(((EntryNameType) entries.get(methodRef.nameAndTypeIndex)).getDescriptorIndex())).getUTF8();
        } catch (RuntimeException re) {
            throw Exceptions.attachMessage(re, description);
        }
    }

    public static ConstantPool parse(byte[] bytes, String description) {
        List<ConstantPool.Entry> entries = new ArrayList<ConstantPool.Entry>();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));
        entries.add(new EntryNULL());
        try {
            do {
                byte tagByte;
                try {
                    tagByte = in.readByte();
                } catch (EOFException eof) {
                    break;
                }
                ConstantPool.Entry entry;
                switch(tagByte) {
                    case TAG_UTF8:
                        entry = new EntryUTF8(in.readUTF());
                        break;
                    case TAG_INTEGER:
                        entry = new EntryInteger(in.readInt());
                        break;
                    case TAG_LONG:
                        entry = new EntryLong(in.readLong());
                        entries.add(entry);
                        entry = new EntryNULL(); // Long takes TWO constant pool entries!?!
                        break;
                    case TAG_FLOAT:
                        entry = new EntryFloat(in.readFloat());
                        break;
                    case TAG_DOUBLE:
                        entry = new EntryDouble(in.readDouble());
                        entries.add(entry);
                        entry = new EntryNULL(); // Double takes TWO constant pool entries!?!
                        break;
                    case TAG_CLASS:
                        entry = new EntryClass(in.readShort());
                        break;
                    case TAG_STRING:
                        entry = new EntryString(in.readShort());
                        break;
                    case TAG_NAMETYPE:
                        entry = new EntryNameType(in.readShort(), in.readShort());
                        break;
                    case TAG_FIELDREF:
                    case TAG_METHODREF:
                    case TAG_INTERFACEREF:
                        entry = new EntryFieldMethodRef(tagByte, in.readShort(), in.readShort());
                        break;
                    case TAG_METHODHANDLE:
                        entry = new EntryMethodHandle(in);
                        break;
                    case TAG_METHODTYPE:
                        entry = new EntryMethodType(in);
                        break;
                    case TAG_CONSTANTDYNAMIC:
                        entry = new EntryConstantDynamic(in);
                        break;
                    case TAG_INVOKEDYNAMIC:
                        entry = new EntryInvokeDynamic(in);
                        break;
                    case TAG_MODULE:
                        entry = new EntryModule(in.readShort());
                        break;
                    case TAG_PACKAGE:
                        entry = new EntryPackage(in.readShort());
                        break;
                    case 0:
                    default:
                        Logger.getLogger(ConstantPool.class.getName()).warning("Unknown tag byte: "+tagByte);
                        entry = new EntryNULL();
                }
                entries.add(entry);
            } while(true);
        } catch (IOException ioex) {
            // Should not occur
            Exceptions.printStackTrace(ioex);
        }
        return new ConstantPool(entries, description);
    }


    // Entries inner classes

    public abstract static class Entry {

        private final byte tag;

        protected Entry(byte tag) {
            this.tag = tag;
        }

        public final byte getTag() {
            return tag;
        }
    }

    public static class EntryNULL extends Entry {
        public EntryNULL() {
            super((byte) 0);
        }
    }

    public static class EntryUTF8 extends Entry {

        private String utf8;

        public EntryUTF8(String utf8) {
            super(TAG_UTF8);
            this.utf8 = utf8;
        }

        public String getUTF8() {
            return utf8;
        }
    }

    public static class EntryInteger extends Entry {

        private int i;

        public EntryInteger(int i) {
            super(TAG_INTEGER);
            this.i = i;
        }

        public int getInteger() {
            return i;
        }
    }

    public static class EntryLong extends Entry {

        private long l;

        public EntryLong(long l) {
            super(TAG_LONG);
            this.l = l;
        }

        public long getLong() {
            return l;
        }
    }

    public static class EntryFloat extends Entry {

        private float f;

        public EntryFloat(float f) {
            super(TAG_FLOAT);
            this.f = f;
        }

        public float getFloat() {
            return f;
        }
    }

    public static class EntryDouble extends Entry {

        private double d;

        public EntryDouble(double d) {
            super(TAG_DOUBLE);
            this.d = d;
        }

        public double getDouble() {
            return d;
        }
    }

    public static class EntryClass extends Entry {

        /** Refrence to TAG_UTF8 entry */
        private short classRef;

        public EntryClass(short classRef) {
            super(TAG_CLASS);
            this.classRef = classRef;
        }

        public short getClassRef() {
            return classRef;
        }
    }

    /**
     * @since 1.41
     */
    public static final class EntryModule extends Entry {

        /** Reference to TAG_UTF8 entry */
        private short nameIndex;

        public EntryModule(short nameIndex) {
            super(TAG_MODULE);
            this.nameIndex = nameIndex;
        }

        public short getModuleRef() {
            return nameIndex;
        }
    }

    /**
     * @since 1.41
     */
    public static final class EntryPackage extends Entry {

        /** Reference to TAG_UTF8 entry */
        private short nameIndex;

        public EntryPackage(short nameIndex) {
            super(TAG_PACKAGE);
            this.nameIndex = nameIndex;
        }

        public short getPackageRef() {
            return nameIndex;
        }
    }

    public static class EntryString extends Entry {

        /** Refrence to TAG_UTF8 entry */
        private short stringRef;

        public EntryString(short stringRef) {
            super(TAG_STRING);
            this.stringRef = stringRef;
        }

        public short getStringRef() {
            return stringRef;
        }
    }

    public static class EntryNameType extends Entry {

        private short nameIndex;
        private short descriptorIndex;

        public EntryNameType(short nameIndex, short descriptorIndex) {
            super(TAG_NAMETYPE);
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }

        public short getNameIndex() {
            return nameIndex;
        }

        public short getDescriptorIndex() {
            return descriptorIndex;
        }
    }

    public static class EntryFieldMethodRef extends Entry {

        private short classIndex;
        private short nameAndTypeIndex;

        public EntryFieldMethodRef(byte type, short classIndex, short nameAndTypeIndex) {
            super(type);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }
    
    public static class BytesEntry extends Entry {
        
        protected final byte[] bytes;
        
        public BytesEntry(byte type, DataInputStream in, int length) throws IOException {
            super(type);
            byte[] b = new byte[length];
            int l = in.read(b, 0, length);
            while (l < length) {
                l += in.read(b, l, length - l);
            }
            bytes = b;
        }
    }
    
    public static class EntryMethodHandle extends BytesEntry {
        
        public EntryMethodHandle(DataInputStream in) throws IOException {
            super(TAG_METHODHANDLE, in, 3);
        }
    }
    
    public static class EntryMethodType extends BytesEntry {
        
        public EntryMethodType(DataInputStream in) throws IOException {
            super(TAG_METHODTYPE, in, 2);
        }
    }
    
    public static class EntryInvokeDynamic extends BytesEntry {
        
        public EntryInvokeDynamic(DataInputStream in) throws IOException {
            super(TAG_INVOKEDYNAMIC, in, 4);
        }
    }
    
    public static class EntryConstantDynamic extends BytesEntry {

        public EntryConstantDynamic(DataInputStream in) throws IOException {
            super(TAG_CONSTANTDYNAMIC, in, 4);
        }
    }
 
}
