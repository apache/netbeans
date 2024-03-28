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

public class GetPropertiesResponse {
    private List<PropertyDescriptor> result;
    private List<InternalPropertyDescriptor> internalProperties;
    private List<PrivatePropertyDescriptor> privateProperties;
    private ExceptionDetails exceptionDetails;

    public GetPropertiesResponse() {
    }

    /**
     * Object properties.
     */
    public List<PropertyDescriptor> getResult() {
        return result;
    }

    /**
     * Object properties.
     */
    public void setResult(List<PropertyDescriptor> result) {
        this.result = result;
    }

    /**
     * Internal object properties (only of the element itself).
     */
    public List<InternalPropertyDescriptor> getInternalProperties() {
        return internalProperties;
    }

    /**
     * Internal object properties (only of the element itself).
     */
    public void setInternalProperties(List<InternalPropertyDescriptor> internalProperties) {
        this.internalProperties = internalProperties;
    }

    /**
     * Object private properties.<br />
     * <p><strong>Experimental</strong></p>
     */
    public List<PrivatePropertyDescriptor> getPrivateProperties() {
        return privateProperties;
    }

    /**
     * Object private properties.<br />
     * <p><strong>Experimental</strong></p>
     */
    public void setPrivateProperties(List<PrivatePropertyDescriptor> privateProperties) {
        this.privateProperties = privateProperties;
    }

    /**
     * Exception details.
     */
    public ExceptionDetails getExceptionDetails() {
        return exceptionDetails;
    }

    /**
     * Exception details.
     */
    public void setExceptionDetails(ExceptionDetails exceptionDetails) {
        this.exceptionDetails = exceptionDetails;
    }

    @Override
    public String toString() {
        return "GetPropertiesResponse{" + "result=" + result + ", internalProperties=" + internalProperties + ", privateProperties=" + privateProperties + ", exceptionDetails=" + exceptionDetails + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.result);
        hash = 29 * hash + Objects.hashCode(this.internalProperties);
        hash = 29 * hash + Objects.hashCode(this.privateProperties);
        hash = 29 * hash + Objects.hashCode(this.exceptionDetails);
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
        final GetPropertiesResponse other = (GetPropertiesResponse) obj;
        if (!Objects.equals(this.result, other.result)) {
            return false;
        }
        if (!Objects.equals(this.internalProperties, other.internalProperties)) {
            return false;
        }
        if (!Objects.equals(this.privateProperties, other.privateProperties)) {
            return false;
        }
        return Objects.equals(this.exceptionDetails, other.exceptionDetails);
    }


}
