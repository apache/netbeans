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

package org.netbeans.modules.groovy.refactoring;

import java.util.Collections;
import java.util.Set;
import javax.swing.text.JTextComponent;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.AstPath;
import org.netbeans.modules.groovy.editor.api.ElementUtils;
import org.netbeans.modules.groovy.editor.api.FindTypeUtils;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.groovy.refactoring.findusages.model.ClassRefactoringElement;
import org.netbeans.modules.groovy.refactoring.findusages.model.MethodRefactoringElement;
import org.netbeans.modules.groovy.refactoring.findusages.model.RefactoringElement;
import org.netbeans.modules.groovy.refactoring.findusages.model.VariableRefactoringElement;
import org.netbeans.modules.groovy.refactoring.utils.FindMethodUtils;
import org.netbeans.modules.groovy.refactoring.utils.FindPossibleMethods;
import org.netbeans.modules.groovy.refactoring.utils.GroovyProjectUtil;
import org.netbeans.modules.groovy.refactoring.utils.TypeResolver;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 * Abstract groovy refactoring task. In the current state it is always either
 * TextComponentTask (which means refactoring called from the editor) or
 * NodeToElementTask (refactoring called on the concrete node)
 *
 * @author Martin Janicek
 */
public abstract class RefactoringTask extends UserTask implements Runnable {

    private RefactoringTask() {
    }


    public abstract boolean isValid();


    protected abstract static class TextComponentTask extends RefactoringTask {

        private final FileObject fileObject;
        private JTextComponent textC;
        private RefactoringUI ui;


        protected TextComponentTask(EditorCookie ec, FileObject fileObject) {
            this.textC = ec.getOpenedPanes()[0];
            this.fileObject = fileObject;

            assert textC != null;
            assert textC.getCaretPosition() != -1;
            assert textC.getSelectionStart() != -1;
            assert textC.getSelectionEnd() != -1;
        }

        @Override
        public boolean isValid() {
            try {
                ParserManager.parse(Collections.singleton(Source.create(fileObject)), this);
                return true;
            } catch (Exception ex) {
                return false;
            } catch (AssertionError error) {
                return false;
            }
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            final GroovyParserResult parserResult = ASTUtils.getParseResult(resultIterator.getParserResult());
            final ASTNode root = ASTUtils.getRoot(parserResult);
            if (root == null) {
                throw new IllegalStateException("Not possible to get correct AST!"); // NOI18N
            }

            final int caret = textC.getCaretPosition();
            final int start = textC.getSelectionStart();
            final int end = textC.getSelectionEnd();

            final BaseDocument doc = GroovyProjectUtil.getDocument(parserResult, fileObject);
            final AstPath path = new AstPath(root, caret, doc);
            final ASTNode findingNode = FindTypeUtils.findCurrentNode(path, doc, caret);
            final ElementKind kind;
            if (findingNode instanceof PackageNode) {
                kind = ElementKind.PACKAGE;
            } else {
                kind = ElementUtils.getKind(path, doc, caret);
            }

            final RefactoringElement element = createRefactoringElement(path, findingNode, kind);
            if (element != null && element.getName() != null && element.getFileObject() != null) {
                ui = createRefactoringUI(element, start, end, parserResult);
            } else {
                throw new IllegalStateException("RefactoringElement isn't initiated correctly!"); // NOI18N
            }
        }

