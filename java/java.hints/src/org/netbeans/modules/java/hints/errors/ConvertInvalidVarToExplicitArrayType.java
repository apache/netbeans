/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.util.NbBundle.Messages;
import javax.lang.model.util.Types;

/**
 *
 * @author arusinha
 */
@Messages({
    "DN_ConvertVarToExplicitType=Replace var with explicit type"
})
public class ConvertInvalidVarToExplicitArrayType implements ErrorRule<Void> {

    private static final Set<String> CODES;

    static {
        Set<String> codes = new HashSet<>();
        codes.add("compiler.err.cant.infer.local.var.type"); // NOI18N
        CODES = Collections.unmodifiableSet(codes);
    }

    @Override
    public Set<String> getCodes() {
        return CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {

        if (treePath.getParentPath() == null) {
            return null;
        }

        TypeMirror arrayType = null;
        if (treePath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
            VariableTree oldVariableTree = (VariableTree) treePath.getLeaf();

            ExpressionTree init = oldVariableTree.getInitializer();

            if (init == null || init.getKind() != Tree.Kind.NEW_ARRAY) {
                return null;
            }

            NewArrayTree arrayTree = (NewArrayTree) oldVariableTree.getInitializer();
            List<? extends ExpressionTree> currentValues = arrayTree.getInitializers();

            if (currentValues.isEmpty()) {
                return null;
            }

            TreePath initArrayTreePath = new TreePath(treePath, arrayTree);
            Types types = compilationInfo.getTypes();
            Trees trees = compilationInfo.getTrees();

            for (ExpressionTree tree : currentValues) {

                TypeMirror etType = trees.getTypeMirror(new TreePath(initArrayTreePath, tree));

                //skipped fix for invalid array member, anonymous class and parameterized array member.
                if (etType == null || etType.getKind() == TypeKind.ERROR || (etType.getKind() == TypeKind.DECLARED
                        && !((DeclaredType) etType).getTypeArguments().isEmpty()) || Utilities.isAnonymousType(etType)) {
                    return null;
                }

                if (arrayType == null) {
                    arrayType = etType;
                } else if (!types.isAssignable(etType, arrayType)) {
                    if (types.isAssignable(arrayType, etType)) {
                        arrayType = etType;
                    } else {
                        return null; //the array is not sufficiently homogeneous.
                    }
                }
            }
            return Collections.<Fix>singletonList(new FixImpl(compilationInfo, treePath, arrayType).toEditorFix());
        }
        return Collections.<Fix>emptyList();

    }

    @Override
    public String getId() {
        return ConvertInvalidVarToExplicitArrayType.class.getName();
    }

    @Override
    public String getDisplayName() {
        return Bundle.DN_ConvertVarToExplicitType();
    }

    @Override
    public void cancel() {
    }

    private static final class FixImpl extends JavaFix {

        private TypeMirror arrayTypeMirror;

        public FixImpl(CompilationInfo info, TreePath tp, TypeMirror arrayType) {
            super(info, tp);
            this.arrayTypeMirror = arrayType;
        }

        @Override
        protected String getText() {
            return Bundle.DN_ConvertVarToExplicitType();
        }

        @Override
        protected void performRewrite(TransformationContext tc) throws Exception {
            WorkingCopy wc = tc.getWorkingCopy();
            wc.toPhase(JavaSource.Phase.RESOLVED);
            TreePath statementPath = tc.getPath();
            TreeMaker make = wc.getTreeMaker();
            VariableTree oldVariableTree = null;

            if (statementPath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
                oldVariableTree = (VariableTree) statementPath.getLeaf();

                arrayTypeMirror = Utilities.resolveCapturedType(wc, arrayTypeMirror);

                VariableTree newVariableTree = make.Variable(
                        oldVariableTree.getModifiers(),
                        oldVariableTree.getName(),
                        make.ArrayType(make.Type(arrayTypeMirror)),
                        oldVariableTree.getInitializer()
                );
                tc.getWorkingCopy().rewrite(oldVariableTree, newVariableTree);
            }
        }
    }
}
