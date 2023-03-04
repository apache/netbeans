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

package org.netbeans.lib.profiler.ui.cpu.statistics;


/**
 *
 * @author Jaroslav Bachorik
 */
public class TimingData {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int incInv;
    private int outInv;
    private long time0Acc;
    private long time1Acc;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public TimingData() {
        time0Acc = 0;
        time1Acc = 0;
        incInv = 0;
        outInv = 0;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public synchronized int getIncInv() {
        return incInv;
    }

    public synchronized int getOutInv() {
        return outInv;
    }

    public synchronized long getTime0Acc() {
        return time0Acc;
    }

    public synchronized long getTime1Acc() {
        return time1Acc;
    }

    public synchronized void addIncomming(int invocations) {
        incInv += invocations;
    }

    public synchronized void addOutgoing(int invocations) {
        outInv += invocations;
    }

    public synchronized void addTime0(long time0) {
        time0Acc += time0;
    }

    public synchronized void addTime1(long time1) {
        time1Acc += time1;
    }

    public synchronized void incrementIncomming() {
        incInv++;
    }

    public synchronized void incrementOutgoing() {
        outInv++;
    }
}
