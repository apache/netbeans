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
package org.netbeans.modules.debugger.jpda.ui.values;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;

public class ComputeInlineValues {

    public static Collection<InlineVariable> computeVariables(CompilationInfo info, int stackLine, int stackCol, AtomicBoolean cancel) {
        Collection<InlineVariable> result = new ArrayList<>();
        int donePos = (int) info.getCompilationUnit().getLineMap().getPosition(stackLine, stackCol);
        int upcomingPos = (int) info.getCompilationUnit().getLineMap().getStartPosition(stackLine + 1);
        TreePath relevantPoint = info.getTreeUtilities().pathFor(donePos);
        OUTER: while (relevantPoint != null) {
            Tree leaf = relevantPoint.getLeaf();
            switch (leaf.getKind()) {
                case METHOD: case LAMBDA_EXPRESSION: break OUTER;
                case BLOCK:
                    if (relevantPoint.getParentPath() != null && TreeUtilities.CLASS_TREE_KINDS.contains(relevantPoint.getParentPath().getLeaf().getKind())) {
                        break OUTER;
                    }
            }
            relevantPoint = relevantPoint.getParentPath();
        }
        LineMap lm = info.getCompilationUnit().getLineMap();
        new CancellableTreePathScanner<Void, Void>(cancel) {
            @Override
            public Void visitVariable(VariableTree node, Void p) {
                int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), node);
                if (end < donePos) {
                    int[] span = info.getTreeUtilities().findNameSpan(node);

                    if (span != null) {
                        int lineEnd = (int) (lm.getStartPosition(lm.getLineNumber(span[1]) + 1) - 1);

                        result.add(new InlineVariable(span[0], span[1], lineEnd, node.getName().toString()));
                    }
                }
                return super.visitVariable(node, p);
            }

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                Element el = info.getTrees().getElement(getCurrentPath());

                if (el != null && el.getKind().isVariable() &&
                    el.getKind() != ElementKind.ENUM_CONSTANT) {
                    int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), node);
                    int end = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), node);

                    if (start != (-1) && end != (-1)) {
                        int lineEnd = (int) (lm.getStartPosition(lm.getLineNumber(end) + 1) - 1);

                        result.add(new InlineVariable(start, end, lineEnd, node.getName().toString()));
                    }
                }

                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                return null;
            }

            @Override
            public Void visitLambdaExpression(LambdaExpressionTree node, Void p) {
                return null;
            }

            @Override
            public Void scan(Tree tree, Void p) {
                if (tree != null) {
                    int start = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);

                    if (start > upcomingPos) {
                        return null;
                    }
                }
                return super.scan(tree, p);
            }

        }.scan(relevantPoint, null);

        return result;
    }

    public static final class InlineVariable {
        public final int start;
        public final int end;
        public final int lineEnd;
        public final String expression;

        public InlineVariable(int start, int end, int lineEnd, String expression) {
            this.start = start;
            this.end = end;
            this.lineEnd = lineEnd;
            this.expression = expression;
        }

    }

}
