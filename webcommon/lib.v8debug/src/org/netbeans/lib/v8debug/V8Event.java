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
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 * An event that occurs in the debugger backend.
 * 
 * @author Martin Entlicher
 */
public final class V8Event extends V8Packet {
    
    public static enum Kind {
        Break,
        Exception,
        AfterCompile,
        ScriptCollected,
        CompileError;       // ES6

        @Override
        public String toString() {
            String str = super.toString();
            return Character.toLowerCase(str.charAt(0)) + str.substring(1);
        }
        
        static Kind fromString(String eventName) {
            eventName = Character.toUpperCase(eventName.charAt(0)) + eventName.substring(1);
            return Kind.valueOf(eventName);
        }
        
    }
    
    private final Kind eventKind;
    private final V8Body body;
    private final ReferencedValue[] referencedValues;
    private Map<Long, V8Value> valuesByReferences;
    private final PropertyBoolean running;
    private final PropertyBoolean success;
    private final String errorMessage;
    
    public V8Event(long sequence, Kind eventKind, V8Body body,
                   ReferencedValue[] referencedValues, Boolean running,
                   Boolean success, String errorMessage) {
        super(sequence, V8Type.event);
        this.eventKind = eventKind;
        this.body = body;
        this.referencedValues = referencedValues;
        this.running = new PropertyBoolean(running);
        this.success = new PropertyBoolean(success);
        this.errorMessage = errorMessage;
    }

    public Kind getKind() {
        return eventKind;
    }

    public V8Body getBody() {
        return body;
    }
    
    public ReferencedValue[] getReferencedValues() {
        return referencedValues;
    }
    
    public V8Value getReferencedValue(long reference) {
        if (referencedValues == null || referencedValues.length == 0) {
            return null;
        }
        synchronized (this) {
            if (valuesByReferences == null) {
                valuesByReferences = V8Response.createValuesByReference(referencedValues);
            }
            return valuesByReferences.get(reference);
        }
    }

    public PropertyBoolean isRunning() {
        return running;
    }

    public PropertyBoolean getSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
