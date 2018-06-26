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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
