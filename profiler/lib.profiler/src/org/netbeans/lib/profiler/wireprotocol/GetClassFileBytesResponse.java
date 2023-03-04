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
 *
 * @author Tomas Hurka
 */
public class GetClassFileBytesResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private byte[][] classBytes;

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    public GetClassFileBytesResponse(byte[][] bytes) {
        this();
        classBytes = bytes;
    }

    // Custom serialization support
    GetClassFileBytesResponse() {
        super(true, GET_CLASS_FILE_BYTES_RESPONSE);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------
    public byte[][] getClassBytes() {
        return classBytes;
    }

    // For debugging
    public String toString() {
        return "GetClassFileBytesResponse, classes: " + classBytes.length + ", " + super.toString(); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        int nClasses = in.readInt();

        if (nClasses == 0) {
            return;
        }

        classBytes = new byte[nClasses][];

        for (int i = 0; i < nClasses; i++) {
            int len = in.readInt();

            if (len > 0) {
                classBytes[i] = new byte[len];
                in.readFully(classBytes[i]);
            }
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(classBytes.length);

        for (int i = 0; i < classBytes.length; i++) {
            if (classBytes[i] == null) {
                out.writeInt(0);
            } else {
                out.writeInt(classBytes[i].length);
                out.write(classBytes[i]);
            }
        }
    }

}
