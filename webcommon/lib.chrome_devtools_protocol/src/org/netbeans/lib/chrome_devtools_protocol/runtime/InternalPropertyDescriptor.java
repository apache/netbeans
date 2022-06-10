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
package org.netbeans.lib.chrome_devtools_protocol.runtime;

import java.util.Objects;

/**
 * Object internal property descriptor. This property isn't normally visible in
 * JavaScript code.
 */
public class InternalPropertyDescriptor {
    private String name;
    private RemoteObject value;

    public InternalPropertyDescriptor() {
    }

    /**
     * Conventional property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Conventional property name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value associated with the property.
     */
    public RemoteObject getValue() {
        return value;
    }

    /**
     * The value associated with the property.
     */
    public void setValue(RemoteObject value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "InternalPropertyDescriptor{" + "name=" + name + ", value=" + value + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.name);
        hash = 59 * hash + Objects.hashCode(this.value);
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
        final InternalPropertyDescriptor other = (InternalPropertyDescriptor) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }


}
