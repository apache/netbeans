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
 * Request from the client to the back end to return names of methods with specified jmethodIDs.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class GetMethodNamesForJMethodIdsCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int[] methodIds;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public GetMethodNamesForJMethodIdsCommand(int[] methodIds) {
        super(GET_METHOD_NAMES_FOR_JMETHOD_IDS);
        this.methodIds = methodIds;
    }

    // Custom serialization support
    GetMethodNamesForJMethodIdsCommand() {
        super(GET_METHOD_NAMES_FOR_JMETHOD_IDS);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int[] getMethodIds() {
        return methodIds;
    }

    // For debugging
    public String toString() {
        return super.toString() + ", length: " + methodIds.length; // NOI18N  
    }

    void readObject(ObjectInputStream in) throws IOException {
        int len = in.readInt();
        methodIds = new int[len];

        for (int i = 0; i < len; i++) {
            methodIds[i] = in.readInt();
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(methodIds.length);

        for (int i = 0; i < methodIds.length; i++) {
            out.writeInt(methodIds[i]);
        }

        methodIds = null;
    }
}
