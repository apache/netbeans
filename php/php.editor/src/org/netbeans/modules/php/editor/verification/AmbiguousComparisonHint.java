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
package org.netbeans.modules.php.editor.verification;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression.OperatorType;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class AmbiguousComparisonHint extends HintRule {

    private static final String HINT_ID = "Ambiguous.Comparison.Hint"; //NOI18N

    @Override
    public void invoke(final PHPRuleContext context, final List<Hint> hints) {
        final PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        final FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject == null) {
            return;
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        final CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc);
        phpParseResult.getProgram().accept(checkVisitor);
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        hints.addAll(checkVisitor.getHints());
    }

    private class CheckVisitor extends DefaultTreePathVisitor {
        private final FileObject fileObject;
        private final BaseDocument doc;
        private final List<InfixExpression> expressions = new ArrayList<>();
        private final List<Hint> hints = new ArrayList<>();

        public CheckVisitor(final FileObject fileObject, final BaseDocument doc) {
            this.fileObject = fileObject;
            this.doc = doc;
        }

        public List<Hint> getHints() {
            for (InfixExpression infixExpression : expressions) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return Collections.emptyList();
                }
                createHint(infixExpression);
            }
            return hints;
        }

        @Messages("AmbiguousComparisonHintCustom=Possible accidental comparison found. Check if you wanted to use '=' instead.")
        private void createHint(final InfixExpression node) {
            final OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (showHint(offsetRange, doc)) {
                hints.add(new Hint(
                        AmbiguousComparisonHint.this,
                        Bundle.AmbiguousComparisonHintCustom(),
                        fileObject,
                        offsetRange,
                        Collections.<HintFix>singletonList(new AssignmentHintFix(doc, node)),
                        500));
            }
        }

        @Override
        public void visit(final InfixExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            final OperatorType operator = node.getOperator();
            if (OperatorType.IS_EQUAL.equals(operator) || OperatorType.IS_IDENTICAL.equals(operator)) {
                checkPathForNode(node);
            }
            super.visit(node);
        }

        private void checkPathForNode(final InfixExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            final List<ASTNode> path = getPath();
            if (path.isEmpty() || !isValidContext(path)) {
                expressions.add(node);
            }
        }

        private boolean isValidContext(final List<ASTNode> path) {
            boolean result = false;
            for (ASTNode node : path) {
                if (isConditionalNode(node) || node instanceof Assignment || node instanceof ReturnStatement || node instanceof FunctionInvocation) {
                    result = true;
                    break;
                } else if (node instanceof Block) {
                    result = false;
                    break;
                }
            }
            return result;
        }

        private boolean isConditionalNode(final ASTNode node) {
            return node instanceof IfStatement
                    || node instanceof WhileStatement
                    || node instanceof DoStatement
                    || node instanceof ForStatement
                    || node instanceof ForEachStatement
                    || node instanceof ConditionalExpression
                    || node instanceof SwitchCase;
        }

    }

    private static class AssignmentHintFix implements HintFix {
        private final BaseDocument doc;
        private final InfixExpression expression;
        private static final String ASSIGNMENT = " = "; //NOI18N

        public AssignmentHintFix(final BaseDocument doc, final InfixExpression expression) {
            this.doc = doc;
            this.expression = expression;
        }

        @Override
        @Messages("AssignmentHintFixDisp=Change Comparison to Assignment")
        public String getDescription() {
            return Bundle.AssignmentHintFixDisp();
        }

        @Override
        public void implement() throws Exception {
            final EditList edits = new EditList(doc);
            final OffsetRange offsetRange = new OffsetRange(expression.getLeft().getEndOffset(), expression.getRight().getStartOffset());
            edits.replace(offsetRange.getStart(), offsetRange.getLength(), ASSIGNMENT, true, 0);
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

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("AmbiguousComparisonHintDescName=Tries to reveal typos in assignments (assignments with more than one assignment operator).")
    public String getDescription() {
        return Bundle.AmbiguousComparisonHintDescName();
    }

    @Override
    @Messages("AmbiguousComparisonHintDispName=Ambiguous Comparison")
    public String getDisplayName() {
        return Bundle.AmbiguousComparisonHintDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

}
