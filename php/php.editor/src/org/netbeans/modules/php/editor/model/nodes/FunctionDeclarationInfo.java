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
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.openide.util.Pair;

/**
 *
 * @author Radek Matous
 */
public class FunctionDeclarationInfo extends ASTNodeInfo<FunctionDeclaration> {

    private final Map<String, Pair<String /*declared type*/, List<Pair<QualifiedName, Boolean>>>> paramDocTypes;

    protected FunctionDeclarationInfo(Program program, FunctionDeclaration node) {
        super(node);
        if (program != null) {
            paramDocTypes = VariousUtils.getParamTypesFromPHPDoc(program, node);
        } else {
            paramDocTypes = Collections.emptyMap();
        }
    }

    public static FunctionDeclarationInfo create(FunctionDeclaration functionDeclaration) {
        return new FunctionDeclarationInfo(null, functionDeclaration);
    }
    public static FunctionDeclarationInfo create(Program program, FunctionDeclaration functionDeclaration) {
        return new FunctionDeclarationInfo(program, functionDeclaration);
    }

    @Override
    public Kind getKind() {
        return Kind.FUNCTION;
    }

    @Override
    public String getName() {
        FunctionDeclaration functionDeclaration = getOriginalNode();
        return functionDeclaration.getFunctionName().getName();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        FunctionDeclaration functionDeclaration = getOriginalNode();
        Identifier name = functionDeclaration.getFunctionName();
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    public List<ParameterElement> getParameters() {
        List<ParameterElement> retval = new ArrayList<>();
        List<FormalParameter> formalParameters = getOriginalNode().getFormalParameters();
        for (FormalParameter formalParameter : formalParameters) {
            FormalParameterInfo parameterInfo = FormalParameterInfo.create(formalParameter, paramDocTypes);
            retval.add(parameterInfo.toParameter());
        }
        return retval;
    }

    public List<QualifiedName> getReturnTypes() {
        Expression returnType = getOriginalNode().getReturnType();
        if (returnType == null) {
            return Collections.emptyList();
        }
        if (returnType instanceof UnionType) {
            return QualifiedName.create((UnionType) returnType);
        } else {
            return Collections.singletonList(QualifiedName.create(returnType));
        }
    }

}
