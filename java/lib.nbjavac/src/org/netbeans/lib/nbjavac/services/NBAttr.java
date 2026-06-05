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
package org.netbeans.lib.nbjavac.services;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSwitchExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;

/**
 *
 * @author lahvac
 */
public class NBAttr extends Attr {

    public static boolean TEST_DO_SINGLE_FAIL;

    public static void preRegister(Context context) {
        context.put(attrKey, new Context.Factory<Attr>() {
            public Attr make(Context c) {
                return new NBAttr(c);
            }
        });
    }

    private final CancelService cancelService;
    private final NBResolve rs;
    private final TreeMaker tm;

    public NBAttr(Context context) {
        super(context);
        cancelService = CancelService.instance(context);
        rs = NBResolve.instance(context);
        tm = TreeMaker.instance(context);
    }

    @Override
    public void attribClass(DiagnosticPosition pos, ClassSymbol c) {
        cancelService.abortIfCanceled();
        if (TEST_DO_SINGLE_FAIL) {
            TEST_DO_SINGLE_FAIL = false;
            throw new AssertionError("Test requested failure");
        }
        super.attribClass(pos, c);
    }

    @Override
    public void visitClassDef(JCClassDecl tree) {
        cancelService.abortIfCanceled();
        super.visitClassDef(tree);
    }

    @Override
    public void visitMethodDef(JCMethodDecl tree) {
        cancelService.abortIfCanceled();
        super.visitMethodDef(tree);
    }

    @Override
    public void visitBlock(JCBlock tree) {
        cancelService.abortIfCanceled();
        super.visitBlock(tree);
    }

    @Override
    public void visitVarDef(JCVariableDecl tree) {
        //for erroneous "var", make sure the synthetic make.Error() has an invalid/synthetic position:
        tm.at(-1);
        super.visitVarDef(tree);
    }

    @Override
    public Type attribType(JCTree tree, Env<AttrContext> env) {
        cancelService.abortIfCanceled();
        return super.attribType(tree, env);
    }

    @Override
    public void visitCatch(JCCatch that) {
        super.visitBlock(tm.Block(0, List.of(that.param, that.body)));
    }

    private boolean fullyAttribute;
    private Env<AttrContext> fullyAttributeResult;

    @Override
    protected void breakTreeFound(Env<AttrContext> env) {
        if (fullyAttribute) {
            fullyAttributeResult = env;
        } else {
            super.breakTreeFound(env);
        }
    }

    public Env<AttrContext> attributeAndCapture(JCTree tree, Env<AttrContext> env, JCTree to) {
        try {
            fullyAttribute = true;

            Env<AttrContext> result = tree instanceof JCExpression ?
                    attribExprToTree((JCExpression) tree, env, to) :
                    attribStatToTree(tree, env, to);

            return fullyAttributeResult != null ? fullyAttributeResult : result;
        } finally {
            fullyAttribute = false;
        }
    }

    @Override
    public Env<AttrContext> attribExprToTree(JCTree expr, Env<AttrContext> env, JCTree tree) {
        return super.attribExprToTree(expr, env, workaroundTreeTarget(expr, tree));
    }

    @Override
    public Env<AttrContext> attribStatToTree(JCTree stmt, Env<AttrContext> env, JCTree tree) {
        return super.attribStatToTree(stmt, env, workaroundTreeTarget(stmt, tree));
    }

    private JCTree workaroundTreeTarget(JCTree root, JCTree toTree) {
        if (!(toTree instanceof JCCase)) {
            return toTree;
        }
        //workaround for a bug in javac: if toTree is JCCase, it is never found
        //slightly incorrectly, but more acceptable: stop after the switch
        class Result extends RuntimeException {
            private final JCTree newToTree;

            public Result(JCTree newTarget) {
                this.newToTree = newTarget;
            }
        }
        try {
            new TreeScanner() {
                @Override
                public void visitSwitch(JCSwitch tree) {
                    if (tree.cases.contains(toTree)) {
                        throw new Result(tree);
                    }
                    super.visitSwitch(tree);
                }

                @Override
                public void visitSwitchExpression(JCSwitchExpression tree) {
                    if (tree.cases.contains(toTree)) {
                        throw new Result(tree);
                    }
                    super.visitSwitchExpression(tree);
                }
            }.scan(root);

            return toTree;
        } catch (Result r) {
            return r.newToTree;
        }
    }
}
