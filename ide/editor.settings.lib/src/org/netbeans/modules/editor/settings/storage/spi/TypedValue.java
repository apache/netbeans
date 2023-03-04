/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.editor.settings.storage.spi;

import org.openide.util.BaseUtilities;

/**
 *
 * @author vita
 */
public final class TypedValue {

    private final String value;
    private String javaType;
    private String apiCategory; // the API stability

    public TypedValue(String value, String javaType) {
        this.value = value;
        this.javaType = javaType;
    }

    public String getApiCategory() {
        return apiCategory;
    }

    public void setApiCategory(String apiCategory) {
        this.apiCategory = apiCategory;
    }

    public String getJavaType() {
        return javaType;
    }

    public void setJavaType(String javaType) {
        this.javaType = javaType;
    }

    public String getValue() {
        return value;
    }

    public @Override boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypedValue other = (TypedValue) obj;
        if (!BaseUtilities.compareObjects(this.value, other.value)) {
            return false;
        }
        if (!BaseUtilities.compareObjects(this.javaType, other.javaType)) {
            return false;
        }
        return true;
    }

    public @Override int hashCode() {
        int hash = 7;
        hash = 37 * hash + (value != null ? value.hashCode() : 0);
        hash = 37 * hash + (javaType != null ? javaType.hashCode() : 0);
        return hash;
    }

    public @Override String toString() {
        return super.toString() + "['" + value + "', " + javaType;
    }
    
} // End of TypedValue class
