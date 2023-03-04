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
import org.netbeans.lib.v8debug.vars.V8Object;

/**
 *
 * @author Martin Entlicher
 */
public final class V8Scope {
    
    public enum Type {
        Global,
        Local,
        With,
        Closure,
        Catch,
        Block,
        Module;     // ES6
        
        public static Type valueOf(int i) {
            for (Type t : values()) {
                if (t.ordinal() == i) {
                    return t;
                }
            }
            return null;
        }
    }
    
    private final long index;
    private final PropertyLong frameIndex;
    private final Type type;
    private final ReferencedValue<V8Object> object;
    private final String text;
    
    public V8Scope(long index, PropertyLong frameIndex, Type type, ReferencedValue<V8Object> object, String text) {
        this.index = index;
        this.frameIndex = frameIndex;
        this.type = type;
        this.object = object;
        this.text = text;
    }

    public long getIndex() {
        return index;
    }

    public PropertyLong getFrameIndex() {
        return frameIndex;
    }

    public Type getType() {
        return type;
    }

    public ReferencedValue<V8Object> getObject() {
        return object;
    }

    public String getText() {
        return text;
    }
}
