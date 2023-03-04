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
package org.netbeans.modules.javascript2.model;

import java.util.Collection;
import java.util.Set;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.model.api.JsFunction;
import org.netbeans.modules.javascript2.model.api.JsObject;
import org.netbeans.modules.javascript2.types.api.DeclarationScope;
import org.netbeans.modules.javascript2.types.api.Identifier;
import org.netbeans.modules.javascript2.types.api.TypeUsage;

/**
 *
 * @author Petr Pisl
 */
public class JsFunctionReference extends JsObjectReference implements JsFunction {

    private final JsFunction original;

    public JsFunctionReference(JsObject parent, Identifier declarationName,
            JsFunction original, boolean isDeclared, Set<Modifier> modifiers) {
        super(parent, declarationName, original, isDeclared, modifiers);
        this.original = original;
    }

    @Override
    public JsFunction getOriginal() {
        return this.original;
    }

    @Override
    public Collection<? extends JsObject> getParameters() {
        return original.getParameters();
    }

    @Override
    public JsObject getParameter(String name) {
        return original.getParameter(name);
    }

    @Override
    public void addReturnType(TypeUsage type) {
        original.addReturnType(type);
    }

    @Override
    public Collection<? extends TypeUsage> getReturnTypes() {
        return original.getReturnTypes();
    }

    @Override
    public Collection<? extends DeclarationScope> getChildrenScopes() {
        return original.getChildrenScopes();
    }

    @Override
    public DeclarationScope getParentScope() {
        return original.getParentScope();
    }

    @Override
    public void addDeclaredScope(DeclarationScope scope) {
        original.addDeclaredScope(scope);
    }

}
