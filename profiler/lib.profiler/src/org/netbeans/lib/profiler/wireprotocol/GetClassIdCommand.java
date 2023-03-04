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
 * Command that is issued by back end when the instance is allocated via reflection
 * and a classId is needed for given class identified by class name and classloader id.
 *
 * It is only used for Memory profiling
 *
 * @author Tomas Hurka
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class GetClassIdCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String className;
    private int classLoaderId;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates new MethodLoadedCommand.
     *
     * @param className name of the class loaded
     * @param classLoaderId id of ClassLoader that loaded the class
     */
    public GetClassIdCommand(String className, int classLoaderId) {
        this();
        this.className = className;

        // At the client side we treat classes loaded by bootstrap and by system classloaders in the same way
        if (classLoaderId == -1) {
            classLoaderId = 0;
        }

        this.classLoaderId = classLoaderId;
    }

    // Custom serialization support
    GetClassIdCommand() {
        super(GET_CLASSID);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * @return id of ClassLoader that loaded the class
     */
    public int getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * @return name of the class loaded
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return Debug println of values
     */
    public String toString() {
        return super.toString() + ", className: " + className // NOI18N
               + ", classLoaderId: " + classLoaderId; // NOI18N
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
