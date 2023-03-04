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
 * Response containing the id of the defining class loader, sent to the client in response to the relevant request.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class DefiningLoaderResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int loaderId;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public DefiningLoaderResponse(int loaderId) {
        super(true, DEFINING_LOADER);

        if (loaderId == -1) {
            loaderId = 0; // At the client side we treat classes loaded by bootstrap and by system classloaders in the same way
        }

        this.loaderId = loaderId;
    }

    // Custom serialization support
    DefiningLoaderResponse() {
        super(true, DEFINING_LOADER);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getLoaderId() {
        return loaderId;
    }

    // For debugging
    public String toString() {
        return "DefiningLoaderResponse, loaderId: " + loaderId + ", " + super.toString(); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        loaderId = in.readInt();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(loaderId);
    }
}
