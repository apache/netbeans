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
 * Code region CPU profiling results, that are sent to the client upon request.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class CodeRegionCPUResultsResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private long[] results;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CodeRegionCPUResultsResponse(long[] results) {
        super(true, CODE_REGION_CPU_RESULTS);
        // Note that he first element of the array is the total number of invocations and should not be changed.
        this.results = results;
    }

    // Custom serialization support
    CodeRegionCPUResultsResponse() {
        super(true, CODE_REGION_CPU_RESULTS);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public long[] getResults() {
        return results;
    }

    // For debugging
    public String toString() {
        return "CodeRegionCPUResultsResponse, length: " + results.length + ", " + super.toString(); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        int len = in.readInt();
        results = new long[len];

        for (int i = 0; i < len; i++) {
            results[i] = in.readLong();
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(results.length);

        for (int i = 0; i < results.length; i++) {
            out.writeLong(results[i]);
        }
    }
}
