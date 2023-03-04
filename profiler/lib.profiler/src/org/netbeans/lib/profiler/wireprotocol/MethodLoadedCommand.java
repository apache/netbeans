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
 * Command that is issued by back end when the option to instrument methods invoked via reflection is on,
 * and a given method is just about to be executed in this way.
 *
 * It is only used for CPU profiling, when Eager or Lazy schemes are used (RecursiveMethodInstrumentor1 or
 * RecursiveMethodInstrumentor2). In total scheme, everything is handle by ClassLoadedCommand.
 *
 * @author Misha Dmitriev
 * @author Ian Formanek
 */
public class MethodLoadedCommand extends Command {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private String className;
    private String methodName;
    private String methodSignature;
    private int classLoaderId;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    /** Creates new MethodLoadedCommand.
     *
     * @param className name fo the class loaded
     * @param classLoaderId id of ClassLoader that loaded the class
     * @param methodName Name of method that is going to be invoked
     * @param methodSignature Signature of method that is going to be invoked
     */
    public MethodLoadedCommand(String className, int classLoaderId, String methodName, String methodSignature) {
        super(METHOD_LOADED);
        this.className = className;

        // At the client side we treat classes loaded by bootstrap and by system classloaders in the same way
        if (classLoaderId == -1) {
            classLoaderId = 0;
        }

        this.classLoaderId = classLoaderId;
        this.methodName = methodName;
        this.methodSignature = methodSignature;
    }

    // Custom serialization support
    MethodLoadedCommand() {
        super(METHOD_LOADED);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * @return id of ClassLoader that loaded the class
     */
    public int getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * @return name fo the class loaded
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return Name of method that is going to be invoked
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return Signature of method that is going to be invoked
     */
    public String getMethodSignature() {
        return methodSignature;
    }

    /**
     * @return Debug println of values
     */
    public String toString() {
        return super.toString() + ", className: " + className // NOI18N
               + ", classLoaderId: " + classLoaderId // NOI18N
               + ", methodName: " + methodName // NOI18N
               + ", methodSignature: " + methodSignature; // NOI18N
    }

    void readObject(ObjectInputStream in) throws IOException {
        className = in.readUTF();
        classLoaderId = in.readInt();
        methodName = in.readUTF();
        methodSignature = in.readUTF();
    }

    void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(className);
        out.writeInt(classLoaderId);
        out.writeUTF(methodName);
        out.writeUTF(methodSignature);
    }
}
