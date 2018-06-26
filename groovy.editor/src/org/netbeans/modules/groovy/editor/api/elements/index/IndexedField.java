/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
