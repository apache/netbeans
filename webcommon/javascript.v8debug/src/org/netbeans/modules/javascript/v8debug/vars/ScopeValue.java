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

package org.netbeans.modules.javascript.v8debug.vars;

import org.netbeans.lib.v8debug.V8Scope;
import org.netbeans.lib.v8debug.vars.V8Object;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin
 */
public class ScopeValue {
    
    private final V8Scope scope;
    private V8Object value;
    
    public ScopeValue(V8Scope scope) {
        this.scope = scope;
    }
    
    public ScopeValue(V8Scope scope, V8Object value) {
        this.scope = scope;
        this.value = value;
    }

    public V8Scope getScope() {
        return scope;
    }

    public V8Object getValue() {
        return value;
    }

    public void setValue(V8Object value) {
        this.value = value;
    }

}
