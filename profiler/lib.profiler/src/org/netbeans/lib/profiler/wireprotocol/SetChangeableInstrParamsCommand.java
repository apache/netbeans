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
 * This command, sent by the client, contains the instrumentation parameters (settings) that
 * can be changed once instrumentation is active and profiling is going on.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 * @author Tomas Hurka
 */
public class SetChangeableInstrParamsCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private boolean runGCOnGetResultsInMemoryProfiling;
    private boolean sleepTrackingEnabled;
    private boolean waitTrackingEnabled;
    private boolean threadsSamplingEnabled;
    private boolean lockContentionMonitoringEnabled;
    private int nProfiledThreadsLimit;
    private int stackDepthLimit;
    private int objAllocStackSamplingDepth;
    private int objAllocStackSamplingInterval;
    private int samplingInterval;
    private int threadsSamplingFrequency;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SetChangeableInstrParamsCommand(boolean lockContentionMonitoringEnabled, int nProfiledThreadsLimit, int stackDepthLimit,
                                           int samplingInterval, int objAllocStackSamplingInterval,
                                           int objAllocStackSamplingDepth, boolean runGCOnGetResults,
                                           boolean waitTrackingEnabled, boolean sleepTrackingEnabled, 
                                           boolean threadsSamplingEnabled, int threadsSamplingFrequency) {
        super(SET_CHANGEABLE_INSTR_PARAMS);
        this.lockContentionMonitoringEnabled = lockContentionMonitoringEnabled;
        this.nProfiledThreadsLimit = nProfiledThreadsLimit;
        this.stackDepthLimit = stackDepthLimit;
        this.samplingInterval = samplingInterval;
        this.threadsSamplingFrequency = threadsSamplingFrequency;
        this.objAllocStackSamplingInterval = objAllocStackSamplingInterval;
        this.objAllocStackSamplingDepth = objAllocStackSamplingDepth;
        this.runGCOnGetResultsInMemoryProfiling = runGCOnGetResults;
        this.waitTrackingEnabled = waitTrackingEnabled;
        this.sleepTrackingEnabled = sleepTrackingEnabled;
        this.threadsSamplingEnabled = threadsSamplingEnabled;
    }

    // Custom serialization support
    SetChangeableInstrParamsCommand() {
        super(SET_CHANGEABLE_INSTR_PARAMS);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isLockContentionMonitoringEnabled() {
        return lockContentionMonitoringEnabled;
    }

    public int getNProfiledThreadsLimit() {
        return nProfiledThreadsLimit;
    }

    public int getStackDepthLimit() {
        return stackDepthLimit;
    }

    public int getObjAllocStackSamplingDepth() {
        return objAllocStackSamplingDepth;
    }

    public int getObjAllocStackSamplingInterval() {
        return objAllocStackSamplingInterval;
    }

    public boolean getRunGCOnGetResultsInMemoryProfiling() {
        return runGCOnGetResultsInMemoryProfiling;
    }

    public int getSamplingInterval() {
        return samplingInterval;
    }

    public int getThreadsSamplingFrequency() {
        return threadsSamplingFrequency;
    }

    public boolean isSleepTrackingEnabled() {
        return sleepTrackingEnabled;
    }

    public boolean isWaitTrackingEnabled() {
        return waitTrackingEnabled;
    }

    public boolean isThreadsSamplingEnabled() {
        return threadsSamplingEnabled;
    }

    // For debugging
    public String toString() {
        return super.toString() + ", lockContentionMonitoringEnabled: " + lockContentionMonitoringEnabled // NOI18N
               + ", nProfiledThreadsLimit: " + nProfiledThreadsLimit // NOI18N
               + ", stackDepthLimit: " + stackDepthLimit // NOI18N
               + ", samplingInterval: " + samplingInterval // NOI18N
               + ", objAllocStackSamplingInterval: " + objAllocStackSamplingInterval // NOI18N
               + ", objAllocStackSamplingDepth: " + objAllocStackSamplingDepth // NOI18N
               + ", runGCOnGetResultsInMemoryProfiling: " + runGCOnGetResultsInMemoryProfiling // NOI18N
               + ", waitTrackingEnabled: " + waitTrackingEnabled // NOI18N
               + ", sleepTrackingEnabled: " + sleepTrackingEnabled // NOI18N
               + ", threadsSamplingEnabled: " + threadsSamplingEnabled // NOI18N
               + ", threadsSamplingFrequency: " + threadsSamplingFrequency; // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        lockContentionMonitoringEnabled = in.readBoolean();
        nProfiledThreadsLimit = in.readInt();
        stackDepthLimit = in.readInt();
        samplingInterval = in.readInt();
        objAllocStackSamplingInterval = in.readInt();
        objAllocStackSamplingDepth = in.readInt();
        runGCOnGetResultsInMemoryProfiling = in.readBoolean();
        waitTrackingEnabled = in.readBoolean();
        sleepTrackingEnabled = in.readBoolean();
        threadsSamplingEnabled = in.readBoolean();
        threadsSamplingFrequency = in.readInt();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(lockContentionMonitoringEnabled);
        out.writeInt(nProfiledThreadsLimit);
        out.writeInt(stackDepthLimit);
        out.writeInt(samplingInterval);
        out.writeInt(objAllocStackSamplingInterval);
        out.writeInt(objAllocStackSamplingDepth);
        out.writeBoolean(runGCOnGetResultsInMemoryProfiling);
        out.writeBoolean(waitTrackingEnabled);
        out.writeBoolean(sleepTrackingEnabled);
        out.writeBoolean(threadsSamplingEnabled);
        out.writeInt(threadsSamplingFrequency);
    }
}
