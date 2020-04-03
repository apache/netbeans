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

package org.netbeans.modules.java.hints.spiimpl;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.api.java.source.support.ErrorAwareTreePathScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class Hacks {

    //XXX: copied from Utilities, for declarative hints, different import management:
    private static long inc;

    public static Scope constructScope(CompilationInfo info, String... importedClasses) {
        StringBuilder clazz = new StringBuilder();

        clazz.append("package $$;\n");

        for (String i : importedClasses) {
            clazz.append("import ").append(i).append(";\n");
        }

        clazz.append("public class $$scopeclass$").append(inc++).append("{");

        clazz.append("private void test() {\n");
        clazz.append("}\n");
        clazz.append("}\n");

        JavacTaskImpl jti = JavaSourceAccessor.getINSTANCE().getJavacTask(info);
        Context context = jti.getContext();

        JavaCompiler jc = JavaCompiler.instance(context);
        Log.instance(context).nerrors = 0;

        JavaFileObject jfo = FileObjects.memoryFileObject("$$", "$", new File("/tmp/t.java").toURI(), System.currentTimeMillis(), clazz.toString());

        try {
            CompilationUnitTree cut = ParserFactory.instance(context).newParser(jfo.getCharContent(true), true, true, true).parseCompilationUnit();

            jti.analyze(jti.enter(Collections.singletonList(cut)));

            return new ScannerImpl().scan(cut, info);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
        }
    }

    public static void copyLambdaKind(LambdaExpressionTree node, LambdaExpressionTree nue) {
        ((JCLambda) nue).paramKind = ((JCLambda) node).paramKind;
    }

    private static final class ScannerImpl extends ErrorAwareTreePathScanner<Scope, CompilationInfo> {

        @Override
        public Scope visitBlock(BlockTree node, CompilationInfo p) {
            return p.getTrees().getScope(getCurrentPath());
        }

        @Override
        public Scope visitMethod(MethodTree node, CompilationInfo p) {
            if (node.getReturnType() == null) {
                return null;
            }
            return super.visitMethod(node, p);
        }

        @Override
        public Scope reduce(Scope r1, Scope r2) {
            return r1 != null ? r1 : r2;
        }

    }


    public static Tree createRenameTree(@NonNull Tree originalTree, @NonNull String newName) {
        return new RenameTree(originalTree, newName);
    }

    public static final class RenameTree extends JCErroneous {

        public final Tree originalTree;
        public final String newName;

        public RenameTree(@NonNull Tree originalTree, @NonNull String newName) {
            super(com.sun.tools.javac.util.List.<JCTree>nil());
            this.originalTree = originalTree;
            this.newName = newName;
        }

    }

    public static @CheckForNull TypeMirror parseFQNType(@NonNull CompilationInfo info, @NonNull String spec) {
        if (spec.length() == 0) {
            return null;
        }
        
        TypeElement jlObject = info.getElements().getTypeElement("java.lang.Object");
        
        //XXX:
        TypeElement scope;

        if (info.getTopLevelElements().isEmpty()) {
            scope = jlObject;
        } else {
            scope = info.getTopLevelElements().iterator().next();
        }
        //XXX end
        
        return info.getTreeUtilities().parseType(spec, /*XXX: jlObject*/scope);
    }

    public static VariableElement attributeThis(CompilationInfo info, TreePath tp) {
        //XXX:
        while (tp != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                Element currentElement = info.getTrees().getElement(tp);

                if (currentElement == null || !(currentElement instanceof ClassSymbol)) return null;

                Enter enter = Enter.instance(JavaSourceAccessor.getINSTANCE().getJavacTask(info).getContext());
                Env<AttrContext> env = enter.getEnv((ClassSymbol) currentElement);

                if (env == null) return null;

                for (Element el : env.info.getLocalElements()) {
                    if (el.getSimpleName().contentEquals("this")) {
                        return (VariableElement) el;
                    }
                }

                return null;
            }

            tp = tp.getParentPath();
        }

        return null;
    }
    
    public static interface InspectAndTransformOpener {
        public void openIAT(HintMetadata hm);
    }
}
