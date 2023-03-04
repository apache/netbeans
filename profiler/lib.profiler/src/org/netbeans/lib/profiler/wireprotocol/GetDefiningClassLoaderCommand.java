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
 * A request to obtain the defining class loader for a given class and its initiating loader,
 * that the client sends to the server.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class GetDefiningClassLoaderCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String className;
    private int classLoaderId;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    // This is to avoid creating a new instance of this class every time a new class is loaded
    public GetDefiningClassLoaderCommand(String className, int classLoaderId) {
        super(GET_DEFINING_CLASS_LOADER);
        this.className = className.replace('/', '.'); // NOI18N
        this.classLoaderId = classLoaderId;
    }

    // Custom serialization support
    GetDefiningClassLoaderCommand() {
        super(GET_DEFINING_CLASS_LOADER);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public int getClassLoaderId() {
        return classLoaderId;
    }

    public String getClassName() {
        return className;
    }

    // For debugging
    public String toString() {
        return super.toString() + ", className: " + className + ", classLoaderId: " + classLoaderId; // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        className = in.readUTF();
        classLoaderId = in.readInt();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(className);
        out.writeInt(classLoaderId);
    }
}
