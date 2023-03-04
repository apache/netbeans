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

package org.netbeans.modules.php.symfony.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpVariable;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.symfony.util.SymfonyUtils;
import org.openide.filesystems.FileObject;

/**
 * @author Tomas Mysik
 */
public class SymfonyEditorExtender extends EditorExtender {
    static final Logger LOGGER = Logger.getLogger(SymfonyEditorExtender.class.getName());
    private static final List<PhpBaseElement> ELEMENTS = Arrays.<PhpBaseElement>asList(
            new PhpVariable("$sf_user", new PhpClass("sfUser", "sfUser")), // NOI18N
            new PhpVariable("$sf_request", new PhpClass("sfWebRequest", "sfWebRequest")), // NOI18N
            new PhpVariable("$sf_response", new PhpClass("sfWebResponse", "sfWebResponse"))); // NOI18N

    @Override
    public List<PhpBaseElement> getElementsForCodeCompletion(FileObject fo) {
        if (SymfonyUtils.isView(fo)) {
            List<PhpBaseElement> elements = new LinkedList<>(ELEMENTS);
            elements.addAll(parseAction(fo));
            return elements;
        }
        return Collections.emptyList();
    }

    private PhpClass getPhpClass(PhpBaseElement element) {
        String fqn = element.getFullyQualifiedName();
        if (fqn == null) {
            return null;
        }
        return new PhpClass(element.getName(), fqn);
    }

    private Set<PhpVariable> parseAction(final FileObject view) {
        assert SymfonyUtils.isView(view) : "Not a view: " + view;

        final FileObject action = SymfonyUtils.getAction(view);
        if (action == null) {
            return Collections.emptySet();
        }
        for (PhpBaseElement phpBaseElement : ELEMENTS) {
            phpBaseElement.setFile(action);
        }
        final Set<PhpVariable> phpVariables = new HashSet<>();
        try {
            ParserManager.parse(Collections.singleton(Source.create(action)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ParserResult parseResult = (ParserResult) resultIterator.getParserResult();
                    final SymfonyControllerVisitor controllerVisitor = new SymfonyControllerVisitor(view, (PHPParseResult) parseResult);
                    controllerVisitor.scan(Utils.getRoot(parseResult));
                    phpVariables.addAll(controllerVisitor.getPhpVariables());
                }
            });
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return phpVariables;
    }

    private static final class SymfonyControllerVisitor extends DefaultVisitor {
        private final String actionName;
        private final FileObject action;
        private final PHPParseResult actionParseResult;
        private final Set<PhpVariable> fields = new HashSet<>();

        private String className = null;
        private String methodName = null;

        public SymfonyControllerVisitor(FileObject view, PHPParseResult actionParseResult) {
            assert view != null;
            assert actionParseResult != null;

            this.actionParseResult = actionParseResult;
            actionName = SymfonyUtils.getActionName(view);
            action = SymfonyUtils.getAction(view);
        }

        @Override
        public void visit(ClassDeclaration node) {
            className = CodeUtils.extractClassName(node).toLowerCase();
            super.visit(node);
        }

        @Override
        public void visit(MethodDeclaration node) {
            methodName = CodeUtils.extractMethodName(node).toLowerCase();
            super.visit(node);
        }

        @Override
        public void visit(Assignment assignment) {
            super.visit(assignment);
            if (assignment.getLeftHandSide() instanceof FieldAccess) {
                final FieldAccess node = (FieldAccess) assignment.getLeftHandSide();
                if (action != null
                        && className != null
                        && methodName != null
                        && className.endsWith(SymfonyUtils.ACTION_CLASS_SUFFIX)
                        && methodName.equals(actionName)) {
                    if (node.getDispatcher() instanceof Variable
                            && "$this".equals(CodeUtils.extractVariableName((Variable) node.getDispatcher()))) { // NOI18N

                        String name = null;
                        String fqn = null;
                        for (TypeScope typeScope : ModelUtils.resolveType(actionParseResult.getModel(), assignment)) {
                            name = typeScope.getName();
                            fqn = typeScope.getFullyQualifiedName().toString();
                            break;
                        }
                        Variable field = node.getField();
                        synchronized (fields) {
                            final PhpVariable phpVariable = new PhpVariable("$" + CodeUtils.extractVariableName(field),
                                    name != null ? new PhpClass(name, fqn) : null, action, ASTNodeInfo.toOffsetRangeVar(field).getStart());
                            phpVariable.setFile(action);
                            fields.add(phpVariable);
                        }
                    }
                }
            }
        }

        public Set<PhpVariable> getPhpVariables() {
            Set<PhpVariable> phpVariables = new HashSet<>();
            synchronized (fields) {
                phpVariables.addAll(fields);
            }
            return phpVariables;
        }
    }
}
