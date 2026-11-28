/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.wireprotocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Request from the client to the back end to initiate TA instrumentation of the given type.
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 * @author Adrian Mos
 * @author Ian Formanek
 */
public class InitiateProfilingCommand extends Command {

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String[] classNames;
    private String[] profilingPointHandlers;
    private int[] profilingPointIDs;
    private String[] profilingPointInfos;
    private boolean instrSpawnedThreads;
    private boolean startProfilingPointsActive;
    private int instrType;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public InitiateProfilingCommand(int instrType, String[] classNames,
                                          int[] ppIDs, String[] ppHandlers, String[] ppInfos,
                                          boolean instrSpawnedThreads, boolean startProfilingPointsActive) {
        super(INITIATE_PROFILING);
        if ((classNames == null)) {
            classNames = new String[] { " " }; // NOI18N
        } else if (classNames[0] == null) {
            classNames[0] = " "; // NOI18N
        }

        this.instrType = instrType;
        this.classNames = classNames;
        profilingPointIDs = ppIDs;
        profilingPointHandlers = ppHandlers;
        profilingPointInfos = ppInfos;
        this.instrSpawnedThreads = instrSpawnedThreads;
        this.startProfilingPointsActive = startProfilingPointsActive;
    }
    
    /** Legacy support for single root instrumentation */
    public InitiateProfilingCommand(int instrType, String className, boolean instrSpawnedThreads,
                                          boolean startProfilingPointsActive) {
        this(instrType,
             className==null ? new String[]{" "} : new String[]{className},
             null,null,null,
             instrSpawnedThreads,startProfilingPointsActive);
    }


    /** This is a special method only called to setup the connection in ProfilerClient.connectToServer() - see comments there */
    public InitiateProfilingCommand(int instrType, String className) {
        this(instrType,className,false,false);
    }

    public InitiateProfilingCommand(int instrType) {
        this(instrType,null);
    }

    // Custom serialzation support
    InitiateProfilingCommand() {
        super(INITIATE_PROFILING);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean getInstrSpawnedThreads() {
        return instrSpawnedThreads;
    }

    public void setInstrType(int t) {
        instrType = t;
    }

    public int getInstrType() {
        return instrType;
    }

    public String[] getProfilingPointHandlers() {
        return profilingPointHandlers;
    }

    public int[] getProfilingPointIDs() {
        return profilingPointIDs;
    }

    public String[] getProfilingPointInfos() {
        return profilingPointInfos;
    }

    public String getRootClassName() {
        return classNames[0];
    } // Legacy support for one root

    public String[] getRootClassNames() {
        return classNames;
    }

    public boolean isStartProfilingPointsActive() {
        return startProfilingPointsActive;
    }

    // for debugging
    public String toString() {
        return super.toString() + ", instrType = " + instrType; // NOI18N
    }

    @Override
    void readObject(DataInputStream in) throws IOException {
        instrType = in.readInt();
        classNames = readStringArr(in, true); // Interning is important, since checks are through '=='
        instrSpawnedThreads = in.readBoolean();
        startProfilingPointsActive = in.readBoolean();

        profilingPointIDs = new int[in.readInt()];
        for (int i = 0; i < profilingPointIDs.length; i++) {
            profilingPointIDs[i] = in.readInt();
        }

        profilingPointHandlers = readStringArr(in, false);
        profilingPointInfos = readStringArr(in, false);
    }

    @Override
    void writeObject(DataOutputStream out) throws IOException {
        out.writeInt(instrType);

        writeStringArr(out, classNames);

        out.writeBoolean(instrSpawnedThreads);
        out.writeBoolean(startProfilingPointsActive);

        out.writeInt(profilingPointIDs.length);
        for (int i = 0; i < profilingPointIDs.length; i++) {
            out.writeInt(profilingPointIDs[i]);

        }

        writeStringArr(out, profilingPointHandlers);
        writeStringArr(out, profilingPointInfos);
    }

    private static void writeStringArr(DataOutputStream out, String[] arr) throws IOException {
        out.writeInt(arr.length);
        for (int i = 0; i < arr.length; i++) {
            out.writeUTF(arr[i]);
        }
    }
    private static String[] readStringArr(DataInputStream in, boolean intern) throws IOException {
        int len = in.readInt();
        String[] arr = new String[len];
        for (int i = 0; i < len; i++) {
            String value = in.readUTF();
            if (intern) {
                value = value.intern();
            }
            arr[i] = value;
        }
        return arr;
    }

}
