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

import java.io.DataOutputStream;
import java.io.IOException;

/**
 *
 * @author Tomas Zezula
 */
final class Writer {
    private final DataOutputStream out;

    Writer(DataOutputStream out) {
        this.out = out;
    }

    void writeUnsignedByte(final int value) throws IOException {
        out.writeByte(value);
    }

    void writeUnsignedShort(final int value) throws IOException {
        out.writeShort(value);
    }

    void writeUnsignedInt(final long value) throws IOException {
        out.writeInt((int)value);
    }

    void writeByte(final byte value) throws IOException {
        out.writeByte(value);
    }

    void writeInt(final int value) throws IOException {
        out.writeInt(value);
    }

    void flush() throws IOException {
        out.flush();
    }
}
