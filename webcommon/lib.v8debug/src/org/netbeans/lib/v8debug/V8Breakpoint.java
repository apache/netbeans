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

/**
 * A breakpoint.
 * 
 * @author Martin Entlicher
 */
public final class V8Breakpoint {
    
    /**
     * The breakpoint type.
     */
    public static enum Type {
    
        function,
        scriptName,
        scriptId,
        scriptRegExp;
        
    }
    
    private final Type type;
    private final PropertyLong scriptId;
    private final String scriptName;
    private final long number;
    private final PropertyLong line;
    private final PropertyLong column;
    private final PropertyLong groupId;
    private final long hitCount;
    private final boolean active;
    private final String condition;
    private final long ignoreCount;
    private final ActualLocation[] actualLocations;
    
    public V8Breakpoint(Type type, PropertyLong scriptId, String scriptName,
                        long number, PropertyLong line, PropertyLong column,
                        PropertyLong groupId,
                        long hitCount, boolean active,
                        String condition, long ignoreCount,
                        ActualLocation[] actualLocations) {
        this.type = type;
        this.scriptId = scriptId;
        this.scriptName = scriptName;
        this.number = number;
        this.line = line;
        this.column = column;
        this.groupId = groupId;
        this.hitCount = hitCount;
        this.active = active;
        this.condition = condition;
        this.ignoreCount = ignoreCount;
        this.actualLocations = actualLocations;
    }

    public Type getType() {
        return type;
    }

    public PropertyLong getScriptId() {
        return scriptId;
    }

    public String getScriptName() {
        return scriptName;
    }

    public long getNumber() {
        return number;
    }

    public PropertyLong getLine() {
        return line;
    }

    public PropertyLong getColumn() {
        return column;
    }

    public PropertyLong getGroupId() {
        return groupId;
    }

    public long getHitCount() {
        return hitCount;
    }

    public boolean isActive() {
        return active;
    }
    
    public String getCondition() {
        return condition;
    }

    public long getIgnoreCount() {
        return ignoreCount;
    }

    public ActualLocation[] getActualLocations() {
        return actualLocations;
    }
    
    public static final class ActualLocation {
        
        private final long line;
        private final long column;
        private final PropertyLong scriptId;
        private final String scriptName;
        
        public ActualLocation(long line, long column, long scriptId) {
            this.line = line;
            this.column = column;
            this.scriptId = new PropertyLong(scriptId);
            this.scriptName = null;
        }
        
        public ActualLocation(long line, long column, String scriptName) {
            this.line = line;
            this.column = column;
            this.scriptId = new PropertyLong(null);
            this.scriptName = scriptName;
        }

        public long getLine() {
            return line;
        }

        public long getColumn() {
            return column;
        }

        public PropertyLong getScriptId() {
            return scriptId;
        }

        public String getScriptName() {
            return scriptName;
        }
    }
}
