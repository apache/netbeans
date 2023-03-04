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
import org.netbeans.lib.v8debug.V8Breakpoint;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Request;

/**
 *
 * @author Martin Entlicher
 */
public final class SetBreakpoint {
    
    private SetBreakpoint() {}
    
    public static V8Request createRequest(long sequence, V8Breakpoint.Type type,
                                          String target, Long line, Long column) {
        return new V8Request(sequence, V8Command.Setbreakpoint, new Arguments(type, target, line, column, null, null, null, null));
    }
    
    public static V8Request createRequest(long sequence, V8Breakpoint.Type type,
                                          String target, Long line, Long column,
                                          Boolean enabled, String condition, Long ignoreCount, Long groupId) {
        return new V8Request(sequence, V8Command.Setbreakpoint, new Arguments(type, target, line, column, enabled, condition, ignoreCount, groupId));
    }
    
    public static final class Arguments extends V8Arguments {
        
        private final V8Breakpoint.Type type;
        private final String target;
        private final PropertyLong line;
        private final PropertyLong column;
        private final PropertyBoolean enabled;
        private final String condition;
        private final PropertyLong ignoreCount;
        private final PropertyLong groupId;
        
        public Arguments(V8Breakpoint.Type type, String target,
                         Long line, Long column, Boolean enabled,
                         String condition, Long ignoreCount, Long groupId) {
            this.type = type;
            this.target = target;
            this.line = new PropertyLong(line);
            this.column = new PropertyLong(column);
            this.enabled = new PropertyBoolean(enabled);
            this.condition = condition;
            this.ignoreCount = new PropertyLong(ignoreCount);
            this.groupId = new PropertyLong(groupId);
        }

        public V8Breakpoint.Type getType() {
            return type;
        }

        public String getTarget() {
            return target;
        }

        public PropertyLong getLine() {
            return line;
        }

        public PropertyLong getColumn() {
            return column;
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

        public PropertyLong getGroupId() {
            return groupId;
        }
    }
    
    public static final class ResponseBody extends V8Body {
        
        private final V8Breakpoint.Type type;
        private final long breakpoint;
        private final String scriptName;
        private final PropertyLong line;
        private final PropertyLong column;
        private final V8Breakpoint.ActualLocation[] actualLocations;
        
        public ResponseBody(V8Breakpoint.Type type, long breakpoint,
                            String scriptName, Long line, Long column,
                            V8Breakpoint.ActualLocation[] actualLocations) {
            this.type = type;
            this.breakpoint = breakpoint;
            this.scriptName = scriptName;
            this.line = new PropertyLong(line);
            this.column = new PropertyLong(column);
            this.actualLocations = actualLocations;
        }

        public V8Breakpoint.Type getType() {
            return type;
        }

        public long getBreakpoint() {
            return breakpoint;
        }

        public String getScriptName() {
            return scriptName;
        }

        public PropertyLong getLine() {
            return line;
        }

        public PropertyLong getColumn() {
            return column;
        }

        public V8Breakpoint.ActualLocation[] getActualLocations() {
            return actualLocations;
        }
    }
}
