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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


/**
 * This command is issued by the back end to signal that the root instrumentation class has been loaded.
 * It contains the information about all classes loaded by the target JVM by that time.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class RootClassLoadedCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private int[] allLoadedClassLoaderIds;
    private String[] allLoadedClassNames;
    private byte[][] cachedClassFileBytes;
    private int[] allLoadedClassesSuper;
    private int[][] allLoadedClassesInterfaces;
    private int[] parentLoaderIds; // An index into this table is a loader id, and the value at this index is this loader's parent loader id.
    private int classCount;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public RootClassLoadedCommand(String[] allLoadedClassNames, int[] loaderIds, byte[][] cachedClassFileBytes, 
                                int[] loadedClassesSuper, int[][] loadedClassesInterfaces,int classCount,
                                int[] parentLoaderIds) {
        super(ROOT_CLASS_LOADED);
        this.allLoadedClassNames = allLoadedClassNames;
        this.allLoadedClassLoaderIds = loaderIds;
        this.cachedClassFileBytes = cachedClassFileBytes;
        this.allLoadedClassesSuper = loadedClassesSuper;
        this.allLoadedClassesInterfaces = loadedClassesInterfaces;
        this.classCount = classCount;
        this.parentLoaderIds = parentLoaderIds;
    }

    // Custom serialization support
    RootClassLoadedCommand() {
        super(ROOT_CLASS_LOADED);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int[] getAllLoadedClassLoaderIds() {
        return allLoadedClassLoaderIds;
    }

    public String[] getAllLoadedClassNames() {
        return allLoadedClassNames;
    }

    public byte[][] getCachedClassFileBytes() {
        byte[][] res = cachedClassFileBytes;
        cachedClassFileBytes = null; // Save memory

        return res;
    }

    public int[] getAllLoaderSuperClassIds() {
        return allLoadedClassesSuper;
    }

    public int[][] getAllLoadedInterfaceIds() {
        return allLoadedClassesInterfaces;
    }
    
    public int[] getParentLoaderIds() {
        // Return a copy, just in case, since this instance of parentLoaderIds is reused when this command is received
        int[] newParentLoaderIds = new int[parentLoaderIds.length];
        System.arraycopy(parentLoaderIds, 0, newParentLoaderIds, 0, parentLoaderIds.length);

        return newParentLoaderIds;
    }

    // for debugging
    public String toString() {
        return "RootClassLoadedCommand, classes: " + classCount + ", " + super.toString();  // NOI18N
    }

    void readObject(ObjectInputStream gin) throws IOException {
        GZIPInputStream eix = new GZIPInputStream(gin, 32768);
        ObjectInputStream in = new ObjectInputStream(eix);
        byte[] EMPTY = new byte[0];
        classCount = in.readInt();
        allLoadedClassNames = new String[classCount];

        for (int i = 0; i < classCount; i++) {
            allLoadedClassNames[i] = in.readUTF().replace('.', '/').intern();   // NOI18N
        }

        allLoadedClassLoaderIds = new int[classCount];

        for (int i = 0; i < classCount; i++) {
            allLoadedClassLoaderIds[i] = in.readInt();
        }

        int len = in.readInt();

        if (len == 0) {
            cachedClassFileBytes = null;
        } else {
            cachedClassFileBytes = new byte[len][];

            for (int i = 0; i < len; i++) {
                int bytesLen = in.readInt();

                if (bytesLen == -1) {
                    continue;
                }
                if (bytesLen == 0) {
                   cachedClassFileBytes[i] = EMPTY;
                   continue;
                }
                cachedClassFileBytes[i] = new byte[bytesLen];
                in.readFully(cachedClassFileBytes[i]);
            }
        }

        allLoadedClassesSuper = new int[classCount];
        for (int i = 0; i < classCount; i++) {
            allLoadedClassesSuper[i] = in.readInt();
        }

        allLoadedClassesInterfaces = new int[classCount][];
        for (int i = 0; i < classCount; i++) {
            int ilen = in.readInt();
            allLoadedClassesInterfaces[i] = new int[ilen];
            for (int j = 0; j < ilen; j++) {
                allLoadedClassesInterfaces[i][j] = in.readInt();
            }
        }
        
        len = in.readInt();
        parentLoaderIds = new int[len];

        for (int i = 0; i < len; i++) {
            parentLoaderIds[i] = in.readInt();
        }
    }

    void writeObject(ObjectOutputStream gout) throws IOException {
        GZIPOutputStream eox = new GZIPOutputStream(gout, 32768);
        ObjectOutputStream out = new ObjectOutputStream(eox);
        out.writeInt(classCount);

        for (int i = 0; i < classCount; i++) {
            out.writeUTF(allLoadedClassNames[i]);
        }

        for (int i = 0; i < classCount; i++) {
            int loaderId = allLoadedClassLoaderIds[i];

            // At the client side we treat classes loaded by the bootstrap and by the system classloaders in the same way
            if (loaderId == -1) {
                loaderId = 0;
            }

            out.writeInt(loaderId);
        }

        if (cachedClassFileBytes == null) {
            out.writeInt(0);
        } else {
            out.writeInt(classCount);

            for (int i = 0; i < classCount; i++) {
                if (cachedClassFileBytes[i] == null) {
                    out.writeInt(-1);
                } else {
                    out.writeInt(cachedClassFileBytes[i].length);
                    if (cachedClassFileBytes[i].length > 0) {
                        out.write(cachedClassFileBytes[i]);
                    }
                }
            }
        }

        for (int i = 0; i < classCount; i++) {
            out.writeInt(allLoadedClassesSuper[i]);
        }

        for (int i = 0; i < classCount; i++) {
            int[] ifacesIds = allLoadedClassesInterfaces[i];
            if (ifacesIds != null) {
                out.writeInt(ifacesIds.length);
                for (int j = 0; j < ifacesIds.length; j++) {
                    out.writeInt(ifacesIds[j]);
                }
            } else {
                out.writeInt(0);
            } 
        }
        
        out.writeInt(parentLoaderIds.length);

        for (int i = 0; i < parentLoaderIds.length; i++) {
            out.writeInt(parentLoaderIds[i]);
        }
        out.flush();
        eox.finish();

        // Free memory
        allLoadedClassNames = null;
        allLoadedClassLoaderIds = null;
        cachedClassFileBytes = null;
        allLoadedClassesSuper = null;
        allLoadedClassesInterfaces = null;
        parentLoaderIds = null;
    }
}
