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
import java.util.Collections;
import java.util.List;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Convert simple closures(lambda functions) to arrow functions(PHP 7.4).
 *
 * e.g.
 * <pre>
 * // before: simple closure i.e. there is just one return statement
 * function($x) use ($y): int {
 *     return $x + $y;
 * };
 *
 * // after: arrow function
 * fn($x): int => $x + $y;
 * </pre>
 */
public class ArrowFunctionSuggestion extends SuggestionRule {

    private static final String HINT_ID = "Arrow.Function.Suggestion"; // NOI18N

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("ArrowFunctionSuggestion.Description=Allows you to change closure to arrow function.")
    public String getDescription() {
        return Bundle.ArrowFunctionSuggestion_Description();
    }

    @Override
    @NbBundle.Messages("ArrowFunctionSuggestion.DisplayName=Arrow Function")
    public String getDisplayName() {
        return Bundle.ArrowFunctionSuggestion_DisplayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final BaseDocument doc = context.doc;
        int caretOffset = getCaretOffset();
        OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, doc);
        if (lineBounds.containsInclusive(caretOffset)) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null && isAtLeastPhp74(fileObject)) {
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, this, context.doc, lineBounds);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    protected boolean isAtLeastPhp74(FileObject fileObject) {
        return CodeUtils.isPhpVersionGreaterThan(fileObject, PhpVersion.PHP_73);
    }

    //~ inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final ArrowFunctionSuggestion suggestion;
        private final BaseDocument document;
        private final OffsetRange lineRange;
        private final List<FixInfo> fixInfos = new ArrayList<>();

        public CheckVisitor(FileObject fileObject, ArrowFunctionSuggestion suggestion, BaseDocument document, OffsetRange lineRange) {
            this.fileObject = fileObject;
            this.suggestion = suggestion;
            this.document = document;
            this.lineRange = lineRange;
        }

        @NbBundle.Messages("ArrowFunctionSuggestion.Hint.Description=You can use arrow function")
        public List<Hint> getHints() {
            List<Hint> hints = new ArrayList<>();
            // deal with an inner function as a priority, in the case of nested functions
            for (int i = fixInfos.size() - 1; 0 <= i; i--) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return Collections.emptyList();
                }
                hints.add(new Hint(suggestion, Bundle.ArrowFunctionSuggestion_Hint_Description(), fileObject, lineRange, createFixes(fixInfos.get(i)), 500));
            }
            return hints;
        }

        private List<HintFix> createFixes(FixInfo fixInfo) {
            List<HintFix> hintFixes = new ArrayList<>();
            hintFixes.add(fixInfo.createFix(document));
            return hintFixes;
        }

        @Override
        public void scan(ASTNode node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node != null && (VerificationUtils.isBefore(node.getStartOffset(), lineRange.getEnd()))) {
                super.scan(node);
            }
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            OffsetRange nodeRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (lineRange.overlaps(nodeRange)) {
                processLambdaFunctionDeclaration(node);
            }
        }

        private void processLambdaFunctionDeclaration(LambdaFunctionDeclaration node) {
            Block body = node.getBody();
            if (body != null) {
                List<Statement> statements = body.getStatements();
                if (statements.size() == 1) {
                    Statement statement = statements.get(0);
                    if (statement instanceof ReturnStatement) {
                        Expression expression = ((ReturnStatement) statement).getExpression();
                        if (expression != null) {
                            fixInfos.add(new FixInfo(node, expression));
                        }
                    }
                }
            }
            super.visit(node);
        }
    }

    private static final class FixInfo {

        private final LambdaFunctionDeclaration lambdaFunction;
        private final Expression returnExpression;

        public FixInfo(LambdaFunctionDeclaration lambdaFunction, Expression returnExpression) {
            this.lambdaFunction = lambdaFunction;
            this.returnExpression = returnExpression;
        }

        public OffsetRange getLambdaFunctionDeclarationRange() {
            return new OffsetRange(lambdaFunction.getStartOffset(), lambdaFunction.getEndOffset());
        }

        public String getFormalParameter(BaseDocument document) throws BadLocationException {
            List<FormalParameter> parameters = lambdaFunction.getFormalParameters();
            if (parameters.isEmpty()) {
                return ""; // NOI18N
            }
            int startOffset = parameters.get(0).getStartOffset();
            int endOffset = parameters.get(parameters.size() - 1).getEndOffset();
            return document.getText(startOffset, endOffset - startOffset);
        }

        public String getReturnType(BaseDocument document) throws BadLocationException {
            Expression returnType = lambdaFunction.getReturnType();
            if (returnType != null) {
                return ": " + document.getText(returnType.getStartOffset(), returnType.getEndOffset() - returnType.getStartOffset()); // NOI18N
            }
            return ""; // NOI18N
        }

        public String getReturnExpression(BaseDocument document) throws BadLocationException {
            return document.getText(returnExpression.getStartOffset(), returnExpression.getEndOffset() - returnExpression.getStartOffset());
        }

        public String getReference() {
            return lambdaFunction.isReference() ? "&" : ""; // NOI18N
        }

        public HintFix createFix(BaseDocument document) {
            return new Fix(this, document);
        }

    }

    private static final class Fix implements HintFix {

        private final FixInfo fixInfo;
        private final BaseDocument document;

        private Fix(FixInfo fixInfo, BaseDocument document) {
            this.fixInfo = fixInfo;
            this.document = document;
        }

        @Override
        @NbBundle.Messages("ArrowFunctionSuggestion.Fix.Description=Use Arrow Function")
        public String getDescription() {
            return Bundle.ArrowFunctionSuggestion_Fix_Description();
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(document);
            OffsetRange lambdaFunctionRange = fixInfo.getLambdaFunctionDeclarationRange();
            // NOTE: comments are removed
            String arrowFunctionText = String.format("fn%s(%s)%s => %s", // NOI18N
                    fixInfo.getReference(),
                    fixInfo.getFormalParameter(document),
                    fixInfo.getReturnType(document),
                    fixInfo.getReturnExpression(document)
            );
            edits.replace(lambdaFunctionRange.getStart(), lambdaFunctionRange.getLength(), arrowFunctionText, true, 0);
            edits.apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }
}
