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
/*
 * Contributor(s): Thomas Ball
 */

package org.netbeans.modules.classfile;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * An InnerClass attribute of a classfile.
 *
 * @author  Thomas Ball
 * @since 1.40
 */
public final class BootstrapMethod {

    int methodRef;
    int[] arguments;

    static BootstrapMethod[] loadBootstrapMethod(DataInputStream in, ConstantPool pool)
      throws IOException {
        int n = in.readUnsignedShort();
        BootstrapMethod[] innerClasses = new BootstrapMethod[n];
        for (int i = 0; i < n; i++)
            innerClasses[i] = new BootstrapMethod(in, pool);
        return innerClasses;
    }

    BootstrapMethod(DataInputStream in, ConstantPool pool) throws IOException {
        this.methodRef = in.readUnsignedShort();
        int args = in.readUnsignedShort();
        arguments = new int[args];
        for (int i = 0; i < args; i++) {
            arguments[i] = in.readUnsignedShort();
        }
    }

    public int getMethodRef() {
        return methodRef;
    }

    public int[] getArguments() {
        return arguments.clone();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("bootstrapmethod=");
        sb.append(methodRef);
        sb.append("(");
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(arguments[i]);
        }
        sb.append(")");
        return sb.toString();
    }
}
