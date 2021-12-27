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
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.openide.util.Pair;

/**
 *
 * @author Radek Matous
 */
public class LambdaFunctionDeclarationInfo extends ASTNodeInfo<LambdaFunctionDeclaration> {

    private final Map<String, List<Pair<QualifiedName, Boolean>>> paramDocTypes = Collections.emptyMap();


    protected LambdaFunctionDeclarationInfo(LambdaFunctionDeclaration node) {
        super(node);
    }

    public static LambdaFunctionDeclarationInfo create(LambdaFunctionDeclaration fnc) {
        return new LambdaFunctionDeclarationInfo(fnc);
    }

    @Override
    public Kind getKind() {
        return Kind.FUNCTION;
    }

    @Override
    public String getName() {
        LambdaFunctionDeclaration fnc = getOriginalNode();
        return String.format("LambdaFunctionDeclaration:%d", fnc.getStartOffset()); //NOI18N
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        LambdaFunctionDeclaration fnc = getOriginalNode();
        return new OffsetRange(fnc.getStartOffset(), fnc.getEndOffset());
    }

    public List<? extends ParameterElement> getParameters() {
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
