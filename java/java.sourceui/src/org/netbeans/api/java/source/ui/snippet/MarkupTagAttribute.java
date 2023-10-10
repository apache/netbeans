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
package org.netbeans.api.java.source.ui.snippet;

import java.util.Objects;

public class MarkupTagAttribute {

    private final String name;
    private final int nameStartPosition;
    private final String value;
    private final int valueStartPosition;

    public MarkupTagAttribute(String name, int nameStartPosition, String value, int valueStartPosition) {
        this.name = name;
        this.nameStartPosition = nameStartPosition;
        this.value = value;
        this.valueStartPosition = valueStartPosition;
    }

    public String getName() {
        return name;
    }

    public int getNameStartPosition() {
        return nameStartPosition;
    }

    public String getValue() {
        return value;
    }

    public int getValueStartPosition() {
        return valueStartPosition;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.name);
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
        final MarkupTagAttribute other = (MarkupTagAttribute) obj;
        return Objects.equals(this.name, other.name);
    }
    
}