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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment.Type;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.InfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchCase;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ImmutableVariablesHint extends HintRule implements CustomisableRule {

    private static final String HINT_ID = "Immutable.Variables.Hint"; //NOI18N
    private static final String NUMBER_OF_ALLOWED_ASSIGNMENTS = "php.verification.number.of.allowed.assignments"; //NOI18N
    private static final int DEFAULT_NUMBER_OF_ALLOWED_ASSIGNMENTS = 1;
    private static final List<String> UNCHECKED_VARIABLES = new ArrayList<>();
    private Preferences preferences;

    static {
        UNCHECKED_VARIABLES.add("this"); //NOI18N
        UNCHECKED_VARIABLES.add("GLOBALS"); //NOI18N
        UNCHECKED_VARIABLES.add("_SERVER"); //NOI18N
        UNCHECKED_VARIABLES.add("_GET"); //NOI18N
        UNCHECKED_VARIABLES.add("_POST"); //NOI18N
        UNCHECKED_VARIABLES.add("_FILES"); //NOI18N
        UNCHECKED_VARIABLES.add("_COOKIE"); //NOI18N
        UNCHECKED_VARIABLES.add("_SESSION"); //NOI18N
        UNCHECKED_VARIABLES.add("_REQUEST"); //NOI18N
        UNCHECKED_VARIABLES.add("_ENV"); //NOI18N
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
        CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc);
        phpParseResult.getProgram().accept(checkVisitor);
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        hints.addAll(checkVisitor.getHints());
    }

    private class CheckVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final ArrayDeque<ASTNode> parentNodes = new ArrayDeque<>();
        private final Map<ASTNode, Map<String, List<Variable>>> assignments = new HashMap<>();
        private final List<Hint> hints = new ArrayList<>();
        private boolean variableAssignment;
        private final int numberOfAllowedAssignments;

        CheckVisitor(FileObject fileObject, BaseDocument baseDocument) {
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.numberOfAllowedAssignments = getNumberOfAllowedAssignments(preferences);
        }

        public List<Hint> getHints() {
            for (Map<String, List<Variable>> names : assignments.values()) {
                checkNamesInScope(names);
            }
            return Collections.unmodifiableList(hints);
        }

        private void checkNamesInScope(Map<String, List<Variable>> names) {
            for (Entry<String, List<Variable>> entry : names.entrySet()) {
                checkAllowedAssignments(entry.getValue());
            }
        }

        private void checkAllowedAssignments(List<Variable> variables) {
            int variablesSize = variables.size();
            if (variablesSize > numberOfAllowedAssignments) {
                createHints(variables);
            }
        }

        @Messages({
            "# {0} - Number of allowed assignments",
            "# {1} - Number of assignments",
            "# {2} - Variable name",
            "ImmutableVariablesHintCustom=You should use only:\n{0} assignment(s) ({1} used)\nto a variable:\n${2}\nto avoid accidentally overwriting it and make your code easier to read."
        })
        private void createHints(List<Variable> variables) {
            for (Variable variable : variables) {
                createHint(variable, variables.size());
            }
        }

        private void createHint(Variable variable, int numberOfAssignments) {
            int start = variable.getStartOffset() + 1;
            int end = variable.getEndOffset();
            OffsetRange offsetRange = new OffsetRange(start, end);
            if (showHint(offsetRange, baseDocument)) {
                Identifier variableIdentifier = getIdentifier(variable);
                String variableName = variableIdentifier == null ? "?" : variableIdentifier.getName(); //NOI18N
                hints.add(new Hint(
                        ImmutableVariablesHint.this,
                        Bundle.ImmutableVariablesHintCustom(numberOfAllowedAssignments, numberOfAssignments, variableName),
                        fileObject,
                        offsetRange,
                        null,
                        500));
            }
        }

        @Override
        public void visit(Program node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(NamespaceDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(FunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(ArrowFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (parentNodes.peek() instanceof ArrowFunctionDeclaration) {
                // nested arrow function
                super.visit(node);
            } else {
                parentNodes.push(node);
                super.visit(node);
                parentNodes.pop();
            }
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(IfStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(CatchClause node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(Block node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (parentNodes.peek() instanceof IfStatement) {
                parentNodes.push(node);
                super.visit(node);
                parentNodes.pop();
            } else {
                super.visit(node);
            }
        }

        @Override
        public void visit(ForStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            scan(node.getInitializers());
            scan(node.getConditions());
            scan(node.getBody());
            parentNodes.pop();
        }

        @Override
        public void visit(ForEachStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(DoStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(WhileStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(SwitchCase node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            parentNodes.push(node);
            super.visit(node);
            parentNodes.pop();
        }

        @Override
        public void visit(StaticFieldAccess node) {
            // intentionally
        }

        @Override
        public void visit(FormalParameter functionParameter) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Expression parameterName = functionParameter.getParameterName();
            if (parameterName instanceof Variable) {
                processVariableAssignment((Variable) parameterName);
            }
        }

        @Override
        public void visit(Variable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (variableAssignment) {
                processVariableAssignment(node);
            }
        }

        private void processVariableAssignment(Variable node) {
            ASTNode parentNode = parentNodes.peek();
            Map<String, List<Variable>> names = getNames(parentNode);
            Identifier identifier = getIdentifier(node);
            if (identifier != null) {
                addValidVariable(identifier, names, node);
            }
        }

        private Map<String, List<Variable>> getNames(ASTNode parentNode) {
            Map<String, List<Variable>> names = assignments.get(parentNode);
            if (names == null) {
                names = new HashMap<>();
                assignments.put(parentNode, names);
            }
            return names;
        }

        private void addValidVariable(Identifier identifier, Map<String, List<Variable>> names, Variable node) {
            String name = identifier.getName();
            if (!UNCHECKED_VARIABLES.contains(name)) {
                List<Variable> variables = getVariables(names, name);
                variables.add(node);
            }
        }

        private List<Variable> getVariables(Map<String, List<Variable>> names, String name) {
            List<Variable> variables = names.get(name);
            if (variables == null) {
                variables = new ArrayList<>();
                names.put(name, variables);
            }
            return variables;
        }

        @Override
        public void visit(Assignment node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.getOperator().equals(Type.EQUAL)) {
                if (parentNodes.peek() instanceof IfStatement) {
                    parentNodes.push(node);
                    processEqualAssignment(node);
                    parentNodes.pop();
                } else {
                    processEqualAssignment(node);
                }
            }
        }

        private void processEqualAssignment(Assignment node) {
            if (!(node.getRightHandSide() instanceof InfixExpression)
                    || (node.getRightHandSide() instanceof InfixExpression && !containsConcatOperator((InfixExpression) node.getRightHandSide()))) {
                variableAssignment = true;
                scan(node.getLeftHandSide());
                variableAssignment = false;
            }
        }

        private boolean containsConcatOperator(InfixExpression infixExpression) {
            boolean retval = false;
            if (infixExpression.getOperator().equals(InfixExpression.OperatorType.CONCAT)) {
                retval = true;
            } else if (infixExpression.getLeft() instanceof InfixExpression) {
                retval = containsConcatOperator((InfixExpression) infixExpression.getLeft());
            } else if (infixExpression.getLeft() instanceof InfixExpression) {
                retval = containsConcatOperator((InfixExpression) infixExpression.getRight());
            }
            return retval;
        }

        @Override
        public void visit(ArrayAccess node) {
            // intentionally
        }

        @Override
        public void visit(ArrayCreation node) {
            // intentionally
        }

        @Override
        public void visit(FieldAccess node) {
            // intentionally
        }

        @CheckForNull
        private Identifier getIdentifier(Variable variable) {
            Identifier retval = null;
            if (variable != null && variable.isDollared()) {
                retval = separateIdentifier(variable);
            }
            return retval;
        }

        @CheckForNull
        private Identifier separateIdentifier(Variable variable) {
            Identifier retval = null;
            if (variable.getName() instanceof Identifier) {
                retval = (Identifier) variable.getName();
            }
            return retval;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("ImmutableVariableHintDesc=Checks a number of assignments into a variable in a block.")
    public String getDescription() {
        return Bundle.ImmutableVariableHintDesc();
    }

    @Override
    @Messages("ImmutableVariableHintDispName=Immutable Variables")
    public String getDisplayName() {
        return Bundle.ImmutableVariableHintDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    @Override
    public void setPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public JComponent getCustomizer(Preferences preferences) {
        JComponent customizer = new ImmutableVariablesCustomizer(preferences, this);
        setNumberOfAllowedAssignments(preferences, getNumberOfAllowedAssignments(preferences));
        return customizer;
    }

    public void setNumberOfAllowedAssignments(Preferences preferences, Integer value) {
        preferences.putInt(NUMBER_OF_ALLOWED_ASSIGNMENTS, value);
    }

    public int getNumberOfAllowedAssignments(Preferences preferences) {
        return preferences.getInt(NUMBER_OF_ALLOWED_ASSIGNMENTS, DEFAULT_NUMBER_OF_ALLOWED_ASSIGNMENTS);
    }

}
