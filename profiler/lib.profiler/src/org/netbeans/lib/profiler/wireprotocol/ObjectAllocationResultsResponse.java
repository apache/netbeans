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
 * This Response, issued by the back end, contains the current information about the number of objects allocated
 * for each type.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class ObjectAllocationResultsResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int[] results;
    private int nEntries;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ObjectAllocationResultsResponse(int[] results, int nEntries) {
        super(true, OBJECT_ALLOCATION_RESULTS);
        this.results = results;
        this.nEntries = nEntries;
    }

    // Custom serialization support
    ObjectAllocationResultsResponse() {
        super(true, OBJECT_ALLOCATION_RESULTS);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int[] getResults() {
        return results;
    }

    // For debugging
    public String toString() {
        return "ObjectAllocationResultsResponse, entries: " + nEntries + ", " + super.toString(); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        nEntries = in.readInt();
        results = new int[nEntries];

        for (int i = 0; i < nEntries; i++) {
            results[i] = in.readInt();
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(nEntries);

        for (int i = 0; i < nEntries; i++) {
            out.writeInt(results[i]);
        }

        results = null;
    }
}
