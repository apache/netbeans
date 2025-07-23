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
package org.netbeans.modules.php.editor.model.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.PropertyHookDeclaration;

public class PropertyHookDeclarationInfo extends ASTNodeInfo<PropertyHookDeclaration> {

    private PropertyHookDeclarationInfo(PropertyHookDeclaration node) {
        super(node);
    }

    public static PropertyHookDeclarationInfo create(PropertyHookDeclaration propertyHook) {
        return new PropertyHookDeclarationInfo(propertyHook);
    }

    @Override
    public Kind getKind() {
        return Kind.PROPERTY_HOOK;
    }

    @Override
    public String getName() {
        PropertyHookDeclaration propertyHook = getOriginalNode();
        return propertyHook.getName().getName();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        PropertyHookDeclaration propertyHook = getOriginalNode();
        return CodeUtils.getOffsetRagne(propertyHook.getName());
    }

    public List<ParameterElement> getParameters() {
        List<ParameterElement> retval = new ArrayList<>();
        List<FormalParameter> formalParameters = getOriginalNode().getFormalParameters();
        for (FormalParameter formalParameter : formalParameters) {
            FormalParameterInfo parameterInfo = FormalParameterInfo.create(formalParameter, Map.of());
            retval.add(parameterInfo.toParameter());
        }
        return retval;
    }

    public PhpModifiers getModifiers() {
        int modifier = getOriginalNode().getModifier();
        return PhpModifiers.fromBitMask(modifier);
    }

    public boolean isAttributed() {
        return getOriginalNode().isAttributed();
    }
}
