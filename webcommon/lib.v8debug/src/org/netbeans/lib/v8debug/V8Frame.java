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
package org.netbeans.lib.v8debug;

import java.util.Map;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Function;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Frame {
    
    private final PropertyLong index;
    private final ReferencedValue receiver;
    private final ReferencedValue<V8Function> func;
    private final long scriptRef;
    private final boolean constructCall;
    private final boolean atReturn;
    private final boolean debuggerFrame;
    private final Map<String, ReferencedValue> argumentRefs;
    private final Map<String, ReferencedValue> localRefs;
    private final long position;
    private final long line;
    private final long column;
    private final String sourceLineText;
    private final V8Scope[] scopes;
    private final String text;
    
    public V8Frame(Long index, ReferencedValue receiver, ReferencedValue<V8Function> func, long scriptRef,
                   boolean constructCall, boolean atReturn, boolean debuggerFrame,
                   Map<String, ReferencedValue> argumentRefs,
                   Map<String, ReferencedValue> localRefs,
                   long position, long line, long column, String sourceLineText,
                   V8Scope[] scopes, String text) {
        this.index = new PropertyLong(index);
        this.receiver = receiver;
        this.func = func;
        this.scriptRef = scriptRef;
        this.constructCall = constructCall;
        this.atReturn = atReturn;
        this.debuggerFrame = debuggerFrame;
        this.argumentRefs = argumentRefs;
        this.localRefs = localRefs;
        this.position = position;
        this.line = line;
        this.column = column;
        this.sourceLineText = sourceLineText;
        this.scopes = scopes;
        this.text = text;
    }

    public PropertyLong getIndex() {
        return index;
    }

    public ReferencedValue getReceiver() {
        return receiver;
    }

    public ReferencedValue<V8Function> getFunction() {
        return func;
    }

    public long getScriptRef() {
        return scriptRef;
    }

    public boolean isConstructCall() {
        return constructCall;
    }

    public boolean isAtReturn() {
        return atReturn;
    }

    public boolean isDebuggerFrame() {
        return debuggerFrame;
    }

    public Map<String, ReferencedValue> getArgumentRefs() {
        return argumentRefs;
    }

    public Map<String, ReferencedValue> getLocalRefs() {
        return localRefs;
    }

    public long getPosition() {
        return position;
    }

    public long getLine() {
        return line;
    }

    public long getColumn() {
        return column;
    }

    public String getSourceLineText() {
        return sourceLineText;
    }

    public V8Scope[] getScopes() {
        return scopes;
    }
    
    public String getText() {
        return text;
    }
}
