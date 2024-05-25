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
package org.netbeans.modules.cloud.oracle.items;

import java.util.Objects;

/**
 *
 * @author Jan Horvath
 */
public final class OCID  {
    private final String value;
    private final String path;
    
    private OCID(String value, String path) {
        this.value = value;
        this.path = path;
    }

    public String getValue() {
        return value;
    }
    
    public final String getPath() {
        return path;
    }   

    public static final OCID of(String value, String path) {
        return new OCID(value, path);
    }

    public String toPersistentForm() {
        return value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.value);
        hash = 61 * hash + Objects.hashCode(this.path);
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
        final OCID other = (OCID) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return Objects.equals(this.path, other.path);
    }

    @Override
    public String toString() {
        return "OCID{ path=" + path  + ", value=" + value + '}';
    }
}
