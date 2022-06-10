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

package org.netbeans.modules.javascript.cdtdebug.vars;

import java.util.Objects;
import org.netbeans.lib.chrome_devtools_protocol.debugger.Scope;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;


public class Variable {

    private final Scope scope;
    private final String name;
    private final String parentPath;
    private RemoteObject value;

    public Variable(String name, RemoteObject value) {
        this.scope = null;
        this.parentPath = null;
        this.name = name;
        this.value = value;
    }

    public Variable(Scope scope, String name, String parentPath, RemoteObject value) {
        this.scope = scope;
        this.name = name;
        this.parentPath = parentPath;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public RemoteObject getValue() {
        return value;
    }

    public Scope getScope() {
        return scope;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setValue(RemoteObject value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.scope);
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.parentPath);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
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
        if (!Objects.equals(this.parentPath, other.parentPath)) {
            return false;
        }
        return Objects.equals(this.scope, other.scope);
    }

    @Override
    public String toString() {
        return "Variable{" + "scope=" + scope.getType() + "/" + scope.getName() + ", name=" + name + ", parentPath=" + parentPath + '}';
    }

}