        @SuppressWarnings("fallthrough")
        private RefactoringElement createRefactoringElement(AstPath path, ASTNode currentNode, ElementKind kind) {
            switch (kind) {
                case CLASS:
                case INTERFACE:
                    if (currentNode instanceof VariableExpression) {
                        return new ClassRefactoringElement(fileObject, TypeResolver.resolveType(path, fileObject));
                    }

                    return new ClassRefactoringElement(fileObject, currentNode);
                case METHOD:
                case CONSTRUCTOR:
                    final ASTNode leaf = path.leaf();
                    final ASTNode leafParent = path.leafParent();

                    if (leaf instanceof MethodNode || leaf instanceof ConstructorNode) {
                        return new MethodRefactoringElement(fileObject, leaf, ElementUtils.getDeclaringClass(leaf));
                    }

                    if (leaf instanceof ConstructorCallExpression) {
                        final ConstructorCallExpression constructorCall = (ConstructorCallExpression) leaf;
                        return new ClassRefactoringElement(fileObject, constructorCall.getType());
                    }

                    if (leaf instanceof ConstantExpression && leafParent instanceof MethodCallExpression) {
                        final MethodNode methodNode = FindMethodUtils.findMethod(path, (MethodCallExpression) leafParent);
                        final ClassNode methodType = FindMethodUtils.findMethodType(path, (MethodCallExpression) leafParent);
                        final String methodName = ((ConstantExpression) leaf).getText();

                        if (methodType != null) {
                            if (methodNode != null) {
                                // This can happen in situations:
                                //    * method()
                                //    * this.method()
                                //    * new SomeClassName().method()
                                return new MethodRefactoringElement(fileObject, methodNode, methodType);
                            } else {
                                // This can happen in situation:
                                //    * SomeClassName abc = new SomeClassName()
                                //    * abc.method()
                                final Set<MethodNode> possibleMethods = FindPossibleMethods.findPossibleMethods(fileObject, methodType.getName(), methodName);
                                if (possibleMethods.size() > 0) {
                                    return new MethodRefactoringElement(fileObject, possibleMethods.iterator().next(), methodType);
                                }
                            }
                        } else {
                            // This can happen in situation with dynamic type:
                            //    * def abc = new SomeClassName()
                            //    * abc.method()
                            return null;
                            // We have to improve type interference for dynamic types --> see issue 219905
                            // return new MethodRefactoringElement(fileObject, leafParent);
                        }
                    }

                    assert false; // Should never happened!
                case VARIABLE:
                    final ClassNode variableType = TypeResolver.resolveType(path, fileObject);
                    return new VariableRefactoringElement(fileObject, variableType, currentNode.getText());
                case PROPERTY:
                case FIELD:
                    if (currentNode instanceof ClassNode) {
                        return new ClassRefactoringElement(fileObject, currentNode);
                    } else if (currentNode instanceof FieldNode) {
                        final FieldNode field = (FieldNode) currentNode;
                        return new VariableRefactoringElement(fileObject, field.getOwner(), field.getName());
                    } else if (currentNode instanceof PropertyNode) {
                        final FieldNode field = ((PropertyNode) currentNode).getField();
                        return new VariableRefactoringElement(fileObject, field.getOwner(), field.getName());
                    }
                case PACKAGE:
//                    return new PackageRefactoringElement(fileObject, currentNode);
                default:
                    throw new IllegalStateException("Unknown element kind. Refactoring shouldn't be enabled in this context !"); // NOI18N
            }
        }

        @Override
        public final void run() {
            try {
                ParserManager.parse(Collections.singleton(Source.create(fileObject)), this);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            UI.openRefactoringUI(ui, TopComponent.getRegistry().getActivated());
        }

        protected abstract RefactoringUI createRefactoringUI(RefactoringElement selectedElement, int startOffset, int endOffset, GroovyParserResult info);
    }

    protected abstract static class NodeToElementTask extends RefactoringTask {

        private final FileObject fileObject;
        private RefactoringUI ui;


        protected NodeToElementTask(FileObject fileObject) {
            this.fileObject = fileObject;
        }

        @Override
        public boolean isValid() {
            try {
                ParserManager.parse(Collections.singleton(Source.create(fileObject)), this);
            } catch (Exception ex) {
                return false;
            }
            return true;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            final GroovyParserResult parserResult = ASTUtils.getParseResult(resultIterator.getParserResult());
            final ASTNode root = ASTUtils.getRoot(parserResult);
            if (root == null) {
                return;
            }

            final RefactoringElement element = new ClassRefactoringElement(fileObject, root);
            if (element.getName() != null) {
                ui = createRefactoringUI(element, parserResult);
            }

            if (ui == null) {
                throw new IllegalStateException();
            }
        }

        @Override
        public final void run() {
            try {
                ParserManager.parse(Collections.singleton(Source.create(fileObject)), this);
            } catch (Exception ex) {
                return;
            }
            UI.openRefactoringUI(ui);
        }

        protected abstract RefactoringUI createRefactoringUI(RefactoringElement selectedElement, GroovyParserResult info);
    }
}
