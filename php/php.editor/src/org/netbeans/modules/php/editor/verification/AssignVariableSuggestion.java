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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.IgnoreError;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Radek Matous
 */
public class AssignVariableSuggestion extends SuggestionRule {

    private static final List<String> LANGUAGE_CUNSTRUCTS = new ArrayList<>(Arrays.asList(new String[] {"die", "exit"})); //NOI18N

    @Override
    public String getId() {
        return "assign.variable.hint"; //NOI18N
    }

    @Override
    @Messages("AssignVariableHintDesc=Assign Return Value To New Variable")
    public String getDescription() {
        return Bundle.AssignVariableHintDesc();
    }

    @Override
    @Messages("AssignVariableHintDisplayName=Introduce Variable")
    public String getDisplayName() {
        return Bundle.AssignVariableHintDisplayName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject == null) {
            return;
        }
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        int caretOffset = getCaretOffset();
        OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, context.doc);
        if (lineBounds.containsInclusive(caretOffset)) {
            IntroduceFixVisitor introduceFixVisitor = new IntroduceFixVisitor(context.doc, lineBounds);
            phpParseResult.getProgram().accept(introduceFixVisitor);
            IntroduceFix variableFix = introduceFixVisitor.getIntroduceFix();
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (variableFix != null) {
                hints.add(new Hint(AssignVariableSuggestion.this, getDisplayName(),
                        fileObject, variableFix.getOffsetRange(),
                        Collections.<HintFix>singletonList(variableFix), 500));
            }
        }
    }

    private class IntroduceFixVisitor extends DefaultVisitor {
        private final BaseDocument doc;
        private final List<Variable> variables;
        private final OffsetRange lineBounds;
        private IntroduceFix fix;

        IntroduceFixVisitor(BaseDocument doc, OffsetRange lineBounds) {
            this.doc = doc;
            this.lineBounds = lineBounds;
            this.variables = new ArrayList<>();
        }

        @Override
        public void scan(ASTNode node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node != null && (VerificationUtils.isBefore(node.getStartOffset(), lineBounds.getEnd()) || fix != null)) {
                super.scan(node);
            }
        }

        @Override
        public void visit(ExpressionStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (lineBounds.containsInclusive(node.getStartOffset())) {
                Expression expression = node.getExpression();
                if (expression instanceof IgnoreError) {
                    expression = ((IgnoreError) expression).getExpression();
                }
                String guessName = null;
                if (expression instanceof ClassInstanceCreation) {
                    ClassInstanceCreation instanceCreation = (ClassInstanceCreation) expression;
                    guessName = CodeUtils.extractClassName(instanceCreation.getClassName());
                    if (instanceCreation.isAnonymous()) {
                        int hashIndex = guessName.lastIndexOf('#'); // NOI18N
                        assert hashIndex != -1 : guessName;
                        assert hashIndex < guessName.length() - 1 : guessName;
                        guessName = "anonCls" + guessName.substring(hashIndex + 1); // NOI18N
                    }
                } else if (expression instanceof MethodInvocation) {
                    MethodInvocation methodInvocation = (MethodInvocation) expression;
                    guessName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                } else if (expression instanceof FunctionInvocation) {
                    FunctionInvocation functionInvocation = (FunctionInvocation) expression;
                    String functionName = CodeUtils.extractFunctionName(functionInvocation);
                    if (hasReturnValue(functionName)) {
                        guessName = functionName;
                    }
                } else if (expression instanceof StaticMethodInvocation) {
                    StaticMethodInvocation methodInvocation = (StaticMethodInvocation) expression;
                    guessName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                }

                if (guessName != null) {
                    fix = new IntroduceFixImpl(doc, node, guessName);
                }
            }
            super.visit(node);
        }

        private boolean hasReturnValue(String functionName) {
            return !LANGUAGE_CUNSTRUCTS.contains(functionName);
        }

        @Override
        public void visit(Variable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            variables.add(node);
            super.visit(node);
        }

        /**
         * @return or null
         */
        public IntroduceFix getIntroduceFix() {
            if (fix != null) {
                fix.setVariables(variables);
            }
            return fix;
        }
    }

    private abstract class IntroduceFix implements HintFix {

        BaseDocument doc;
        ASTNode node;
        List<Variable> variables;

        public IntroduceFix(BaseDocument doc, ASTNode node) {
            this.doc = doc;
            this.node = node;
        }

        OffsetRange getOffsetRange() {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        public void setVariables(List<Variable> variables) {
            this.variables = variables;
        }

        @Override
        public String getDescription() {
            return AssignVariableSuggestion.this.getDescription();
        }

        @Override
        public void implement() throws Exception {
            int textOffset = getTextOffset();
            String variableName = getVariableName();
            EditList edits = new EditList(doc);
            edits.replace(textOffset, 0, String.format("$%s = ", variableName), true, 0); //NOI18N
            edits.apply();
            JTextComponent target = GsfUtilities.getOpenPane();
            if (target != null) {
                int selectStart = textOffset + 1; //after $
                int selectEnd = selectStart + variableName.length();
                target.select(selectStart, selectEnd);
            }
        }

        protected int getTextOffset() {
            return node.getStartOffset();
        }

        abstract String getVariableName();

        String adjustName(String name) {
            if (name == null) {
                return null;
            }

            String shortName = null;

            if (name.startsWith("get") && name.length() > 3) {
                shortName = name.substring(3);
            }

            if (name.startsWith("is") && name.length() > 2) {
                shortName = name.substring(2);
            }

            if (shortName != null) {
                return firstToLower(shortName);
            }

            return name;
        }

        String getVariableName(String guessName) {
            guessName = adjustName(firstToLower(guessName));
            guessName = guessName != null ? guessName : "variable"; //NOI18N
            String proposedName = guessName;
            int incr = -1;
            boolean cont = true;
            while (cont) {
                if (incr != -1) {
                    proposedName = String.format("%s%d", guessName, incr); //NOI18N
                }
                cont = false;
                for (Variable variable : variables) {
                    String varName = CodeUtils.extractVariableName(variable);
                    if (varName != null) {
                        if (variable.isDollared()) {
                            varName = varName.substring(1);
                            if (proposedName.equals(varName)) {
                                incr++;
                                cont = true;
                                break;
                            }
                        }
                    }
                }
            }
            return proposedName;
        }
    }

    private class IntroduceFixImpl extends IntroduceFix {
        private final String guessName;
        IntroduceFixImpl(final BaseDocument doc, final ASTNode node, final String variable) {
            super(doc, node);
            this.guessName = variable;
        }

        @Override
        protected String getVariableName() {
            return super.getVariableName(guessName);
        }
    }

    private static String firstToLower(String name) {
        if (name.length() == 0) {
            return null;
        }

        String cand = Character.toLowerCase(name.charAt(0)) + name.substring(1);

        /*if (isKeyword(cand)) {
        cand = "a" + name;
        }*/
        return cand;
    }
}
