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

import java.util.List;
import java.util.Objects;

public final class ObjectPreview {
    private String type;
    private String subtype;
    private String description;
    private boolean overflow;
    private List<PropertyPreview> properties;
    private List<EntryPreview> entries;

    public ObjectPreview() {
    }

    /**
     * Object type.<br />
     * Allowed Values: {@code object, function, undefined, string, number,
     * boolean, symbol, bigint}
     */
    public String getType() {
        return type;
    }

    /**
     * Object type.<br />
     * Allowed Values: {@code object, function, undefined, string, number,
     * boolean, symbol, bigint}
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Object subtype hint. Specified for object type values only. <br />
     * Allowed Values: {@code array, null, node, regexp, date, map, set,
     * weakmap, weakset, iterator, generator, error, proxy, promise, typedarray,
     * arraybuffer, dataview, webassemblymemory, wasmvalue}
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Object subtype hint. Specified for object type values only. <br />
     * Allowed Values: {@code array, null, node, regexp, date, map, set,
     * weakmap, weakset, iterator, generator, error, proxy, promise, typedarray,
     * arraybuffer, dataview, webassemblymemory, wasmvalue}
     */
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    /**
     * String representation of the object.
     */
    public String getDescription() {
        return description;
    }

    /**
     * String representation of the object.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * True iff some of the properties or entries of the original object did not fit.
     */
    public boolean isOverflow() {
        return overflow;
    }

    /**
     * True iff some of the properties or entries of the original object did not fit.
     */
    public void setOverflow(boolean overflow) {
        this.overflow = overflow;
    }

    /**
     *  List of the properties.
     */
    public List<PropertyPreview> getProperties() {
        return properties;
    }

    /**
     *  List of the properties.
     */
    public void setProperties(List<PropertyPreview> properties) {
        this.properties = properties;
    }

    /**
     * List of the entries. Specified for map and set subtype values only.
     */
    public List<EntryPreview> getEntries() {
        return entries;
    }

    /**
     * List of the entries. Specified for map and set subtype values only.
     */
    public void setEntries(List<EntryPreview> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "ObjectPreview{" + "type=" + type + ", subtype=" + subtype + ", description=" + description + ", overflow=" + overflow + ", properties=" + properties + ", entries=" + entries + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.subtype);
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + (this.overflow ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.properties);
        hash = 53 * hash + Objects.hashCode(this.entries);
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
        final ObjectPreview other = (ObjectPreview) obj;
        if (this.overflow != other.overflow) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.subtype, other.subtype)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.properties, other.properties)) {
            return false;
        }
        return Objects.equals(this.entries, other.entries);
    }


}
