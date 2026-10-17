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
package org.netbeans.modules.php.editor.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.PropertyHookElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.PropertyHookScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import static org.netbeans.modules.php.editor.model.impl.ScopeImpl.filter;
import org.netbeans.modules.php.editor.model.nodes.PropertyHookDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;

class PropertyHookScopeImpl extends ScopeImpl implements PropertyHookScope, VariableNameFactory {

    private final List<? extends ParameterElement> parameters;
    private final boolean isReference;
    private final boolean hasBody;
    private final boolean isAttributed;
    private final OffsetRange offsetRange;

    PropertyHookScopeImpl(Scope inScope, PropertyHookDeclarationInfo info) {
        super(inScope, info, info.getModifiers(), info.getOriginalNode().getBody(), false);
        this.parameters = List.copyOf(info.getParameters());
        this.isReference = info.getOriginalNode().isReference();
        this.hasBody = info.getOriginalNode().getBody() != null;
        this.isAttributed = info.isAttributed();
        this.offsetRange = info.getRange();
    }

    PropertyHookScopeImpl(Scope inScope, PropertyHookElement propertyHookElement) {
        super(inScope, propertyHookElement, PhpElementKind.PROPERTY_HOOK);
        this.parameters = List.copyOf(propertyHookElement.getParameters());
        this.isReference = propertyHookElement.isReference();
        this.hasBody = propertyHookElement.hasBody();
        this.isAttributed = propertyHookElement.isAttributed();
        this.offsetRange = propertyHookElement.getOffsetRange();
    }

    @Override
    public List<? extends String> getParameterNames() {
        List<String> parameterNames = new ArrayList<>();
        for (ParameterElement parameter : parameters) {
            parameterNames.add(parameter.getName());
        }
        return parameterNames;
    }

    @Override
    public boolean isReference() {
        return isReference;
    }

    @Override
    public boolean hasBody() {
        return hasBody;
    }

    @Override
    public boolean isAttributed() {
        return isAttributed;
    }

    @Override
    public List<? extends ParameterElement> getParameters() {
        return parameters;
    }

    @Override
    public OffsetRange getOffsetRange() {
        return offsetRange;
    }

    @Override
    public Collection<? extends VariableName> getDeclaredVariables() {
        final Scope inScope = getInScope().getInScope();
        if (inScope instanceof ClassScope || inScope instanceof TraitScope) {
            if (inScope instanceof VariableScope variableScope) {
                Collection<? extends VariableName> variables = filter(getElements(), (ModelElement element) -> element.getPhpElementKind().equals(PhpElementKind.VARIABLE));
                return ModelUtils.merge(variableScope.getDeclaredVariables(), variables);
            }
        }
        return filter(getElements(), (ModelElement element) -> element.getPhpElementKind().equals(PhpElementKind.VARIABLE));
    }

    @Override
    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }
}
