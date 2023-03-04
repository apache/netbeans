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

package org.netbeans.modules.php.zend.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.editor.PhpVariable;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.nodes.ASTNodeInfo;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.netbeans.modules.php.zend.util.ZendUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Mysik
 */
public class ZendEditorExtender extends EditorExtender {
    static final Logger LOGGER = Logger.getLogger(ZendEditorExtender.class.getName());

    @Override
    public List<PhpBaseElement> getElementsForCodeCompletion(FileObject fo) {
        if (ZendUtils.isView(fo)) {
            return new ArrayList<PhpBaseElement>(parseAction(fo));
        }
        return Collections.emptyList();
    }

    private Set<PhpVariable> parseAction(final FileObject view) {
        assert ZendUtils.isView(view) : "Not a view: " + view;

        final FileObject action = ZendUtils.getAction(view);
        if (action == null) {
            return Collections.emptySet();
        }
        final Set<PhpVariable> phpVariables = new HashSet<>();
        try {
            ParserManager.parse(Collections.singleton(Source.create(action)), new UserTask() {
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    ParserResult parseResult = (ParserResult) resultIterator.getParserResult();
                    final ZendControllerVisitor controllerVisitor = new ZendControllerVisitor(view, (PHPParseResult) parseResult);
                    controllerVisitor.scan(Utils.getRoot(parseResult));
                    phpVariables.add(controllerVisitor.getView());
                }
            });
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return phpVariables;
    }

    private static final class ZendControllerVisitor extends DefaultVisitor {
        private final String actionName;
        private final FileObject action;
        private final PHPParseResult actionParseResult;
        private final PhpVariable view; // NOI18N
        private final Set<String> addedFields;

        private String className = null;
        private String methodName = null;

        public ZendControllerVisitor(FileObject viewFile, PHPParseResult actionParseResult) {
            assert viewFile != null;
            assert actionParseResult != null;
            Index index = actionParseResult.getModel().getIndexScope().getIndex();
            Set<InterfaceElement> interfaces = index.getInterfaces(NameKind.exact("Zend_View_Interface"));//NOI18N
            PhpClass phpClass = null;
            if (interfaces.size() > 0) {
                InterfaceElement zendViewIface = interfaces.iterator().next();
                phpClass = new PhpClass("Zend_View_Interface", "Zend_View_Interface", zendViewIface.getOffset());//NOI18N
                phpClass.setFile(zendViewIface.getFileObject());
            } else {
                phpClass = new PhpClass("Zend_View_Interface", "Zend_View_Interface");//NOI18N
            }
            this.view = new PhpVariable("$this", phpClass);// NOI18N
            FileObject file = phpClass.getFile();
            if (file != null) {
                this.view.setFile(file);
            }
            this.actionParseResult = actionParseResult;
            actionName = ZendUtils.getActionName(viewFile);
            action = ZendUtils.getAction(viewFile);
            this.addedFields = new HashSet<>();
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
                if (className != null
                        && methodName != null
                        && className.endsWith(ZendUtils.CONTROLLER_CLASS_SUFFIX.toLowerCase())
                        && methodName.equalsIgnoreCase(actionName)) {

                    // $this->view->variable?
                    if (node.getDispatcher() instanceof FieldAccess) {
                        FieldAccess fieldAccess = (FieldAccess) node.getDispatcher();
                        if ("view".equals(CodeUtils.extractVariableName(fieldAccess.getField()))) { // NOI18N
                            if (fieldAccess.getDispatcher() instanceof Variable) {
                                Variable var = (Variable) fieldAccess.getDispatcher();
                                if ("$this".equals(CodeUtils.extractVariableName(var))) { // NOI18N

                                    String name = null;
                                    String fqn = null;
                                    for (TypeScope typeScope : ModelUtils.resolveType(actionParseResult.getModel(), assignment)) {
                                        name = typeScope.getName();
                                        fqn = typeScope.getFullyQualifiedName().toString();
                                        break;
                                    }
                                    Variable field = node.getField();

                                    PhpType type = view.getType();
                                    if (type instanceof PhpClass) {
                                        PhpClass phpClass = (PhpClass) type;
                                        String fieldName = "$" + CodeUtils.extractVariableName(field);  // NOI18N
                                        PhpClass fieldType = name != null ? new PhpClass(name, fqn) : null;
                                        String fieldTypesS = fieldName + "#" + (fieldType == null ? "null" : fieldType.getFullyQualifiedName()); // NOI18N
                                        if (!addedFields.contains(fieldTypesS)) {
                                            phpClass.addField(
                                                    fieldName,
                                                    fieldType,
                                                    action,
                                                    ASTNodeInfo.toOffsetRangeVar(field).getStart());
                                            addedFields.add(fieldTypesS);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        public PhpVariable getView() {
            return view;
        }
    }
}
