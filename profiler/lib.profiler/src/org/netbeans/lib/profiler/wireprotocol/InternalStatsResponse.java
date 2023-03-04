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
 * Response containing instrumentation- and profiling-related statistics - most of the data that is presented if one
 * invokes Profile | Get internal statistics command in the tool.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class InternalStatsResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    public double averageHotswappingTime;
    public double clientDataProcTime;
    public double clientInstrTime;
    public double maxHotswappingTime;
    public double methodEntryExitCallTime0;
    public double methodEntryExitCallTime1;
    public double methodEntryExitCallTime2;
    public double minHotswappingTime;
    public double totalHotswappingTime;
    public int nClassLoads;
    public int nEmptyInstrMethodGroupResponses;
    public int nFirstMethodInvocations;
    public int nNonEmptyInstrMethodGroupResponses;
    public int nSingleMethodInstrMethodGroupResponses;

    // Fields made public as an exception, to avoid too many accessors
    public int nTotalInstrMethods;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /**
     * We don't use a normal constructor with parameters here, since there are too many parameters to pass.
     * Instead we use public data fields.
     */
    public InternalStatsResponse() {
        super(true, INTERNAL_STATS);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    // For debugging
    public String toString() {
        return "InternalStatsResponse, " + super.toString(); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        nTotalInstrMethods = in.readInt();
        nClassLoads = in.readInt();
        nFirstMethodInvocations = in.readInt();
        nNonEmptyInstrMethodGroupResponses = in.readInt();
        nEmptyInstrMethodGroupResponses = in.readInt();
        nSingleMethodInstrMethodGroupResponses = in.readInt();
        clientInstrTime = in.readDouble();
        clientDataProcTime = in.readDouble();
        totalHotswappingTime = in.readDouble();
        averageHotswappingTime = in.readDouble();
        minHotswappingTime = in.readDouble();
        maxHotswappingTime = in.readDouble();
        methodEntryExitCallTime0 = in.readDouble();
        methodEntryExitCallTime1 = in.readDouble();
        methodEntryExitCallTime2 = in.readDouble();
    }

    // Custom serialization support
    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(nTotalInstrMethods);
        out.writeInt(nClassLoads);
        out.writeInt(nFirstMethodInvocations);
        out.writeInt(nNonEmptyInstrMethodGroupResponses);
        out.writeInt(nEmptyInstrMethodGroupResponses);
        out.writeInt(nSingleMethodInstrMethodGroupResponses);
        out.writeDouble(clientInstrTime);
        out.writeDouble(clientDataProcTime);
        out.writeDouble(totalHotswappingTime);
        out.writeDouble(averageHotswappingTime);
        out.writeDouble(minHotswappingTime);
        out.writeDouble(maxHotswappingTime);
        out.writeDouble(methodEntryExitCallTime0);
        out.writeDouble(methodEntryExitCallTime1);
        out.writeDouble(methodEntryExitCallTime2);
    }
}
