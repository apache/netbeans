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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayDimension;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ClassName;
import org.netbeans.modules.php.editor.parser.astnodes.CompositionExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantVariable;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Quote;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public final class PHP83UnhandledError extends UnhandledErrorRule {

    @NbBundle.Messages("PHP83UnhandledError.displayName=Language feature not compatible with PHP version indicated in project settings")
    @Override
    public String getDisplayName() {
        return Bundle.PHP83UnhandledError_displayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Error> errors) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject != null
                && appliesTo(fileObject)) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor(fileObject);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            errors.addAll(checkVisitor.getErrors());
        }
    }

    private static boolean appliesTo(FileObject fileObject) {
        return CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_83);
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public Collection<VerificationError> getErrors() {
            return Collections.unmodifiableCollection(errors);
        }

        @Override
        public void visit(StaticConstantAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isDynamicName()) {
                createError(node.getConstant());
            }
            super.visit(node);
        }

        @Override
        public void visit(ConstantDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.getConstType() != null) {
                // e.g. const Type|null CONST_NAME = null;
                createError(node.getConstType());
            }
            super.visit(node);
        }

        @Override
        public void visit(StaticStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // static $example = 1, $example2 = $variable;
            for (Expression expression : node.getExpressions()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (expression instanceof Assignment) {
                    Assignment assignment = (Assignment) expression;
                    Expression rightHandSide = assignment.getRightHandSide();
                    if (!isStaticScalarValueExpression(rightHandSide)) {
                        createError(node);
                        break;
                    }
                }
            }
            super.visit(node);
        }

        private boolean isStaticScalarValueExpression(Expression expression) {
            if (expression != null
                    && !isStaticScalarValue(expression)
                    && !isStaticOperation(expression)) {
                return false;
            }
            if (isStaticOperation(expression)) {
                if (expression instanceof InfixExpression) {
                    InfixExpression infixExpression = (InfixExpression) expression;
                    if (!isStaticScalarValueExpression(infixExpression.getLeft())
                            || !isStaticScalarValueExpression(infixExpression.getRight())) {
                        // e.g. $a + 1; example() * 3; "string" . $string;
                        return false;
                    }
                } else if (expression instanceof UnaryOperation) {
                    UnaryOperation unaryOperation = (UnaryOperation) expression;
                    if (!isStaticScalarValueExpression(unaryOperation.getExpression())) {
                        // e.g. !$variable;
                        return false;
                    }
                } else if (expression instanceof ConditionalExpression) {
                    ConditionalExpression conditionalExpression = (ConditionalExpression) expression;
                    if (!isStaticScalarValueExpression(conditionalExpression.getIfTrue())
                            || !isStaticScalarValueExpression(conditionalExpression.getIfFalse())
                            || !isStaticScalarValueExpression(conditionalExpression.getCondition())) {
                        // e.g. $a > 0 ? $a : -$a;
                        return false;
                    }
                } else if (expression instanceof ArrayCreation) {
                    ArrayCreation arrayCreation = (ArrayCreation) expression;
                    List<ArrayElement> elements = arrayCreation.getElements();
                    for (ArrayElement element : elements) {
                        if (!isStaticScalarValueExpression(element.getKey())
                                || !isStaticScalarValueExpression(element.getValue())) {
                            // e.g. ["a" => $variable];
                            return false;
                        }
                    }
                } else if (expression instanceof ExpressionArrayAccess) {
                    // e.g. CONSTANT[1]; "string"[2]; [1, 2, 3][0];
                    // CONSTANT[1][2]; MyClass::CONSTANT[1]; \Foo\CONSTANT[0]; namespace\CONSTANT[0];
                    ExpressionArrayAccess expressionArrayAccess = (ExpressionArrayAccess) expression;
                    Expression expr = expressionArrayAccess.getExpression();
                    ArrayDimension dimension = expressionArrayAccess.getDimension();
                    if ((!(expr instanceof Identifier) && !isStaticScalarValueExpression(expr)) // C[1]
                            || !isStaticScalarValueExpression(dimension.getIndex())) {
                        return false;
                    }
                }
            } else if (isNewExpression(expression)) {
                ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
                ClassName className = classInstanceCreation.getClassName();
                if (Type.STATIC.equals(CodeUtils.extractClassName(className))
                        || !isStaticScalarValueExpression(className.getName())
                        || (className.getName() instanceof ParenthesisExpression)) {
                    // e.g. new static; new (Example); new $variable;
                    return false;
                }
                for (Expression param : classInstanceCreation.ctorParams()) {
                    if (!isStaticScalarValueExpression(param)) {
                        // e.g. new stdClass($variable);
                        return false;
                    }
                }
            } else if (expression instanceof FieldAccess) {
                FieldAccess fieldAccess = (FieldAccess) expression;
                VariableBase dispatcher = fieldAccess.getDispatcher();
                // e.g.
                // OK: C->name; E::Case1->name;
                if (!(dispatcher instanceof ConstantVariable)
                        && !(dispatcher instanceof StaticConstantAccess)) {
                    // NG: $this->field;
                    return false;
                }
            } else if (expression instanceof StaticConstantAccess) {
                StaticConstantAccess staticConstantAccess = (StaticConstantAccess) expression;
                Expression constant = staticConstantAccess.getConstant();
                if (!(constant instanceof Identifier) && !isStaticScalarValueExpression(constant)) {
                    // e.g. Example::CONSTANT[$index];
                    return false;
                }
            } else if (expression instanceof Quote) {
                Quote quote = (Quote) expression;
                for (Expression expr : quote.getExpressions()) {
                    if (!isStaticScalarValueExpression(expr)) {
                        // e.g.
                        // static $example = <<<EOD
                        // something $variable
                        // EOD;
                        return false;
                    }
                }
            }
            return true;
        }

        private boolean isStaticScalarValue(Expression expression) {
            return expression instanceof Scalar
                    || expression instanceof NamespaceName
                    || expression instanceof StaticConstantAccess
                    || expression instanceof FieldAccess
                    || ((expression instanceof Quote) && ((Quote) expression).getQuoteType() == Quote.Type.HEREDOC)
                    || isNewExpression(expression);
        }

        private boolean isNewExpression(Expression expression) {
            return (expression instanceof ClassInstanceCreation)
                    && !((ClassInstanceCreation) expression).isAnonymous();
        }

        private boolean isStaticOperation(Expression expression) {
            return expression instanceof ExpressionArrayAccess
                    || expression instanceof ArrayCreation
                    || expression instanceof InfixExpression
                    || expression instanceof UnaryOperation
                    || expression instanceof ConditionalExpression;
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

        private void createError(OffsetRange offsetRange) {
            errors.add(new PHP83VersionError(fileObject, offsetRange.getStart(), offsetRange.getEnd()));
        }

        private void createError(int startOffset, int endOffset) {
            errors.add(new PHP83VersionError(fileObject, startOffset, endOffset));
        }
    }

    private static final class PHP83VersionError extends VerificationError {

        private static final String KEY = "Php.Version.83"; // NOI18N

        private PHP83VersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @NbBundle.Messages("PHP83VersionError.displayName=Language feature not compatible with PHP version indicated in project settings")
        @Override
        public String getDisplayName() {
            return Bundle.PHP83VersionError_displayName();
        }

        @NbBundle.Messages("PHP83VersionError.description=Detected language features not compatible with PHP version indicated in project settings")
        @Override
        public String getDescription() {
            return Bundle.PHP83VersionError_description();
        }

        @Override
        public String getKey() {
            return KEY;
        }
    }

}
