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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;

public class ArrowFunctionDeclarationInfo extends ASTNodeInfo<ArrowFunctionDeclaration> {

    private ArrowFunctionDeclarationInfo(ArrowFunctionDeclaration node) {
        super(node);
    }

    public static ArrowFunctionDeclarationInfo create(ArrowFunctionDeclaration fnc) {
        return new ArrowFunctionDeclarationInfo(fnc);
    }

    @Override
    public Kind getKind() {
        return Kind.FUNCTION;
    }

    @Override
    public String getName() {
        ArrowFunctionDeclaration fnc = getOriginalNode();
        return String.format("ArrowFunctionDeclaration:%d", fnc.getStartOffset()); // NOI18N
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        ArrowFunctionDeclaration fnc = getOriginalNode();
        return new OffsetRange(fnc.getStartOffset(), fnc.getEndOffset());
    }

    public List<? extends ParameterElement> getParameters() {
        List<ParameterElement> retval = new ArrayList<>();
        List<FormalParameter> formalParameters = getOriginalNode().getFormalParameters();
        for (FormalParameter formalParameter : formalParameters) {
            FormalParameterInfo parameterInfo = FormalParameterInfo.create(formalParameter, Collections.emptyMap());
            retval.add(parameterInfo.toParameter());
        }
        return retval;
    }

    @CheckForNull
    public QualifiedName getReturnType() {
        Expression returnType = getOriginalNode().getReturnType();
        if (returnType == null) {
            return null;
        }
        return QualifiedName.create(returnType);
    }

}
