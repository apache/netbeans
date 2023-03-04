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
 * Notification about a class load event that the server sends to the client.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class ClassLoadedCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String className;
    private byte[] classFileBytes;
    private int[] thisAndParentLoaderData;
    private boolean threadInCallGraph;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ClassLoadedCommand(String className, int[] thisAndParentLoaderData, byte[] classFileBytes, boolean threadInCallGraph) {
        super(CLASS_LOADED);
        this.className = className;
        this.thisAndParentLoaderData = thisAndParentLoaderData;
        this.classFileBytes = classFileBytes;
        this.threadInCallGraph = threadInCallGraph;
    }

    // Custom serialization support
    ClassLoadedCommand() {
        super(CLASS_LOADED);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public byte[] getClassFileBytes() {
        return classFileBytes;
    }

    public String getClassName() {
        return className;
    }

    public int[] getThisAndParentLoaderData() {
        return thisAndParentLoaderData;
    }

    public boolean getThreadInCallGraph() {
        return threadInCallGraph;
    }

    // for debugging
    public String toString() {
        return super.toString() + ", className: " + className // NOI18N
               + ", threadInCallGraph: " + threadInCallGraph // NOI18N
               + ", thisAndParentLoaderData: " // NOI18N
               + thisAndParentLoaderData[0] + ", " // NOI18N
               + thisAndParentLoaderData[1] + ", " // NOI18N
               + thisAndParentLoaderData[2] + ", classFileBytes: "
               + ((classFileBytes == null) ? "null" : ("" + classFileBytes.length)); // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        className = in.readUTF();
        thisAndParentLoaderData = new int[3];

        for (int i = 0; i < 3; i++) {
            thisAndParentLoaderData[i] = in.readInt();
        }

        int len = in.readInt();

        if (len == 0) {
            classFileBytes = null;
        } else {
            classFileBytes = new byte[len];
            in.readFully(classFileBytes);
        }

        threadInCallGraph = in.readBoolean();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(className);

        for (int i = 0; i < 3; i++) {
            out.writeInt(thisAndParentLoaderData[i]);
        }

        if (classFileBytes != null) {
            out.writeInt(classFileBytes.length);
            out.write(classFileBytes);
            classFileBytes = null;
        } else {
            out.writeInt(0);
        }

        out.writeBoolean(threadInCallGraph);
    }
}
