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

public final class EntryPreview {
    private ObjectPreview key;
    private ObjectPreview value;

    public EntryPreview() {
    }

    /**
     * Preview of the key. Specified for map-like collection entries.
     */
    public ObjectPreview getKey() {
        return key;
    }

    /**
     * Preview of the key. Specified for map-like collection entries.
     */
    public void setKey(ObjectPreview key) {
        this.key = key;
    }

    /**
     * Preview of the value.
     */
    public ObjectPreview getValue() {
        return value;
    }

    /**
     * Preview of the value.
     */
    public void setValue(ObjectPreview value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EntryPreview{" + "key=" + key + ", value=" + value + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.key);
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
        final EntryPreview other = (EntryPreview) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }
}
