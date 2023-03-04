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
package org.netbeans.modules.java.j2seproject.moduletask.classfile;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tomas Zezula
 */
public final class ConstantPool {
    public enum ConstantKind {
        CONSTANT_Class(7),
        CONSTANT_Fieldref(9),
        CONSTANT_Methodref(10),
        CONSTANT_InterfaceMethodref(11),
        CONSTANT_String(8),
        CONSTANT_Integer(3),
        CONSTANT_Float(4),
        CONSTANT_Long(5),
        CONSTANT_Double(6),
        CONSTANT_NameAndType(12),
        CONSTANT_Utf8(1),
        CONSTANT_MethodHandle(15),
        CONSTANT_MethodType(16),
        CONSTANT_ConstantDynamic(17),
        CONSTANT_InvokeDynamic(18),
        CONSTANT_Module(19),
        CONSTANT_Package(20);

        private static final Map<Integer,ConstantKind> byTag;
        static {
            final Map<Integer,ConstantKind> m = new HashMap<>();
            for (ConstantKind c : values()) {
                m.put(c.getTag(), c);
            }
            byTag = Collections.unmodifiableMap(m);
        }
        private final int tag;

        private ConstantKind(final int tag) {
            this.tag = tag;
        }

        public int getTag() {
            return tag;
        }

        public static ConstantKind fromTag(final int tag) {
            final ConstantKind k = byTag.get(tag);
            if (k != null) {
                return k;
            } else {
                throw new IllegalArgumentException("Unknown ConstantPool constant:" + tag); //NOI18N
            }
        }
    }

    private CPInfo[] entries;

    ConstantPool(final Reader in) throws IOException {
        final int cnt = in.readUnsignedShort();
        entries = new CPInfo[cnt];
        final int[] increment = new int[1];
        for (int i = 1; i < cnt;) {
            entries[i] = readInfo(in, increment);
            i+=increment[0];
        }
    }

    void write(final Writer out) throws IOException {
        out.writeUnsignedShort(entries.length);
        for (int i = 0; i < entries.length; i++) {
            CPInfo entry = entries[i];
            if (entry != null) {
                entry.write(out);
            }
        }
    }

    public CPInfo get(int index) {
        if (index < 0 || index >= entries.length) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        return entries[index];
    }

