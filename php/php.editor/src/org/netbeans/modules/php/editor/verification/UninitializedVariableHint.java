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
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayAccess;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayElement;
import org.netbeans.modules.php.editor.parser.astnodes.ArrowFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.CatchClause;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.GlobalStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.LambdaFunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ListVariable;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.Variadic;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class UninitializedVariableHint extends HintRule implements CustomisableRule {

    private static final String HINT_ID = "Uninitialized.Variable.Hint"; //NOI18N
    private static final String CHECK_VARIABLES_INITIALIZED_BY_REFERENCE = "php.verification.check.variables.initialized.by.reference"; //NOI18N
    private static final List<String> UNCHECKED_VARIABLES = new ArrayList<>();
    private static final List<UglyElement> UGLY_ELEMENTS = new ArrayList<>();
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
        UNCHECKED_VARIABLES.add("argc"); //NOI18N
        UNCHECKED_VARIABLES.add("argv"); //NOI18N
        UNCHECKED_VARIABLES.add("HTTP_RAW_POST_DATA"); //NOI18N
        UNCHECKED_VARIABLES.add("php_errormsg"); //NOI18N
        UNCHECKED_VARIABLES.add("http_response_header"); //NOI18N

        UGLY_ELEMENTS.add(new UglyElementImpl("bind_result", "mysqli_stmt"));
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
        CheckVisitor checkVisitor = new CheckVisitor(fileObject, phpParseResult.getModel(), context.doc);
        phpParseResult.getProgram().accept(checkVisitor);
        if (CancelSupport.getDefault().isCancelled()) {
            return;
        }
        hints.addAll(checkVisitor.getHints());
    }

    private final class CheckVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final ArrayDeque<ASTNode> parentNodes = new ArrayDeque<>();
        private final Map<ASTNode, List<Variable>> initializedVariablesAll = new HashMap<>();
        private final Map<ASTNode, List<Variable>> uninitializedVariablesAll = new HashMap<>();
        private final List<Hint> hints = new ArrayList<>();
        private final Model model;
        private final Map<String, Set<BaseFunctionElement>> invocationCache = new HashMap<>();
        private final BaseDocument baseDocument;
        private final Map<ArrowFunctionDeclaration, Set<String>> arrowFunctionParameters = new HashMap<>();
        private ArrowFunctionDeclaration firstArrowFunction = null;
        private ArrowFunctionDeclaration currentArrowFunction = null;

        private CheckVisitor(FileObject fileObject, Model model, BaseDocument baseDocument) {
            this.fileObject = fileObject;
            this.model = model;
            this.baseDocument = baseDocument;
        }

        private Collection<? extends Hint> getHints() {
            for (ASTNode scopeNode : uninitializedVariablesAll.keySet()) {
                createHints(getUninitializedVariables(scopeNode));
            }
            return Collections.unmodifiableCollection(hints);
        }

        private void createHints(List<Variable> uninitializedVariables) {
            for (Variable variable : uninitializedVariables) {
                createHint(variable);
            }
        }

        @Messages({
            "# {0} - Name of the variable",
            "UninitializedVariableVariableHintCustom=Variable ${0} seems to be uninitialized"
        })
        private void createHint(Variable variable) {
            int start = variable.getStartOffset() + 1;
            int end = variable.getEndOffset();
            OffsetRange offsetRange = new OffsetRange(start, end);
            if (showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(UninitializedVariableHint.this, Bundle.UninitializedVariableVariableHintCustom(getVariableName(variable)), fileObject, offsetRange, null, 500));
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
        public void visit(LambdaFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getLexicalVariables());
            parentNodes.push(node);
            initializeExpressions(node.getLexicalVariables());
            scan(node.getFormalParameters());
            scan(node.getBody());
            parentNodes.pop();
        }

        @Override
        public void visit(ArrowFunctionDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (firstArrowFunction == null) {
                firstArrowFunction = node;
            }
            if (currentArrowFunction == null || parentNodes.peek() instanceof LambdaFunctionDeclaration) {
                currentArrowFunction = node;
            }
            Set<String> arrowFunctionParams = getArrowFunctionParams(currentArrowFunction);
            for (FormalParameter parameter : node.getFormalParameters()) {
                arrowFunctionParams.add(CodeUtils.extractFormalParameterName(parameter));
            }
            scan(node.getExpression());
            // clear
            if (firstArrowFunction == node) {
                firstArrowFunction = null;
                currentArrowFunction = null;
                arrowFunctionParameters.clear();
            }
        }

        @Override
        public void visit(Assignment node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            VariableBase leftHandSide = node.getLeftHandSide();
            initializeVariableBase(leftHandSide);
            scan(node.getRightHandSide());
        }

        @Override
        public void visit(CatchClause node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            initializeVariable(node.getVariable());
            scan(node.getClassNames());
            scan(node.getBody());
        }

        @Override
        public void visit(DoStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getBody());
            scan(node.getCondition());
        }

        @Override
        public void visit(ForEachStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getExpression());
            initializeExpression(node.getKey());
            initializeExpression(node.getValue());
            scan(node.getStatement());
        }

        @Override
        public void visit(ForStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getInitializers());
            scan(node.getConditions());
            scan(node.getBody());
            scan(node.getUpdaters());
        }

        @Override
        public void visit(FormalParameter node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Expression expression = node.getParameterName();
            if (expression instanceof Reference) {
                Reference reference = (Reference) expression;
                expression = reference.getExpression();
            } else if (expression instanceof Variadic) {
                Variadic variadic = (Variadic) expression;
                expression = variadic.getExpression();
            }
            initializeExpression(expression);
        }

        @Override
        public void visit(GlobalStatement node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            for (Variable variable : node.getVariables()) {
                initializeVariable(variable);
            }
        }

        @Override
        public void visit(Variable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (isProcessableVariable(node) && !isArrowFunctionParameter(node)) {
                addUninitializedVariable(node);
            }
        }

        @Override
        public void visit(FunctionInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (checkVariablesInitializedByReference(preferences)) {
                List<Expression> invocationParametersExp = node.getParameters();
                String functionName = CodeUtils.extractFunctionName(node);
                if (functionName != null) {
                    Set<BaseFunctionElement> allFunctions = invocationCache.get(functionName);
                    if (allFunctions == null) {
                        allFunctions = new HashSet<>(model.getIndexScope().getIndex().getFunctions(NameKind.create(functionName, QuerySupport.Kind.EXACT)));
                        invocationCache.put(functionName, allFunctions);
                    }
                    processAllFunctions(allFunctions, invocationParametersExp);
                }
                scan(node.getFunctionName());
            } else {
                super.visit(node);
            }
        }

        @Override
        public void visit(MethodInvocation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (checkVariablesInitializedByReference(preferences)) {
                List<Expression> invocationParametersExp = node.getMethod().getParameters();
                if (invocationParametersExp.size() > 0) {
                    String functionName = CodeUtils.extractFunctionName(node.getMethod());
                    if (functionName != null) {
                        Set<BaseFunctionElement> allFunctions = invocationCache.get(functionName);
                        if (allFunctions == null) {
                            Collection<? extends TypeScope> resolvedTypes = ModelUtils.resolveType(model, node);
                            if (resolvedTypes.size() > 0) {
                                TypeScope resolvedType = ModelUtils.getFirst(resolvedTypes);
                                Index index = model.getIndexScope().getIndex();
                                allFunctions = new HashSet<>(ElementFilter.forName(NameKind.exact(functionName)).filter(index.getAllMethods(resolvedType)));
                                invocationCache.put(functionName, allFunctions);
                            }
                        }
                        processAllFunctions(allFunctions, invocationParametersExp);
                    }
                }
            } else {
                super.visit(node);
            }
        }

        @Override
        public void visit(FieldsDeclaration node) {
            // intentionally - variables in fields shouldn't be checked
        }

        @Override
        public void visit(StaticFieldAccess node) {
            // intentionally - variables in fields shouldn't be checked
        }

        private void processAllFunctions(Set<BaseFunctionElement> allFunctions, List<Expression> invocationParametersExp) {
            if (allFunctions != null && !allFunctions.isEmpty()) {
                BaseFunctionElement methodElement = ModelUtils.getFirst(allFunctions);
                if (methodElement != null && !UGLY_ELEMENTS.contains(new UglyElementImpl(methodElement))) {
                    List<ParameterElement> methodParameters = methodElement.getParameters();
                    int invocationParamsSize = invocationParametersExp.size();
                    if (matchNumberOfParams(invocationParamsSize, methodParameters)) {
                        for (int i = 0; i < invocationParamsSize; i++) {
                            Expression invocationParameterExp = invocationParametersExp.get(i);
                            if (methodParameters.get(i).isReference()) {
                                initializeExpression(invocationParameterExp);
                            } else {
                                scan(invocationParameterExp);
                            }
                        }
                    } else {
                        scan(invocationParametersExp);
                    }
                }
            } else {
                scan(invocationParametersExp);
            }
        }

        private boolean matchNumberOfParams(int invocationParamsNumber, List<ParameterElement> methodParameters) {
            int mandatoryParams = 0;
            for (ParameterElement parameterElement : methodParameters) {
                if (parameterElement.isMandatory()) {
                    mandatoryParams++;
                }
            }
            return invocationParamsNumber >= mandatoryParams && invocationParamsNumber <= methodParameters.size();
        }

        private boolean isProcessableVariable(Variable node) {
            Identifier identifier = getIdentifier(node);
            return !isInGlobalContext() && identifier != null && !UNCHECKED_VARIABLES.contains(identifier.getName())
                    && !isInitialized(node) && !isUninitialized(node);
        }

        private boolean isInGlobalContext() {
            return (parentNodes.peek() instanceof Program) || (parentNodes.peek() instanceof NamespaceDeclaration);
        }

        private void initializeVariable(Variable variable) {
            if (!isInitialized(variable) && !isUninitialized(variable)) {
                addInitializedVariable(variable);
            }
        }

        private boolean isInitialized(Variable node) {
            return contains(getInitializedVariables(parentNodes.peek()), node);
        }

        private boolean isUninitialized(Variable node) {
            return contains(getUninitializedVariables(parentNodes.peek()), node);
        }

        private boolean contains(List<Variable> scopeVariables, Variable node) {
            boolean retval = false;
            String currentVariableName = getVariableName(node);
            for (Variable variable : scopeVariables) {
                if (currentVariableName.equals(getVariableName(variable))) {
                    retval = true;
                    break;
                }
            }
            return retval;
        }

        private String getVariableName(Variable variable) {
            String retval = "";
            Identifier identifier = getIdentifier(variable);
            if (identifier != null) {
                retval = identifier.getName();
            }
            return retval;
        }

        private void initializeExpressions(List<Expression> expressions) {
            for (Expression expression : expressions) {
                initializeExpression(expression);
            }
        }

        private void initializeExpression(Expression expression) {
            if (expression instanceof Variable) {
                initializeVariable((Variable) expression);
            } else if (expression instanceof Reference) {
                initializeReference((Reference) expression);
            } else if (expression instanceof Variadic) {
                initializeVariadic((Variadic) expression);
            } else if (expression instanceof  ListVariable) { // #249508
                initializeListVariable((ListVariable) expression);
            }
        }

        private void initializeReference(Reference node) {
            initializeExpression(node.getExpression());
        }

        private void initializeVariadic(Variadic node) {
            initializeExpression(node.getExpression());
        }

        private void initializeVariableBase(VariableBase variableBase) {
            if (variableBase instanceof ArrayAccess) {
                initializeArrayAccessVariable((ArrayAccess) variableBase);
            } else if (variableBase instanceof Variable) {
                initializeVariable((Variable) variableBase);
            } else if (variableBase instanceof ListVariable) {
                initializeListVariable((ListVariable) variableBase);
            } else {
                super.visit(variableBase);
            }
        }

        private void initializeArrayAccessVariable(ArrayAccess node) {
            VariableBase name = node.getName();
            if (name instanceof Variable) {
                initializeVariable((Variable) name);
            }
        }

        private void initializeListVariable(ListVariable node) {
            List<ArrayElement> elements = node.getElements();
            for (ArrayElement element : elements) {
                Expression value = element.getValue();
                if (value instanceof VariableBase) {
                    initializeVariableBase((VariableBase)value);
                }
            }
        }

        private void addInitializedVariable(Variable node) {
            List<Variable> scopeVariables = getInitializedVariables(parentNodes.peek());
            scopeVariables.add(node);
        }

        private void addUninitializedVariable(Variable node) {
            List<Variable> scopeVariables = getUninitializedVariables(parentNodes.peek());
            scopeVariables.add(node);
        }

        private List<Variable> getInitializedVariables(ASTNode parent) {
            List<Variable> scopeVariables = initializedVariablesAll.get(parent);
            if (scopeVariables == null) {
                scopeVariables = new ArrayList<>();
                initializedVariablesAll.put(parent, scopeVariables);
            }
            return scopeVariables;
        }

        private List<Variable> getUninitializedVariables(ASTNode parent) {
            List<Variable> scopeVariables = uninitializedVariablesAll.get(parent);
            if (scopeVariables == null) {
                scopeVariables = new ArrayList<>();
                uninitializedVariablesAll.put(parent, scopeVariables);
            }
            return scopeVariables;
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

        private boolean isArrowFunctionParameter(Variable node) {
            if (currentArrowFunction == null) {
                return false;
            }
            return getArrowFunctionParams(currentArrowFunction).contains(CodeUtils.extractVariableName(node));
        }

        private Set<String> getArrowFunctionParams(ArrowFunctionDeclaration arrowFunctionDeclaration) {
            Set<String> arrowFunctionParams = arrowFunctionParameters.get(arrowFunctionDeclaration);
            if (arrowFunctionParams == null) {
                arrowFunctionParams = new HashSet<>();
                arrowFunctionParameters.put(arrowFunctionDeclaration, arrowFunctionParams);
            }
            return arrowFunctionParams;
        }

    }

    private interface UglyElement {
        boolean matches(BaseFunctionElement functionElement);
    }

    private static final class UglyElementImpl implements UglyElement {
        private final String methodName;
        private final String className;

        public UglyElementImpl(String methodName, String className) {
            assert methodName != null;
            assert className != null;
            this.methodName = methodName;
            this.className = className;
        }

        public UglyElementImpl(BaseFunctionElement baseFunctionElement) {
            this(baseFunctionElement.getName(), baseFunctionElement.getIn() == null ? "" : baseFunctionElement.getIn());
        }

        @Override
        public boolean matches(BaseFunctionElement functionElement) {
            return methodName.equals(functionElement.getName()) && className.equals(functionElement.getIn());
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 53 * hash + Objects.hashCode(this.methodName);
            hash = 53 * hash + Objects.hashCode(this.className);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final UglyElementImpl other = (UglyElementImpl) obj;
            if (!Objects.equals(this.methodName, other.methodName)) {
                return false;
            }
            return Objects.equals(this.className, other.className);
        }

    }

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("UninitializedVariableHintDesc=Detects variables which are used, but not initialized.<br><br>Every variable should be initialized before its first use.")
    public String getDescription() {
        return Bundle.UninitializedVariableHintDesc();
    }

    @Override
    @Messages("UninitializedVariableHintDispName=Uninitialized Variables")
    public String getDisplayName() {
        return Bundle.UninitializedVariableHintDispName();
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
        JComponent customizer = new UninitializedVariableCustomizer(preferences, this);
        setCheckVariablesInitializedByReference(preferences, checkVariablesInitializedByReference(preferences));
        return customizer;
    }

    public void setCheckVariablesInitializedByReference(Preferences preferences, boolean isEnabled) {
        preferences.putBoolean(CHECK_VARIABLES_INITIALIZED_BY_REFERENCE, isEnabled);
    }

    public boolean checkVariablesInitializedByReference(Preferences preferences) {
        return preferences.getBoolean(CHECK_VARIABLES_INITIALIZED_BY_REFERENCE, false);
    }

}
