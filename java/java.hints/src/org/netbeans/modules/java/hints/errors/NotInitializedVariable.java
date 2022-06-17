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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.ui.ElementHeaders;
import org.netbeans.modules.java.hints.introduce.Flow;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.refactoring.java.api.ChangeParametersRefactoring;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.JavaFix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;


/**
 *
 * @author Tomas Zezula
 */
public class NotInitializedVariable implements ErrorRule<Void> {
    
    private static final Set<String> ERROR_CODES = new HashSet<>(Arrays.asList(
            "compiler.err.var.might.not.have.been.initialized",
            "compiler.err.var.not.initialized.in.default.constructor"));
    private volatile boolean canceled;
    
    public NotInitializedVariable () {        
    }

    @Override
    public Set<String> getCodes() {
        return ERROR_CODES;
    }

    @Override
    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey, int offset, TreePath treePath, Data<Void> data) {
        assert ERROR_CODES.contains(diagnosticKey);
        final List<Fix> result = new ArrayList<>();
        if (!canceled) {
            final Trees t = compilationInfo.getTrees();
            final Element e = t.getElement(treePath);            
            if (!canceled && e != null) {
                switch (e.getKind()) {
                    case FIELD: {
                        TreePath declaration = t.getPath(e);
                        if (!canceled && declaration != null) {
                            result.add(new NIVFix(e.getSimpleName().toString(), TreePathHandle.create(declaration, compilationInfo)).toEditorFix());
                            TreePath clsDeclaration = declaration.getParentPath();
                            if (clsDeclaration != null && TreeUtilities.CLASS_TREE_KINDS.contains(clsDeclaration.getLeaf().getKind())) {
                                result.add(new NIVCtorFix(e.getSimpleName().toString(), ElementHandle.create((VariableElement) e),
                                        TreePathHandle.create(clsDeclaration, compilationInfo)).toEditorFix());
                                TypeMirror type = e.asType();
                                TypeKind kind = type.getKind();
                                String value;
                                if (kind.isPrimitive()) {
                                    if (kind == TypeKind.BOOLEAN) {
                                        value = "false";
                                    }
                                    else {
                                        value = "0";
                                    }
                                }
                                else {
                                    value = "null";
                                }

                                ClassTree ct = (ClassTree) clsDeclaration.getLeaf();
                                for (Tree tree : ct.getMembers()) {
                                    if (tree.getKind() == Tree.Kind.METHOD && "<init>".contentEquals(((MethodTree)tree).getName())) {
                                        TreePath methodPath = new TreePath(clsDeclaration, tree);
                                        boolean assigned = Flow.definitellyAssigned(compilationInfo, (VariableElement) e, Collections.singletonList(new TreePath(methodPath, ((MethodTree) tree).getBody())), () -> canceled);
                                        if (!assigned) {
                                            result.add(new NIVAddCtorParamFix(compilationInfo, methodPath, (VariableElement) e, value));
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case LOCAL_VARIABLE: {
                        TreePath declaration = t.getPath(e);
                        if (!canceled && declaration != null) {
                            result.add(new NIVFix(e.getSimpleName().toString(), TreePathHandle.create(declaration, compilationInfo)).toEditorFix());
                        }
                        break;
                    }
                }
            }            
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public String getId() {
        return "NotInitializedVariable";    //NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(NotInitializedVariable.class, "LBL_NotInitializedVariable");
    }

    @Override
    public void cancel() {
        this.canceled = true;
    }
    
    
    private static class NIVFix extends JavaFix {
        
        private final String variableName;
        
        public NIVFix(final String variableName, final TreePathHandle variable) {
            super(variable);
            assert variableName != null;
            assert variable != null;
            this.variableName = variableName;
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(NotInitializedVariable.class, "LBL_NotInitializedVariable_fix",variableName); //NOI18N
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            VariableTree vt = (VariableTree) tp.getLeaf();
            ExpressionTree init = vt.getInitializer();
            if (init != null) {
                return;
            }
            Element decl = wc.getTrees().getElement(tp);
            if (decl == null) {
                return;
            }
            TypeMirror type = decl.asType();
            TypeKind kind = type.getKind();
            Object value;
            if (kind.isPrimitive()) {
                if (kind == TypeKind.BOOLEAN) {
                    value = false;
                }
                else {
                    value = 0;
                }
            }
            else {
                value = null;
            }
            ExpressionTree newInit = wc.getTreeMaker().Literal(value);
            VariableTree newVt = wc.getTreeMaker().Variable(
                    vt.getModifiers(),
                    vt.getName(),
                    vt.getType(),
                    newInit);
            wc.rewrite(vt, newVt);
        }
    }

    private static class NIVCtorFix extends JavaFix {

        private final String variableName;
        private final ElementHandle<VariableElement> field;

        public NIVCtorFix(final String variableName, final ElementHandle<VariableElement> field, final TreePathHandle constructor) {
            super(constructor);
            this.variableName = variableName;
            this.field = field;
        }

        @NbBundle.Messages({
            "LBL_NotInitializedVariableCtor_fix=Initialize variable {0} in constructor(s)"
        })
        @Override
        protected String getText() {
            return Bundle.LBL_NotInitializedVariableCtor_fix(variableName);
        }

        @Override
        protected void performRewrite(TransformationContext ctx) throws Exception {
            WorkingCopy wc = ctx.getWorkingCopy();
            TreePath tp = ctx.getPath();
            ClassTree ct = (ClassTree) tp.getLeaf();
            VariableElement ve = field.resolve(wc);
            if (ve != null) {
                TreeMaker make = wc.getTreeMaker();
                Trees trees = wc.getTrees();
                TreeUtilities tu = wc.getTreeUtilities();
                boolean isStatic = ve.getModifiers().contains(Modifier.STATIC);
                TypeMirror type = ve.asType();
                TypeKind kind = type.getKind();
                Object value;
                if (kind.isPrimitive()) {
                    if (kind == TypeKind.BOOLEAN) {
                        value = false;
                    }
                    else {
                        value = 0;
                    }
                }
                else {
                    value = null;
                }
                ExpressionTree init = make.Literal(value);
                ExpressionTree var = make.QualIdent((isStatic? ct.getSimpleName().toString() : "this") + '.' + variableName);
                ExpressionStatementTree stat = make.ExpressionStatement(make.Assignment(var, init));
                for (Tree tree : ct.getMembers()) {
                    if (tree.getKind() == Tree.Kind.METHOD && "<init>".contentEquals(((MethodTree)tree).getName())) {
                        MethodTree method = (MethodTree)tree;
                        TreePath methodPath = new TreePath(tp, method);
                        boolean synthetic = tu.isSynthetic(methodPath);
                        BlockTree body = method.getBody();
                        if (body != null) {
                            boolean assigned = Flow.definitellyAssigned(wc, ve, Collections.singletonList(new TreePath(methodPath, body)), () -> false);
                            if (!assigned) {
                                if (synthetic) {
                                    Tree constructor;
                                    if (tp.getParentPath().getLeaf().getKind() == Tree.Kind.NEW_CLASS) {
                                        constructor = make.Block(Collections.singletonList(stat), false);
                                    } else {
                                        constructor = make.Constructor(make.Modifiers(method.getModifiers().getFlags(), method.getModifiers().getAnnotations()),
                                                method.getTypeParameters(), method.getParameters(), method.getThrows(), make.addBlockStatement(body, stat));
                                    }
                                    ClassTree newClass = GeneratorUtilities.get(wc).insertClassMember(ct, constructor);
                                    wc.rewrite(ct, newClass);
                                } else {
                                    BlockTree newBody = wc.getTreeMaker().addBlockStatement(body, stat);
                                    wc.rewrite(body, newBody);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static class NIVAddCtorParamFix extends ModificationResultBasedFix implements Fix {

        private final ChangeParametersFix fix;
        private final String ctorHeader;
        private final TreePathHandle tph;
        private final String fieldName;
        private final String name;

        public NIVAddCtorParamFix(CompilationInfo compilationInfo, TreePath tp, VariableElement ve, String value) {
            assert tp != null && tp.getLeaf().getKind() == Tree.Kind.METHOD;
            this.ctorHeader = ElementHeaders.getHeader(tp, compilationInfo, ElementHeaders.NAME + ElementHeaders.PARAMETERS);
            this.tph = TreePathHandle.create(tp, compilationInfo);
            MethodTree method = (MethodTree) tp.getLeaf();
            Scope scope = compilationInfo.getTreeUtilities().scopeFor((int) compilationInfo.getTrees().getSourcePositions().getEndPosition(tp.getCompilationUnit(), method.getBody()) - 1);
            List<? extends VariableTree> parameters = method.getParameters();
            ChangeParametersRefactoring.ParameterInfo[] parameterInfo = new ChangeParametersRefactoring.ParameterInfo[parameters.size() + 1];
            for (int i = 0; i < parameters.size(); i++) {
                VariableTree param = parameters.get(i);
                parameterInfo[i] = new ChangeParametersRefactoring.ParameterInfo(i, param.getName().toString(), param.getType().toString(), null);
            }
            this.fieldName = ve.getSimpleName().toString();
            this.name = uniqueName(compilationInfo, scope, fieldName);
            parameterInfo[parameterInfo.length - 1] = new ChangeParametersRefactoring.ParameterInfo(-1, this.name, ve.asType().toString(), value);
            this.fix = new ChangeParametersFix(false, this.tph, method.getModifiers().getFlags(), "", "", parameterInfo, true);
        }

        @NbBundle.Messages({
            "LBL_AddCtorParameter_fix=Add parameter to constructor {0}"
        })
        public String getText() {
            return Bundle.LBL_AddCtorParameter_fix(ctorHeader);
        }

        public ChangeInfo implement() throws Exception {
            ModificationResult result = getModificationResult();
            if (result != null) {
                result.commit();
            }
            fix.implement();
            return null;
        }

        @Override
        public List<ModificationResult> getModificationResults() throws IOException {
            List<ModificationResult> results = fix.getModificationResults();
            if (!results.isEmpty()) {
                ModificationResult result = getModificationResult();
                if (result != null) {
                    mergeResults(results, result);
                }
            }
            return results;
        }

        @Override
        public ModificationResult getModificationResult() throws IOException {
            FileObject fo = tph.getFileObject();
            JavaSource js = fo != null ? JavaSource.forFileObject(fo) : null;
            if (js != null) {
                return js.runModificationTask(wc -> {
                    wc.toPhase(JavaSource.Phase.RESOLVED);
                    TreePath tp = tph.resolve(wc);
                    if (tp != null && tp.getLeaf().getKind() == Tree.Kind.METHOD && "<init>".contentEquals(((MethodTree) tp.getLeaf()).getName())) {
                        BlockTree body = ((MethodTree) tp.getLeaf()).getBody();
                        if (body != null) {
                            TreeMaker make = wc.getTreeMaker();
                            ExpressionStatementTree stat = make.ExpressionStatement(make.Assignment(make.QualIdent("this." + fieldName), make.Identifier(name)));
                            BlockTree newBody = wc.getTreeMaker().addBlockStatement(body, stat);
                            wc.rewrite(body, newBody);
                        }
                    }
                });
            }
            return null;
        }

        private static String uniqueName(CompilationInfo info, Scope s, String name) {
            int counter = 0;
            boolean cont = true;
            String proposedName = name;
            while (cont) {
                cont = false;
                proposedName = name + (counter > 0 ? String.valueOf(counter) : "");
                if (s != null) {
                    for (Element e : info.getElementUtilities().getLocalMembersAndVars(s, new Utilities.VariablesFilter())) {
                        if (!e.getKind().isField() && proposedName.equals(e.getSimpleName().toString())) {
                            counter++;
                            cont = true;
                            break;
                        }
                    }
                }
            }
            return proposedName;
        }

        private static void mergeResults(List<ModificationResult> results, ModificationResult result) {
            for (FileObject fo : result.getModifiedFileObjects()) {
                for (ModificationResult r : results) {
                    if (r.getModifiedFileObjects().contains(fo)) {
                        List<ModificationResult.Difference> differences = (List<ModificationResult.Difference>) r.getDifferences(fo);
                        List<ModificationResult.Difference> unhandled = new ArrayList<>();
                        outer: for (ModificationResult.Difference diff : result.getDifferences(fo)) {
                            for (int i = 0; i < differences.size(); i++) {
                                ModificationResult.Difference d = differences.get(i);
                                if (diff.getKind() == d.getKind() && diff.getStartPosition().getOffset() == d.getStartPosition().getOffset() && diff.getEndPosition().getOffset() == d.getEndPosition().getOffset()) {
                                    String t1 = diff.getNewText();
                                    int idx1 = t1.indexOf('(');
                                    String t2 = d.getNewText();
                                    int idx2 = t2.indexOf('(');
                                    if (idx1 > 0 && idx1 == idx2 && t1.substring(0, idx1).equals(t2.substring(0, idx2))) {
                                        idx1 = t1.indexOf('{', idx1);
                                        idx2 = t2.indexOf('{', idx2);
                                        if (idx1 > 0 && idx2 > 0) {
                                            String newText = t2.substring(0, idx2) + t1.substring(idx1);
                                            ModificationResult.Difference cd = JavaSourceAccessor.getINSTANCE().createDifference(d.getKind(), d.getStartPosition(), d.getEndPosition(), d.getOldText(), newText, d.getDescription(), null);
                                            differences.set(i, cd);
                                        }
                                        continue outer;
                                    }
                                }
                            }
                            unhandled.add(diff);
                        }
                        for (ModificationResult.Difference diff : unhandled) {
                            differences.add(diff);
                        }
                    }
                }
            }
        }
    }
}
