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
import java.util.Arrays;

/**
 *
 * @author Tomas Hurka
 */
public class GetClassFileBytesCommand extends Command {

    private String[] classes;
    private int[] classLoaderIds;

    public GetClassFileBytesCommand(String[] classes, int[] classLoaderIds) {
        this();
        this.classes = classes;
        this.classLoaderIds = classLoaderIds;
    }

    // Custom serializaion support
    GetClassFileBytesCommand() {
        super(GET_CLASS_FILE_BYTES);
    }
    
    public int[] getClassLoaderIds() {
        return classLoaderIds;
    }

    public String[] getClasses() {
        return classes;
    }
    
    void readObject(ObjectInputStream in) throws IOException {
        int nClasses = in.readInt();

        if (nClasses == 0) {
            return;
        }

        classes = new String[nClasses];
        classLoaderIds = new int[nClasses];

        for (int i = 0; i < nClasses; i++) {
            classes[i] = in.readUTF().replace('/', '.');    // NOI18N
            classLoaderIds[i] = in.readInt();
        }
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        if (classes == null) {
            out.writeInt(0);

            return;
        }

        int nClasses = classes.length;
        out.writeInt(nClasses);

        for (int i = 0; i < nClasses; i++) {
            out.writeUTF(classes[i]);
            out.writeInt(classLoaderIds[i]);
        }

        classes = null;
        classLoaderIds = null;
    }

    public String toString() {
        return super.toString() + " "+classes.length+" classes(): "+Arrays.toString(classes);   // NOI18N
    }
}
