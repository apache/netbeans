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
 * Object private field descriptor.
 */
public class PrivatePropertyDescriptor {

    private String name;
    private RemoteObject value;
    private RemoteObject get;
    private RemoteObject set;

    public PrivatePropertyDescriptor() {
    }

    /**
     * Private property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Private property name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The value associated with the private property.
     */
    public RemoteObject getValue() {
        return value;
    }

    /**
     * The value associated with the private property.
     */
    public void setValue(RemoteObject value) {
        this.value = value;
    }

    /**
     * A function which serves as a getter for the private property, or
     * undefined if there is no getter (accessor descriptors only).
     */
    public RemoteObject getGet() {
        return get;
    }

    /**
     * A function which serves as a getter for the private property, or
     * undefined if there is no getter (accessor descriptors only).
     */
    public void setGet(RemoteObject get) {
        this.get = get;
    }

    /**
     * A function which serves as a setter for the private property, or
     * undefined if there is no setter (accessor descriptors only).
     */
    public RemoteObject getSet() {
        return set;
    }

    /**
     * A function which serves as a setter for the private property, or
     * undefined if there is no setter (accessor descriptors only).
     */
    public void setSet(RemoteObject set) {
        this.set = set;
    }

    @Override
    public String toString() {
        return "PrivatePropertyDescriptor{" + "name=" + name + ", value=" + value + ", get=" + get + ", set=" + set + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + Objects.hashCode(this.value);
        hash = 17 * hash + Objects.hashCode(this.get);
        hash = 17 * hash + Objects.hashCode(this.set);
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
        final PrivatePropertyDescriptor other = (PrivatePropertyDescriptor) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.get, other.get)) {
            return false;
        }
        return Objects.equals(this.set, other.set);
    }


}
