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
package org.netbeans.modules.javascript2.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.Documentation;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.doc.spi.JsDocumentationHolder;
import org.netbeans.modules.javascript2.model.api.JsElement;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.model.api.JsReference;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public class JsObjectReference extends JsObjectImpl implements JsReference {

    private final JsObject original;

    private final Set<Modifier> modifiers;

    public JsObjectReference(JsObject parent, Identifier declarationName,
            @NonNull JsObject original, boolean isDeclared, Set<Modifier> modifiers) {
        super(parent, declarationName, declarationName.getOffsetRange(), isDeclared,
                modifiers == null ? EnumSet.noneOf(Modifier.class) : modifiers, original.getMimeType(), original.getSourceLabel());
        this.original = original;
        this.modifiers = modifiers;
    }

    @Override
    public Map<String, ? extends JsObject> getProperties() {
        return original.getProperties();
    }

    @Override
    public void addProperty(String name, JsObject property) {
        original.addProperty(name, property);
    }

    @Override
    public JsObject getProperty(String name) {
        return original.getProperty(name);
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Override
    public Kind getJSKind() {
        Kind kind = original.getJSKind();
        if (kind == JsElement.Kind.ANONYMOUS_OBJECT) {
            kind = JsElement.Kind.OBJECT_LITERAL;
        }
        return kind;
    }

    @Override
    public ElementKind getKind() {
        return original.getKind();
    }

    @Override
    public Set<Modifier> getModifiers() {
        if (modifiers != null) {
            return modifiers;
        }
        return original.getModifiers();
    }

    public JsObject getOriginal() {
        return original;
    }

    @Override
    public Collection<? extends TypeUsage> getAssignmentForOffset(int offset) {
        return original.getAssignmentForOffset(offset);
    }

    @Override
    public Collection<? extends TypeUsage> getAssignments() {
        return original.getAssignments();
    }

    @Override
    public void resolveTypes(JsDocumentationHolder docHolder) {
        // do nothing
    }

    @Override
    public Documentation getDocumentation() {
        return original.getDocumentation();
    }

    @Override
    protected void correctTypes(String fromType, String toType) {
        //Do nothing
    }

}
