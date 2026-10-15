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

public final class PropertyPreview {
    private String name;
    private String type;
    private String subtype;
    private String value;
    private ObjectPreview valuePreview;

    public PropertyPreview() {
    }

    /**
     * Property name.
     */
    public String getName() {
        return name;
    }

    /**
     * @see #getName()
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Object type. Accessor means that the property itself is an accessor
     * property.<br />
     * Allowed Values: {@code object, function, undefined, string, number,
     * boolean, symbol, accessor, bigint}
     */
    public String getType() {
        return type;
    }

    /**
     * Object type. Accessor means that the property itself is an accessor
     * property.<br />
     * Allowed Values: {@code object, function, undefined, string, number,
     * boolean, symbol, accessor, bigint}
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Object subtype hint. Specified for object type values only.<br />
     * Allowed Values: {@code array, null, node, regexp, date, map, set,
     * weakmap, weakset, iterator, generator, error, proxy, promise, typedarray,
     * arraybuffer, dataview, webassemblymemory, wasmvalue}
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Object subtype hint. Specified for object type values only.<br />
     * Allowed Values: {@code array, null, node, regexp, date, map, set,
     * weakmap, weakset, iterator, generator, error, proxy, promise, typedarray,
     * arraybuffer, dataview, webassemblymemory, wasmvalue}
     */
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    /**
     * User-friendly property value string.
     */
    public String getValue() {
        return value;
    }

    /**
     * User-friendly property value string.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Nested value preview.
     */
    public ObjectPreview getValuePreview() {
        return valuePreview;
    }

    /**
     * Nested value preview.
     */
    public void setValuePreview(ObjectPreview valuePreview) {
        this.valuePreview = valuePreview;
    }

    @Override
    public String toString() {
        return "PropertyPreview{" + "name=" + name + ", type=" + type + ", subtype=" + subtype + ", value=" + value + ", valuePreview=" + valuePreview + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.name);
        hash = 37 * hash + Objects.hashCode(this.type);
        hash = 37 * hash + Objects.hashCode(this.subtype);
        hash = 37 * hash + Objects.hashCode(this.value);
        hash = 37 * hash + Objects.hashCode(this.valuePreview);
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
        final PropertyPreview other = (PropertyPreview) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.subtype, other.subtype)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return Objects.equals(this.valuePreview, other.valuePreview);
    }
}
