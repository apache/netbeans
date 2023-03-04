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

/**
 *
 * @author Martin Entlicher
 */
public final class ChangeBreakpoint {
    
    private ChangeBreakpoint() {}
    
    public static V8Request createRequest(long sequence, long breakpoint,
                                          Boolean enabled, String condition,
                                          Long ignoreCount) {
        return new V8Request(sequence, V8Command.Changebreakpoint, new Arguments(breakpoint, enabled, condition, ignoreCount));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final long breakpoint;
        private final PropertyBoolean enabled;
        private final String condition;
        private final PropertyLong ignoreCount;
        
        public Arguments(long breakpoint, Boolean enabled, String condition, Long ignoreCount) {
            this.breakpoint = breakpoint;
            this.enabled = new PropertyBoolean(enabled);
            this.condition = condition;
            this.ignoreCount = new PropertyLong(ignoreCount);
        }

        public long getBreakpoint() {
            return breakpoint;
        }

        public PropertyBoolean isEnabled() {
            return enabled;
        }

        public String getCondition() {
            return condition;
        }

        public PropertyLong getIgnoreCount() {
            return ignoreCount;
        }
    }
    
}
