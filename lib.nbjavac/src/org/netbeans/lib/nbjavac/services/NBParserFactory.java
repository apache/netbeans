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

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.netbeans.lib.nbjavac.services.NBTreeMaker.IndexedClassDecl;

/**
 *
 * @author lahvac
 */
public class NBParserFactory extends ParserFactory {

    public static void preRegister(Context context) {
        context.put(parserFactoryKey, new Context.Factory<ParserFactory>() {
            @Override
            public ParserFactory make(Context c) {
                return new NBParserFactory(c);
            }
        });
    }

    private final ScannerFactory scannerFactory;
    private final Names names;
    private final CancelService cancelService;

    protected NBParserFactory(Context context) {
        super(context);
        this.scannerFactory = ScannerFactory.instance(context);
        this.names = Names.instance(context);
        this.cancelService = CancelService.instance(context);
    }

    @Override
    public JavacParser newParser(CharSequence input, boolean keepDocComments, boolean keepEndPos, boolean keepLineMap, boolean parseModuleInfo) {
        Lexer lexer = scannerFactory.newScanner(input, keepDocComments);
        return new NBJavacParser(this, lexer, keepDocComments, keepLineMap, keepEndPos, parseModuleInfo, cancelService);
    }

    public static class NBJavacParser extends JavacParser {

        private final Names names;
        private final CancelService cancelService;

        public NBJavacParser(NBParserFactory fac, Lexer S, boolean keepDocComments, boolean keepLineMap, boolean keepEndPos, boolean parseModuleInfo, CancelService cancelService) {
            super(fac, S, keepDocComments, keepLineMap, keepEndPos, parseModuleInfo);
            this.names = fac.names;
            this.cancelService = cancelService;
        }

        @Override
        protected AbstractEndPosTable newEndPosTable(boolean keepEndPositions) {
            AbstractEndPosTable res = super.newEndPosTable(keepEndPositions);
            
            if (keepEndPositions) {
                return new EndPosTableImpl(S, this, (SimpleEndPosTable) res);
            }
            
            return res;
        }

        @Override
        protected JCClassDecl classDeclaration(JCModifiers mods, Comment dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.classDeclaration(mods, dc);
        }

        @Override
        protected JCClassDecl interfaceDeclaration(JCModifiers mods, Comment dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.interfaceDeclaration(mods, dc);
        }

        @Override
        protected JCClassDecl enumDeclaration(JCModifiers mods, Comment dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.enumDeclaration(mods, dc);
        }

        @Override
        protected JCTree methodDeclaratorRest(int pos, JCModifiers mods, JCExpression type, Name name, List<JCTypeParameter> typarams, boolean isInterface, boolean isVoid, Comment dc) {
            if (cancelService != null) {
                cancelService.abortIfCanceled();
            }
            return super.methodDeclaratorRest(pos, mods, type, name, typarams, isInterface, isVoid, dc);
        }

        @Override
        public JCCompilationUnit parseCompilationUnit() {
            JCCompilationUnit toplevel = super.parseCompilationUnit();
            assignAnonymousClassIndices(names, toplevel, null, -1);
            return toplevel;
        }

        @Override
        public int getEndPos(JCTree jctree) {
            return TreeInfo.getEndPos(jctree, endPosTable);
        }

        public final class EndPosTableImpl extends AbstractEndPosTable {
            
            private final Lexer lexer;
            private final SimpleEndPosTable delegate;

            private EndPosTableImpl(Lexer lexer, JavacParser parser, SimpleEndPosTable delegate) {
                super(parser);
                this.lexer = lexer;
                this.delegate = delegate;
            }
            
            public void resetErrorEndPos() {
                delegate.errorEndPos = Position.NOPOS;
                errorEndPos = delegate.errorEndPos;
            }
            
            @Override public void storeEnd(JCTree tree, int endpos) {
                if (endpos >= 0)
                    delegate.storeEnd(tree, endpos);
            }

