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
 * Object property descriptor.
 */
public class PropertyDescriptor {
    private String name;
    private RemoteObject value;
    private Boolean writeable;
    private RemoteObject get;
    private RemoteObject set;
    private boolean configurable;
    private boolean enumerable;
    private Boolean wasThrown;
    private Boolean isOwn;
    private RemoteObject symbol;

    public PropertyDescriptor() {
    }

    /**
     * Property name or symbol description.
     */
    public String getName() {
        return name;
    }

    /**
     * Property name or symbol description.
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

    /**
     * True if the value associated with the property may be changed (data
     * descriptors only)
     */
    public Boolean getWriteable() {
        return writeable;
    }

    /**
     * True if the value associated with the property may be changed (data
     * descriptors only)
     */
    public void setWriteable(Boolean writeable) {
        this.writeable = writeable;
    }

    /**
     * A function which serves as a get for the property, or undefined if there
     * is no get (accessor descriptors only).
     */
    public RemoteObject getGet() {
        return get;
    }

    /**
     * A function which serves as a get for the property, or undefined if there
     * is no get (accessor descriptors only).
     */
    public void setGet(RemoteObject get) {
        this.get = get;
    }

    /**
     * A function which serves as a set for the property, or undefined if there
     * is no set (accessor descriptors only).
     */
    public RemoteObject getSet() {
        return set;
    }

    /**
     * A function which serves as a set for the property, or undefined if there
     * is no set (accessor descriptors only).
     */
    public void setSet(RemoteObject set) {
        this.set = set;
    }

    /**
     * True if the type of this property descriptor may be changed and if the
     * property may be deleted from the corresponding object.
     */
    public boolean isConfigurable() {
        return configurable;
    }

    /**
     * True if the type of this property descriptor may be changed and if the
     * property may be deleted from the corresponding object.
     */
    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }

    /**
     * True if this property shows up during enumeration of the properties on
     * the corresponding object.
     */
    public boolean isEnumerable() {
        return enumerable;
    }

    /**
     * True if this property shows up during enumeration of the properties on
     * the corresponding object.
     */
    public void setEnumerable(boolean enumerable) {
        this.enumerable = enumerable;
    }

    /**
     * True if the result was thrown during the evaluation.
     */
    public Boolean getWasThrown() {
        return wasThrown;
    }

    /**
     * True if the result was thrown during the evaluation.
     */
    public void setWasThrown(Boolean wasThrown) {
        this.wasThrown = wasThrown;
    }

    /**
     * True if the property is owned for the object.
     */
    public Boolean getIsOwn() {
        return isOwn;
    }

    /**
     * True if the property is owned for the object.
     */
    public void setIsOwn(Boolean isOwn) {
        this.isOwn = isOwn;
    }

    /**
     * Property symbol object, if the property is of the symbol type.
     */
    public RemoteObject getSymbol() {
        return symbol;
    }

    /**
     * Property symbol object, if the property is of the symbol type.
     */
    public void setSymbol(RemoteObject symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return "PropertyDescriptor{" + "name=" + name + ", value=" + value + ", writeable=" + writeable + ", getter=" + get + ", setter=" + set + ", configurable=" + configurable + ", enumerable=" + enumerable + ", wasThrown=" + wasThrown + ", isOwn=" + isOwn + ", symbol=" + symbol + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.name);
        hash = 97 * hash + Objects.hashCode(this.value);
        hash = 97 * hash + Objects.hashCode(this.writeable);
        hash = 97 * hash + Objects.hashCode(this.get);
        hash = 97 * hash + Objects.hashCode(this.set);
        hash = 97 * hash + (this.configurable ? 1 : 0);
        hash = 97 * hash + (this.enumerable ? 1 : 0);
        hash = 97 * hash + Objects.hashCode(this.wasThrown);
        hash = 97 * hash + Objects.hashCode(this.isOwn);
        hash = 97 * hash + Objects.hashCode(this.symbol);
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
        final PropertyDescriptor other = (PropertyDescriptor) obj;
        if (this.configurable != other.configurable) {
            return false;
        }
        if (this.enumerable != other.enumerable) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.writeable, other.writeable)) {
            return false;
        }
        if (!Objects.equals(this.get, other.get)) {
            return false;
        }
        if (!Objects.equals(this.set, other.set)) {
            return false;
        }
        if (!Objects.equals(this.wasThrown, other.wasThrown)) {
            return false;
        }
        if (!Objects.equals(this.isOwn, other.isOwn)) {
            return false;
        }
        return Objects.equals(this.symbol, other.symbol);
    }

    
}
