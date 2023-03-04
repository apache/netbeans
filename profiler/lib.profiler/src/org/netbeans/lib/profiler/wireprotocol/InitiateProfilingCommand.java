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

    void readObject(ObjectInputStream in) throws IOException {
        instrType = in.readInt();

        int len = in.readInt();
        classNames = new String[len];

        for (int i = 0; i < len; i++) {
            classNames[i] = in.readUTF().intern(); // Interning is important, since checks are through '=='
        }

        instrSpawnedThreads = in.readBoolean();
        startProfilingPointsActive = in.readBoolean();

        try {
            profilingPointIDs = (int[]) in.readObject();
            profilingPointHandlers = (String[]) in.readObject();
            profilingPointInfos = (String[]) in.readObject();
        } catch (ClassNotFoundException e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(instrType);
        out.writeInt(classNames.length);

        for (int i = 0; i < classNames.length; i++) {
            out.writeUTF(classNames[i]);
        }

        out.writeBoolean(instrSpawnedThreads);
        out.writeBoolean(startProfilingPointsActive);
        out.writeObject(profilingPointIDs);
        out.writeObject(profilingPointHandlers);
        out.writeObject(profilingPointInfos);
    }
}
