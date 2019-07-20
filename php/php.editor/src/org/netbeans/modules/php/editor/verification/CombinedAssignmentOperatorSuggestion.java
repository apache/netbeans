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
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.editor.util.StringEscapeUtils;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ParenthesisExpression;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Suggest to use combind assignment operaotors.
 *
 * e.g.
 * <pre>
 * // + operator
 * $x = $x + 100; // before
 * $x += 100; // after
 *
 * // ?? operator
 * $this->something->data['messages']['id'] = $this->something->data['messages']['id'] ?? "value";
 * $this->something->data['messages']['id'] ??= "value";
 * </pre>
 */
public class CombinedAssignmentOperatorSuggestion extends SuggestionRule {

    private static final String HINT_ID = "Combined.Assignment.Operator.Suggestion"; // NOI18N

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @NbBundle.Messages("CombinedAssignmentOperatorSuggestion.Description=Allows you to change to a combined assignment operator.")
    public String getDescription() {
        return Bundle.CombinedAssignmentOperatorSuggestion_Description();
    }

    @Override
    @NbBundle.Messages("CombinedAssignmentOperatorSuggestion.DisplayName=Combined Assignment Operators")
    public String getDisplayName() {
        return Bundle.CombinedAssignmentOperatorSuggestion_DisplayName();
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
            if (fileObject != null) {
                CheckVisitor checkVisitor = new CheckVisitor(fileObject, this, context.doc, lineBounds);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    protected PhpVersion getPhpVersion(FileObject fileObject) {
        return CodeUtils.getPhpVersion(fileObject);
    }

    private boolean isAtLeastPhp74(FileObject fileObject) {
        return getPhpVersion(fileObject).compareTo(PhpVersion.PHP_74) >= 0;
    }

    //~ inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final CombinedAssignmentOperatorSuggestion suggestion;
        private final BaseDocument document;
        private final OffsetRange lineRange;
        private final List<FixInfo> fixInfos = new ArrayList<>();

        public CheckVisitor(FileObject fileObject, CombinedAssignmentOperatorSuggestion suggestion, BaseDocument document, OffsetRange lineRange) {
            this.fileObject = fileObject;
            this.suggestion = suggestion;
            this.document = document;
            this.lineRange = lineRange;
        }

        @NbBundle.Messages("CombinedAssignmentOperatorSuggestion.Hint.Description=You can use combined assignment operator")
        public List<Hint> getHints() {
            List<Hint> hints = new ArrayList<>();
            for (FixInfo fixInfo : fixInfos) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return Collections.emptyList();
                }
                hints.add(new Hint(suggestion, Bundle.CombinedAssignmentOperatorSuggestion_Hint_Description(), fileObject, lineRange, createFixes(fixInfo), 500));
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
        public void visit(Assignment node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            OffsetRange nodeRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (lineRange.overlaps(nodeRange)) {
                processAssignment(node);
            }
        }

        private void processAssignment(Assignment node) {
            Expression rightHandSide = node.getRightHandSide();
            if (suggestion.isAtLeastPhp74(fileObject)
                    && rightHandSide instanceof ConditionalExpression) {
                // ?? operator
                processConditionalExpression((ConditionalExpression) rightHandSide, node);
            } else if (rightHandSide instanceof InfixExpression) {
                // +, -, *, /, %, etc.
                processInfixExpression((InfixExpression) rightHandSide, node);
            }

        }

        private void processConditionalExpression(ConditionalExpression conditionalExpression, Assignment assignment) {
            if (conditionalExpression.getOperator() == ConditionalExpression.OperatorType.COALESCE) {
                Expression condition = conditionalExpression.getCondition();
                addFixInfo(conditionalExpression.getOperator().toString(), condition, conditionalExpression.getIfFalse(), assignment);
            }
        }

        private void processInfixExpression(InfixExpression infixExpression, Assignment assignment) {
            if (isValidInfixExpressionOperator(infixExpression.getOperator())) {
                Expression left = infixExpression.getLeft();
                Expression right = infixExpression.getRight();
                String operator = infixExpression.getOperator().toString();
                // convert only simple infix expressions
                // e.g. don't convert the following case
                // $y = 10; $x = 5;
                // $y = $y * $x + 3; // 53
                // $y *= $x + 3; // 80
                if (!(left instanceof InfixExpression)
                        && !(right instanceof InfixExpression)
                        && !(right instanceof ParenthesisExpression)) {
                    addFixInfo(operator, left, right, assignment);
                }
            }
        }

        private void addFixInfo(String operator, Expression removalExpression, Expression removalEndExpression, Assignment assignment) {
            VariableBase leftHandSide = assignment.getLeftHandSide();
            if (removalExpression instanceof VariableBase
                    && leftHandSide.toString().equals(removalExpression.toString())) { // compare them using another way? e.g use the lexer
                int removalStart = leftHandSide.getEndOffset();
                int removalEnd = removalEndExpression.getStartOffset();
                fixInfos.add(new FixInfo(operator, new OffsetRange(removalStart, removalEnd)));
            }
        }

        private static boolean isValidInfixExpressionOperator(InfixExpression.OperatorType operator) {
            return operator == InfixExpression.OperatorType.PLUS
                    || operator == InfixExpression.OperatorType.MINUS
                    || operator == InfixExpression.OperatorType.MUL
                    || operator == InfixExpression.OperatorType.DIV
                    || operator == InfixExpression.OperatorType.CONCAT
                    || operator == InfixExpression.OperatorType.MOD
                    || operator == InfixExpression.OperatorType.SL
                    || operator == InfixExpression.OperatorType.SR
                    || operator == InfixExpression.OperatorType.AND
                    || operator == InfixExpression.OperatorType.OR
                    || operator == InfixExpression.OperatorType.XOR
                    || operator == InfixExpression.OperatorType.POW;
        }

    }

    private static final class FixInfo {

        private final String operator;
        private final OffsetRange removalOffsetRange;

        public FixInfo(String operator, OffsetRange removingOffsetRange) {
            this.operator = operator;
            this.removalOffsetRange = removingOffsetRange;
        }

        public String getOperator() {
            return operator;
        }

        public OffsetRange getRemovalOffsetRange() {
            return removalOffsetRange;
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
        @NbBundle.Messages({
            "# {0} - combined operator",
            "CombinedAssignmentOperatorSuggestion.Fix.Description=Use Combined Assignment Operator\"{0}\""
        })
        public String getDescription() {
            // escape "<<"
            return Bundle.CombinedAssignmentOperatorSuggestion_Fix_Description(StringEscapeUtils.escapeHtml(fixInfo.getOperator()) + "="); // NOI18N
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(document);
            OffsetRange removalOffsetRange = fixInfo.getRemovalOffsetRange();
            String combinedOperator = String.format(" %s= ", fixInfo.getOperator()); // NOI18N
            edits.replace(removalOffsetRange.getStart(), removalOffsetRange.getLength(), combinedOperator, true, 0);
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
