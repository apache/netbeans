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
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStringTemplate;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

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

    @Override
    public void visitStringTemplate(JCStringTemplate tree) {
        //workaround for:
        //a) crash when tree.processor is null
        //b) crash when the StringTemplate.process method does not exist
        //should be removed when javac is improved to be more resilient w.r.t. these errors:
        boolean prevInStringTemplate = rs.inStringTemplate;
        try {
            if (tree.processor == null) {
                tree.processor = tm.Erroneous();
            }
            rs.inStringTemplate = true;
            super.visitStringTemplate(tree);
        } finally {
            rs.inStringTemplate = prevInStringTemplate;
        }
    }

    private boolean fullyAttribute;
    private Env<AttrContext> fullyAttributeResult;

    protected void breakTreeFound(Env<AttrContext> env) {
        if (fullyAttribute) {
            fullyAttributeResult = env;
        } else {
            try {
                MethodHandles.lookup()
                             .findSpecial(Attr.class, "breakTreeFound", MethodType.methodType(void.class, Env.class), NBAttr.class)
                             .invokeExact(this, env);
            } catch (Throwable ex) {
                sneakyThrows(ex);
            }
        }
    }

    protected void breakTreeFound(Env<AttrContext> env, Type result) {
        if (fullyAttribute) {
            fullyAttributeResult = env;
        } else {
            try {
                MethodHandles.lookup()
                             .findSpecial(Attr.class, "breakTreeFound", MethodType.methodType(void.class, Env.class, Type.class), NBAttr.class)
                             .invokeExact(this, env, result);
            } catch (Throwable ex) {
                sneakyThrows(ex);
            }
        }
    }

    private <T extends Throwable> void sneakyThrows(Throwable t) throws T {
        throw (T) t;
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
}
