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

import org.netbeans.lib.v8debug.PropertyLong;
import org.netbeans.lib.v8debug.V8Command;

/**
 * A new value for {@link V8Command#SetVariableValue} command.
 * 
 * @author Martin Entlicher
 */
public class NewValue {
    
    private final PropertyLong handle;
    private final V8Value.Type type;
    private final String description;
    
    public NewValue(long handle) {
        this.handle = new PropertyLong(handle);
        this.type = null;
        this.description = null;
    }
    
    public NewValue(V8Value.Type type, String description) {
        this.handle = new PropertyLong(null);
        this.type = type;
        this.description = description;
    }

    public PropertyLong getHandle() {
        return handle;
    }

    public V8Value.Type getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }
}
