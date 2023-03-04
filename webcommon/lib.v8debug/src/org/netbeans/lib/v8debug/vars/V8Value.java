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
package org.netbeans.lib.v8debug.vars;

/**
 *
 * @author Martin Entlicher
 */
public class V8Value {

    public enum Type {

        Undefined,
        Null,
        Boolean,
        Number,
        String,
        Object,
        Function,
        Frame,
        Script,
        Context,
        Error,
        Regexp,
        Symbol,     // ES6
        Promise,    // ES6
        Map,        // ES6
        Set,        // ES6
        Generator;  // ES6

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }

        public static Type fromString(String typeName) {
            typeName = Character.toUpperCase(typeName.charAt(0)) + typeName.substring(1);
            return Type.valueOf(typeName);
        }

    }
    
    private final long handle;
    private final Type type;
    private final String text;

    public V8Value(long handle, Type type, String text) {
        this.handle = handle;
        this.type = type;
        this.text = text;
    }

    public long getHandle() {
        return handle;
    }

    public Type getType() {
        return type;
    }

    public String getText() {
        return text;
    }
}
