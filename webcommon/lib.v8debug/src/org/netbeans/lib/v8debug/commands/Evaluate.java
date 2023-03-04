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

import org.netbeans.lib.v8debug.PropertyBoolean;
import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Body;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
public final class Evaluate {
    
    private Evaluate() {}
    
    public static V8Request createRequest(long sequence, String expression) {
        return new V8Request(sequence, V8Command.Evaluate, new Arguments(expression));
    }
    
    public static V8Request createRequest(long sequence, String expression,
                                          Long frame, Boolean global,
                                          Boolean disableBreak,
                                          Arguments.Context[] additionalContext) {
        return new V8Request(sequence, V8Command.Evaluate,
                             new Arguments(expression, frame, global, disableBreak,
                                           additionalContext));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final String expression;
        private final PropertyLong frame;
        private final PropertyBoolean global;
        private final PropertyBoolean disableBreak;
        private final Context[] additionalContext;
        
        public Arguments(String expression) {
            this(expression, null, null, null, null);
        }
        
        public Arguments(String expression, Long frame, Boolean global,
                         Boolean disableBreak, Context[] additionalContext) {
            this.expression = expression;
            this.frame = new PropertyLong(frame);
            this.global = new PropertyBoolean(global);
            this.disableBreak = new PropertyBoolean(disableBreak);
            this.additionalContext = additionalContext;
        }

        public String getExpression() {
            return expression;
        }

        public PropertyLong getFrame() {
            return frame;
        }

        public PropertyBoolean isGlobal() {
            return global;
        }

        public PropertyBoolean isDisableBreak() {
            return disableBreak;
        }

        public Context[] getAdditionalContext() {
            return additionalContext;
        }
        
        public static final class Context {
            
            private final String name;
            private final long handle;
            
            public Context(String name, long handle) {
                this.name = name;
                this.handle = handle;
            }

            public String getName() {
                return name;
            }

            public long getHandle() {
                return handle;
            }
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final V8Value value;
        
        public ResponseBody(V8Value value) {
            this.value = value;
        }

        public V8Value getValue() {
            return value;
        }
    }
    
}
