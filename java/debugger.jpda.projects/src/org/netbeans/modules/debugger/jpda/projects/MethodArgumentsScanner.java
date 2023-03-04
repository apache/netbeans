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

package org.netbeans.modules.debugger.jpda.projects;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.util.List;
import javax.tools.Diagnostic;
import org.netbeans.spi.debugger.jpda.EditorContext.MethodArgument;

/**
 * A tree scanner, which collects argument names of a method.
 * 
 * @author Martin Entlicher
 */
class MethodArgumentsScanner extends ErrorAwareTreeScanner<MethodArgument[], Object> {
    
    private int offset;
    private CompilationUnitTree tree;
    private SourcePositions positions;
    private LineMap lineMap;
    private boolean methodInvocation;
    private ASTOperationCreationDelegate positionDelegate;
    
    private MethodArgument[] arguments;
    
    /** Creates a new instance of MethodArgumentsScanner */
    public MethodArgumentsScanner(int offset, CompilationUnitTree tree,
                                  SourcePositions positions, boolean methodInvocation,
                                  ASTOperationCreationDelegate positionDelegate) {
        this.offset = offset;
        this.tree = tree;
        this.positions = positions;
        this.lineMap = tree.getLineMap();
        this.methodInvocation = methodInvocation;
        this.positionDelegate = positionDelegate;
    }
    
    /*private MethodArgument[] scanAndReduce(Tree node, Object p, MethodArgument[] r) {
        return reduce(scan(node, p), r);
    }
    
    private MethodArgument[] scanAndReduce(Iterable<? extends Tree> nodes, Object p, MethodArgument[] r) {
        return reduce(scan(nodes, p), r);
    }*/
    
    @Override
    public MethodArgument[] visitMethodInvocation(MethodInvocationTree node, Object p) {
        if (!methodInvocation || offset != positions.getEndPosition(tree, node.getMethodSelect())) {
            return super.visitMethodInvocation(node, p);
            /*MethodArgument[] r = scan(node.getTypeArguments(), p);
            r = scanAndReduce(node.getMethodSelect(), p, r);
            r = scanAndReduce(node.getArguments(), p, r);
            return r;*/
        }
        List<? extends ExpressionTree> args = node.getArguments();
        List<? extends Tree> argTypes = node.getTypeArguments();
        /*int n = args.size();
        arguments = new MethodArgument[n];
        for (int i = 0; i < n; i++) {
            arguments[i] = new MethodArgument(args.get(i).toString(), argTypes.get(i).toString());
        }
        return arguments;*/
        arguments = composeArguments(args, argTypes);
        return arguments;
    }
    
    @Override
    public MethodArgument[] visitNewClass(NewClassTree node, Object p) {
        if (!methodInvocation || offset != positions.getEndPosition(tree, node.getIdentifier())) {
            return super.visitNewClass(node, p);
        }
        List<? extends ExpressionTree> args = node.getArguments();
        List<? extends Tree> argTypes = node.getTypeArguments();
        arguments = composeArguments(args, argTypes);
        return arguments;
    }

    @Override
    public MethodArgument[] visitMethod(MethodTree node, Object p) {
        long startMethod = positions.getStartPosition(tree, node);
        long endMethod = positions.getEndPosition(tree, node);
        if (methodInvocation || startMethod == Diagnostic.NOPOS || endMethod == Diagnostic.NOPOS ||
                                !(offset >= lineMap.getLineNumber(startMethod) &&
                                 (offset <= lineMap.getLineNumber(endMethod)))) {
            return super.visitMethod(node, p);
        }
        List<? extends VariableTree> args = node.getParameters();
        List<? extends TypeParameterTree> argTypes = node.getTypeParameters();
        int n = args.size();
        arguments = new MethodArgument[n];
        for (int i = 0; i < n; i++) {
            VariableTree var = args.get(i);
            long startOffset = positions.getStartPosition(tree, var);
            long endOffset = positions.getEndPosition(tree, var);
            if (startOffset == Diagnostic.NOPOS || endOffset == Diagnostic.NOPOS) {
                return new MethodArgument[] {};
            }
            arguments[i] = new MethodArgument(var.getName().toString(),
                                              var.getType().toString(),
                                              positionDelegate.createPosition(
                                                (int) startOffset,
                                                (int) lineMap.getLineNumber(startOffset),
                                                (int) lineMap.getColumnNumber(startOffset)),
                                              positionDelegate.createPosition(
                                                (int) endOffset,
                                                (int) lineMap.getLineNumber(endOffset),
                                                (int) lineMap.getColumnNumber(endOffset)));
        }
        return arguments;
        //return composeArguments(args, argTypes);
    }
    
    MethodArgument[] getArguments() {
        return arguments;
    }
    
    private final MethodArgument[] composeArguments(List<? extends Tree> args, List<? extends Tree> argTypes) {
        int n = args.size();
        MethodArgument[] arguments = new MethodArgument[n];
        for (int i = 0; i < n; i++) {
            Tree var = args.get(i);
            long startOffset = positions.getStartPosition(tree, var);
            long endOffset = positions.getEndPosition(tree, var);
            if (startOffset == Diagnostic.NOPOS || endOffset == Diagnostic.NOPOS) {
                return new MethodArgument[] {};
            }
            arguments[i] = new MethodArgument(var.toString(),
                                              (argTypes.size() > i) ? argTypes.get(i).toString() : "",
                                              positionDelegate.createPosition(
                                                (int) startOffset,
                                                (int) lineMap.getLineNumber(startOffset),
                                                (int) lineMap.getColumnNumber(startOffset)),
                                              positionDelegate.createPosition(
                                                (int) endOffset,
                                                (int) lineMap.getLineNumber(endOffset),
                                                (int) lineMap.getColumnNumber(endOffset)));
        }
        return arguments;
    }
    
}
