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
package org.netbeans.lib.v8debug.vars;

import java.util.Map;
import org.netbeans.lib.v8debug.PropertyLong;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Generator extends V8Object {
    
    private final PropertyLong functionHandle;
    private final PropertyLong receiverHandle;
    
    public V8Generator(long handle, String className,
                       PropertyLong constructorFunctionHandle,
                       PropertyLong protoObjectHandle,
                       PropertyLong prototypeObjectHandle,
                       PropertyLong functionHandle,
                       PropertyLong receiverHandle,
                       Map<String, Property> properties, String text) {
        super(handle, Type.Generator, className,
              constructorFunctionHandle, protoObjectHandle, prototypeObjectHandle,
              properties, text);
        this.functionHandle = functionHandle;
        this.receiverHandle = receiverHandle;
    }
    
    public PropertyLong getFunctionHandle() {
        return functionHandle;
    }

    public PropertyLong getReceiverHandle() {
        return receiverHandle;
    }

}
