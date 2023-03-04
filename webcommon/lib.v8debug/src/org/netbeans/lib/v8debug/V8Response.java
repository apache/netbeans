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

import java.util.HashMap;
import java.util.Map;
import org.netbeans.lib.v8debug.vars.ReferencedValue;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 * A command response.
 * 
 * @author Martin Entlicher
 */
public final class V8Response extends V8Packet {
    
    private final long requestSequence;
    private final V8Command command;
    private final V8Body body;
    private final ReferencedValue[] referencedValues;
    private Map<Long, V8Value> valuesByReferences;
    private final boolean running;
    private final boolean success;
    private final String errorMessage;
    
    V8Response(long sequence, long requestSequence, V8Command command, V8Body body,
               ReferencedValue[] referencedValues, boolean running, boolean success,
               String errorMessage) {
        super(sequence, V8Type.response);
        this.requestSequence = requestSequence;
        this.command = command;
        this.body = body;
        this.referencedValues = referencedValues;
        this.running = running;
        this.success = success;
        this.errorMessage = errorMessage;
    }

    public long getRequestSequence() {
        return requestSequence;
    }

    public V8Command getCommand() {
        return command;
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
                valuesByReferences = createValuesByReference(referencedValues);
            }
            return valuesByReferences.get(reference);
        }
    }
    static Map<Long, V8Value> createValuesByReference(ReferencedValue[] referencedValues) {
        Map<Long, V8Value> valuesByReferences = new HashMap<>();
        for (int i = 0; i < referencedValues.length; i++) {
            valuesByReferences.put(referencedValues[i].getReference(), referencedValues[i].getValue());
        }
        return valuesByReferences;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
    
}
