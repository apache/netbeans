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
package org.netbeans.lib.chrome_devtools_protocol.debugger;

import java.util.Objects;
import org.netbeans.lib.chrome_devtools_protocol.runtime.RemoteObject;

/**
 * Scope description.
 */
public final class Scope {

    private String type;
    private RemoteObject object;
    private String name;
    private Location startLocation;
    private Location endLocation;

    public Scope() {
    }

    /**
     * Scope type.<br />
     * Allowed Values: {@code global, local, with, closure, catch, block,
     * script, eval, module, wasm-expression-stack}
     */
    public String getType() {
        return type;
    }

    /**
     * Scope type.<br />
     * Allowed Values: {@code global, local, with, closure, catch, block,
     * script, eval, module, wasm-expression-stack}
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Object representing the scope. For global and with scopes it represents
     * the actual object; for the rest of the scopes, it is artificial transient
     * object enumerating scope variables as its properties.
     */
    public RemoteObject getObject() {
        return object;
    }

    /**
     * Object representing the scope. For global and with scopes it represents
     * the actual object; for the rest of the scopes, it is artificial transient
     * object enumerating scope variables as its properties.
     */
    public void setObject(RemoteObject object) {
        this.object = object;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Location in the source code where scope starts
     */
    public Location getStartLocation() {
        return startLocation;
    }

    /**
     * Location in the source code where scope starts
     */
    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    /**
     * Location in the source code where scope ends.
     */
    public Location getEndLocation() {
        return endLocation;
    }

    /**
     * Location in the source code where scope ends.
     */
    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    @Override
    public String toString() {
        return "Scope{" + "type=" + type + ", object=" + object + ", name=" + name + ", startLocation=" + startLocation + ", endLocation=" + endLocation + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.type);
        hash = 17 * hash + Objects.hashCode(this.object);
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + Objects.hashCode(this.startLocation);
        hash = 17 * hash + Objects.hashCode(this.endLocation);
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
        final Scope other = (Scope) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.object, other.object)) {
            return false;
        }
        if (!Objects.equals(this.startLocation, other.startLocation)) {
            return false;
        }
        return Objects.equals(this.endLocation, other.endLocation);
    }


}
