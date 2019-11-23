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
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;

/**
 * @author Radek Matous
 */
public class ClassConstantDeclarationInfo extends ASTNodeInfo<Identifier> {
    private final String value;
    private final ConstantDeclaration constantDeclaration;

    ClassConstantDeclarationInfo(Identifier node, final String value, ConstantDeclaration constantDeclaration) {
        super(node);
        this.value = value;
        this.constantDeclaration = constantDeclaration;
    }

    public static List<? extends ClassConstantDeclarationInfo> create(ConstantDeclaration constantDeclaration) {
        List<ClassConstantDeclarationInfo> retval = new ArrayList<>();
        Iterator<Identifier> iteratorNames = constantDeclaration.getNames().iterator();
        Iterator<Expression> iteratorInitializers = constantDeclaration.getInitializers().iterator();
        Identifier name;
        while (iteratorNames.hasNext()) {
            String value = null;
            name = iteratorNames.next();
            Expression initializer = iteratorInitializers.next();
            if (initializer instanceof Scalar) {
                value = ((Scalar) initializer).getStringValue();
            } else if (initializer instanceof UnaryOperation) {
                UnaryOperation unaryOperation = (UnaryOperation) initializer;
                Expression expression = unaryOperation.getExpression();
                if (expression instanceof Scalar) {
                    value = unaryOperation.getOperator() + ((Scalar) expression).getStringValue();
                }
            }
            retval.add(new ClassConstantDeclarationInfo(name, value, constantDeclaration));
        }
        return retval;
    }

    @Override
    public Kind getKind() {
        return Kind.CLASS_CONSTANT;
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

    public String getValue() {
        return value;
    }

    public PhpModifiers getAccessModifiers() {
        return PhpModifiers.fromBitMask(constantDeclaration.getModifier());
    }

}
