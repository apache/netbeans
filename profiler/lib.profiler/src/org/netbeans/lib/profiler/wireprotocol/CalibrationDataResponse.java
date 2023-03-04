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
 * Contains the calibration information obtained for CPU instrumentation used for profiling.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class CalibrationDataResponse extends Response {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    // The following is the same stuff that we have in ProfilingSessionStatus
    private double[] methodEntryExitCallTime;
    private double[] methodEntryExitInnerTime;
    private double[] methodEntryExitOuterTime;
    private long[] timerCountsInSecond; // This is always of length 2

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public CalibrationDataResponse(double[] callTime, double[] innerTime, double[] outerTime, long[] timerCountsInSecond) {
        super(true, CALIBRATION_DATA);
        this.methodEntryExitCallTime = callTime;
        this.methodEntryExitInnerTime = innerTime;
        this.methodEntryExitOuterTime = outerTime;
        this.timerCountsInSecond = timerCountsInSecond;
    }

    // Custom serialization support
    CalibrationDataResponse() {
        super(true, CALIBRATION_DATA);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public double[] getMethodEntryExitCallTime() {
        return methodEntryExitCallTime;
    }

    public double[] getMethodEntryExitInnerTime() {
        return methodEntryExitInnerTime;
    }

    public double[] getMethodEntryExitOuterTime() {
        return methodEntryExitOuterTime;
    }

    public long[] getTimerCountsInSecond() {
        return timerCountsInSecond;
    }

    // For debugging
    public String toString() {
        return "CalibrationDataResponse, " + super.toString(); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        int len = in.readInt();
        methodEntryExitCallTime = new double[len];
        methodEntryExitInnerTime = new double[len];
        methodEntryExitOuterTime = new double[len];

        for (int i = 0; i < len; i++) {
            methodEntryExitCallTime[i] = in.readDouble();
        }

        for (int i = 0; i < len; i++) {
            methodEntryExitInnerTime[i] = in.readDouble();
        }

        for (int i = 0; i < len; i++) {
            methodEntryExitOuterTime[i] = in.readDouble();
        }

        timerCountsInSecond = new long[2];
        timerCountsInSecond[0] = in.readLong();
        timerCountsInSecond[1] = in.readLong();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        int len = methodEntryExitCallTime.length;
        out.writeInt(len);

        for (int i = 0; i < len; i++) {
            out.writeDouble(methodEntryExitCallTime[i]);
        }

        for (int i = 0; i < len; i++) {
            out.writeDouble(methodEntryExitInnerTime[i]);
        }

        for (int i = 0; i < len; i++) {
            out.writeDouble(methodEntryExitOuterTime[i]);
        }

        out.writeLong(timerCountsInSecond[0]);
        out.writeLong(timerCountsInSecond[1]);
    }
}
