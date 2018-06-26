/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression.OperatorType;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class IdenticalComparisonSuggestion extends SuggestionRule {

    private static final String HINT_ID = "Identical.Comparison.Hint"; //NOI18N

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        final BaseDocument doc = context.doc;
        int caretOffset = getCaretOffset();
        OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, doc);
        if (lineBounds.containsInclusive(caretOffset)) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject == null) {
                return;
            }
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor(fileObject, phpParseResult.getModel(), context.doc, lineBounds);
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            hints.addAll(checkVisitor.getHints());
        }
    }

    private class CheckVisitor extends DefaultVisitor {
        private final FileObject fileObject;
        private final Model model;
        private final List<FixInfo> fixInfos = new ArrayList<>();
        private final List<Hint> hints = new ArrayList<>();
        private final BaseDocument doc;
        private final OffsetRange lineRange;

        public CheckVisitor(FileObject fileObject, Model model, BaseDocument doc, OffsetRange lineRange) {
            this.fileObject = fileObject;
            this.model = model;
            this.doc = doc;
            this.lineRange = lineRange;
        }

        @Messages("IdenticalComparisonDesc=Comparison with \"equal (==)\" operator should be avoided, use \"identical (===)\" operator instead")
        public List<Hint> getHints() {
            for (FixInfo fixInfo : fixInfos) {
                hints.add(new Hint(IdenticalComparisonSuggestion.this, Bundle.IdenticalComparisonDesc(), fileObject, fixInfo.getScopeRange(), createFixes(fixInfo), 500));
            }
            return hints;
        }

        private List<HintFix> createFixes(FixInfo fixInfo) {
            List<HintFix> hintFixes = new ArrayList<>();
            hintFixes.add(new WithoutTypeFix(fixInfo, doc));
            if (!fixInfo.getTypeName().isEmpty()) {
                hintFixes.add(new WithRightTypeFix(fixInfo, doc));
            }
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
        public void visit(InfixExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (lineRange.containsInclusive(node.getStartOffset())) {
                processExpression(node);
            }
        }

        private void processExpression(InfixExpression node) {
            if (node.getOperator().equals(OperatorType.IS_EQUAL)) {
                int start = node.getLeft().getEndOffset();
                int length = node.getRight().getStartOffset() - start;
                OffsetRange scopeRange = new OffsetRange(node.getLeft().getStartOffset(), node.getRight().getEndOffset());
                fixInfos.add(new FixInfo(scopeRange, start, length, extractTypeName(node.getLeft())));
            }
            scan(node.getLeft());
            scan(node.getRight());
        }

        private String extractTypeName(Expression node) {
            String retval = ""; //NOI18N
            if (node instanceof Variable) {
                Variable variable = (Variable) node;
                retval = extractTypeName(variable);
            }
            return retval;
        }

        private String extractTypeName(Variable variable) {
            VariableScope variableScope = model.getVariableScope(variable.getStartOffset());
            Collection<? extends VariableName> declaredVariables = variableScope.getDeclaredVariables();
            String originalVariableName = CodeUtils.extractVariableName(variable);
            VariableName exactVariable = getExactVariable(declaredVariables, originalVariableName);
            Collection<? extends String> typeNames = getTypeNames(exactVariable, variable.getStartOffset());
            return getCastableType(typeNames);
        }

        private VariableName getExactVariable(Collection<? extends VariableName> declaredVariables, String originalVariableName) {
            VariableName retval = null;
            for (VariableName variableName : declaredVariables) {
                if (variableName.getName().equals(originalVariableName)) {
                    retval = variableName;
                    break;
                }
            }
            return retval;
        }

        private Collection<? extends String> getTypeNames(VariableName variableName, int offset) {
            Collection<? extends String> retval = null;
            if (variableName != null) {
                retval = variableName.getTypeNames(offset);
            }
            return retval;
        }

        private String getCastableType(Collection<? extends String> typeNames) {
            String retval = ""; //NOI18N
            if (typeNames != null && typeNames.size() == 1) {
                String typeName = ModelUtils.getFirst(typeNames);
                if (typeName != null) {
                    retval = resolveCastableType(typeName); //NOI18N
                }
            }
            return retval;
        }

        private String resolveCastableType(String typeName) {
            String retval = ""; //NOI18N
            switch (typeName) {
                case Type.INT:
                case Type.INTEGER:
                case Type.DOUBLE:
                case Type.FLOAT:
                case Type.BOOL:
                case Type.BOOLEAN:
                case Type.STRING:
                case Type.ARRAY:
                    retval = typeName;
                    break;
                case Type.REAL:
                    retval = Type.FLOAT;
                    break;
                default:
                    // no-op
            }
            return retval;
        }

    }

    private abstract class TypeFix implements HintFix {
        protected final BaseDocument doc;
        protected final FixInfo fixInfo;

        public TypeFix(FixInfo fixInfo, BaseDocument doc) {
            this.fixInfo = fixInfo;
            this.doc = doc;
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

    private class WithRightTypeFix extends TypeFix {

        public WithRightTypeFix(FixInfo fixInfo, BaseDocument doc) {
            super(fixInfo, doc);
        }

        @Override
        @Messages({
            "# {0} - Type name",
            "WithRightTypeFixDesc=Fix comparison: === ({0}) "
        })
        public String getDescription() {
            return Bundle.WithRightTypeFixDesc(fixInfo.getTypeName());
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            edits.replace(fixInfo.getStart(), fixInfo.getLength(), " === (" + fixInfo.getTypeName() + ") ", true, 0); //NOI18N
            edits.apply();
        }

    }

    private class WithoutTypeFix extends TypeFix {

        public WithoutTypeFix(FixInfo fixInfo, BaseDocument doc) {
            super(fixInfo, doc);
        }

        @Override
        @Messages("WithoutTypeFixDesc=Fix comparison: ===")
        public String getDescription() {
            return Bundle.WithoutTypeFixDesc();
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            edits.replace(fixInfo.getStart(), fixInfo.getLength(), " === ", true, 0); //NOI18N
            edits.apply();
        }

    }

    private static class FixInfo {
        private final String typeName;
        private final int length;
        private final int start;
        private final OffsetRange scopeRange;

        public FixInfo(OffsetRange scopeRange, int start, int length, String typeName) {
            this.scopeRange = scopeRange;
            this.start = start;
            this.length = length;
            this.typeName = typeName;
        }

        public int getStart() {
            return start;
        }

        public int getLength() {
            return length;
        }

        public String getTypeName() {
            return typeName;
        }

        public OffsetRange getScopeRange() {
            return scopeRange;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("IdenticalComparisonHintDesc=You should use \"identical\" instead of \"equal\" comparison to have better control over your code.")
    public String getDescription() {
        return Bundle.IdenticalComparisonHintDesc();
    }

    @Override
    @Messages("IdenticalComparisonHintDispName=Identical Comparisons")
    public String getDisplayName() {
        return Bundle.IdenticalComparisonHintDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.CURRENT_LINE_WARNING;
    }

}
