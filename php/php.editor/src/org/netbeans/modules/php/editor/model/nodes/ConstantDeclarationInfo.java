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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;

/**
 * @author Radek Matous
 */
public class ConstantDeclarationInfo extends ClassConstantDeclarationInfo {

    ConstantDeclarationInfo(final Identifier node, final String value, final ConstantDeclaration constantDeclaration) {
        super(node, value, constantDeclaration);
    }

    public static List<? extends ConstantDeclarationInfo> create(ConstantDeclaration constantDeclaration) {
        List<ConstantDeclarationInfo> retval = new ArrayList<>();
        List<Identifier> names = constantDeclaration.getNames();
        for (Identifier identifier : names) {
            String value = null;
            for (final Expression expression : constantDeclaration.getInitializers()) {
                value = getConstantValue(expression);
                if (value != null) {
                    break;
                }
                /*
                if (expression instanceof Scalar) {
                    value = ((Scalar) expression).getStringValue();
                    break;
                }
                if (expression instanceof UnaryOperation) {
                    UnaryOperation up = (UnaryOperation) expression;
                    if (up.getOperator() == UnaryOperation.Operator.MINUS
                            && up.getExpression() instanceof Scalar) {
                        value = "-" + ((Scalar) up.getExpression()).getStringValue();
                        break;
                    }
                }
                 */
            }
            retval.add(new ConstantDeclarationInfo(identifier, value, constantDeclaration));
        }
        return retval;
    }

    @Override
    public Kind getKind() {
        return Kind.CONSTANT;
    }

    @CheckForNull
    private static String getConstantValue(Expression expr) {
        if (expr instanceof Scalar) {
            return ((Scalar) expr).getStringValue();
        }
        if (expr instanceof UnaryOperation) {
            UnaryOperation up = (UnaryOperation) expr;
            if (up.getOperator() == UnaryOperation.Operator.MINUS
                    && up.getExpression() instanceof Scalar) {
                return "-" + ((Scalar) up.getExpression()).getStringValue();
            }
        }
        if (expr instanceof ArrayCreation) {
            return getConstantValue((ArrayCreation) expr);
        }
        return null;
    }

    private static String getConstantValue(ArrayCreation expr) {
        StringBuilder sb = new StringBuilder("["); //NOI18N
        boolean itemAdded = false;
        List<ArrayElement> elements = expr.getElements();
        if (elements.size() > 0) {
            ArrayElement firstElement = elements.get(0);
            Expression key = firstElement.getKey();
            if (key != null) {
                String convertedKey = getConstantValue(key);
                if (convertedKey != null) {
                    sb.append(convertedKey);
                    sb.append(" => "); //NOI18N
                }
            }
            String convertedValue = getConstantValue(firstElement.getValue());
            if (convertedValue != null) {
                sb.append(convertedValue);
                itemAdded = true;
            } else {
                // Case when element exist but value was not converted to output.
                sb.append("..."); //NOI18N
            }
        }
        if (itemAdded && elements.size() > 1) {
            sb.append(",..."); //NOI18N
        }
        sb.append("]"); //NOI18N
        return sb.toString();
    }

}
