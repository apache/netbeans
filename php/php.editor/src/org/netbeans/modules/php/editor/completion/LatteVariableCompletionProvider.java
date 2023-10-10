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
package org.netbeans.modules.php.editor.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.spi.templates.completion.CompletionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
@ServiceProvider(service = CompletionProvider.class, path = "Latte/Completion/Variables") //NOI18N
public class LatteVariableCompletionProvider implements CompletionProvider {
    private static final Logger LOGGER = Logger.getLogger(LatteVariableCompletionProvider.class.getName());
    private static final String PRESENTER_CLASS_SUFFIX = "Presenter"; //NOI18N
    private static final String ACTION_METHOD_PREFIX = "action"; //NOI18N
    private static final String RENDER_METHOD_PREFIX = "render"; //NOI18N
    private static final String STARTUP_METHOD = "startup"; //NOI18N
    private static final String BEFORE_RENDER_METHOD = "beforeRender"; //NOI18N

    private Set<String> result;
    private FileObject templateFile;
    private String variablePrefix;

    @Override
    public Set<String> getItems(FileObject templateFile, String variablePrefix) {
        result = new HashSet<>();
        if (LatteUtils.isView(templateFile)) {
            this.templateFile = templateFile;
            this.variablePrefix = variablePrefix;
            processTemplateFile(templateFile);
        }
        return result;
    }

    private void processTemplateFile(FileObject templateFile) {
        FileObject presenterFile = LatteUtils.getPresenterFile(templateFile);
        if (presenterFile != null) {
            try {
                parsePresenter(presenterFile);
            } catch (ParseException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
    }

    private void parsePresenter(FileObject presenterFile) throws ParseException {
        ParserManager.parse(Collections.singleton(Source.create(presenterFile)), new UserTask() {

            @Override
            public void run(ResultIterator resultIterator) throws Exception {
                PHPParseResult parseResult = (PHPParseResult) resultIterator.getParserResult();
                PresenterVisitor presenterVisitor = new PresenterVisitor(templateFile);
                presenterVisitor.scan(parseResult.getProgram());
                for (MethodDeclaration methodToScan : presenterVisitor.getMethodsToScan()) {
                    VariableVisitor variableVisitor = new VariableVisitor(parseResult.getModel(), variablePrefix);
                    methodToScan.accept(variableVisitor);
                    result.addAll(variableVisitor.getVariables());
                }
            }
        });
    }

    private static String extractActionName(FileObject templateFile) {
        String result = templateFile.getName();
        if (result.contains(".")) { //NOI18N
            String[] parts = result.split("\\."); //NOI18N
            assert parts.length > 1;
            result = parts[1];
        }
        return result;
    }

    private static final class PresenterVisitor extends DefaultVisitor {
        private final String actionName;
        private Set<MethodDeclaration> methodsToScan = new HashSet<>();

        public PresenterVisitor(FileObject templateFile) {
            actionName = extractActionName(templateFile);
        }

        @Override
        public void visit(ClassDeclaration node) {
            if (CodeUtils.extractClassName(node).toLowerCase().endsWith(PRESENTER_CLASS_SUFFIX.toLowerCase())) {
                super.visit(node);
            }
        }

        @Override
        public void visit(MethodDeclaration node) {
            if (isProperActionMethod(node, actionName) || isProperRenderMethod(node, actionName) || isStartupMethod(node) || isBeforeRenderMethod(node)) {
                methodsToScan.add(node);
            }
        }

        private static boolean isProperActionMethod(MethodDeclaration node, String actionName) {
            return CodeUtils.extractMethodName(node).equalsIgnoreCase(ACTION_METHOD_PREFIX + actionName);
        }

        private static boolean isProperRenderMethod(MethodDeclaration node, String actionName) {
            return CodeUtils.extractMethodName(node).equalsIgnoreCase(RENDER_METHOD_PREFIX + actionName);
        }

        private static boolean isStartupMethod(MethodDeclaration node) {
            return CodeUtils.extractMethodName(node).equalsIgnoreCase(STARTUP_METHOD);
        }

        private static boolean isBeforeRenderMethod(MethodDeclaration node) {
            return CodeUtils.extractMethodName(node).equalsIgnoreCase(BEFORE_RENDER_METHOD);
        }

        public Set<MethodDeclaration> getMethodsToScan() {
            return new HashSet<>(methodsToScan);
        }

    }

    private static final class VariableVisitor extends DefaultVisitor {
        private static final List<String> TEMPLATE_DISPATCHER_TYPES = new ArrayList<>();
        static {
            TEMPLATE_DISPATCHER_TYPES.add("Nette\\Templating\\ITemplate"); //NOI18N
            TEMPLATE_DISPATCHER_TYPES.add("Nette\\Application\\UI\\ITemplate"); //NOI18N
        }
        private static final String VARIABLE_PREFIX = "$"; //NOI18N
        private final Set<FieldAccess> fieldAccesses = new HashSet<>();
        private final Model model;
        private final String variablePrefix;

        public VariableVisitor(Model model, String variablePrefix) {
            this.model = model;
            this.variablePrefix = variablePrefix;
        }

        @Override
        public void visit(Assignment node) {
            VariableBase leftHandSide = node.getLeftHandSide();
            if (leftHandSide instanceof FieldAccess) {
                fieldAccesses.add((FieldAccess) leftHandSide);
            }
        }

        public Set<String> getVariables() {
            Set<String> result = new HashSet<>();
            List<TypeScope> templateTypes = new ArrayList<>();
            for (String templateDispatcherType : TEMPLATE_DISPATCHER_TYPES) {
                templateTypes.addAll(model.getIndexScope().findTypes(QualifiedName.create(templateDispatcherType)));
            }
            for (FieldAccess fieldAccess : fieldAccesses) {
                Collection<? extends TypeScope> types = ModelUtils.resolveType(model, fieldAccess.getDispatcher(), false);
                TypeScope dispatcherType = ModelUtils.getFirst(types);
                if (existsTemplateTypeAndDispatcherTypeMatches(dispatcherType, templateTypes) || addAllPossibleVariables(templateTypes)) {
                    Variable field = fieldAccess.getField();
                    String varName = CodeUtils.extractVariableName(field);
                    if (varName != null) {
                        String variableName = VARIABLE_PREFIX + varName;
                        if (variablePrefix.length() == 0 || variableName.toLowerCase().startsWith(variablePrefix.toLowerCase())) {
                            result.add(variableName);
                        }
                    }
                }
            }
            return result;
        }

        private static boolean existsTemplateTypeAndDispatcherTypeMatches(TypeScope dispatcherType, List<TypeScope> templateTypes) {
            return templateTypes != null && dispatcherType != null && isOfType(dispatcherType, templateTypes);
        }

        private static boolean isOfType(TypeScope dispatcherType, List<TypeScope> templateTypes) {
            boolean result = false;
            if (dispatcherType != null) {
                for (TypeScope templateType : templateTypes) {
                    if (dispatcherType.isSubTypeOf(templateType) || dispatcherType.equals(templateType)) {
                        result = true;
                        break;
                    }
                }
            }
            return result;
        }

        private static boolean addAllPossibleVariables(List<TypeScope> templateTypes) {
            return templateTypes == null || templateTypes.isEmpty();
        }

    }

}
