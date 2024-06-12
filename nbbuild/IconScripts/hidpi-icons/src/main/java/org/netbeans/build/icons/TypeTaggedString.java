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
package org.netbeans.build.icons;

import com.google.common.base.Preconditions;

public abstract class TypeTaggedString {
    private final String s;

    public TypeTaggedString(String s) {
        Preconditions.checkArgument(s.trim().equals(s));
        Preconditions.checkArgument(!s.isEmpty());
        this.s = s;
    }

    @Override
    public final String toString() {
        return s;
    }

    @Override
    public final int hashCode() {
        return s.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof TypeTaggedString)) {
            return false;
        }
        TypeTaggedString other = (TypeTaggedString) obj;
        return this.getClass().equals(other.getClass())
            && this.s.equals(other.s);
    }

    public static class Hash extends TypeTaggedString {
        public Hash(String s) {
            super(s);
            Preconditions.checkArgument(s.length() == 64);
        }
    }

    public static class IconPath extends TypeTaggedString {
        public IconPath(String s) {
            super(s);
            Preconditions.checkArgument(
                    s.endsWith(".gif") || s.endsWith(".png") || s.endsWith(".svg"));
        }
    }

    public static class ArtboardName extends TypeTaggedString {
        public ArtboardName(String s) {
            super(s);
            Preconditions.checkArgument(!s.contains("/"));
        }
    }
}
