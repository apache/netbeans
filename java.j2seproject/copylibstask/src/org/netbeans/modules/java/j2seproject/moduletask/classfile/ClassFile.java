/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
