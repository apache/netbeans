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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.overridden.ComputeOverriding;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.Hint.Options;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.support.FixFactory;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class ClassStructure {

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.finalClass", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.finalClass", category = "class_structure", enabled = false, suppressWarnings = "FinalClass") //NOI18N
    @TriggerTreeKind({Tree.Kind.ANNOTATION_TYPE, Tree.Kind.CLASS, Tree.Kind.ENUM, Tree.Kind.INTERFACE})
    public static ErrorDescription finalClass(HintContext context) {
        final ClassTree cls = (ClassTree) context.getPath().getLeaf();
        if (cls.getModifiers().getFlags().contains(Modifier.FINAL)) {
            return ErrorDescriptionFactory.forName(context, cls, NbBundle.getMessage(ClassStructure.class, "MSG_FinalClass", cls.getSimpleName()), //NOI18N
                    FixFactory.removeModifiersFix(context.getInfo(), TreePath.getPath(context.getPath(), cls.getModifiers()), EnumSet.of(Modifier.FINAL), NbBundle.getMessage(ClassStructure.class, "FIX_RemoveFinalFromClass", cls.getSimpleName()))); //NOI18N
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.finalMethod", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.finalMethod", category = "class_structure", enabled = false, suppressWarnings = {"FinalMethod"}) //NOI18N
    @TriggerTreeKind(Kind.METHOD)
    public static ErrorDescription finalMethod(HintContext context) {
        final MethodTree mth = (MethodTree) context.getPath().getLeaf();
        if (mth.getModifiers().getFlags().contains(Modifier.FINAL)) {
            return ErrorDescriptionFactory.forName(context, mth, NbBundle.getMessage(ClassStructure.class, "MSG_FinalMethod", mth.getName()), //NOI18N
                    FixFactory.removeModifiersFix(context.getInfo(), TreePath.getPath(context.getPath(), mth.getModifiers()), EnumSet.of(Modifier.FINAL), NbBundle.getMessage(ClassStructure.class, "FIX_RemoveFinalFromMethod", mth.getName()))); //NOI18N
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.finalPrivateMethod", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.finalPrivateMethod", category = "class_structure", suppressWarnings = {"FinalPrivateMethod"}) //NOI18N
    @TriggerTreeKind(Kind.METHOD)
    public static ErrorDescription finalPrivateMethod(HintContext context) {
        final MethodTree mth = (MethodTree) context.getPath().getLeaf();
        if (mth.getModifiers().getFlags().containsAll(EnumSet.of(Modifier.FINAL, Modifier.PRIVATE))) {
            return ErrorDescriptionFactory.forName(context, mth, NbBundle.getMessage(ClassStructure.class, "MSG_FinalPrivateMethod", mth.getName()), //NOI18N
                    FixFactory.removeModifiersFix(context.getInfo(), TreePath.getPath(context.getPath(), mth.getModifiers()), EnumSet.of(Modifier.FINAL), NbBundle.getMessage(ClassStructure.class, "FIX_RemoveFinalFromMethod", mth.getName()))); //NOI18N
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.finalMethodInFinalClass", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.finalMethodInFinalClass", category = "class_structure", enabled = false, suppressWarnings = {"FinalMethodInFinalClass"}) //NOI18N
    @TriggerTreeKind(Kind.METHOD)
    public static ErrorDescription finalMethodInFinalClass(HintContext context) {
        final MethodTree mth = (MethodTree) context.getPath().getLeaf();
        final Tree parent = context.getPath().getParentPath().getLeaf();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind()) && mth.getModifiers().getFlags().contains(Modifier.FINAL) && ((ClassTree) parent).getModifiers().getFlags().contains(Modifier.FINAL)) {
            // see defect #236974; although redundant, if the method is annotated with @SafeVarargs, we must permit final modifier.
            ExecutableElement ee = (ExecutableElement)context.getInfo().getTrees().getElement(context.getPath());
            if (ee == null || ee.getAnnotation(SafeVarargs.class) == null) {
                return ErrorDescriptionFactory.forName(context, mth, NbBundle.getMessage(ClassStructure.class, "MSG_FinalMethodInFinalClass", mth.getName()), //NOI18N
                        FixFactory.removeModifiersFix(context.getInfo(), TreePath.getPath(context.getPath(), mth.getModifiers()), EnumSet.of(Modifier.FINAL), NbBundle.getMessage(ClassStructure.class, "FIX_RemoveFinalFromMethod", mth.getName()))); //NOI18N
            }
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.noopMethodInAbstractClass", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.noopMethodInAbstractClass", category = "class_structure", enabled = false, suppressWarnings = {"NoopMethodInAbstractClass"}, options=Options.QUERY) //NOI18N
    @TriggerTreeKind(Kind.METHOD)
    public static ErrorDescription noopMethodInAbstractClass(HintContext context) {
        final MethodTree mth = (MethodTree) context.getPath().getLeaf();
        final Tree parent = context.getPath().getParentPath().getLeaf();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind()) && ((ClassTree) parent).getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
            final BlockTree body = mth.getBody();
            if (body != null && body.getStatements().isEmpty()) {
                return ErrorDescriptionFactory.forName(context, mth, NbBundle.getMessage(ClassStructure.class, "MSG_NoopMethodInAbstractClass", mth.getName())); //NOI18N
            }
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.publicConstructorInNonPublicClass", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.publicConstructorInNonPublicClass", category = "class_structure", enabled = false, suppressWarnings = {"PublicConstructorInNonPublicClass"}) //NOI18N
    @TriggerTreeKind(Kind.METHOD)
    public static ErrorDescription publicConstructorInNonPublicClass(HintContext context) {
        final MethodTree mth = (MethodTree) context.getPath().getLeaf();
        final Tree parent = context.getPath().getParentPath().getLeaf();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind()) && mth.getReturnType() == null && "<init>".contentEquals(mth.getName()) && //NOI18N
                mth.getModifiers().getFlags().contains(Modifier.PUBLIC) && !((ClassTree) parent).getModifiers().getFlags().contains(Modifier.PUBLIC)) {
            return ErrorDescriptionFactory.forName(context, mth, NbBundle.getMessage(ClassStructure.class, "MSG_PublicConstructorInNonPublicClass", mth.getName()), //NOI18N
                    FixFactory.removeModifiersFix(context.getInfo(), TreePath.getPath(context.getPath(), mth.getModifiers()), EnumSet.of(Modifier.PUBLIC), NbBundle.getMessage(ClassStructure.class, "FIX_RemovePublicFromConstructor"))); //NOI18N
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.protectedMemberInFinalClass", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.protectedMemberInFinalClass", category = "class_structure", enabled = false, suppressWarnings = {"ProtectedMemberInFinalClass"}) //NOI18N
    @TriggerTreeKind({Kind.METHOD, Kind.VARIABLE})
    public static ErrorDescription protectedMemberInFinalClass(HintContext context) {
        final Tree tree = context.getPath().getLeaf();
        final Tree parent = context.getPath().getParentPath().getLeaf();
        if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind())) {
            if (tree.getKind() == Kind.METHOD) {
                final MethodTree mth = (MethodTree) tree;
                if (mth.getModifiers().getFlags().contains(Modifier.PROTECTED) && ((ClassTree) parent).getModifiers().getFlags().contains(Modifier.FINAL)) {
                    Element el = context.getInfo().getTrees().getElement(context.getPath());
                    if (el == null || el.getKind() != ElementKind.METHOD) {
                        return null;
                    }
                    List<ElementDescription> overrides = new LinkedList<ElementDescription>();
                    ComputeOverriding.detectOverrides(context.getInfo(), (TypeElement) el.getEnclosingElement(), (ExecutableElement) el, overrides);
                    for (ElementDescription ed : overrides) {
                        Element res = ed.getHandle().resolve(context.getInfo());
                        if (res == null) {
                            continue; //XXX: log
                        }
                        if (   res.getModifiers().contains(Modifier.PROTECTED)
                            || /*to prevent reports for broken sources:*/ res.getModifiers().contains(Modifier.PUBLIC)) {
                            return null;
                        }
                    }
                    return ErrorDescriptionFactory.forName(context, mth, NbBundle.getMessage(ClassStructure.class, "MSG_ProtectedMethodInFinalClass", mth.getName()), //NOI18N
                            FixFactory.removeModifiersFix(context.getInfo(), TreePath.getPath(context.getPath(), mth.getModifiers()), EnumSet.of(Modifier.PROTECTED), NbBundle.getMessage(ClassStructure.class, "FIX_RemoveProtectedFromMethod", mth.getName()))); //NOI18N
                }
            } else {
                final VariableTree var = (VariableTree) tree;
                if (var.getModifiers().getFlags().contains(Modifier.PROTECTED) && ((ClassTree) parent).getModifiers().getFlags().contains(Modifier.FINAL)) {
                    return ErrorDescriptionFactory.forName(context, var, NbBundle.getMessage(ClassStructure.class, "MSG_ProtectedFieldInFinalClass", var.getName()), //NOI18N
                            FixFactory.removeModifiersFix(context.getInfo(), TreePath.getPath(context.getPath(), var.getModifiers()), EnumSet.of(Modifier.PROTECTED), NbBundle.getMessage(ClassStructure.class, "FIX_RemoveProtectedFromField", var.getName()))); //NOI18N
                }
            }
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.markerInterface", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.markerInterface", category = "class_structure", enabled = false, suppressWarnings = {"MarkerInterface"}, options=Options.QUERY) //NOI18N
    @TriggerTreeKind({Tree.Kind.ANNOTATION_TYPE, Tree.Kind.CLASS, Tree.Kind.ENUM, Tree.Kind.INTERFACE})
    public static ErrorDescription markerInterface(HintContext context) {
        final ClassTree cls = (ClassTree) context.getPath().getLeaf();
        if (context.getInfo().getTreeUtilities().isInterface(cls) && cls.getMembers().isEmpty() && cls.getImplementsClause().size() < 2) {
            return ErrorDescriptionFactory.forName(context, cls, NbBundle.getMessage(ClassStructure.class, "MSG_MarkerInterface", cls.getSimpleName())); //NOI18N
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.classMayBeInterface", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.classMayBeInterface", category = "class_structure", enabled = false, suppressWarnings = {"ClassMayBeInterface"}, options=Options.NO_BATCH) //NOI18N
    @TriggerTreeKind({Tree.Kind.ANNOTATION_TYPE, Tree.Kind.CLASS, Tree.Kind.ENUM, Tree.Kind.INTERFACE})
    public static ErrorDescription classMayBeInterface(HintContext context) {
        final ClassTree cls = (ClassTree) context.getPath().getLeaf();
        final TreeUtilities treeUtilities = context.getInfo().getTreeUtilities();
        if (treeUtilities.isClass(cls) && testClassMayBeInterface(context.getInfo().getTrees(), treeUtilities, context.getPath())) {
            return ErrorDescriptionFactory.forName(context, cls, NbBundle.getMessage(ClassStructure.class, "MSG_ClassMayBeInterface", cls.getSimpleName()), //NOI18N
                    new ConvertClassToInterfaceFixImpl(TreePathHandle.create(context.getPath(), context.getInfo()), NbBundle.getMessage(ClassStructure.class, "FIX_ConvertClassToInterface", cls.getSimpleName())).toEditorFix()); //NOI18N
        }
        return null;
    }

    @Hint(displayName = "#DN_org.netbeans.modules.java.hints.ClassStructure.multipleTopLevelClassesInFile", description = "#DESC_org.netbeans.modules.java.hints.ClassStructure.multipleTopLevelClassesInFile", category = "class_structure", enabled = false, suppressWarnings = {"MultipleTopLevelClassesInFile"}, options=Options.QUERY) //NOI18N
    @TriggerTreeKind({Tree.Kind.ANNOTATION_TYPE, Tree.Kind.CLASS, Tree.Kind.ENUM, Tree.Kind.INTERFACE})
    public static ErrorDescription multipleTopLevelClassesInFile(HintContext context) {
        final ClassTree cls = (ClassTree) context.getPath().getLeaf();
        final Tree parent = context.getPath().getParentPath().getLeaf();
        if (parent.getKind() == Kind.COMPILATION_UNIT) {
            final List<? extends Tree> typeDecls = new ArrayList<Tree>(((CompilationUnitTree) parent).getTypeDecls());
            for (Iterator<? extends Tree> it = typeDecls.iterator(); it.hasNext();) {
                if (it.next().getKind() == Kind.EMPTY_STATEMENT) it.remove();
            }
            if (typeDecls.size() > 1 && typeDecls.get(0) != cls) {
                return ErrorDescriptionFactory.forName(context, cls, NbBundle.getMessage(ClassStructure.class, "MSG_MultipleTopLevelClassesInFile")); //NOI18N
            }
        }
        return null;
    }

    private static boolean testClassMayBeInterface(Trees trees, TreeUtilities treeUtilities, TreePath path) {
        final ClassTree cls = (ClassTree) path.getLeaf();
        if (!treeUtilities.isClass(cls)) {
            return true;
        }
        final Element element = trees.getElement(path);
        if (element == null) return false; //see bug #221820
        final TypeMirror superclass = element.getKind().isClass() ? ((TypeElement) element).getSuperclass() : null;
        if (superclass == null || superclass.getKind() != TypeKind.DECLARED
                || !"java.lang.Object".contentEquals(((TypeElement) ((DeclaredType) superclass).asElement()).getQualifiedName())) { //NOI18N
            return false;
        }
        for (Tree member : cls.getMembers()) {
            TreePath memberPath = TreePath.getPath(path, member);
            if (!treeUtilities.isSynthetic(memberPath)) {
                switch (member.getKind()) {
                    case VARIABLE:
                        if (!((VariableTree) member).getModifiers().getFlags().containsAll(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL))) {
                            return false;
                        }
                        break;
                    case METHOD:
                        if (!((MethodTree) member).getModifiers().getFlags().containsAll(EnumSet.of(Modifier.PUBLIC, Modifier.ABSTRACT))) {
                            return false;
                        }
                        break;
                    case ANNOTATION_TYPE:
                    case CLASS:
                    case ENUM:
                    case INTERFACE:
                        if (!testClassMayBeInterface(trees, treeUtilities, memberPath)) {
                            return false;
                        }
                        break;
                    default:
                        return false;
                }
            }
        }
        return true;
    }

    private static final class ConvertClassToInterfaceFixImpl extends JavaFix {

        private final String text;

        public ConvertClassToInterfaceFixImpl(TreePathHandle clsHandle, String text) {
            super(clsHandle);
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
            final ClassTree cls = (ClassTree) path.getLeaf();
            gu.importComments(cls, wc.getCompilationUnit());
            final TreeMaker treeMaker = wc.getTreeMaker();
            ModifiersTree mods = cls.getModifiers();
            if (mods.getFlags().contains(Modifier.ABSTRACT)) {
                Set<Modifier> modifiers = EnumSet.copyOf(mods.getFlags());
                modifiers.remove(Modifier.ABSTRACT);
                ModifiersTree nmods = treeMaker.Modifiers(modifiers, mods.getAnnotations());
                gu.copyComments(mods, nmods, true);
                gu.copyComments(mods, nmods, false);
                mods = nmods;
            }
            Tree nue = treeMaker.Interface(mods, cls.getSimpleName(), cls.getTypeParameters(), cls.getImplementsClause(), cls.getPermitsClause(), cls.getMembers());
            gu.copyComments(cls, nue, true);
            gu.copyComments(cls, nue, false);
            wc.rewrite(path.getLeaf(), nue);
        }
    }
}
