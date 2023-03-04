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
package org.netbeans.modules.groovy.editor.api.elements.ast;

import java.util.Collections;
import java.util.Set;
import org.codehaus.groovy.ast.FieldNode;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.groovy.editor.api.ASTUtils;

public final class ASTField extends ASTElement {

    private final String fieldType;
    private final boolean isProperty;


    public ASTField(FieldNode node, String in, boolean isProperty) {
        super(node, in, node.getName());
        this.isProperty = isProperty;
        this.fieldType = ASTUtils.getSimpleName(node.getType());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSignature() {
        return name;
    }
    
    @Override
    public ElementKind getKind() {
        return ElementKind.FIELD;
    }
    
    public String getType() {
        return fieldType;
    }

    public boolean isProperty() {
        return isProperty;
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
}
