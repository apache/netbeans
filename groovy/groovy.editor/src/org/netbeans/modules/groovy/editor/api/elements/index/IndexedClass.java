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

package org.netbeans.modules.groovy.editor.api.elements.index;

import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.elements.common.ClassElement;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;

/**
 * A class describing a Groovy class that is in "textual form" (signature, filename, etc.)
 * obtained from the code index.
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
public final class IndexedClass extends IndexedElement implements ClassElement {

    /** This class is a module rather than a proper class */
    public static final int MODULE = 1 << 6;

    private final String simpleName;

    protected IndexedClass(IndexResult result, String fqn, String simpleName, String attributes, int flags) {
        super(result, fqn, attributes, flags);
        this.simpleName = simpleName;
    }

    public static IndexedClass create(String simpleName, String fqn, IndexResult result,
        String attributes, int flags) {
        IndexedClass c = new IndexedClass(result, fqn, simpleName, attributes, flags);
        return c;
    }

    // XXX Is this necessary?
    @Override
    public String getSignature() {
        return in;
    }

    @Override
    public String getName() {
        return simpleName;
    }

    @Override
    public ElementKind getKind() {
        return (flags & MODULE) != 0 ? ElementKind.MODULE : ElementKind.CLASS;
    }
    
    @Override 
    public boolean equals(Object o) {
        if (o instanceof IndexedClass && in != null) {
            return in.equals(((IndexedClass) o).in);
        }
        return super.equals(o);
    }
    
    @Override
    public int hashCode() {
        return in == null ? super.hashCode() : in.hashCode();
    }

    @Override
    public String getFqn() {
        return in;
    }
}
