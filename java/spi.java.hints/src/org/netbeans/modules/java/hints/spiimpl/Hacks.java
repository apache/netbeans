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
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import java.util.Collections;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.source.JavaSourceAccessor;

/**
 *
 * @author lahvac
 */
public class Hacks {

    public static void copyLambdaKind(LambdaExpressionTree node, LambdaExpressionTree nue) {
        ((JCLambda) nue).paramKind = ((JCLambda) node).paramKind;
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
        
        if (info.getTopLevelElements().isEmpty()) {
            //TreeUtilities.parseType requires a type that originates in source, but we have none.
            //so, use a synthetic scope (keeping the case where we have a type that originates in source,
            //to keep compatibility:
            StatementTree block = info.getTreeUtilities().parseStatement("{" + spec + " $;}", new SourcePositions[1]);

            if (block.getKind() != Kind.BLOCK) {
                return null;
            }

            List<? extends StatementTree> statements = ((BlockTree) block).getStatements();

            if (statements.size() != 1 || statements.get(0).getKind() != Kind.VARIABLE) {
                return null;
            }

            VariableTree var = (VariableTree) statements.get(0);
            Scope scope = Utilities.constructScope(info, Collections.emptyMap());

            info.getTreeUtilities().attributeTree(var, scope);

            return info.getTrees().getTypeMirror(new TreePath(new TreePath(info.getCompilationUnit()), var.getType()));
        } else {
            return info.getTreeUtilities().parseType(spec, info.getTopLevelElements().iterator().next());
        }
    }

    public static VariableElement attributeThis(CompilationInfo info, TreePath tp) {
        //XXX:
        while (tp != null) {
            if (TreeUtilities.CLASS_TREE_KINDS.contains(tp.getLeaf().getKind())) {
                Element currentElement = info.getTrees().getElement(tp);

                if (!(currentElement instanceof ClassSymbol)) return null;

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
