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

import org.netbeans.lib.v8debug.vars.ReferencedValue;

/**
 * A command request.
 * 
 * @author Martin Entlicher
 */
public final class V8Request extends V8Packet {
    
    private final V8Command command;
    private final V8Arguments arguments;
    
    public V8Request(long sequence, V8Command command, V8Arguments arguments) {
        super(sequence, V8Type.request);
        this.command = command;
        this.arguments = arguments;
    }

    public V8Command getCommand() {
        return command;
    }

    public V8Arguments getArguments() {
        return arguments;
    }
    
    public V8Response createSuccessResponse(long sequence, V8Body body,
                                            ReferencedValue[] referencedValues,
                                            boolean running) {
        return new V8Response(sequence, getSequence(), command, body,
                              referencedValues, running, true, null);
    }
    
    public V8Response createErrorResponse(long sequence,
                                          boolean running,
                                          String errorMessage) {
        return new V8Response(sequence, getSequence(), command, null,
                              null, running, false, errorMessage);
    }
}
