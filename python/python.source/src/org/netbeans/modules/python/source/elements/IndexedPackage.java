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
package org.netbeans.modules.python.source.elements;

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;

public class IndexedPackage extends IndexedElement {
    private String pkg;
    private boolean hasMore;

    public IndexedPackage(String name, String pkg, String url, boolean hasMore) {
        super(name, ElementKind.PACKAGE, url, null, null, null);
        this.pkg = pkg;
        this.hasMore = hasMore;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    public String getPkg() {
        return pkg;
    }

    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedPackage other = (IndexedPackage)obj;

        // Side effect:
        // If any element thinks we have more
        if (other.hasMore) {
            this.hasMore = other.hasMore;
        } else if (this.hasMore) {
            other.hasMore = this.hasMore;
        }

        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
