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
package org.netbeans.lib.v8debug.commands;

import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.vars.NewValue;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
public final class SetVariableValue {
    
    private SetVariableValue() {}
    
    public static V8Request createRequest(long sequence, String name, NewValue newValue, long scopeNumber) {
        return createRequest(sequence, name, newValue, scopeNumber, null);
    }
    
    public static V8Request createRequest(long sequence, String name, NewValue newValue, long scopeNumber, Long scopeFrameNumber) {
        return new V8Request(sequence, V8Command.SetVariableValue, new Arguments(name, newValue, scopeNumber, scopeFrameNumber));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final String name;
        private final NewValue newValue;
        private final long scopeNumber;
        private final PropertyLong scopeFrameNumber;
        
        public Arguments(String name, NewValue newValue, long scopeNumber, Long scopeFrameNumber) {
            this.name = name;
            this.newValue = newValue;
            this.scopeNumber = scopeNumber;
            this.scopeFrameNumber = new PropertyLong(scopeFrameNumber);
        }

        public String getName() {
            return name;
        }
        
        public NewValue getNewValue() {
            return newValue;
        }

        public long getScopeNumber() {
            return scopeNumber;
        }

        public PropertyLong getScopeFrameNumber() {
            return scopeFrameNumber;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final V8Value newValue;
        
        public ResponseBody(V8Value newValue) {
            this.newValue = newValue;
        }

        public V8Value getNewValue() {
            return newValue;
        }
    }
}
