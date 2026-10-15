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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.openide.util.Pair;

/**
 * @author Radek Matous
 */
public class MethodDeclarationInfo extends ASTNodeInfo<MethodDeclaration> {

    private Map<String, Pair<String /*raw types*/, List<Pair<QualifiedName, Boolean>>>> paramDocTypes = Collections.emptyMap();
    private final boolean isFromInterface;

    MethodDeclarationInfo(Program program, MethodDeclaration methodDeclaration, final boolean isFromInterface) {
        super(methodDeclaration);
        this.isFromInterface = isFromInterface;
        if (program != null) {
            paramDocTypes = VariousUtils.getParamTypesFromPHPDoc(program, methodDeclaration);
        }
    }

    public static MethodDeclarationInfo create(Program program, MethodDeclaration methodDeclaration, final boolean isFromInterface) {
        return new MethodDeclarationInfo(program, methodDeclaration, isFromInterface);
    }
    public static MethodDeclarationInfo create(Program program, MethodDeclaration methodDeclaration, final TypeScope typeScope) {
        return create(program, methodDeclaration, typeScope.isInterface());
    }
    public static MethodDeclarationInfo create(MethodDeclaration classDeclaration, final TypeScope typeScope) {
        return new MethodDeclarationInfo(null, classDeclaration, typeScope.isInterface());
    }

    @Override
    public Kind getKind() {
        PhpModifiers modifiers = PhpModifiers.fromBitMask(getOriginalNode().getModifier());
        return modifiers.isStatic() ? Kind.STATIC_METHOD : Kind.METHOD;
    }

    @Override
    public String getName() {
        MethodDeclaration methodDeclaration = getOriginalNode();
        return methodDeclaration.getFunction().getFunctionName().getName();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        MethodDeclaration methodDeclaration = getOriginalNode();
        Identifier name = methodDeclaration.getFunction().getFunctionName();
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public List<ParameterElement> getParameters() {
        List<ParameterElement> retval = new ArrayList<>();
        List<FormalParameter> formalParameters = getOriginalNode().getFunction().getFormalParameters();
        for (FormalParameter formalParameter : formalParameters) {
            FormalParameterInfo parameterInfo = FormalParameterInfo.create(formalParameter, paramDocTypes);
            retval.add(parameterInfo.toParameter());
        }
        return retval;
    }

    public PhpModifiers getAccessModifiers() {
        int realModifiers = getOriginalNode().getModifier();
        realModifiers = (isFromInterface) ? (realModifiers | Modifier.ABSTRACT | Modifier.PUBLIC) : realModifiers;
        return PhpModifiers.fromBitMask(realModifiers);
    }
}
