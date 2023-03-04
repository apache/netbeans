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

import java.util.Objects;
import org.netbeans.lib.v8debug.V8Scope;
import org.netbeans.lib.v8debug.vars.V8Value;

/**
 *
 * @author Martin Entlicher
 */
public class Variable {
    
    public static enum Kind {
        ARGUMENT,
        LOCAL,
        PROPERTY,
        ARRAY_ELEMENT
    }
    
    private final Kind kind;
    private final String name;
    private final long ref;
    private V8Value value;
    private final V8Scope scope;
    private String valueLoadError;
    private boolean hasIncompleteValue;
    
    public Variable(Kind kind, String name, long ref, V8Value value, boolean incompleteValue) {
        this(kind, name, ref, value, incompleteValue, null);
    }
    
    public Variable(Kind kind, String name, long ref, V8Value value, boolean incompleteValue,
                    V8Scope scope) {
        this.kind = kind;
        this.name = name;
        this.ref = ref;
        this.value = value;
        this.scope = scope;
        this.hasIncompleteValue = incompleteValue;
    }
    
    public boolean hasIncompleteValue() {
        return hasIncompleteValue;
    }
    
    public Kind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public long getRef() {
        return ref;
    }

    public V8Value getValue() throws EvaluationError {
        if (valueLoadError != null) {
            throw new EvaluationError(valueLoadError);
        }
        return value;
    }
    
    void setValue(V8Value value) {
        this.value = value;
        this.hasIncompleteValue = false;
    }
    
    void setValueLoadError(String valueLoadError) {
        this.valueLoadError = valueLoadError;
    }
    
    /** Variable's scope or <code>null</code>. */
    public V8Scope getScope() {
        return scope;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.name);
        hash = 67 * hash + (int) (this.ref ^ (this.ref >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Variable other = (Variable) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (this.ref != other.ref) {
            return false;
        }
        return true;
    }
    
    // TODO: Attach referenced values?
}
