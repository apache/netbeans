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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.CastExpression;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.CloneExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ConditionalExpression;
import org.netbeans.modules.php.editor.parser.astnodes.ConstantDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ContinueStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DeclareStatement;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.EchoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GotoLabel;
import org.netbeans.modules.php.editor.parser.astnodes.GotoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.GroupUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Include;
import org.netbeans.modules.php.editor.parser.astnodes.InstanceOfExpression;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocMethodTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocStaticAccessType;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocVarTypeTag;
import org.netbeans.modules.php.editor.parser.astnodes.PHPVarComment;
import org.netbeans.modules.php.editor.parser.astnodes.PostfixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.PrefixExpression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.ReflectionVariable;
import org.netbeans.modules.php.editor.parser.astnodes.ReturnStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleUseStatementPart;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.SwitchStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ThrowStatement;
import org.netbeans.modules.php.editor.parser.astnodes.UnaryOperation;
import org.netbeans.modules.php.editor.parser.astnodes.UseStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UnusedVariableHint extends HintRule implements CustomisableRule {

    private static final String HINT_ID = "Unused.Variable.Hint"; //NOI18N
    private static final String CHECK_UNUSED_FORMAL_PARAMETERS = "php.verification.check.unused.formal.parameters"; //NOI18N
    private static final String CHECK_INHERITED_METHOD_PARAMETERS = "php.verification.check.inherited.method.parameters"; //NOI18N
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
        if (fileObject != null) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor(fileObject, context.doc, getInheritedMethods(context));
            phpParseResult.getProgram().accept(checkVisitor);
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            hints.addAll(checkVisitor.getHints());
        }
    }

    private Map<String, List<String>> getInheritedMethods(PHPRuleContext context) {
        if (!checkUnusedFormalParameters(preferences) || checkInheritedMethodParameters(preferences)) {
            return Collections.emptyMap();
        }
        FileScope fileScope = context.fileScope;
        Collection<? extends ClassScope> allClasses = ModelUtils.getDeclaredClasses(fileScope);
        ElementQuery.Index index = context.getIndex();
        Map<String, List<String>> allInheritedMethods = new HashMap<>();
        for (ClassScope classScope : allClasses) {
            if (CancelSupport.getDefault().isCancelled()) {
                return Collections.emptyMap();
            }
            Set<MethodElement> inheritedMethods = getInheritedMethods(classScope, index);
            for (MethodElement inheritedMethod : inheritedMethods) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return Collections.emptyMap();
                }
                List<String> methodElements = allInheritedMethods.get(inheritedMethod.getName());
                if (methodElements == null) {
                    methodElements = new ArrayList<>();
                    methodElements.add(classScope.getName());
                    allInheritedMethods.put(inheritedMethod.getName(), methodElements);
                } else {
                    methodElements.add(classScope.getName());
                }
            }
        }
        return allInheritedMethods;
    }

    private Set<MethodElement> getInheritedMethods(final ClassScope classScope, final ElementQuery.Index index) {
        Set<MethodElement> inheritedMethods = new HashSet<>();
        Set<MethodElement> declaredSuperMethods = new HashSet<>();
        Set<MethodElement> accessibleSuperMethods = new HashSet<>();
        Collection<? extends ClassScope> superClasses = classScope.getSuperClasses();
        for (ClassScope cls : superClasses) {
            declaredSuperMethods.addAll(index.getDeclaredMethods(cls));
            accessibleSuperMethods.addAll(index.getAccessibleMethods(cls, classScope));
        }
        Collection<? extends InterfaceScope> superInterface = classScope.getSuperInterfaceScopes();
        for (InterfaceScope interfaceScope : superInterface) {
            declaredSuperMethods.addAll(index.getDeclaredMethods(interfaceScope));
            accessibleSuperMethods.addAll(index.getAccessibleMethods(interfaceScope, classScope));
        }
        Collection<? extends TraitScope> traits = classScope.getTraits();
        for (TraitScope traitScope : traits) {
            declaredSuperMethods.addAll(index.getDeclaredMethods(traitScope));
            accessibleSuperMethods.addAll(index.getAccessibleMethods(traitScope, classScope));
        }
        inheritedMethods.addAll(declaredSuperMethods);
        inheritedMethods.addAll(accessibleSuperMethods);
        return inheritedMethods;
    }

    private class CheckVisitor extends DefaultVisitor {

        private final ArrayDeque<ASTNode> parentNodes = new ArrayDeque<>();
        private final Map<ASTNode, List<HintVariable>> unusedVariables = new HashMap<>();
        private final Map<ASTNode, List<HintVariable>> usedVariables = new HashMap<>();
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final List<Hint> hints;
        private boolean forceVariableAsUsed;
        private boolean forceVariableAsUnused;
        private boolean isInInheritedMethod;
        private String className = ""; // NOI18N
        private final Map<String, List<String>> allInheritedMethods; // method name, class names
        private final Map<ArrowFunctionDeclaration, Set<String>> arrowFunctionParameters = new HashMap<>();
        private ArrowFunctionDeclaration firstArrowFunctionNode = null;
        private ArrowFunctionDeclaration currentArrowFunctionNode = null;

        CheckVisitor(FileObject fileObject, BaseDocument baseDocument, Map<String, List<String>> allInheritedMethods) {
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.allInheritedMethods = allInheritedMethods;
            hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            for (List<HintVariable> scopeVariables : unusedVariables.values()) {
                for (HintVariable variable : scopeVariables) {
                    createHint(variable);
                }
            }
            return Collections.unmodifiableList(hints);
        }

        @Messages({
            "# {0} - Name of the variable",
            "UnusedVariableHintCustom=Variable ${0} seems to be unused in its scope"
        })
        private void createHint(HintVariable variable) {
            int start = variable.getStartOffset();
            int end = variable.getEndOffset();
            OffsetRange offsetRange = new OffsetRange(start, end);
            if (showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(UnusedVariableHint.this, Bundle.UnusedVariableHintCustom(variable.getName()), fileObject, offsetRange, null, 500));
            }
        }

        @CheckForNull
        private Identifier getIdentifier(Variable variable) {
            Identifier retval = null;
            if (variable != null && variable.isDollared()) {
                if (variable.getName() instanceof Identifier) {
                    retval = (Identifier) variable.getName();
                }
            }
            return retval;
        }

        private List<HintVariable> getUsedScopeVariables(ASTNode parentNode) {
            List<HintVariable> usedScopeVariables = usedVariables.get(parentNode);
            if (usedScopeVariables == null) {
                usedScopeVariables = new ArrayList<>();
                usedVariables.put(parentNode, usedScopeVariables);
            }
            return usedScopeVariables;
        }

        private List<HintVariable> getUnusedScopeVariables(ASTNode parentNode) {
            List<HintVariable> unusedScopeVariables = unusedVariables.get(parentNode);
            if (unusedScopeVariables == null) {
                unusedScopeVariables = new ArrayList<>();
                unusedVariables.put(parentNode, unusedScopeVariables);
            }
            return unusedScopeVariables;
        }

        @CheckForNull
        private HintVariable getUnusedVariable(String currentVarName, List<HintVariable> unusedScopeVariables) {
            HintVariable retval = null;
            for (HintVariable variable : unusedScopeVariables) {
                String varName = variable.getName();
                if (currentVarName.equals(varName)) {
                    retval = variable;
                    break;
                }
            }
            return retval;
        }

        private boolean isVariableUsed(String currentVarName, List<HintVariable> usedScopeVariables) {
            boolean retval = false;
            for (HintVariable variable : usedScopeVariables) {
                String varName = variable.getName();
                if (currentVarName.equals(varName)) {
                    retval = true;
                    break;
                }
            }
            return retval;
        }

        private void forceVariableAsUnused(HintVariable node, List<HintVariable> unusedScopeVariables) {
            HintVariable unusedVariable = getUnusedVariable(node.getName(), unusedScopeVariables);
            if (unusedVariable != null) {
                unusedScopeVariables.remove(unusedVariable);
            }
            unusedScopeVariables.add(node);
        }

        private void forceVariableAsUsed(HintVariable hintVariable, List<HintVariable> usedScopeVariables, List<HintVariable> unusedScopeVariables) {
            String currentVarName = hintVariable.getName();
            if (isVariableUsed(currentVarName, usedScopeVariables)) {
                return;
            }
            usedScopeVariables.add(hintVariable);
            HintVariable unusedVariable = getUnusedVariable(currentVarName, unusedScopeVariables);
            if (unusedVariable != null) {
                unusedScopeVariables.remove(unusedVariable);
            }
        }

        @Override
        public void visit(Variable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Identifier identifier = getIdentifier(node);
            if (identifier != null && !isInGlobalContext()) {
                if (parentNodes.peek() instanceof ArrowFunctionDeclaration) {
                    if (!isArrowFunctionParameter(node)) {
                        return;
                    }
                }
                process(HintVariable.create(node, identifier.getName()));
            }
        }

        private boolean isInGlobalContext() {
            return (parentNodes.peek() instanceof Program) || (parentNodes.peek() instanceof NamespaceDeclaration);
        }

        private boolean isArrowFunctionParameter(Variable variable) {
            Set<String> params = arrowFunctionParameters.get(currentArrowFunctionNode);
            return params != null && params.contains(CodeUtils.extractVariableName(variable));
        }

        private void process(HintVariable hintVariable) {
            if (hintVariable != null && !UNCHECKED_VARIABLES.contains(hintVariable.getName())) {
                ASTNode parentNode = parentNodes.peek();
                if (parentNode instanceof ArrowFunctionDeclaration) {
                    // for nested arrow function
                    if (currentArrowFunctionNode != null) {
                        parentNode = currentArrowFunctionNode;
                    }
                }
                String currentVarName = hintVariable.getName();
                List<HintVariable> usedScopeVariables = getUsedScopeVariables(parentNode);
                List<HintVariable> unusedScopeVariables = getUnusedScopeVariables(parentNode);
                if (forceVariableAsUnused) {
                    forceVariableAsUnused(hintVariable, unusedScopeVariables);
                    return;
                }
                if (forceVariableAsUsed) {
                    forceVariableAsUsed(hintVariable, usedScopeVariables, unusedScopeVariables);
                    return;
                }
                if (isVariableUsed(currentVarName, usedScopeVariables)) {
                    return;
                }
                HintVariable unusedVariable = getUnusedVariable(currentVarName, unusedScopeVariables);
                if (unusedVariable != null) {
                    unusedScopeVariables.remove(unusedVariable);
                    usedScopeVariables.add(hintVariable);
                    return;
                }
                unusedScopeVariables.add(hintVariable);
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
            if (node.getBody() != null) {
                parentNodes.push(node);
                super.visit(node);
                parentNodes.pop();
            }
        }

        @Override
        public void visit(EchoStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpressions());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(ExpressionStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.getExpression() instanceof Variable) { // just variable without anything: {  $var; }
                forceVariableAsUnused = true;
                scan(node.getExpression());
                forceVariableAsUnused = false;
            } else {
                scan(node.getExpression());
            }
        }

        @Override
        public void visit(Include node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpression());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(FunctionInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            String functionName = CodeUtils.extractFunctionName(node);
            if ("compact".equals(functionName)) { //NOI18N
                handleCompactFunction(node);
            }
            super.visit(node);
            forceVariableAsUsed = false;
        }

        private void handleCompactFunction(final FunctionInvocation node) {
            List<Expression> parameters = node.getParameters();
            for (Expression parameter : parameters) {
                handleFunctionParameter(parameter);
            }
        }

        private void handleFunctionParameter(final Expression parameter) {
            if (parameter instanceof Scalar) {
                handleScalar((Scalar) parameter);
            }
        }

        private void handleScalar(final Scalar scalar) {
            if (scalar.getScalarType().equals(Scalar.Type.STRING)) {
                process(HintVariable.create(scalar, extractVariableName(scalar.getStringValue())));
            }
        }

        private String extractVariableName(final String quotedValue) {
            String result = quotedValue;
            if ((quotedValue.startsWith("'") && quotedValue.endsWith("'")) || (quotedValue.startsWith("\"") && quotedValue.endsWith("\""))) { //NOI18N
                result = quotedValue.substring(1, quotedValue.length() - 1);
            }
            return result;
        }

        @Override
        public void visit(MethodInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getDispatcher());
            forceVariableAsUsed = false;
            scan(node.getMethod());
        }

        @Override
        public void visit(IfStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getCondition());
            forceVariableAsUsed = false;
            scan(node.getTrueStatement());
            scan(node.getFalseStatement());
        }

        @Override
        public void visit(InstanceOfExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpression());
            scan(node.getClassName());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(PostfixExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getVariable());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(PrefixExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getVariable());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(ReflectionVariable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            Expression name = node.getName();
            if (name instanceof Scalar) {
                handleScalar((Scalar) name);
            } else {
                scan(name);
            }
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(CloneExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpression());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(CastExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpression());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(Assignment node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getLeftHandSide());
            forceVariableAsUsed = true;
            scan(node.getRightHandSide());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(ConditionalExpression node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getCondition());
            scan(node.getIfTrue());
            scan(node.getIfFalse());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(ReturnStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpression());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(SwitchStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpression());
            forceVariableAsUsed = false;
            scan(node.getBody());
        }

        @Override
        public void visit(ThrowStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpression());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(UnaryOperation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpression());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(ClassDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            className = node.getName().getName();
            scan(node.getBody());
            className = ""; // NOI18N
        }

        @Override
        public void visit(ClassInstanceCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getClassName());
            scan(node.ctorParams());
            scan(node.getBody());
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(DoStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getCondition());
            forceVariableAsUsed = false;
            scan(node.getBody());
        }

        @Override
        public void visit(DeclareStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getBody());
        }

        @Override
        public void visit(CatchClause node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Block body = node.getBody();
            if (!body.getStatements().isEmpty()) {
                scan(node.getVariable());
            }
            scan(body);
        }

        @Override
        public void visit(MethodDeclaration node) {
            if (checkUnusedFormalParameters(preferences) && !checkInheritedMethodParameters(preferences)) {
                String methodName = node.getFunction().getFunctionName().getName();
                List<String> classNames = allInheritedMethods.get(methodName);
                if (classNames != null) {
                    for (String clsName : classNames) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        if (className.equals(clsName)) {
                            isInInheritedMethod = true;
                            break;
                        }
                    }
                }
                super.visit(node);
                isInInheritedMethod = false;
            } else {
                super.visit(node);
            }
        }

        @Override
        public void visit(FormalParameter node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (checkUnusedFormalParameters(preferences) && !isInInheritedMethod) {
                scan(node.getParameterName());
            } else {
                forceVariableAsUsed = true;
                scan(node.getParameterName());
                forceVariableAsUsed = false;
            }
        }

        @Override
        public void visit(ForEachStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getExpression());
            forceVariableAsUsed = false;
            scan(node.getKey());
            scan(node.getValue());
            scan(node.getStatement());
        }

        @Override
        public void visit(ForStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getInitializers());
            scan(node.getConditions());
            scan(node.getUpdaters());
            forceVariableAsUsed = false;
            scan(node.getBody());
        }

        @Override
        public void visit(StaticMethodInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getDispatcher());
            forceVariableAsUsed = false;
            scan(node.getMethod());
        }

        @Override
        public void visit(WhileStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            scan(node.getCondition());
            forceVariableAsUsed = false;
            scan(node.getBody());
        }

        @Override
        public void visit(ArrowFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (firstArrowFunctionNode == null) {
                firstArrowFunctionNode = node;
            }
            if (currentArrowFunctionNode == null || parentNodes.peek() instanceof LambdaFunctionDeclaration) {
                currentArrowFunctionNode = node;
            }
            isInInheritedMethod = false;
            collectArrowFunctionParameters(node);

            // avoid scaning formal parameters of Arrow Functions
            Expression expression = node.getExpression();
            while (expression instanceof ArrowFunctionDeclaration) {
                expression = ((ArrowFunctionDeclaration) expression).getExpression();
            }

            // scaning for current parent node
            // we have to check whether variables of an arrow function expression are used
            // e.g. $value is used in the following case:
            // function myFunction($param1, $param2) {
            //     $value = 100;
            //     return fn($x) => ($param1 + $param2) * $x * $value;
            // }
            forceVariableAsUsed = true;
            if (expression instanceof LambdaFunctionDeclaration) {
                scan(((LambdaFunctionDeclaration) expression).getLexicalVariables());
            } else {
                scan(expression);
            }
            forceVariableAsUsed = false;

            parentNodes.push(node);
            scan(node.getFormalParameters());
            scan(node.getExpression());
            parentNodes.pop();

            if (firstArrowFunctionNode == node) {
                // clear
                firstArrowFunctionNode = null;
                currentArrowFunctionNode = null;
                arrowFunctionParameters.clear();
            }
        }

        private void collectArrowFunctionParameters(ArrowFunctionDeclaration node) {
            Set<String> arrowFunctionParams = arrowFunctionParameters.get(currentArrowFunctionNode);
            if (arrowFunctionParams == null) {
                arrowFunctionParams = new HashSet<>();
                arrowFunctionParameters.put(currentArrowFunctionNode, arrowFunctionParams);
            }
            node.getFormalParameters().stream()
                    .map(param -> CodeUtils.extractFormalParameterName(param))
                    .filter(parameterName -> (parameterName != null))
                    .forEachOrdered(parameterName -> arrowFunctionParameters.get(currentArrowFunctionNode).add(parameterName));
        }

        @Override
        public void visit(LambdaFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            isInInheritedMethod = false;
            forceVariableAsUsed = true;
            scan(node.getLexicalVariables());
            forceVariableAsUsed = false;
            parentNodes.push(node);
            scan(node.getLexicalVariables());
            scan(node.getFormalParameters());
            scan(node.getBody());
            parentNodes.pop();
        }

        @Override
        public void visit(StaticFieldAccess node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            forceVariableAsUsed = true;
            super.visit(node);
            forceVariableAsUsed = false;
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            // intentionally
        }

        @Override
        public void visit(FieldsDeclaration node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocBlock node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocTypeTag node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocMethodTag node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocVarTypeTag node) {
            // intentionally
        }

        @Override
        public void visit(PHPDocStaticAccessType node) {
            // intentionally
        }

        @Override
        public void visit(PHPVarComment node) {
            // intentionally
        }

        @Override
        public void visit(ConstantDeclaration node) {
            // intentionally
        }

        @Override
        public void visit(ContinueStatement node) {
            // intentionally
        }

        @Override
        public void visit(SingleFieldDeclaration node) {
            // intentionally
        }

        @Override
        public void visit(UseStatement node) {
            // intentionally
        }

        @Override
        public void visit(SingleUseStatementPart node) {
            // intentionally
        }

        @Override
        public void visit(GroupUseStatementPart node) {
            // intentionally
        }

        @Override
        public void visit(GotoLabel node) {
            // intentionally
        }

        @Override
        public void visit(GotoStatement node) {
            // intentionally
        }

    }

    private static final class HintVariable {
        private final ASTNode node;
        private final String name;

        static HintVariable create(final ASTNode node, final String name) {
            return new HintVariable(node, name);
        }

        private HintVariable(final ASTNode node, final String name) {
            this.node = node;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public int getStartOffset() {
            return node.getStartOffset() + 1;
        }

        public int getEndOffset() {
            return node.getEndOffset();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final HintVariable other = (HintVariable) obj;
            return Objects.equals(this.name, other.name);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.name);
            return hash;
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("UnusedVariableHintDesc=Detects variables which are declared, but not used in their scope.")
    public String getDescription() {
        return Bundle.UnusedVariableHintDesc();
    }

    @Override
    @Messages("UnusedVariableHintDispName=Unused Variables")
    public String getDisplayName() {
        return Bundle.UnusedVariableHintDispName();
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
        JComponent customizer = new UnusedVariableCustomizer(preferences, this);
        setCheckUnusedFormalParameters(preferences, checkUnusedFormalParameters(preferences));
        setCheckInheritedMethodParameters(preferences, checkInheritedMethodParameters(preferences));
        return customizer;
    }

    public void setCheckUnusedFormalParameters(Preferences preferences, boolean isEnabled) {
        preferences.putBoolean(CHECK_UNUSED_FORMAL_PARAMETERS, isEnabled);
    }

    public boolean checkUnusedFormalParameters(Preferences preferences) {
        return preferences.getBoolean(CHECK_UNUSED_FORMAL_PARAMETERS, true);
    }

    public void setCheckInheritedMethodParameters(Preferences preferences, boolean isEnabled) {
        preferences.putBoolean(CHECK_INHERITED_METHOD_PARAMETERS, isEnabled);
    }

    public boolean checkInheritedMethodParameters(Preferences preferences) {
        return preferences.getBoolean(CHECK_INHERITED_METHOD_PARAMETERS, true);
    }

}
