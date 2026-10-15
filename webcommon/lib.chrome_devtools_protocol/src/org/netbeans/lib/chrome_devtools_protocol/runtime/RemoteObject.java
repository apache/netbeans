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

public final class RemoteObject {

    private String type;
    private String subtype;
    private String className;
    private Object value;
    private String unserializableValue;
    private String description;
    private WebDriverValue webDriverValue;
    private String objectId;
    private ObjectPreview preview;
    private CustomPreview customPreview;

    public RemoteObject() {
    }

    /**
     * Object type.<br />
     * Allowed Values:
     * {@code object, function, undefined, string, number, boolean, symbol, bigint}
     */
    public String getType() {
        return type;
    }

    /**
     * Object type.<br />
     * Allowed Values:
     * {@code object, function, undefined, string, number, boolean, symbol, bigint}
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Object subtype hint. Specified for {@code object} type values only. NOTE:
     * If you change anything here, make sure to also update {@code subtype} in
     * {@link ObjectPreview} and {@link PropertyPreview} below.<br />
     * Allowed Values: {@code array, null, node, regexp, date, map, set,
     * weakmap, weakset, iterator, generator, error, proxy, promise, typedarray,
     * arraybuffer, dataview, webassemblymemory, wasmvalue}
     */
    public String getSubtype() {
        return subtype;
    }

    /**
     * Object subtype hint. Specified for {@code object} type values only. NOTE:
     * If you change anything here, make sure to also update {@code subtype} in
     * {@link ObjectPreview} and {@link PropertyPreview} below.<br />
     * Allowed Values: {@code array, null, node, regexp, date, map, set,
     * weakmap, weakset, iterator, generator, error, proxy, promise, typedarray,
     * arraybuffer, dataview, webassemblymemory, wasmvalue}
     */
    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    /**
     * Object class (constructor) name. Specified for object type values only.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Object class (constructor) name. Specified for object type values only.
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Remote object value in case of primitive values or JSON values (if it was
     * requested).
     */
    public Object getValue() {
        return value;
    }

    /**
     * Remote object value in case of primitive values or JSON values (if it was
     * requested).
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Primitive value which can not be JSON-stringified does not have value,
     * but gets this property.
     */
    public String getUnserializableValue() {
        return unserializableValue;
    }

    /**
     * Primitive value which can not be JSON-stringified does not have value,
     * but gets this property.
     */
    public void setUnserializableValue(String unserializableValue) {
        this.unserializableValue = unserializableValue;
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
     * WebDriver BiDi representation of the value.
     * <p><strong>Experimental</strong></p>
     */
    public WebDriverValue getWebDriverValue() {
        return webDriverValue;
    }

    /**
     * WebDriver BiDi representation of the value.
     * <p><strong>Experimental</strong></p>
     */
    public void setWebDriverValue(WebDriverValue webDriverValue) {
        this.webDriverValue = webDriverValue;
    }

    /**
     * Unique object identifier (for non-primitive values).
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Unique object identifier (for non-primitive values).
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    /**
     * Preview containing abbreviated property values. Specified for object type
     * values only.
     * <p><strong>Experimental</strong></p>
     */
    public ObjectPreview getPreview() {
        return preview;
    }

    /**
     * Preview containing abbreviated property values. Specified for object type
     * values only.
     * <p><strong>Experimental</strong></p>
     */
    public void setPreview(ObjectPreview preview) {
        this.preview = preview;
    }

    /**
     * <p><strong>Experimental</strong></p>
     */
    public CustomPreview getCustomPreview() {
        return customPreview;
    }

    /**
     * <p><strong>Experimental</strong></p>
     */
    public void setCustomPreview(CustomPreview customPreview) {
        this.customPreview = customPreview;
    }

    @Override
    public String toString() {
        return "RemoteObject{" + "type=" + type + ", subtype=" + subtype + ", className=" + className + ", value=" + value + ", unserializableValue=" + unserializableValue + ", description=" + description + ", webDriverValue=" + webDriverValue + ", objectId=" + objectId + ", preview=" + preview + ", customPreview=" + customPreview + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.subtype);
        hash = 53 * hash + Objects.hashCode(this.className);
        hash = 53 * hash + Objects.hashCode(this.value);
        hash = 53 * hash + Objects.hashCode(this.unserializableValue);
        hash = 53 * hash + Objects.hashCode(this.description);
        hash = 53 * hash + Objects.hashCode(this.webDriverValue);
        hash = 53 * hash + Objects.hashCode(this.objectId);
        hash = 53 * hash + Objects.hashCode(this.preview);
        hash = 53 * hash + Objects.hashCode(this.customPreview);
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
        final RemoteObject other = (RemoteObject) obj;
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.subtype, other.subtype)) {
            return false;
        }
        if (!Objects.equals(this.className, other.className)) {
            return false;
        }
        if (!Objects.equals(this.unserializableValue, other.unserializableValue)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.objectId, other.objectId)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        if (!Objects.equals(this.webDriverValue, other.webDriverValue)) {
            return false;
        }
        if (!Objects.equals(this.preview, other.preview)) {
            return false;
        }
        return Objects.equals(this.customPreview, other.customPreview);
    }

}
