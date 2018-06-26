/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
