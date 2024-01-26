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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo.Kind;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;
import org.netbeans.modules.php.editor.parser.astnodes.UnpackableArrayElement;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class ClassConstantDeclarationInfo extends ASTNodeInfo<Identifier> {
    // Array display is stopped after this length of string is reached.
    private static final int ARRAY_CUT_LENGTH = 50;
    private static final String UNKNOWN_VALUE = "?"; //NOI18N

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
            name = iteratorNames.next();
            Expression initializer = iteratorInitializers.next();
            String value = getConstantValue(initializer);
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

    @CheckForNull
    public String getDeclaredType() {
        return VariousUtils.getDeclaredType(constantDeclaration.getConstType());
    }

    public PhpModifiers getAccessModifiers() {
        return PhpModifiers.fromBitMask(constantDeclaration.getModifier());
    }

    @CheckForNull
    protected static String getConstantValue(Expression expr) {
        if (expr instanceof Scalar) {
            return ((Scalar) expr).getStringValue();
        }
        if (expr instanceof UnaryOperation) {
            UnaryOperation up = (UnaryOperation) expr;
            if (up.getOperator() == UnaryOperation.Operator.MINUS
                    && up.getExpression() instanceof Scalar) {
                return "-" + ((Scalar) up.getExpression()).getStringValue(); //NOI18N
            }
        }
        if (expr instanceof ArrayCreation) {
            return getConstantValue((ArrayCreation) expr);
        }
        return null;
    }

    @NbBundle.Messages({
        "# {0} - undisplayed size",
        "MoreElementsDesc={0} more"
    })
    private static String getConstantValue(ArrayCreation expr) {
        StringBuilder sb = new StringBuilder("["); //NOI18N
        Integer displayedElements = 0;
        List<ArrayElement> elements = expr.getElements();
        for (ArrayElement element : elements) {
            if (displayedElements > 0) {
                sb.append(", "); //NOI18N
            }
            sb.append(getConstantValue(element));
            displayedElements++;
            if (sb.length() > ARRAY_CUT_LENGTH) {
                break;
            }
        }
        if (displayedElements < elements.size()) {
            sb.append(", ... (").append(Bundle.MoreElementsDesc(elements.size() - displayedElements)).append(")"); //NOI18N
        }
        sb.append("]"); //NOI18N
        return sb.toString();
    }

    private static String getConstantValue(ArrayElement element) {
        if (element instanceof UnpackableArrayElement) {
            String innerContent = getConstantValue(element.getValue());
            return innerContent != null ? "..." + innerContent : UNKNOWN_VALUE; //NOI18N
        }
        StringBuilder sb = new StringBuilder();
        Expression key = element.getKey();
        if (key != null) {
            String convertedKey = getConstantValue(key);
            sb.append(convertedKey != null ? convertedKey : UNKNOWN_VALUE);
            sb.append(" => "); //NOI18N
        }
        String convertedValue = getConstantValue(element.getValue());
        sb.append(convertedValue != null ? convertedValue : UNKNOWN_VALUE);
        return sb.toString();
    }

}
