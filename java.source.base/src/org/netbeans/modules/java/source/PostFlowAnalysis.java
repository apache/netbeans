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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.source;

import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.jvm.Pool;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.TypeTag;

/**
 *
 * @author Dusan Balek
 */
public class PostFlowAnalysis extends TreeScanner {
    
    private Log log;
    private Types types;
    private Enter enter;
    private Names names;
    private Symtab syms;

    private List<Pair<TypeSymbol, Symbol>> outerThisStack;
    private TypeSymbol currentClass;
    private boolean checkThis = true;

    private PostFlowAnalysis(Context ctx) {
        log = Log.instance(ctx);
        types = Types.instance(ctx);
        enter = Enter.instance(ctx);
        names = Names.instance(ctx);
        syms = Symtab.instance(ctx);
        outerThisStack = List.nil();
    }
    
    public static void analyze(Iterable<? extends Element> elems, Context ctx) {
        assert elems != null;
        PostFlowAnalysis postFlowAnalysis = new PostFlowAnalysis(ctx);
        for (Element e : elems) {
            if (e instanceof TypeSymbol) {
                Env<AttrContext> env = postFlowAnalysis.enter.getClassEnv((TypeSymbol)e);
                if (env != null) {
                    JavaFileObject prev = postFlowAnalysis.log.useSource(env.enclClass.sym.sourcefile != null
                            ? env.enclClass.sym.sourcefile : env.toplevel.sourcefile);
                    try {
                        postFlowAnalysis.scan(env.toplevel);
                    } finally {
                        postFlowAnalysis.log.useSource(prev);
                    }
                }
            }
        }
    }
    
    @Override
    public void scan(JCTree tree) {
        if (tree != null && tree.type != null && tree.type.constValue() != null) {
            checkStringConstant(tree.pos(), tree.type.constValue());
        }
        super.scan(tree);
    }

    @Override
    public void visitClassDef(JCClassDecl tree) {
        TypeSymbol currentClassPrev = currentClass;
        currentClass = tree.sym;
        List<Pair<TypeSymbol, Symbol>> prevOuterThisStack = outerThisStack;
        try {
            if (currentClass != null) {
                if (currentClass.hasOuterInstance())
                    outerThisDef(currentClass);
                super.visitClassDef(tree);
            }
        } finally {
            outerThisStack = prevOuterThisStack;
            currentClass = currentClassPrev;
        }
    }

    @Override
    public void visitMethodDef(JCMethodDecl tree) {
        if (tree.name == names.init &&
            (currentClass.isInner() || currentClass.isLocal())) {
            List<Pair<TypeSymbol, Symbol>> prevOuterThisStack = outerThisStack;
            try {
                if (currentClass.hasOuterInstance())
                    outerThisDef(tree.sym);
                super.visitMethodDef(tree);
            } finally {
                outerThisStack = prevOuterThisStack;
            }
        } else {
            super.visitMethodDef(tree);
        }
        if (tree.sym == null || tree.sym.owner == null || tree.type == null || tree.type == syms.unknownType)
            return;
        Scope s = tree.sym.owner.members();
        if (s == null) {
            // err symbols may produce null scope, see #249472
            return;
        }
        Type type = types.erasure(tree.type);
        for (Symbol sym : s.getSymbolsByName(tree.name)) {
            if (sym != tree.sym && !sym.type.isErroneous() && !type.isErroneous() &&
                !isUnknown(sym.type) && !isUnknown(type) &&
                types.isSameType(types.erasure(sym.type), type)) {
                log.error(tree.pos(), "name.clash.same.erasure", tree.sym, sym); //NOI18N
                return;
            }
        }
    }

    @Override
    public void visitNewClass(JCNewClass tree) {
        super.visitNewClass(tree);
        Symbol c = tree.constructor != null ? tree.constructor.owner : null;
        if (c != null && c.hasOuterInstance()) {
            if (tree.encl == null && c.isLocal()) {
                checkThis(tree.pos(), c.type.getEnclosingType().tsym);
            }
        }
    }

    @Override
    public void visitApply(JCMethodInvocation tree) {
        boolean prevCheckThis = checkThis;
        try {
            Symbol meth = TreeInfo.symbol(tree.meth);
            Name methName = TreeInfo.name(tree.meth);
            if (meth != null && meth.name == names.init) {
                Symbol c = meth.owner;
                if (c.hasOuterInstance()) {
                    checkThis = false;
                    if (tree.meth.getTag() != JCTree.Tag.SELECT && (c.isLocal() || methName == names._this)) {
                        checkThis(tree.meth.pos(), c.type.getEnclosingType().tsym);
                    }
                }
            }
            super.visitApply(tree);
        } finally {
            checkThis = prevCheckThis;
        }
    }

    @Override
    public void visitSelect(JCFieldAccess tree) {
        super.visitSelect(tree);
        if (tree.selected.type != null && (tree.name == names._this || (tree.name == names._super && !types.isDirectSuperInterface(tree.selected.type.tsym, currentClass))))
            checkThis(tree.pos(), tree.selected.type.tsym);
    }

    private void checkThis(DiagnosticPosition pos, TypeSymbol c) {
        if (checkThis && currentClass != c) {
            List<Pair<TypeSymbol, Symbol>> ots = outerThisStack;
            if (ots.isEmpty()) {
                log.error(pos, "no.encl.instance.of.type.in.scope", c); //NOI18N
                return;
            }
            Pair<TypeSymbol, Symbol> ot = ots.head;
            TypeSymbol otc = ot.fst;
            while (otc != c) {
                do {
                    ots = ots.tail;
                    if (ots.isEmpty()) {
                        log.error(pos, "no.encl.instance.of.type.in.scope", c); //NOI18N
                        return;
                    }
                    ot = ots.head;
                } while (ot.snd != otc);
                if (otc.owner.kind != Kinds.Kind.PCK && !otc.hasOuterInstance()) {
                    log.error(pos, "cant.ref.before.ctor.called", c); //NOI18N
                    return;
                }
                otc = ot.fst;
            }
        }
    }

    private void outerThisDef(Symbol owner) {
        Type target = types.erasure(owner.enclClass().type.getEnclosingType());
        Pair<TypeSymbol, Symbol> outerThis = Pair.of(target.tsym, owner);
        outerThisStack = outerThisStack.prepend(outerThis);
    }
    
    private void checkStringConstant(DiagnosticPosition pos, Object constValue) {
        if (constValue instanceof String && ((String)constValue).length() >= Pool.MAX_STRING_LENGTH)
            log.error(pos, "limit.string"); //NOI18N
    }
    
    private boolean isUnknown(Type t) {
        return t != null && t.accept(new Types.DefaultTypeVisitor<Boolean, Void>() {
            @Override
            public Boolean visitType(Type t, Void s) {
                return t.hasTag(TypeTag.UNKNOWN);
            }

            @Override
            public Boolean visitMethodType(Type.MethodType t, Void s) {
                return visit(t.getReturnType(), s);
            }            
        }, null);
    }
}
