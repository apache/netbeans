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
import org.netbeans.lib.v8debug.PropertyBoolean;
import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Scope;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Function extends V8Object {
    
    private static final String FUNCTION_CLASS_NAME = "Function";       // NOI18N
    
    private final String name;
    private final String inferredName;
    private final PropertyBoolean resolved;
    private final String source;
    private final PropertyLong scriptRef;
    private final PropertyLong scriptId;
    private final PropertyLong position;
    private final PropertyLong line;
    private final PropertyLong column;
    private final V8Scope[] scopes;
    
    public V8Function(long handle, PropertyLong constructorFunctionHandle,
                      PropertyLong protoObjectHandle, PropertyLong prototypeObjectHandle,
                      String name, String inferredName, Boolean resolved,
                      String source, PropertyLong scriptRef, Long scriptId,
                      PropertyLong position, PropertyLong line, PropertyLong column,
                      V8Scope[] scopes, Map<String, Property> properties, String text) {
        super(handle, V8Value.Type.Function, FUNCTION_CLASS_NAME,
              constructorFunctionHandle, protoObjectHandle, prototypeObjectHandle,
              properties, text);
        this.name = name;
        this.inferredName = inferredName;
        this.resolved = new PropertyBoolean(resolved);
        this.source = source;
        this.scriptRef = scriptRef;
        this.scriptId = new PropertyLong(scriptId);
        this.position = position;
        this.line = line;
        this.column = column;
        this.scopes = scopes;
    }

    public String getName() {
        return name;
    }

    public String getInferredName() {
        return inferredName;
    }
    
    public PropertyBoolean isResolved() {
        return resolved;
    }

    public String getSource() {
        return source;
    }

    public PropertyLong getScriptRef() {
        return scriptRef;
    }

    public PropertyLong getScriptId() {
        return scriptId;
    }

    public PropertyLong getPosition() {
        return position;
    }

    public PropertyLong getLine() {
        return line;
    }

    public PropertyLong getColumn() {
        return column;
    }
    
    public V8Scope[] getScopes() {
        return scopes;
    }
    
}
