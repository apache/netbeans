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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Tomas Zezula
 */
public final class ClassFile {

    enum Access {
        ACC_PUBLIC(0x0001),
        ACC_FINAL(0x0010),
        ACC_SUPER(0x0020),
        ACC_INTERFACE(0x0200),
        ACC_ABSTRACT (0x0400),
        ACC_SYNTHETIC(0x1000),
        ACC_ANNOTATION(0x2000),
        ACC_ENUM(0x4000);

        private final int mask;

        private Access(int mask) {
            this.mask = mask;
        }

        static Set<Access> fromIntBits(final int val) {
            final Set<Access> s = EnumSet.noneOf(Access.class);
            for (Access a : values()) {
                if ((val & a.mask) == a.mask) {
                    s.add(a);
                }
            }
            return s;
        }

        static int toIntBits(final Set<? extends Access> set) {
            int res = 0;
            for (Access a : set) {
                res |= a.mask;
            }
            return res;
        }
    }

    private static final long MAGIC = 0xcafebabeL;

    private final int minorVersion;
    private final int majorVersion;
    private ConstantPool cp;
    private final int accessFlags;
    private final int thisIndex;
    private final int superIndex;
    private final int[] interfacesIndexes;
    private final FieldInfo[] fields;
    private final MethodInfo[] methods;
    private Attribute[] attributes;

    public ClassFile(final InputStream input) throws IOException {
        final Reader in = new Reader(new DataInputStream(input));
        if (in.readUnsignedInt()!= MAGIC) {
            throw new IllegalArgumentException("Not a classfile");
        }
        this.minorVersion = in.readUnsignedShort();
        this.majorVersion = in.readUnsignedShort();
        this.cp = new ConstantPool(in);
        this.accessFlags = in.readUnsignedShort();
        this.thisIndex = in.readUnsignedShort();
        this.superIndex = in.readUnsignedShort();
        this.interfacesIndexes = new int[in.readUnsignedShort()];
        for (int i = 0; i < this.interfacesIndexes.length; i++) {
            this.interfacesIndexes[i] = in.readUnsignedShort();
        }
        this.fields = new FieldInfo[in.readUnsignedShort()];
        for (int i = 0; i < this.fields.length; i++) {
            this.fields[i] = new FieldInfo(in);
        }
        this.methods = new MethodInfo[in.readUnsignedShort()];
        for (int i = 0; i < this.methods.length; i++) {
            this.methods[i] = new MethodInfo(in);
        }
        this.attributes = new Attribute[in.readUnsignedShort()];
        for (int i = 0; i < this.attributes.length; i++) {
            this.attributes[i] = new Attribute(in);
        }
    }

    public void write(final OutputStream output) throws IOException {
        final Writer out = new Writer(new DataOutputStream(output));
        out.writeUnsignedInt(MAGIC);
        out.writeUnsignedShort(minorVersion);
        out.writeUnsignedShort(majorVersion);
        cp.write(out);
        out.writeUnsignedShort(accessFlags);
        out.writeUnsignedShort(thisIndex);
        out.writeUnsignedShort(superIndex);
        out.writeUnsignedShort(interfacesIndexes.length);
        for (int i = 0; i < interfacesIndexes.length; i++) {
            out.writeUnsignedShort(interfacesIndexes[i]);
        }
        out.writeUnsignedShort(fields.length);
        for (int i = 0; i < fields.length; i++) {
            fields[i].write(out);
        }
        out.writeUnsignedShort(methods.length);
        for (int i = 0; i < methods.length; i++) {
            methods[i].write(out);
        }
        out.writeUnsignedShort(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].write(out);
        }
        out.flush();
    }

    public ConstantPool getConstantPool() {
        return cp;
    }

    public Attribute[] getAttributes() {
        return Arrays.copyOf(attributes, attributes.length);
    }

    public boolean addAttribute(final Attribute attr) {
        attributes = Arrays.copyOf(attributes, attributes.length + 1);
        attributes[attributes.length-1] = attr;
        return true;
    }

    public boolean removeAttribute(final int index) {
        if (index < 0 || index >= attributes.length) {
            throw new ArrayIndexOutOfBoundsException(index);
        }
        final Attribute[] newAttrs = new Attribute[attributes.length-1];
        System.arraycopy(attributes, 0, newAttrs, 0, index);
        System.arraycopy(attributes, index+1, newAttrs, index, newAttrs.length - index);
        attributes = newAttrs;
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder res = new StringBuilder();
        res.append(majorVersion)
                .append('.')
                .append(minorVersion)
                .append('\n');
        res.append(cp)
                .append('\n');
        res.append(Access.fromIntBits(accessFlags))
                .append('\n');
        res.append(thisIndex)
                .append('\n');
        res.append(superIndex)
                .append('\n');
        res.append('[');
        boolean first = true;
        for (int i : interfacesIndexes) {
            if (first) {
                first = false;
            } else {
                res.append(" ,");
            }
            res.append(i);
        }
        res.append(']');
        for (Attribute attr : attributes) {
            res.append(attr)
                    .append('\n');
        }
        return res.toString();
    }
}
