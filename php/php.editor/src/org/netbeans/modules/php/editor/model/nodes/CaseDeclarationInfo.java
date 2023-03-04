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
package org.netbeans.modules.php.editor.model.nodes;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;

public class CaseDeclarationInfo extends ASTNodeInfo<Identifier> {

    private final String value;
    private final CaseDeclaration caseDeclaration;

    CaseDeclarationInfo(Identifier node, final String value, CaseDeclaration caseDeclaration) {
        super(node);
        this.value = value;
        this.caseDeclaration = caseDeclaration;
    }

    public static CaseDeclarationInfo create(CaseDeclaration caseDeclaration) {
        Identifier name = caseDeclaration.getName();
        Expression initializer = caseDeclaration.getInitializer();
        String value = getCaseValue(initializer);
        return new CaseDeclarationInfo(name, value, caseDeclaration);
    }

    @Override
    public Kind getKind() {
        return Kind.ENUM_CASE;
    }

    @Override
    public String getName() {
        return getOriginalNode().getName();
    }

    @Override
    public QualifiedName getQualifiedName() {
        return QualifiedName.createUnqualifiedName(getName());
    }

    @Override
    public OffsetRange getRange() {
        Identifier name = getOriginalNode();
        return new OffsetRange(name.getStartOffset(), name.getEndOffset());
    }

    @CheckForNull
    public String getValue() {
        return value;
    }

    public PhpModifiers getAccessModifiers() {
        return PhpModifiers.fromBitMask(caseDeclaration.getModifier());
    }

    @CheckForNull
    protected static String getCaseValue(Expression expr) {
        if (expr instanceof Scalar) {
            return ((Scalar) expr).getStringValue();
        }
        if (expr instanceof UnaryOperation) {
            UnaryOperation up = (UnaryOperation) expr;
            if (up.getOperator() == UnaryOperation.Operator.MINUS
                    && up.getExpression() instanceof Scalar) {
                return "-" + ((Scalar) up.getExpression()).getStringValue(); // NOI18N
            }
        }
        return null;
    }

}
