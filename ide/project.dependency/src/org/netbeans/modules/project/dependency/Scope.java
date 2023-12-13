/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.project.dependency;

import java.util.Objects;

/**
 * Represents "scope" (maven term) or "configuration" (gradle term). Eeach project type
 * may define multiple additional Scopes, but should support the abstract ones defined in
 * {@link Scopes}. 
 * <p/>
 * Scopes are identified by its {@link #name name}; two scopes with the same name are equal.
 * Project implementations may provide their own scopes with standard names since they
 * may use different include/imply hierarchy.
 * 
 * @author sdedic
 */
public abstract class Scope {
    private final String name;

    protected Scope(String name) {
        this.name = name;
    }
    
    /**
     * Checks if this scope includes the other one. If yes, then queries that executed for this scope
     * should return all results from the included scope.
     * 
     * @param s scope to test
     * @return true, if data for scope "s" are included by this scope; false otherwise (i.e. unrelated scopes)
     */
    public abstract boolean includes(Scope s);
    
    /**
     * Checks if this scope exports the other scope. A scope may {@link #includes include} other
     * scope, but can choose not to propagate its contents further.
     * @param s the scope to test
     * @return true, if data for scope "s" are exported by this scope; false otherwise (i.e. unrelated scopes)
     */
    public abstract boolean exports(Scope s);
    
    /**
     * Determines if artifacts in this scope apply to the other one. This is the reverse of {@link includes} and
     * allows injection to existing scopes.
     * 
     * @param s the scope to test.
     * @return true, if the scope 's' is implied (includes) this one.
     */
    public abstract boolean implies(Scope s);
    
    /**
     * @return name / identifier for the scope. Not subject to L10N.
     */
    public String name() {
        return name;
    }

    @Override
    public final int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Scope)) {
            return false;
        }
        final Scope other = (Scope) obj;
        return Objects.equals(this.name, other.name);
    }
}
