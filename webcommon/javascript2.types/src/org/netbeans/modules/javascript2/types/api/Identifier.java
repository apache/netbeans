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

import org.netbeans.modules.csl.api.OffsetRange;

/**
 *
 * @author Petr Hejl
 */
public final class Identifier {

    private final String name;

    private final OffsetRange offsetRange;

    public Identifier(String name, OffsetRange offsetRange) {
        this.name = name;
        this.offsetRange = offsetRange;
    }

    public Identifier(String name, int startOffset) {
        this(name, startOffset >= 0 ? new OffsetRange(startOffset, startOffset + name.length()) : OffsetRange.NONE);
    }

    public String getName() {
        return name;
    }

    public OffsetRange getOffsetRange() {
        return offsetRange;
    }

    @Override
    public String toString() {
        return "IdentifierImpl{" + "name=" + name + ", offsetRange=" + offsetRange + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Identifier other = (Identifier) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if (this.offsetRange != other.offsetRange && (this.offsetRange == null || !this.offsetRange.equals(other.offsetRange))) {
            return false;
        }
        return true;
    }
}
