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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.sun.source.tree.VariableTree;
import java.util.HashMap;
import java.util.Map;
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

/**
 *
 * @author arusinha
 */
@Messages({
    "DN_ConvertVarToExplicitType=Convert Var to Explicit Type"
})
public class ConvertInvalidVarToExplicitArrayType implements ErrorRule<Void> {

    private static final Set<String> CODES;
    private static final Map<TypeKind, Integer> NUMERIC_PRIMITIVE_TYPE_PRIORITY_MAP;

    static {
        Set<String> codes = new HashSet<>();
        codes.add("compiler.err.cant.infer.local.var.type"); // NOI18N
        CODES = Collections.unmodifiableSet(codes);

        Map<TypeKind, Integer> map = new HashMap<>();
        map.put(TypeKind.BYTE, 1);
        map.put(TypeKind.SHORT, 2);
        map.put(TypeKind.INT, 3);
        map.put(TypeKind.LONG, 4);
        map.put(TypeKind.FLOAT, 5);
        map.put(TypeKind.DOUBLE, 6);
        NUMERIC_PRIMITIVE_TYPE_PRIORITY_MAP = Collections.unmodifiableMap(map);
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

        TypeMirror arrayTypeMirror = null;
        if (treePath.getLeaf().getKind() == Tree.Kind.VARIABLE) {
            VariableTree oldVariableTree = (VariableTree) treePath.getLeaf();
            NewArrayTree arrayTree = (NewArrayTree) oldVariableTree.getInitializer();
            List<? extends ExpressionTree> currentValues = arrayTree.getInitializers();

            TypeMirror arrayElementTypeMirror = null;
            Integer maxTypePriority = -1;

            boolean isHomoGeneousArray = true;
            for (ExpressionTree tree : currentValues) {

                Scope s = compilationInfo.getTrees().getScope(treePath);

                arrayElementTypeMirror = compilationInfo.getTreeUtilities().attributeTree(tree, s);

                if (tree instanceof NewClassTree) {
                    NewClassTree nct = (NewClassTree) tree;
                    if (nct.getIdentifier().getKind() == Tree.Kind.PARAMETERIZED_TYPE) {

                        return null;
                    }
                }

                if (arrayTypeMirror == null) {
                    arrayTypeMirror = arrayElementTypeMirror;
                }

                isHomoGeneousArray = compilationInfo.getTypes().isSameType(arrayElementTypeMirror, arrayTypeMirror);
                if (isHomoGeneousArray) {
                    continue;
                }

                // Hint will be enabled only for primitive Numeric array initializers or for homogeneous array members.
                if (!isHomoGeneousArray && !isPrimitiveNumeric(arrayElementTypeMirror.getKind())) {
                    return null;
                }

                if (!isHomoGeneousArray) {
                    Integer arrayElementPriority = NUMERIC_PRIMITIVE_TYPE_PRIORITY_MAP.get(arrayElementTypeMirror.getKind());
                    if (maxTypePriority == -1) {
                        maxTypePriority = NUMERIC_PRIMITIVE_TYPE_PRIORITY_MAP.get(arrayTypeMirror.getKind());
                    }

                    if (maxTypePriority < arrayElementPriority) {
                        arrayTypeMirror = arrayElementTypeMirror;
                        maxTypePriority = arrayElementPriority;
                    }
                }
            }
        }
        return Collections.<Fix>singletonList(new FixImpl(compilationInfo, treePath, arrayTypeMirror).toEditorFix());

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

    private boolean isPrimitiveNumeric(TypeKind type) {
        Set<TypeKind> primitiveNumbericTypes = NUMERIC_PRIMITIVE_TYPE_PRIORITY_MAP.keySet();

        return primitiveNumbericTypes.contains(type);

    }
}
