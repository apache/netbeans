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
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
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
import org.netbeans.modules.java.hints.Feature;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Suggests converting a class declaration into a record.
 * <p>
 * The conversion is only offered when it is safe to do so: the class is not
 * abstract, sealed or {@code strictfp}, it declares no explicit constructor,
 * it does not extend another class, and its instance fields are plain (no
 * initializer or annotations).
 *
 * @author mbien
 */
public class ClassToRecord {

    @Hint(
        displayName = "#DN_convertClassToRecord",
        description = "#DESC_convertClassToRecord",
        category = "class_structure",
        hintKind = Hint.Kind.INSPECTION,
        minSourceVersion = "14",
        suppressWarnings = "ClassToRecord"
    )
    @Messages({
        "DN_convertClassToRecord=Convert Class to Record",
        "DESC_convertClassToRecord=Reports classes which can be converted to records. The non-static fields become record components, static fields and methods remain in the body and the canonical constructor is implied.",
        "MSG_ClassToRecord=Class {0} may be converted to a record",
        "FIX_ConvertClassToRecord=Convert class {0} to a record"
    })
    @TriggerTreeKind(Kind.CLASS)
    public static ErrorDescription convertClassToRecord(HintContext context) {
        if (!Feature.RECORD.isEnabled(context.getInfo())) {
            return null;
        }
        if (testClassMayBeRecord(context.getInfo().getTrees(), context.getInfo().getTreeUtilities(), context.getPath())) {
            ClassTree cls = (ClassTree) context.getPath().getLeaf();
            return ErrorDescriptionFactory.forName(context, cls, NbBundle.getMessage(ClassToRecord.class, "MSG_ClassToRecord", cls.getSimpleName()), //NOI18N
                    new ConvertClassToRecordFixImpl(TreePathHandle.create(context.getPath(), context.getInfo()),
                            NbBundle.getMessage(ClassToRecord.class, "FIX_ConvertClassToRecord", cls.getSimpleName())).toEditorFix()); //NOI18N
        }
        return null;
    }

    private static boolean testClassMayBeRecord(Trees trees, TreeUtilities treeUtilities, TreePath path) {
        // a record may be top level or a member of another class,
        // but not a local or anonymous class
        if (path.getParentPath() == null) {
            return false;
        }
        Tree parent = path.getParentPath().getLeaf();
        if (parent.getKind() != Kind.COMPILATION_UNIT && !TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind())) {
            return false;
        }
        Element element = trees.getElement(path);
        if (element == null || element.getKind() != ElementKind.CLASS) {
            return false;
        }
        Set<Modifier> classModifiers = element.getModifiers();
        if (classModifiers.contains(Modifier.SEALED) || classModifiers.contains(Modifier.NON_SEALED)) {
            return false;
        }
        if (classModifiers.contains(Modifier.ABSTRACT) || classModifiers.contains(Modifier.STRICTFP)) {
            return false;
        }
        ClassTree cls = (ClassTree) path.getLeaf();
        if (cls.getExtendsClause() != null) {
            return false;
        }
        // record components are the plain non-static fields
        Set<String> componentNames = new HashSet<>();
        for (Tree member : cls.getMembers()) {
            if (member.getKind() == Kind.VARIABLE && !((VariableTree) member).getModifiers().getFlags().contains(Modifier.STATIC)) {
                VariableTree field = (VariableTree) member;
                // a record component cannot have an initializer or annotations
                if (field.getInitializer() != null || !field.getModifiers().getAnnotations().isEmpty()) {
                    return false;
                }
                componentNames.add(field.getName().toString());
            }
        }
        for (Tree member : cls.getMembers()) {
            if (treeUtilities.isSynthetic(TreePath.getPath(path, member))) {
                continue;
            }
            switch (member.getKind()) {
                case METHOD ->  {
                    MethodTree mth = (MethodTree) member;
                    if (mth.getModifiers().getFlags().contains(Modifier.ABSTRACT)) {
                        // records cannot be abstract
                        return false;
                    }
                    if (mth.getReturnType() == null) {
                        // an explicit constructor would be dropped in favor
                        // of the implicit canonical one
                        return false;
                    }
                    if (mth.getParameters().isEmpty() && componentNames.contains(mth.getName().toString())) {
                        // would collide with the implicitly declared component accessor
                        return false;
                    }
                }
                case VARIABLE -> {
                    // fields were already validated above: static fields and
                    // record components both stay valid in the record body
                    break;
                }
                case CLASS, ENUM, INTERFACE, ANNOTATION_TYPE -> {
                    // nested types stay in the record body
                    break;
                }
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    private static final class ConvertClassToRecordFixImpl extends JavaFix {

        private final String text;

        public ConvertClassToRecordFixImpl(TreePathHandle clsHandle, String text) {
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
            ClassTree cls = (ClassTree) path.getLeaf();
            gu.importComments(cls, wc.getCompilationUnit());
            TreeMaker make = wc.getTreeMaker();
            // records are implicitly final
            Set<Modifier> modifiers = EnumSet.copyOf(cls.getModifiers().getFlags());
            modifiers.remove(Modifier.FINAL);
            ModifiersTree mods = make.Modifiers(modifiers, cls.getModifiers().getAnnotations());
            gu.copyComments(cls.getModifiers(), mods, true);
            gu.copyComments(cls.getModifiers(), mods, false);
            List<Tree> members = new ArrayList<>();
            for (Tree member : cls.getMembers()) {
                if (member.getKind() == Kind.METHOD && ((MethodTree) member).getReturnType() == null) {
                    // the implicit canonical constructor replaces the default one
                    continue;
                }
                if (member.getKind() == Kind.VARIABLE && !((VariableTree) member).getModifiers().getFlags().contains(Modifier.STATIC)) {
                    VariableTree field = (VariableTree) member;
                    VariableTree component = make.RecordComponent(
                            make.Modifiers(EnumSet.noneOf(Modifier.class)),
                            field.getName(),
                            field.getType()
                    );
                    gu.copyComments(field, component, true);
                    gu.copyComments(field, component, false);
                    members.add(component);
                } else {
                    members.add(member);
                }
            }
            ClassTree nue = make.Record(mods, cls.getSimpleName(), cls.getTypeParameters(), cls.getImplementsClause(), members);
            gu.copyComments(cls, nue, true);
            gu.copyComments(cls, nue, false);
            wc.rewrite(path.getLeaf(), nue);
        }
    }
}

