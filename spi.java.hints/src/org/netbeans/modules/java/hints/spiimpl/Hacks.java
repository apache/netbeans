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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java.hints.spiimpl;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;
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

        clazz.append("public class $").append(inc++).append("{");

        clazz.append("private void test() {\n");
        clazz.append("}\n");
        clazz.append("}\n");

        JavacTaskImpl jti = JavaSourceAccessor.getINSTANCE().getJavacTask(info);
        Context context = jti.getContext();

        JavaCompiler jc = JavaCompiler.instance(context);
        Log.instance(context).nerrors = 0;

        JavaFileObject jfo = FileObjects.memoryFileObject("$$", "$", new File("/tmp/t.java").toURI(), System.currentTimeMillis(), clazz.toString());
        boolean oldSkipAPs = jc.skipAnnotationProcessing;

        try {
            jc.skipAnnotationProcessing = true;

            Iterable<? extends CompilationUnitTree> parsed = jti.parse(jfo);
            CompilationUnitTree cut = parsed.iterator().next();

            jti.analyze(jti.enter(parsed));

            return new ScannerImpl().scan(cut, info);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        } finally {
            jc.skipAnnotationProcessing = oldSkipAPs;
        }
    }

    private static final class ScannerImpl extends TreePathScanner<Scope, CompilationInfo> {

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
