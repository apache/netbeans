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

package org.netbeans.lib.profiler.wireprotocol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


/**
 * This is issued by back end in response to GetMethodNamesForJMethodIdsCommand. It contains strings with methods'
 * classes, names and signatures, packed into a single byte[] array. At the client side this data is subsequently
 * unpacked (not in this class to avoid having unused code at the back end side).
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class MethodNamesResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int[] packedArrayOffsets;
    private byte[] packedData;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public MethodNamesResponse(byte[] packedData, int[] packedArrayOffsets) {
        super(true, METHOD_NAMES);
        this.packedData = packedData;
        this.packedArrayOffsets = packedArrayOffsets;
    }

    // Custom serialization support
    MethodNamesResponse() {
        super(true, METHOD_NAMES);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int[] getPackedArrayOffsets() {
        return packedArrayOffsets;
    }

    public byte[] getPackedData() {
        return packedData;
    }

    void readObject(ObjectInputStream in) throws IOException {
        int len = in.readInt();
        packedData = new byte[len];
        in.readFully(packedData);
        len = in.readInt();
        packedArrayOffsets = new int[len];

        for (int i = 0; i < len; i++) {
            packedArrayOffsets[i] = in.readInt();
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(packedData.length);
        out.write(packedData);
        out.writeInt(packedArrayOffsets.length);

        for (int i = 0; i < packedArrayOffsets.length; i++) {
            out.writeInt(packedArrayOffsets[i]);
        }

        packedData = null;
        packedArrayOffsets = null;
    }
}
