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

package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.errors.Utilities;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerPattern;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.suggestions.ExpandEnhancedForLoop", description = "#DESC_org.netbeans.modules.java.hints.suggestions.ExpandEnhancedForLoop", category="suggestions", hintKind=Kind.ACTION)
public class ExpandEnhancedForLoop {

    @TriggerPattern("for ($type $varName : $expression) { $stmts$; }")
    public static ErrorDescription run(HintContext ctx) {
        TreePath tp = ctx.getPath();
        EnhancedForLoopTree efl = (EnhancedForLoopTree) tp.getLeaf();
        long statementStart = ctx.getInfo().getTrees().getSourcePositions().getStartPosition(ctx.getInfo().getCompilationUnit(), efl.getStatement());
        int caret = ctx.getCaretLocation();

        if (caret >= statementStart) {
            return null;
        }
        
        TypeMirror expressionType = ctx.getInfo().getTrees().getTypeMirror(new TreePath(tp, efl.getExpression()));

        if (expressionType == null || expressionType.getKind() != TypeKind.DECLARED) {
            return null;
        }

        ExecutableElement iterator = findIterable(ctx.getInfo());
        Types t = ctx.getInfo().getTypes();
        if (iterator == null || !t.isSubtype(((DeclaredType) expressionType), t.erasure(iterator.getEnclosingElement().asType()))) {
            return null;
        }

        FixImpl fix = new FixImpl(TreePathHandle.create(tp, ctx.getInfo()));
        List<Fix> fixes = Collections.<Fix>singletonList(fix.toEditorFix());
        return ErrorDescriptionFactory.createErrorDescription(ctx.getSeverity(),
                                                              NbBundle.getMessage(ExpandEnhancedForLoop.class, "ERR_ExpandEhancedForLoop"),
                                                              fixes,
                                                              ctx.getInfo().getFileObject(),
                                                              caret,
                                                              caret);
        
    }

    public static ExecutableElement findIterable(CompilationInfo info) {
        TypeElement iterable = info.getElements().getTypeElement("java.lang.Iterable");

        if (iterable == null) {
            return null;
        }

        for (ExecutableElement ee : ElementFilter.methodsIn(iterable.getEnclosedElements())) {
            if (ee.getParameters().isEmpty() && ee.getSimpleName().contentEquals("iterator")) {
                return ee;
            }
        }

        return null;
    }

    private static final class FixImpl extends JavaFix {

        public FixImpl(TreePathHandle forLoop) {
            super(forLoop);
        }

        public String getText() {
            return NbBundle.getMessage(ExpandEnhancedForLoop.class, "ERR_ExpandEhancedForLoop");
        }
        
        protected void performRewrite(@NonNull TransformationContext ctx) throws Exception {
            WorkingCopy copy = ctx.getWorkingCopy();
            TreePath path = ctx.getPath();

            if (path == null) {
                return ; //XXX: log
            }

            EnhancedForLoopTree efl = (EnhancedForLoopTree) path.getLeaf();
            TypeMirror expressionType = copy.getTrees().getTypeMirror(new TreePath(path, efl.getExpression()));

            if (expressionType == null || expressionType.getKind() != TypeKind.DECLARED) {
                return ; //XXX: log
            }

            ExecutableElement getIterator = findIterable(copy);
            ExecutableType    getIteratorType = (ExecutableType) copy.getTypes().asMemberOf((DeclaredType) expressionType, getIterator);
            TypeMirror        iteratorType = Utilities.resolveTypeForDeclaration(copy, getIteratorType.getReturnType());
            TreeMaker         make = copy.getTreeMaker();
            Tree              iteratorTypeTree = make.Type(iteratorType);
            ExpressionTree    getIteratorTree = make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                                                      make.MemberSelect(efl.getExpression(), "iterator"),
                                                                      Collections.<ExpressionTree>emptyList());
            ExpressionTree    getNextTree = make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                                                  make.MemberSelect(make.Identifier("it"), "next"),
                                                                  Collections.<ExpressionTree>emptyList());
            ExpressionTree    hasNextTree = make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                                                  make.MemberSelect(make.Identifier("it"), "hasNext"),
                                                                  Collections.<ExpressionTree>emptyList());
            VariableTree orig = efl.getVariable();
            VariableTree init = make.Variable(orig.getModifiers(), "it", iteratorTypeTree, getIteratorTree);
            VariableTree value = make.Variable(orig.getModifiers(), orig.getName(), orig.getType(), getNextTree);
            List<StatementTree> statements = new LinkedList<StatementTree>();

            statements.add(0, value);

            if (efl.getStatement() != null) {
                switch (efl.getStatement().getKind()) {
                    case BLOCK:
                        BlockTree oldBlock = (BlockTree) efl.getStatement();
                        statements.addAll(oldBlock.getStatements());
                        break;
                    case EMPTY_STATEMENT:
                        break;
                    default:
                        statements.add(efl.getStatement());
                        break;
                }
            }

            BlockTree newBlock = make.Block(statements, false);
            ForLoopTree forLoop = make.ForLoop(Collections.singletonList(init), hasNextTree, Collections.<ExpressionStatementTree>emptyList(), newBlock);

            copy.rewrite(efl, forLoop);
        }
    }
}
