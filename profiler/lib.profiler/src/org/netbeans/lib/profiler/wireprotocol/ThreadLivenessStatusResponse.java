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
 * This Response, issued by the back end, contains the current information about the live/dead status for tracked threads.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class ThreadLivenessStatusResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private byte[] status;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ThreadLivenessStatusResponse(byte[] status) {
        super(true, THREAD_LIVENESS_STATUS);
        this.status = status;
    }

    // Custom serialization support
    ThreadLivenessStatusResponse() {
        super(true, THREAD_LIVENESS_STATUS);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public byte[] getStatus() {
        return status;
    }

    // For debugging
    public String toString() {
        return "ThreadLivenessStatusResponse, " + super.toString(); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        int len = in.readInt();
        status = new byte[len];
        in.readFully(status);
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(status.length);
        out.write(status);
    }
}
