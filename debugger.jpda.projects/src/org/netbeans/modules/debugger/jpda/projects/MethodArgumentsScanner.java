/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import com.sun.source.util.TreeScanner;
import java.util.List;
import javax.tools.Diagnostic;
import org.netbeans.spi.debugger.jpda.EditorContext.MethodArgument;

/**
 * A tree scanner, which collects argument names of a method.
 * 
 * @author Martin Entlicher
 */
class MethodArgumentsScanner extends TreeScanner<MethodArgument[], Object> {
    
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
