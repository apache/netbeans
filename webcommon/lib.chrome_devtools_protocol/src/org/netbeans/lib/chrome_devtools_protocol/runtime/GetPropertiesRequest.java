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

public class GetPropertiesRequest {
    private String objectId;
    private Boolean ownProperties;
    private Boolean accessorPropertiesOnly;
    private Boolean generatPreview;
    private Boolean nonIndexedPropertiesOnly;

    public GetPropertiesRequest() {
    }

    public GetPropertiesRequest(String objectId, Boolean ownProperties) {
        this.objectId = objectId;
        this.ownProperties = ownProperties;
    }

    /**
     * Identifier of the object to return properties for.
     */
    public String getObjectId() {
        return objectId;
    }

    /**
     * Identifier of the object to return properties for.
     */
    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    /**
     * If true, returns properties belonging only to the element itself, not to
     * its prototype chain.
     */
    public Boolean getOwnProperties() {
        return ownProperties;
    }

    /**
     * If true, returns properties belonging only to the element itself, not to
     * its prototype chain.
     */
    public void setOwnProperties(Boolean ownProperties) {
        this.ownProperties = ownProperties;
    }

    /**
     * If true, returns accessor properties (with getter/setter) only; internal
     * properties are not returned either.
     * <p><strong>Experimental</strong></p>
     */
    public Boolean getAccessorPropertiesOnly() {
        return accessorPropertiesOnly;
    }

    /**
     * If true, returns accessor properties (with getter/setter) only; internal
     * properties are not returned either.
     * <p><strong>Experimental</strong></p>
     */
    public void setAccessorPropertiesOnly(Boolean accessorPropertiesOnly) {
        this.accessorPropertiesOnly = accessorPropertiesOnly;
    }

    /**
     * Whether preview should be generated for the results
     * <p><strong>Experimental</strong></p>
     */
    public Boolean getGeneratPreview() {
        return generatPreview;
    }

    /**
     * Whether preview should be generated for the results
     * <p><strong>Experimental</strong></p>
     */
    public void setGeneratPreview(Boolean generatPreview) {
        this.generatPreview = generatPreview;
    }

     /**
     * If true, returns non-indexed properties only.
     * <p><strong>Experimental</strong></p>
     */
    public Boolean getNonIndexedPropertiesOnly() {
        return nonIndexedPropertiesOnly;
    }

    /**
     * If true, returns non-indexed properties only.
     * <p><strong>Experimental</strong></p>
     */
    public void setNonIndexedPropertiesOnly(Boolean nonIndexedPropertiesOnly) {
        this.nonIndexedPropertiesOnly = nonIndexedPropertiesOnly;
    }


    @Override
    public String toString() {
        return "GetPropertiesRequest{" + "objectId=" + objectId + ", ownProperties=" + ownProperties + ", accessorPropertiesOnly=" + accessorPropertiesOnly + ", generatPreview=" + generatPreview + ", nonIndexedPropertiesOnly=" + nonIndexedPropertiesOnly + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.objectId);
        hash = 61 * hash + Objects.hashCode(this.ownProperties);
        hash = 61 * hash + Objects.hashCode(this.accessorPropertiesOnly);
        hash = 61 * hash + Objects.hashCode(this.generatPreview);
        hash = 61 * hash + Objects.hashCode(this.nonIndexedPropertiesOnly);
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
        final GetPropertiesRequest other = (GetPropertiesRequest) obj;
        if (!Objects.equals(this.objectId, other.objectId)) {
            return false;
        }
        if (!Objects.equals(this.ownProperties, other.ownProperties)) {
            return false;
        }
        if (!Objects.equals(this.accessorPropertiesOnly, other.accessorPropertiesOnly)) {
            return false;
        }
        if (!Objects.equals(this.generatPreview, other.generatPreview)) {
            return false;
        }
        return Objects.equals(this.nonIndexedPropertiesOnly, other.nonIndexedPropertiesOnly);
    }


}
