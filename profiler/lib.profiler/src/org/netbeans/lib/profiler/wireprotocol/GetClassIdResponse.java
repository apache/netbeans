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
 * Response to the client's request when classId is needed for given class identified 
 * by class name and classloader id.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class GetClassIdResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int classId;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public GetClassIdResponse(boolean yes, int classId) {
        super(yes, CLASSID_RESPONSE);
        this.classId = classId;
    }

    // Custom serialization support
    GetClassIdResponse() {
        super(true, CLASSID_RESPONSE);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getClassId() {
        return classId;
    }

    // For debugging
    public String toString() {
        return "GetClassIdResponse, classId: " + classId + ", " + super.toString(); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        classId = in.readInt();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(classId);
    }
}
