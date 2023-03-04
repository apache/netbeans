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

/**
 *
 * @author Tomas Zezula
 */
public final class FieldInfo {
    private final int accessFlags;
    private final int nameIndex;
    private final int descriptorIndex;
    private final Attribute[] attributes;

    FieldInfo(final Reader in) throws IOException {
        this.accessFlags = in.readUnsignedShort();
        this.nameIndex = in.readUnsignedShort();
        this.descriptorIndex = in.readUnsignedShort();
        this.attributes = new Attribute[in.readUnsignedShort()];
        for (int i = 0; i < this.attributes.length; i++) {
            this.attributes[i] = new Attribute(in);
        }
    }

    void write(final Writer out) throws IOException {
        out.writeUnsignedShort(accessFlags);
        out.writeUnsignedShort(nameIndex);
        out.writeUnsignedShort(descriptorIndex);
        out.writeUnsignedShort(attributes.length);
        for (int i = 0; i < attributes.length; i++) {
            attributes[i].write(out);
        }
    }
}