            @Override
            public void setErrorEndPos(int errPos) {
                delegate.setErrorEndPos(errPos);
                errorEndPos = delegate.errorEndPos;
            }

            @Override
            protected <T extends JCTree> T to(T t) {
                storeEnd(t, parser.token().endPos);
                return t;
            }

            @Override
            protected <T extends JCTree> T toP(T t) {
                storeEnd(t, lexer.prevToken().endPos);
                return t;
            }

            @Override
            public int getEndPos(JCTree jctree) {
                return delegate.getEndPos(jctree);
            }

            @Override
            public int replaceTree(JCTree jctree, JCTree jctree1) {
                return delegate.replaceTree(jctree, jctree1);
            }
        }
    }

    public static void assignAnonymousClassIndices(Names names, JCTree tree, Name name, int startNumber) {
        AssignAnonymousIndices aai = new AssignAnonymousIndices(names);

        if (name != null) {
            aai.newAnonScope(name, startNumber);
        }

        aai.scan(tree);
    }

    private static final class AssignAnonymousIndices extends TreeScanner {
        private final Names names;

        public AssignAnonymousIndices(Names names) {
            this.names = names;
        }

        /**
         *Represents a scope for anon class number assignment
         */
        private static class AnonScope {
            public boolean localClass;
            private final Name parentDecl;
            private int currentNumber;
            private Map<Name,Integer> localClasses;

            private AnonScope (final Name name, final int startNumber) {
                assert name != null;
                this.parentDecl = name;
                this.currentNumber = startNumber;
            }

            public int assignNumber () {
                int ret = this.currentNumber;
                if (this.currentNumber != -1) {
                    this.currentNumber++;
                }
                return ret;
            }

            public int assignLocalNumber (final Name name) {
                if (localClasses == null) {
                    localClasses = new HashMap<Name,Integer> ();
                }
                Integer num = localClasses.get(name);
                if (num == null) {
                    num = 1;
                }
                else {
                    num += 1;
                }
                localClasses.put(name, num);
                return num.intValue();
            }

            @Override
            public String toString () {
                return String.format("%s : %d",this.parentDecl.toString(), this.currentNumber);
            }
        }

        private final Map<Name, AnonScope> anonScopeMap = new HashMap<Name, AnonScope>();
        private final Stack<AnonScope> anonScopes = new Stack<AnonScope> ();

        void newAnonScope(final Name name) {
            newAnonScope(name, 1);
        }

        public void newAnonScope(final Name name, final int startNumber) {
            AnonScope parent = anonScopes.isEmpty() ? null : anonScopes.peek();
            Name fqn = parent != null && parent.parentDecl != names.empty ? parent.parentDecl.append('.', name) : name;
            AnonScope scope = anonScopeMap.get(fqn);
            if (scope == null) {
                scope = new AnonScope(name, startNumber);
                anonScopeMap.put(fqn, scope);
            }
            anonScopes.push(scope);
        }

        @Override
        public void visitClassDef(JCClassDecl tree) {
            if (tree.name == names.empty) {
                ((IndexedClassDecl) tree).index = this.anonScopes.peek().assignNumber();
            }
            newAnonScope(tree.name);
            try {
                super.visitClassDef(tree);
            } finally {
                this.anonScopes.pop();
            }
            if (!this.anonScopes.isEmpty() && this.anonScopes.peek().localClass && tree.name != names.empty) {
                ((IndexedClassDecl) tree).index = this.anonScopes.peek().assignLocalNumber(tree.name);
            }
        }
        @Override
        public void visitBlock(JCBlock tree) {
            final AnonScope as = this.anonScopes.peek();
            boolean old = as.localClass;
            as.localClass = true;
            try {
                super.visitBlock(tree);
            } finally {
                as.localClass = old;
            }
        }
        @Override
        public void visitApply(JCMethodInvocation tree) {
            scan(tree.args);
            scan(tree.meth);
        }
    }

}
