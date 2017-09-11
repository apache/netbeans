/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.util.TreePath;
import java.util.Deque;
import java.util.LinkedList;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.support.CancellableTreePathScanner;
import org.netbeans.spi.java.hints.HintContext;

/**
 * If not in method or a lambda, it detects all assignments/compound assignments
 * as side effects. Within a method call, it should catch all field assignments.
 * When inside lambda or anonymous class, it ignores field assignments to those.
 * If a method is invoked on a created instance,
 * class scopes
 */
public class SideEffectVisitor extends CancellableTreePathScanner {
    private int nestingLevel;
    private int invocationChainLevel;
    private final boolean nonLocals;
    private final HintContext ctx;
    private Deque<TypeElement> enclosingElements = new LinkedList<TypeElement>();
    private Tree invocationTree;
    private boolean stopOnUnknownMethods;

    public SideEffectVisitor(HintContext ctx) {
        this.ctx = ctx;
        this.nonLocals = false;
    }
    
    /**
     * Explores side effects of the scanned tree. If 'nonLocals' is true, it will report
     * as a potential side effect each call to a method on a different object. QName.this
     * and QName.super qualifiers are recognized as local calls. Called methods are still
     * inspected for side effects
     * 
     * @param ctx context
     * @param nonLocals if true, any call to a different object is reported as potential side effect
     */
    public SideEffectVisitor(HintContext ctx, boolean nonLocals) {
        this.ctx = ctx;
        this.nonLocals = false;
    }

    public SideEffectVisitor stopOnUnknownMethods(boolean flag) {
        this.stopOnUnknownMethods = flag;
        return this;
    }

    @Override
    public Object visitUnary(UnaryTree node, Object p) {
        switch (node.getKind()) {
            case POSTFIX_DECREMENT:
            case POSTFIX_INCREMENT:
            case PREFIX_DECREMENT:
            case PREFIX_INCREMENT:
                break;
            default:
                return super.visitUnary(node, p);
        }
        checkVariableAccess(node.getExpression(), node);
        return super.visitUnary(node, p);
    }

    @Override
    public Object visitCompoundAssignment(CompoundAssignmentTree node, Object p) {
        checkVariableAccess(node.getVariable(), node.getVariable());
        return super.visitCompoundAssignment(node, p);
    }

    /**
     * On level 1 (the method itself), any access to any local/field reports a side effect. Within nested classes
     * or lambdas, only access to fields of the method's enclosing types report side effect.
     * Anonymous, locals and lambdas may not have static fields.
     *
     * @param subNode
     */
    private void checkVariableAccess(Tree checkVar, Tree subNode) {
        if (nestingLevel == 0) {
            stop(subNode);
        }
        Element el = ctx.getInfo().getTrees().getElement(checkVar == getCurrentPath().getLeaf() ? getCurrentPath() : new TreePath(getCurrentPath(), checkVar));
        if (el != null && el.getKind() == ElementKind.FIELD) {
            Element x = el.getEnclosingElement();
            if (!enclosingElements.contains(x)) {
                stop(subNode);
            }
        }
    }

    @Override
    public Object visitAssignment(AssignmentTree node, Object p) {
        checkVariableAccess(node.getVariable(), node.getVariable());
        return super.visitAssignment(node, p);
    }

    private void stop(Tree node) {
        throw new StopProcessing(invocationTree != null ? invocationTree : node);
    }

    @Override
    public Object visitClass(ClassTree node, Object p) {
        Element e = ctx.getInfo().getTrees().getElement(getCurrentPath());
        Object r = scan(node.getModifiers(), p);
        r = scanAndReduce(node.getTypeParameters(), p, r);
        r = scanAndReduce(node.getExtendsClause(), p, r);
        r = scanAndReduce(node.getImplementsClause(), p, r);
        nestingLevel++;
        enclosingElements.push((TypeElement) e);
        r = scanAndReduce(node.getMembers(), p, r);
        nestingLevel--;
        enclosingElements.pop();
        return r;
    }

    @Override
    public Object visitNewClass(NewClassTree node, Object p) {
        Element e = ctx.getInfo().getTrees().getElement(getCurrentPath());
        if (e == null) {
            return super.visitNewClass(node, p);
        } else {
            e = e.getEnclosingElement();
        }
        if (e != null && e.getKind().isClass()) {
            Object r = scan(node.getEnclosingExpression(), p);
            r = scanAndReduce(node.getIdentifier(), p, r);
            r = scanAndReduce(node.getTypeArguments(), p, r);
            r = scanAndReduce(node.getArguments(), p, r);
            nestingLevel++;
            enclosingElements.push((TypeElement) e);
            r = scanAndReduce(node.getClassBody(), p, r);
            nestingLevel--;
            enclosingElements.pop();
            return r;
        } else {
            return super.visitNewClass(node, p);
        }
    }

    @Override
    public Object visitMemberSelect(MemberSelectTree node, Object p) {
        String s = node.getExpression().toString();
        Name id = node.getIdentifier();
        if (id.contentEquals("this") || id.contentEquals("super")) {
            // this.sym || super.sym
            return Boolean.TRUE;
        }
        if (s.endsWith("this") || s.endsWith("super")) {
            return Boolean.TRUE;
        }
        return node;
    }
    
    @Override
    public Object visitMethodInvocation(MethodInvocationTree node, Object p) {
        Object r = scan(node.getArguments(), p);
        Object x = scan(node.getMethodSelect(), p);
        if (x instanceof Tree && nonLocals) {
            stop((Tree)x);
        }
        Object o = reduce(r, x);

        Element e = ctx.getInfo().getTrees().getElement(getCurrentPath());
        if (e != null && e.getKind() != ElementKind.METHOD) {
            return o;
        }
        if (invocationChainLevel > 0) {
            if (stopOnUnknownMethods) {
                stop(node);
            }
            return o;
        }
        ExecutableElement el = (ExecutableElement) e;
        TreePath target = ctx.getInfo().getTrees().getPath(el);
        if (target != null) {
            invocationChainLevel++;
            nestingLevel++;
            invocationTree = node;
            // hack! will replace current path with the path to the method. The scan may process a Path from
            // a different CU !
            scan(target, null);
            invocationTree = null;
            nestingLevel--;
            invocationChainLevel--;
            // no current path is defined here !!
        } else if (stopOnUnknownMethods) {
            stop(node);
        }
        return o;
    }

    @Override
    protected boolean isCanceled() {
        return ctx.isCanceled();
    }
    
    // helper methods
    private Object scanAndReduce(Tree node, Object p, Object r) {
        return reduce(scan(node, p), r);
    }

    private Object scanAndReduce(Iterable<? extends Tree> nodes, Object p, Object r) {
        return reduce(scan(nodes, p), r);
    }
    
}
