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
package org.netbeans.modules.debugger.jpda.truffle;

import java.util.Objects;

/**
 *
 * @author Martin
 */
public final class LanguageName {

    public static final LanguageName NONE = new LanguageName("", "");

    private final String id;
    private final String name;

    private LanguageName(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static LanguageName parse(String string) {
        string = string.trim();
        if (string.isEmpty()) {
            return NONE;
        }
        int i = string.indexOf(" ");
        assert i > 0 : string;
        return new LanguageName(string.substring(0, i), string.substring(i + 1));
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
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
        final LanguageName other = (LanguageName) obj;
        return this.id.equals(other.id);
    }

}
