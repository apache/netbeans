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

import java.io.IOException;
import java.util.Arrays;

/**
 *
 * @author Tomas Zezula
 */
public class Attribute {
    private final int nameIndex;
    private final byte[] info;

    public Attribute(
            final int nameIndex,
            final byte[] info) {
        this.nameIndex = nameIndex;
        this.info = info;
    }

    Attribute(final Reader in) throws IOException {
        this.nameIndex = in.readUnsignedShort();
        this.info = new byte[(int)in.readUnsignedInt()];
        for (int i = 0; i < this.info.length; i++) {
            this.info[i] = in.readByte();
        }
    }

    void write(final Writer out) throws IOException {
        out.writeUnsignedShort(nameIndex);
        out.writeUnsignedInt(info.length);
        for (int i = 0; i < info.length; i++) {
            out.writeByte(this.info[i]);
        }
    }

    public int getNameIndex() {
        return nameIndex;
    }

    public byte[] getValue() {
        return Arrays.copyOf(info, info.length);
    }

    @Override
    public String toString() {
        return "nameIndex: " + nameIndex + ", length: " + info.length;
    }
}
