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
package org.netbeans.modules.java.hints.suggestions;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Suggests converting a record declaration into a class.
 * <p>
 * The record components become {@code private final} fields, the record's
 * implicit finality is preserved by a {@code final} class modifier, and an
 * explicit canonical constructor is generated when the record does not
 * declare one.
 *
 * @author mbien
 */
public class RecordToClass {

    @Hint(
        displayName = "#DN_convertRecordToClass",
        description = "#DESC_convertRecordToClass",
        category = "class_structure",
        hintKind = Hint.Kind.INSPECTION,
        severity = Severity.HINT,
        minSourceVersion = "14",
        suppressWarnings = "RecordToClass"
    )
    @Messages({
        "DN_convertRecordToClass=Convert Record to Class",
        "DESC_convertRecordToClass=Reports records which can be converted to classes. The record components become private final fields, the canonical constructor is made explicit and the remaining members stay in the body.",
        "MSG_RecordToClass=Record {0} can be converted to a class",
        "FIX_ConvertRecordToClass=Convert record {0} to a class"
    })
    @TriggerTreeKind(Kind.RECORD)
    public static ErrorDescription convertRecordToClass(HintContext context) {
        if (testRecordMayBeClass(context.getInfo().getTrees(), context.getPath())) {
            ClassTree rec = (ClassTree) context.getPath().getLeaf();
            return ErrorDescriptionFactory.forName(context, rec, NbBundle.getMessage(RecordToClass.class, "MSG_RecordToClass", rec.getSimpleName()), //NOI18N
                    new ConvertRecordToClassFixImpl(TreePathHandle.create(context.getPath(), context.getInfo()),
                            NbBundle.getMessage(RecordToClass.class, "FIX_ConvertRecordToClass", rec.getSimpleName())).toEditorFix()); //NOI18N
        }
        return null;
    }

    private static boolean testRecordMayBeClass(Trees trees, TreePath path) {
        // a class may be top level or a member of another class,
        // but not a local or anonymous class
        if (path.getParentPath() == null) {
            return false;
        }
        Tree parent = path.getParentPath().getLeaf();
        if (parent.getKind() != Kind.COMPILATION_UNIT && !TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind())) {
            return false;
        }
        Element element = trees.getElement(path);
        return element != null && element.getKind() == ElementKind.RECORD;
    }

    private static final class ConvertRecordToClassFixImpl extends JavaFix {

        private final String text;

        public ConvertRecordToClassFixImpl(TreePathHandle recHandle, String text) {
            super(recHandle);
            this.text = text;
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        protected void performRewrite(TransformationContext ctx) {
            WorkingCopy wc = ctx.getWorkingCopy();
            GeneratorUtilities gu = GeneratorUtilities.get(wc);
            TreePath path = ctx.getPath();
            ClassTree rec = (ClassTree) path.getLeaf();
            gu.importComments(rec, wc.getCompilationUnit());
            TreeMaker make = wc.getTreeMaker();
            TreeUtilities tu = wc.getTreeUtilities();
            List<VariableTree> components = new ArrayList<>();
            List<Tree> otherMembers = new ArrayList<>();
            for (Tree member : rec.getMembers()) {
                if (tu.isSynthetic(new TreePath(path, member))) {
                    // skip the implicit canonical constructor the compiler
                    // adds to the tree - it has no source text of its own
                    continue;
                }
                if (member.getKind() == Kind.VARIABLE && tu.isRecordComponent((VariableTree) member)) {
                    components.add((VariableTree) member);
                } else {
                    otherMembers.add(member);
                }
            }
            // a record is implicitly final: keep the finality in the class
            Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            for (Modifier m : rec.getModifiers().getFlags()) {
                if (m == Modifier.PUBLIC || m == Modifier.PROTECTED || m == Modifier.PRIVATE) {
                    modifiers.add(m);
                }
            }
            modifiers.add(Modifier.FINAL);
            ModifiersTree mods = make.Modifiers(modifiers, rec.getModifiers().getAnnotations());
            gu.copyComments(rec.getModifiers(), mods, true);
            gu.copyComments(rec.getModifiers(), mods, false);
            boolean hasCanonicalConstructor = false;
            for (Tree member : otherMembers) {
                if (member.getKind() == Kind.METHOD) {
                    MethodTree mth = (MethodTree) member;
                    if (mth.getReturnType() == null && isCanonicalConstructor(mth, components)) {
                        hasCanonicalConstructor = true;
                    }
                }
            }
            List<Tree> members = new ArrayList<>();
            for (VariableTree component : components) {
                VariableTree field = make.Variable(make.Modifiers(EnumSet.of(Modifier.PRIVATE, Modifier.FINAL)),
                        component.getName(),
                        component.getType(),
                        null);
                gu.copyComments(component, field, true);
                gu.copyComments(component, field, false);
                members.add(field);
            }
            if (!hasCanonicalConstructor) {
                members.add(makeCanonicalConstructor(make, rec, components));
            }
            members.addAll(otherMembers);
            ClassTree nue = make.Class(mods, rec.getSimpleName(), rec.getTypeParameters(), null, rec.getImplementsClause(), List.of(), members);
            gu.copyComments(rec, nue, true);
            gu.copyComments(rec, nue, false);
            wc.rewrite(path.getLeaf(), nue);
        }

        // the compiler enforces that the canonical constructor's parameters
        // are named after the record components, in order
        private static boolean isCanonicalConstructor(MethodTree mth, List<VariableTree> components) {
            List<? extends VariableTree> params = mth.getParameters();
            if (params.size() != components.size()) {
                return false;
            }
            for (int i = 0; i < params.size(); i++) {
                if (!params.get(i).getName().contentEquals(components.get(i).getName())) {
                    return false;
                }
            }
            return true;
        }

        private static MethodTree makeCanonicalConstructor(TreeMaker make, ClassTree rec, List<VariableTree> components) {
            // the implicit canonical constructor has the record's access
            Set<Modifier> flags = rec.getModifiers().getFlags().contains(Modifier.PUBLIC) ?
                    EnumSet.of(Modifier.PUBLIC) :
                    EnumSet.noneOf(Modifier.class);
            List<VariableTree> params = new ArrayList<>();
            List<StatementTree> body = new ArrayList<>();
            for (VariableTree component : components) {
                params.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)),
                        component.getName(),
                        component.getType(),
                        null));
                body.add(make.ExpressionStatement(make.Assignment(
                        make.MemberSelect(make.Identifier("this"), component.getName()), //NOI18N
                        make.Identifier(component.getName()))));
            }
            return make.Method(make.Modifiers(flags),
                    "<init>", //NOI18N
                    null,
                    List.of(),
                    params,
                    List.of(),
                    make.Block(body, false),
                    null);
        }
    }
}

