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
import com.sun.tools.javac.util.JCDiagnostic.Error;
import java.util.Set;
import org.netbeans.modules.java.source.builder.ElementsService;

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
    private ElementsService elementsService;

    private List<Pair<TypeSymbol, Symbol>> outerThisStack;
    private TypeSymbol currentClass;
    private boolean checkThis = true;

    private PostFlowAnalysis(Context ctx) {
        log = Log.instance(ctx);
        types = Types.instance(ctx);
        enter = Enter.instance(ctx);
        names = Names.instance(ctx);
        syms = Symtab.instance(ctx);
        elementsService = ElementsService.instance(ctx);
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
            (currentClass.isInner() || elementsService.isLocal(currentClass))) {
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
            try {
                boolean clash = sym != tree.sym
                        && !sym.type.isErroneous()
                        && !type.isErroneous()
                        && types.isSameType(types.erasure(sym.type), type);
                if (clash) {
                    log.error(tree.pos(), new Error(Set.of(), "compiler", "name.clash.same.erasure", tree.sym, sym)); //NOI18N
                    return;
                }
            } catch (AssertionError e) {}
        }
    }

    @Override
    public void visitNewClass(JCNewClass tree) {
        super.visitNewClass(tree);
        Symbol c = tree.constructor != null ? tree.constructor.owner : null;
        if (c != null && c != syms.noSymbol && c.hasOuterInstance()) {
            if (tree.encl == null && elementsService.isLocal(c)) {
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
                    if (tree.meth.getTag() != JCTree.Tag.SELECT && (elementsService.isLocal(c) || methName == names._this)) {
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
                log.error(pos, new Error(Set.of(), "compiler", "no.encl.instance.of.type.in.scope", c)); //NOI18N
                return;
            }
            Pair<TypeSymbol, Symbol> ot = ots.head;
            TypeSymbol otc = ot.fst;
            while (otc != c) {
                do {
                    ots = ots.tail;
                    if (ots.isEmpty()) {
                        log.error(pos, new Error(Set.of(), "compiler", "no.encl.instance.of.type.in.scope", c)); //NOI18N
                        return;
                    }
                    ot = ots.head;
                } while (ot.snd != otc);
                if (otc.owner.kind != Kinds.Kind.PCK && !otc.hasOuterInstance()) {
                    log.error(pos, new Error(Set.of(), "compiler", "cant.ref.before.ctor.called", c)); //NOI18N
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
    
    private static final int MAX_STRING_LENGTH = 65535;
    private void checkStringConstant(DiagnosticPosition pos, Object constValue) {
        if (constValue instanceof String && ((String)constValue).length() >= MAX_STRING_LENGTH)
            log.error(pos, new Error(Set.of(), "compiler", "limit.string")); //NOI18N
    }

}
