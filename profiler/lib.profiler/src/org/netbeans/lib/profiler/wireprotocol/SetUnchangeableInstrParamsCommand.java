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
 * This command, sent by the client, contains the instrumentation parameters (settings),
 * that cannot be modified once instrumentation is active and profiling is going on.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class SetUnchangeableInstrParamsCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private boolean remoteProfiling;
    private boolean absoluteTimerOn;
    private boolean threadCPUTimerOn;
    private int codeRegionCPUResBufSize;
    private int instrScheme;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public SetUnchangeableInstrParamsCommand(boolean remote, boolean absoluteTimerOn, boolean threadCPUTimerOn, int instrScheme,
                                             int codeRegionCPUResBufSize) {
        super(SET_UNCHANGEABLE_INSTR_PARAMS);
        remoteProfiling = remote;
        this.absoluteTimerOn = absoluteTimerOn;
        this.threadCPUTimerOn = threadCPUTimerOn;
        this.instrScheme = instrScheme;
        this.codeRegionCPUResBufSize = codeRegionCPUResBufSize;
    }

    // Custom serialization support
    SetUnchangeableInstrParamsCommand() {
        super(SET_UNCHANGEABLE_INSTR_PARAMS);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean getRemoteProfiling() {
        return remoteProfiling;
    }

    public boolean getAbsoluteTimerOn() {
        return absoluteTimerOn;
    }

    public int getCodeRegionCPUResBufSize() {
        return codeRegionCPUResBufSize;
    }

    public int getInstrScheme() {
        return instrScheme;
    }

    public boolean getThreadCPUTimerOn() {
        return threadCPUTimerOn;
    }

    // For debugging
    public String toString() {
        return super.toString() + ", remoteProfiling: " + remoteProfiling // NOI18N
               + ", absoluteTimerOn: " + absoluteTimerOn // NOI18N
               + ", threadCPUTimerOn: " + threadCPUTimerOn // NOI18N
               + ", instrScheme: " + instrScheme // NOI18N
               + ", codeRegionCPUResBufSize: " + codeRegionCPUResBufSize; // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        remoteProfiling = in.readBoolean();
        absoluteTimerOn = in.readBoolean();
        threadCPUTimerOn = in.readBoolean();
        instrScheme = in.readInt();
        codeRegionCPUResBufSize = in.readInt();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeBoolean(remoteProfiling);
        out.writeBoolean(absoluteTimerOn);
        out.writeBoolean(threadCPUTimerOn);
        out.writeInt(instrScheme);
        out.writeInt(codeRegionCPUResBufSize);
    }
}