    public int add(CPInfo constant) {
        for (int i = 0; i < entries.length; i++) {
            if (constant.equals(entries[i])) {
                return i;
            }
        }
        entries = Arrays.copyOf(entries, entries.length+1);
        entries[entries.length-1] = constant;
        return entries.length-1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.length; i++) {
            CPInfo info = entries[i];
            if (info != null) {
                sb.append(i)
                        .append('\t')   //NOI18N
                        .append(entries[i])
                        .append('\n');  //NOI18N
            }
        }
        return sb.toString();
    }

    private CPInfo readInfo(final Reader in, int[] increment) throws IOException {
        final int tag = in.readUnsignedByte();
        final ConstantKind c = ConstantKind.fromTag(tag);
        increment[0] = 1;
        switch (c) {
            case CONSTANT_Class:
                return new CPClass(this, in);
            case CONSTANT_Fieldref:
                return new CPFieldref(this, in);
            case CONSTANT_Methodref:
                return new CPMethodref(this, in);
            case CONSTANT_InterfaceMethodref:
                return new CPInterfaceMethodref(this, in);
            case CONSTANT_String:
                return new CPString(this, in);
            case CONSTANT_Integer:
                return new CPInteger(this, in);
            case CONSTANT_Float:
                return new CPFloat(this, in);
            case CONSTANT_Long:
                increment[0] = 2;
                return new CPLong(this, in);
            case CONSTANT_Double:
                increment[0] = 2;
                return new CPDouble(this, in);
            case CONSTANT_NameAndType:
                return new CPNameAndType(this, in);
            case CONSTANT_Utf8:
                return new CPUtf8(this, in);
            case CONSTANT_MethodHandle:
                return new CPMethodHandle(this, in);
            case CONSTANT_MethodType:
                return new CPMethodType(this, in);
            case CONSTANT_ConstantDynamic:
                return new CPConstantDynamic(this, in);
            case CONSTANT_InvokeDynamic:
                return new CPInvokeDynamic(this, in);
            case CONSTANT_Module:
                return new CPModule(this, in);
            case CONSTANT_Package:
                return new CPPackage(this, in);
            default:
                throw new IllegalArgumentException("Unknown ConstantPool constant: " + c);    //NOI18N
        }
    }

    public abstract static class CPInfo {
        private final ConstantPool owner;
        private final ConstantKind tag;

        CPInfo(
                final ConstantPool owner,
                final ConstantKind tag) {
            this.owner = owner;
            this.tag = tag;
        }

        public ConstantKind getTag() {
            return tag;
        }

        public Object getValue() {
            return null;
        }

        ConstantPool getOwner() {
            return owner;
        }

        void write(Writer out) throws IOException {
            out.writeUnsignedByte(getTag().getTag());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CPInfo)) {
                return false;
            }
            return tag == ((CPInfo)obj).tag;
        }

        @Override
        public int hashCode() {
            return tag.getTag();
        }

        @Override
        public String toString() {
            return tag.toString();
        }
    }

    public abstract static class CPUTF8Ref extends CPInfo {
        private final int nameIndex;

        public CPUTF8Ref(
                final ConstantPool owner,
                final ConstantKind kind,
                final int nameIndex) {
            super(owner, kind);
            this.nameIndex = nameIndex;
        }

        public int getNameIndex() {
            return nameIndex;
        }

        @Override
        public Object getValue() {
            final CPInfo info = getOwner().get(nameIndex);
            return info == null ? null : info.getValue();
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedShort(nameIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPUTF8Ref)) {
                return false;
            }
            return nameIndex == ((CPUTF8Ref)obj).nameIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s %d",    //NOI18N
                    super.toString(),
                    nameIndex);
        }
    }

    public static final class CPClass extends CPUTF8Ref {

        public CPClass(
                final ConstantPool owner,
                final int nameIndex) {
            super(owner, ConstantKind.CONSTANT_Class, nameIndex);
        }

        CPClass(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Class, in.readUnsignedShort());
        }
    }

    public static class CPFieldref extends CPInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;

        CPFieldref(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Fieldref);
            this.classIndex = in.readUnsignedShort();
            this.nameAndTypeIndex = in.readUnsignedShort();
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedShort(classIndex);
            out.writeUnsignedShort(nameAndTypeIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPFieldref)) {
                return false;
            }
            final CPFieldref fld = (CPFieldref) obj;
            return classIndex == fld.classIndex &&
                    nameAndTypeIndex == fld.nameAndTypeIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s class: %d, nameAndType: %d",    //NOI18N
                    super.toString(),
                    classIndex,
                    nameAndTypeIndex);
        }
    }

    public static class CPMethodref extends CPInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;

        CPMethodref(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Methodref);
            this.classIndex = in.readUnsignedShort();
            this.nameAndTypeIndex = in.readUnsignedShort();
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedShort(classIndex);
            out.writeUnsignedShort(nameAndTypeIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPMethodref)) {
                return false;
            }
            final CPMethodref m = (CPMethodref) obj;
            return classIndex == m.classIndex &&
                    nameAndTypeIndex == m.nameAndTypeIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s class: %d, nameAndType: %d",    //NOI18N
                    super.toString(),
                    classIndex,
                    nameAndTypeIndex);
        }
    }

    public static class CPInterfaceMethodref extends CPInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;

        CPInterfaceMethodref(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_InterfaceMethodref);
            this.classIndex = in.readUnsignedShort();
            this.nameAndTypeIndex = in.readUnsignedShort();
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedShort(classIndex);
            out.writeUnsignedShort(nameAndTypeIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPInterfaceMethodref)) {
                return false;
            }
            final CPInterfaceMethodref m = (CPInterfaceMethodref) obj;
            return classIndex == m.classIndex &&
                    nameAndTypeIndex == m.nameAndTypeIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s class: %d, nameAndType: %d",    //NOI18N
                    super.toString(),
                    classIndex,
                    nameAndTypeIndex);
        }
    }

    public static class CPString extends CPUTF8Ref {

        CPString(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_String, in.readUnsignedShort());
        }
    }

    public static class CPInteger extends CPInfo {
        private final int value;

        CPInteger(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Integer);
            this.value = in.readInt();
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeInt(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPInteger)) {
                return false;
            }
            return value == ((CPInteger)obj).value;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s 0x%x",    //NOI18N
                    super.toString(),
                    value);
        }
    }

    public static class CPFloat extends CPInfo {
        private final int value;

        CPFloat(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Float);
            this.value = in.readInt();
        }

        @Override
        public Object getValue() {
            return Float.intBitsToFloat(value);
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeInt(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPFloat)) {
                return false;
            }
            return value == ((CPFloat)obj).value;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s %f",    //NOI18N
                    super.toString(),
                    (Float)getValue());
        }
    }

    public static class CPLong extends CPInfo {
        private final int highBytes;
        private final int lowBytes;

        CPLong(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Long);
            this.highBytes = in.readInt();
            this.lowBytes = in.readInt();
        }

        @Override
        public Object getValue() {
            return ((long)highBytes)<<32 | (lowBytes & 0xffffffffL);
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeInt(highBytes);
            out.writeInt(lowBytes);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPLong)) {
                return false;
            }
            final CPLong l = (CPLong) obj;
            return highBytes == l.highBytes &&
                    lowBytes == l.lowBytes;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s 0x%x",    //NOI18N
                    super.toString(),
                    (Long)getValue());
        }
    }

    public static class CPDouble extends CPInfo {
        private final int highBytes;
        private final int lowBytes;

        CPDouble(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Double);
            this.highBytes = in.readInt();
            this.lowBytes = in.readInt();
        }

        @Override
        public Object getValue() {
            return Double.longBitsToDouble(((long)highBytes)<<32 | (lowBytes & 0xffffffffL));
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeInt(highBytes);
            out.writeInt(lowBytes);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPDouble)) {
                return false;
            }
            final CPDouble d = (CPDouble) obj;
            return highBytes == d.highBytes &&
                    lowBytes == d.lowBytes;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s %f",    //NOI18N
                    super.toString(),
                    (Double)getValue());
        }
    }

    public static class CPNameAndType extends CPInfo {
        private final int nameIndex;
        private final int descriptorIndex;

        CPNameAndType(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_NameAndType);
            this.nameIndex = in.readUnsignedShort();
            this.descriptorIndex = in.readUnsignedShort();
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedShort(nameIndex);
            out.writeUnsignedShort(descriptorIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPNameAndType)) {
                return false;
            }
            final CPNameAndType l = (CPNameAndType) obj;
            return nameIndex == l.nameIndex &&
                    descriptorIndex == l.descriptorIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s name: %d, type: %d",    //NOI18N
                    super.toString(),
                    nameIndex,
                    descriptorIndex);
        }
    }

    public static class CPUtf8 extends CPInfo {
        private final byte[] bytes;

        public CPUtf8(
                final ConstantPool owner,
                final String str) throws IOException {
            super(owner, ConstantKind.CONSTANT_Utf8);
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try(final DataOutputStream out = new DataOutputStream(bos)) {
                out.writeUTF(str);
            }
            final byte[] arr = bos.toByteArray();
            bytes = Arrays.copyOfRange(arr, 2, arr.length);
        }

        CPUtf8(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Utf8);
            final int length = in.readUnsignedShort();
            this.bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                bytes[i] = in.readByte();
            }
        }

        @Override
        public Object getValue() {
            return new String(bytes, Charset.forName("UTF-8")); //NOI18N
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedShort(bytes.length);
            for (int i = 0; i < bytes.length; i++) {
                out.writeByte(bytes[i]);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPUtf8)) {
                return false;
            }
            final CPUtf8 s = (CPUtf8) obj;
            return Arrays.equals(bytes, s.bytes);
        }

        @Override
        public String toString() {
            return String.format(
                    "%s %s",    //NOI18N
                    super.toString(),
                    getValue());
        }
    }

    public static class CPMethodHandle extends CPInfo {
        private final int referenceKind;
        private final int referenceIndex;

        CPMethodHandle(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_MethodHandle);
            this.referenceKind = in.readUnsignedByte();
            this.referenceIndex = in.readUnsignedShort();
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedByte(referenceKind);
            out.writeUnsignedShort(referenceIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPMethodHandle)) {
                return false;
            }
            final CPMethodHandle h = (CPMethodHandle) obj;
            return referenceKind == h.referenceKind &&
                    referenceIndex == h.referenceIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s kind: %x, reference: %d",    //NOI18N
                    super.toString(),
                    referenceKind,
                    referenceIndex);
        }
    }

    public static class CPMethodType extends CPInfo {
        private final int descriptorIndex;

        CPMethodType(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_MethodType);
            this.descriptorIndex = in.readUnsignedShort();
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedShort(descriptorIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPMethodType)) {
                return false;
            }
            return descriptorIndex == ((CPMethodType)obj).descriptorIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s %d",    //NOI18N
                    super.toString(),
                    descriptorIndex);
        }
    }

    public static class CPInvokeDynamic extends CPInfo {
        private final int bootstrapMethodAttrIndex;
        private final int nameAndTypeIndex;

        CPInvokeDynamic(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_InvokeDynamic);
            this.bootstrapMethodAttrIndex = in.readUnsignedShort();
            this.nameAndTypeIndex = in.readUnsignedShort();
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedShort(bootstrapMethodAttrIndex);
            out.writeUnsignedShort(nameAndTypeIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPInvokeDynamic)) {
                return false;
            }
            final CPInvokeDynamic i = (CPInvokeDynamic) obj;
            return bootstrapMethodAttrIndex == i.bootstrapMethodAttrIndex &&
                    nameAndTypeIndex == i.nameAndTypeIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s bootstrapMethod: %d, nameAndType: %d",    //NOI18N
                    super.toString(),
                    bootstrapMethodAttrIndex,
                    nameAndTypeIndex);
        }
    }

    public static class CPConstantDynamic extends CPInfo {

        private final int bootstrapMethodAttrIndex;
        private final int nameAndTypeIndex;

        CPConstantDynamic(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_ConstantDynamic);
            this.bootstrapMethodAttrIndex = in.readUnsignedShort();
            this.nameAndTypeIndex = in.readUnsignedShort();
        }

        @Override
        void write(Writer out) throws IOException {
            super.write(out);
            out.writeUnsignedShort(bootstrapMethodAttrIndex);
            out.writeUnsignedShort(nameAndTypeIndex);
        }

        @Override
        public boolean equals(Object obj) {
            if (!super.equals(obj) || !(obj instanceof CPConstantDynamic)) {
                return false;
            }
            final CPConstantDynamic i = (CPConstantDynamic)obj;
            return bootstrapMethodAttrIndex == i.bootstrapMethodAttrIndex
                    && nameAndTypeIndex == i.nameAndTypeIndex;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s bootstrapMethod: %d, nameAndType: %d", //NOI18N
                    super.toString(),
                    bootstrapMethodAttrIndex,
                    nameAndTypeIndex);
        }
    }

    public static class CPModule extends CPUTF8Ref {

        CPModule(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Module, in.readUnsignedShort());
        }
    }

    public static class CPPackage extends CPUTF8Ref {
        CPPackage(
                final ConstantPool owner,
                final Reader in) throws IOException {
            super(owner, ConstantKind.CONSTANT_Package, in.readUnsignedShort());
        }
    }
}
