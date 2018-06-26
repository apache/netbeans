/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
            return CodeUtils.extractMethodName(node).toLowerCase().equalsIgnoreCase(ACTION_METHOD_PREFIX + actionName);
        }

        private static boolean isProperRenderMethod(MethodDeclaration node, String actionName) {
            return CodeUtils.extractMethodName(node).toLowerCase().equalsIgnoreCase(RENDER_METHOD_PREFIX + actionName);
        }

        private static boolean isStartupMethod(MethodDeclaration node) {
            return CodeUtils.extractMethodName(node).toLowerCase().equalsIgnoreCase(STARTUP_METHOD);
        }

        private static boolean isBeforeRenderMethod(MethodDeclaration node) {
            return CodeUtils.extractMethodName(node).toLowerCase().equalsIgnoreCase(BEFORE_RENDER_METHOD);
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
