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
package org.netbeans.modules.javascript2.types.api;

/**
 *
 * @author Petr Hejl
 */
public final class TypeUsage implements Type {

    private final String type;

    private final int offset;

    private final boolean resolved;

    public TypeUsage(String type, int offset, boolean resolved) {
        this.type = type;
        this.offset = offset;
        this.resolved = resolved;
    }

    public TypeUsage(String type, int offset) {
        this(type, offset, false);
    }

    public TypeUsage(String type) {
        this(type, -1, false);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    public boolean isResolved() {
        return resolved;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeUsage other = (TypeUsage) obj;
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        if (this.offset != other.offset) {
            return false;
        }
        if (this.resolved != other.resolved) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 83 * hash + this.offset;
        hash = 83 * hash + (this.resolved ? 1 : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "TypeUsageImpl{" + "type=" + type + ", offset=" + offset + ", resolved=" + resolved + '}';
    }

}
