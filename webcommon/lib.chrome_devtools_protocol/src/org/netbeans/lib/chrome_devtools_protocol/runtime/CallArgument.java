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
 * Represents function call argument. Either remote object id objectId,
 * primitive value, unserializable primitive value or neither of (for undefined)
 * them should be specified.
 */
public final class CallArgument {
    private Object value;
    private String unserializableValue;
    private String objectId;

    public CallArgument() {
    }

    /**
     * Primitive value or serializable javascript object.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Primitive value or serializable javascript object.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Primitive value which can not be JSON-stringified.
     */
    public String getUnserializableValue() {
        return unserializableValue;
    }

    /**
     * Primitive value which can not be JSON-stringified.
     */
    public void setUnserializableValue(String unserializableValue) {
        this.unserializableValue = unserializableValue;
    }

    /**
     * Remote object handle.
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Remote object handle.
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return "CallArgument{" + "value=" + value + ", unserializableValue=" + unserializableValue + ", objectId=" + objectId + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.value);
        hash = 29 * hash + Objects.hashCode(this.unserializableValue);
        hash = 29 * hash + Objects.hashCode(this.objectId);
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
        final CallArgument other = (CallArgument) obj;
        if (!Objects.equals(this.unserializableValue, other.unserializableValue)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return Objects.equals(this.objectId, other.objectId);
    }


}
