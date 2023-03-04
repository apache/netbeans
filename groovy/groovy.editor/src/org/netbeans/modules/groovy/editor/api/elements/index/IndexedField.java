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

import java.util.Collections;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.groovy.editor.utils.GroovyUtils;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;


/**
 *
 * @author Tor Norbye
 */
public class IndexedField extends IndexedElement {

    private final String typeName;
    private final String fieldName;
    private boolean inherited;
    private boolean smart;

    private IndexedField(String typeName, String fieldName, IndexResult result, String classFqn,
            String attributes, int flags) {
        super(result, classFqn, attributes, flags);
        this.typeName = GroovyUtils.stripPackage(typeName);
        this.fieldName = fieldName;
    }

    public static IndexedField create(String typeName, String fieldName, String classFqn,
            IndexResult result, String attributes, int flags) {
        
        IndexedField m = new IndexedField(typeName, fieldName, result, classFqn, attributes, flags);
        return m;
    }

    public boolean isSmart() {
        return smart;
    }

    public void setSmart(boolean smart) {
        this.smart = smart;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.FIELD;
    }

    @Override
    public String getSignature() {
        return in + "#" + fieldName;
    }

    @Override
    public String getName() {
        return fieldName;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean isInherited() {
        return inherited;
    }

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    public boolean isProperty() {
        if (attributes != null) {
            int separatorIndex = attributes.indexOf(';');
            if (separatorIndex != -1) {
                return Boolean.parseBoolean(attributes.substring(separatorIndex + 1));
            }
        }
        return false;
    }

    @Override
    public Set<Modifier> getModifiers() {
        Set<Modifier> mods = super.getModifiers();
        if (isProperty()) {
            if (mods.isEmpty()) {
                return Collections.singleton(Modifier.PRIVATE);
            } else {
                mods.add(Modifier.PRIVATE);
            }
        }
        return mods;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IndexedField other = (IndexedField) obj;
        if (this.fieldName != other.fieldName && (this.fieldName == null || !this.fieldName.equals(other.fieldName))) {
            return false;
        }
        if (this.in != other.in && (this.in == null || !this.in.equals(other.in))) {
            return false;
        }
        if (this.flags != other.flags) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.fieldName != null ? this.fieldName.hashCode() : 0);
        hash = 43 * hash + (this.in != null ? this.in.hashCode() : 0);
        hash = 53 * hash + flags;
        return hash;
    }
}
