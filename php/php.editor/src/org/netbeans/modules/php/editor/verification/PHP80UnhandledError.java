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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.modules.csl.api.Error;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.Dispatch;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MatchArm;
import org.netbeans.modules.php.editor.parser.astnodes.MatchExpression;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamedArgument;
import org.netbeans.modules.php.editor.parser.astnodes.ThrowExpression;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.UnionType;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Note: don't check the "mixed" type because it is available as a class name in
 * PHP 7.4 or older.
 */
public final class PHP80UnhandledError extends UnhandledErrorRule {

    @NbBundle.Messages("PHP80UnhandledError.displayName=Language feature not compatible with PHP version indicated in project settings")
    @Override
    public String getDisplayName() {
        return Bundle.PHP80UnhandledError_displayName();
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
            TokenSequence<PHPTokenId> ts = phpParseResult.getSnapshot().getTokenHierarchy().tokenSequence(PHPTokenId.language());
            assert ts != null;
            errors.addAll(checkVisitor.getErrors(ts));
        }
    }

    private static boolean appliesTo(FileObject fileObject) {
        return CodeUtils.isPhpVersionLessThan(fileObject, PhpVersion.PHP_80);
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final List<VerificationError> errors = new ArrayList<>();
        private final FileObject fileObject;
        private final List<ASTNode> lastParams = new ArrayList<>();
        private final List<ASTNode> lastLexicalVariables = new ArrayList<>();
        private boolean isSameAsThrowStatement = false;

        public CheckVisitor(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        public Collection<VerificationError> getErrors(TokenSequence<PHPTokenId> ts) {
            checkTrailingCommas(ts, lastParams);
            checkTrailingCommas(ts, lastLexicalVariables);
            return Collections.unmodifiableCollection(errors);
        }

        @Override
        public void visit(CatchClause node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkNonCapturingCatches(node);
            super.visit(node);
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            addLastParam(node.getFormalParameters());
            checkStaticReturnType(node.getReturnType());
            super.visit(node);
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            addLastParam(node.getFormalParameters());
            checkStaticReturnType(node.getReturnType());
            addLastLexicalVariable(node.getLexicalVariables());
            super.visit(node);
        }

        @Override
        public void visit(ArrowFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkStaticReturnType(node.getReturnType());
            super.visit(node);
        }

        @Override
        public void visit(ExpressionStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.getExpression() instanceof ThrowExpression) {
                isSameAsThrowStatement = true;
            }
            super.visit(node);
            isSameAsThrowStatement = false;
        }

        @Override
        public void visit(ThrowExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkThrowExpression(node);
            super.visit(node);
        }

        @Override
        public void visit(StaticConstantAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkClassNameLiteralOnObject(node);
            super.visit(node);
        }

        @Override
        public void visit(MatchArm node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkMatchExpression(node);
            super.visit(node);
        }

        @Override
        public void visit(MatchExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkMatchExpression(node);
            super.visit(node);
        }

        @Override
        public void visit(UnionType node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkUnionType(node);
            super.visit(node);
        }

        @Override
        public void visit(FieldAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkNullsafeOperator(node);
            super.visit(node);
        }

        @Override
        public void visit(MethodInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkNullsafeOperator(node);
            super.visit(node);
        }

        @Override
        public void visit(Attribute attribute) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkAttributeSyntax(attribute);
            super.visit(attribute);
        }

        @Override
        public void visit(FormalParameter node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkConstructorPropertyPromotion(node);
            super.visit(node);
        }

        @Override
        public void visit(NamedArgument node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkNamedArgument(node);
            super.visit(node);
        }

        private void addLastParam(List<FormalParameter> parameters) {
            if (!parameters.isEmpty()) {
                lastParams.add(parameters.get(parameters.size() - 1));
            }
        }

        private void addLastLexicalVariable(List<Expression> lexicalVariables) {
            if (!lexicalVariables.isEmpty()) {
                lastLexicalVariables.add(lexicalVariables.get(lexicalVariables.size() - 1));
            }
        }

        private void checkNonCapturingCatches(CatchClause node) {
            if (node.getVariable() == null) {
                createError(node);
            }
        }

        private void checkTrailingCommas(TokenSequence<PHPTokenId> ts, List<ASTNode> nodes) {
            if (!nodes.isEmpty()) {
                try {
                    for (ASTNode node: nodes) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }

                        // find a comma ","
                        Token<? extends PHPTokenId> token = findNextToken(ts, node.getEndOffset());
                        if (token != null
                                && token.id() == PHPTokenId.PHP_TOKEN
                                && TokenUtilities.textEquals(token.text(), ",")) { // NOI18N
                            createError(node);
                        }
                    }
                } finally {
                    nodes.clear();
                }
            }
        }

        @CheckForNull
        private Token<? extends PHPTokenId> findNextToken(TokenSequence<PHPTokenId> ts, int startOffset) {
            ts.move(startOffset);
            if (!ts.moveNext()) {
                return null;
            }
            if (TokenUtilities.textEquals(ts.token().text(), ")")) { // NOI18N
                return null;
            }
            // function name($a, $b ,){}
            // function name($a, $b/* comment */,){}
            // function name($a, $b/** comment */,){}
            List<PHPTokenId> ignores = Arrays.asList(
                    PHPTokenId.WHITESPACE,
                    PHPTokenId.PHP_COMMENT_START,
                    PHPTokenId.PHP_COMMENT,
                    PHPTokenId.PHP_COMMENT_END,
                    PHPTokenId.PHPDOC_COMMENT_START,
                    PHPTokenId.PHPDOC_COMMENT,
                    PHPTokenId.PHPDOC_COMMENT_END
            );
            return LexUtilities.findNext(ts, ignores);
        }

        private void checkThrowExpression(Expression expression) {
            if (!isSameAsThrowStatement) {
                createError(expression);
            }
        }

        private void checkClassNameLiteralOnObject(StaticConstantAccess node) {
            if ("class".equals(node.getConstantName().getName())) { // NOI18N
                Expression dispatcher = node.getDispatcher();
                if (!(dispatcher instanceof NamespaceName)
                        && !(dispatcher instanceof Identifier)) {
                    // other than namespacename(e.g. \Foo\Bar::class) and identifier(e.g. Foo::class)
                    // i.e. in case of dinamic class name (e.g. $object::class, create()::class)
                    createError(node);
                }
            }
        }

        private void checkUnionType(UnionType node) {
            createError(node);
        }

        private void checkMatchExpression(MatchExpression node) {
            createError(node);
        }

        private void checkMatchExpression(MatchArm node) {
            createError(node);
        }

        private void checkStaticReturnType(Expression returnType) {
            // don't check union types because they are already errors (see checkUnionType())
            Expression type = returnType;
            if (type instanceof NullableType) {
                type = ((NullableType) type).getType();
            }
            if (type instanceof Identifier && isStatic((Identifier) type)) {
                createError(returnType);
            }
        }

        private void checkNullsafeOperator(Dispatch dispatch) {
            if (dispatch.isNullsafe()) {
                createError(dispatch);
            }
        }

        private void checkAttributeSyntax(Attribute attribute) {
            createError(attribute);
        }

        private void checkConstructorPropertyPromotion(FormalParameter parameter) {
            if (parameter.getModifier() != 0) {
                createError(parameter);
            }
        }

        private void checkNamedArgument(NamedArgument namedArgument) {
            createError(namedArgument);
        }

        private void createError(ASTNode node) {
            createError(node.getStartOffset(), node.getEndOffset());
        }

        private void createError(int startOffset, int endOffset) {
            errors.add(new PHP80VersionError(fileObject, startOffset, endOffset));
        }

        private static boolean isStatic(Identifier identifier) {
            return Type.STATIC.equals(identifier.getName());
        }
    }

    private static final class PHP80VersionError extends VerificationError {

        private static final String KEY = "Php.Version.80"; // NOI18N

        private PHP80VersionError(FileObject fileObject, int startOffset, int endOffset) {
            super(fileObject, startOffset, endOffset);
        }

        @NbBundle.Messages("PHP80VersionError.displayName=Language feature not compatible with PHP version indicated in project settings")
        @Override
        public String getDisplayName() {
            return Bundle.PHP80VersionError_displayName();
        }

        @NbBundle.Messages("PHP80VersionError.description=Detected language features not compatible with PHP version indicated in project settings")
        @Override
        public String getDescription() {
            return Bundle.PHP80VersionError_description();
        }

        @Override
        public String getKey() {
            return KEY;
        }
    }

}
