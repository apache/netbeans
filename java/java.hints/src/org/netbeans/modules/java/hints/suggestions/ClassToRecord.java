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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.Feature;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.JavaFix;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.UseOptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/**
 * Suggests converting a class declaration into a record.
 * <p>
 * The conversion is only offered when it does not change the program's
 * behavior: a canonical constructor and accessor methods that are redundant
 * with the record's implicit ones are dropped, everything else remains in
 * the body.
 * <p>
 * The {@link #OPTION_REMOVE_OBJECT_METHODS} option trades that guarantee for
 * a slimmer record body: the class' {@code equals()}, {@code hashCode()} and
 * {@code toString()} methods are dropped in favor of the ones inherited from
 * {@code java.lang.Record}, even when their implementation differs.
 *
 * @author mbien
 */
public class ClassToRecord {

    @BooleanOption(
        displayName = "#OPT_ClassToRecord_RemoveObjectMethods",
        tooltip = "#DESC_ClassToRecord_RemoveObjectMethods",
        defaultValue = false
    )
    public static final String OPTION_REMOVE_OBJECT_METHODS = "classtorecord.remove.object.methods"; //NOI18N

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
        "DESC_convertClassToRecord=Reports classes which can be converted to records. The non-static fields become record components, a redundant canonical constructor and record-style accessors are dropped and everything else remains in the body.",
        "MSG_ClassToRecord=Class {0} may be converted to a record",
        "FIX_ConvertClassToRecord=Convert class {0} to a record"
    })
    @TriggerTreeKind(Kind.CLASS)
    @UseOptions(OPTION_REMOVE_OBJECT_METHODS)
    public static ErrorDescription convertClassToRecord(HintContext context) {
        CompilationInfo info = context.getInfo();
        if (!Feature.RECORD.isEnabled(info)) {
            return null;
        }
        if (testClassMayBeRecord(info, context.getPath())) {
            ClassTree cls = (ClassTree) context.getPath().getLeaf();
            Fix fix = new ConvertClassToRecordFixImpl(
                    TreePathHandle.create(context.getPath(), info),
                    NbBundle.getMessage(ClassToRecord.class, "FIX_ConvertClassToRecord", cls.getSimpleName()),
                    context.getPreferences()
            ).toEditorFix();
            return ErrorDescriptionFactory.forName(context, cls, NbBundle.getMessage(ClassToRecord.class, "MSG_ClassToRecord", cls.getSimpleName()), fix); //NOI18N
        }
        return null;
    }

    private static boolean testClassMayBeRecord(CompilationInfo info, TreePath path) {
        // a record may be top level or a member of another class,
        // but not a local or anonymous class
        if (path.getParentPath() == null) {
            return false;
        }
        Tree parent = path.getParentPath().getLeaf();
        if (parent.getKind() != Kind.COMPILATION_UNIT && !TreeUtilities.CLASS_TREE_KINDS.contains(parent.getKind())) {
            return false;
        }
        Trees trees = info.getTrees();
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
        List<String> componentNames = new ArrayList<>();
        Map<String, VariableTree> components = new HashMap<>();
        for (Tree member : cls.getMembers()) {
            if (member.getKind() == Kind.VARIABLE && !((VariableTree) member).getModifiers().getFlags().contains(Modifier.STATIC)) {
                VariableTree field = (VariableTree) member;
                // a record component cannot have an initializer or annotations
                if (field.getInitializer() != null || !field.getModifiers().getAnnotations().isEmpty()) {
                    return false;
                }
                componentNames.add(field.getName().toString());
                components.put(field.getName().toString(), field);
            }
        }
        TreeUtilities treeUtilities = info.getTreeUtilities();
        Types types = info.getTypes();
        for (Tree member : cls.getMembers()) {
            if (treeUtilities.isSynthetic(new TreePath(path, member))) {
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
                        // a constructor is kept as the record's explicitly
                        // declared canonical constructor, unless it is
                        // redundant with the implicit one
                        if (!isRedundantCanonicalConstructor(mth, componentNames, components, trees, types, treeUtilities, path)
                                && hasSuperCall(mth, treeUtilities, path)) {
                            // records extend java.lang.Record, an explicit
                            // super(...) call would not be safe
                            return false;
                        }
                    } else if (mth.getParameters().isEmpty() && components.containsKey(mth.getName().toString())) {
                        // a no-arg method named after a component would
                        // collide with the implicitly declared accessor
                        if (!isRedundantAccessor(mth, components.get(mth.getName().toString()), trees, types, path)) {
                            return false;
                        }
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

    /**
     * Checks whether the given constructor is redundant with the implicit
     * canonical constructor of the record: it has one parameter per record
     * component in declaration order (same name and type) and its body merely
     * assigns the parameters to the components.
     */
    private static boolean isRedundantCanonicalConstructor(MethodTree ctor,
            List<String> componentNames, Map<String, VariableTree> components,
            Trees trees, Types types, TreeUtilities treeUtilities, TreePath classPath) {
        if (!ctor.getModifiers().getAnnotations().isEmpty()) {
            // an annotated constructor is kept, it becomes the record's
            // explicitly declared canonical constructor
            return false;
        }
        List<? extends VariableTree> params = ctor.getParameters();
        if (params.size() != componentNames.size()) {
            return false;
        }
        TreePath ctorPath = new TreePath(classPath, ctor);
        for (int i = 0; i < params.size(); i++) {
            VariableTree param = params.get(i);
            if (!componentNames.get(i).equals(param.getName().toString())
                    || !param.getModifiers().getAnnotations().isEmpty()
                    || !types.isSameType(trees.getElement(new TreePath(ctorPath, param)).asType(),
                            trees.getElement(new TreePath(classPath, components.get(componentNames.get(i)))).asType())) {
                return false;
            }
        }
        if (!(ctor.getBody() instanceof BlockTree block)) {
            return false;
        }
        TreePath bodyPath = new TreePath(ctorPath, block);
        List<StatementTree> statements = new ArrayList<>();
        for (StatementTree statement : block.getStatements()) {
            if (!treeUtilities.isSynthetic(new TreePath(bodyPath, statement))) {
                // javac injects the implicit super(...) call into
                // resolved constructor bodies, it is not part of the source
                statements.add(statement);
            }
        }
        if (statements.size() != params.size()) {
            return false;
        }
        for (int i = 0; i < statements.size(); i++) {
            if (!isThisAssignment(statements.get(i), componentNames.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks whether the given method is a redundant record-style accessor:
     * a public, non-annotated, no-argument method named after a record
     * component whose body merely returns the component field.
     */
    private static boolean isRedundantAccessor(MethodTree mth, VariableTree component,
            Trees trees, Types types, TreePath classPath) {
        if (!mth.getModifiers().getFlags().equals(EnumSet.of(Modifier.PUBLIC))
                || !mth.getModifiers().getAnnotations().isEmpty()) {
            return false;
        }
        if (!types.isSameType(((ExecutableElement) trees.getElement(new TreePath(classPath, mth))).getReturnType(),
                trees.getElement(new TreePath(classPath, component)).asType())) {
            return false;
        }
        if (!(mth.getBody() instanceof BlockTree block) || block.getStatements().size() != 1) {
            return false;
        }
        String name = mth.getName().toString();
        if (!(block.getStatements().getFirst() instanceof ReturnTree ret) || ret.getExpression() == null) {
            return false;
        }
        Tree expression = ret.getExpression();
        if (expression.getKind() == Kind.IDENTIFIER) {
            return name.equals(((IdentifierTree) expression).getName().toString());
        }
        if (expression.getKind() == Kind.MEMBER_SELECT) {
            MemberSelectTree select = (MemberSelectTree) expression;
            return "this".equals(identifierName(select.getExpression())) //NOI18N
                    && name.equals(select.getIdentifier().toString());
        }
        return false;
    }

    /**
    * Checks whether the given method is one of the three {@code Object} methods
    * provided by {@code java.lang.Record}: {@code public [final] boolean
    * equals(Object)}, {@code public [final] int hashCode()} or {@code public
    * [final] String toString()}. An {@code @Override} annotation is tolerated,
    * any other annotation keeps the method in the record body.
    */
    private static boolean isRecordObjectMethod(MethodTree mth, Trees trees, Types types, Elements elements, TreePath classPath) {
        Set<Modifier> flags = mth.getModifiers().getFlags();
        if (!flags.equals(EnumSet.of(Modifier.PUBLIC)) && !flags.equals(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL))) {
            return false;
        }
        List<? extends AnnotationTree> annotations = mth.getModifiers().getAnnotations();
        if (annotations.size() > 1
                || (!annotations.isEmpty() && !isOverrideAnnotation(annotations.getFirst(), trees, types, elements, classPath, mth))) {
            return false;
        }
        List<? extends VariableTree> params = mth.getParameters();
        String name = mth.getName().toString();
        if ("equals".equals(name)) { //NOI18N
            if (params.size() != 1) {
                return false;
            }
            // the return type is forced to boolean by the compiler, the
            // parameter type must be Object for this to be an override
            return types.isSameType(trees.getElement(new TreePath(new TreePath(classPath, mth), params.getFirst())).asType(),
                    elements.getTypeElement("java.lang.Object").asType()); //NOI18N
        }
        // the return types are forced to int and String by the compiler
        return params.isEmpty() && ("hashCode".equals(name) || "toString".equals(name)); //NOI18N
    }

    private static boolean isOverrideAnnotation(AnnotationTree annotation, Trees trees, Types types, Elements elements, TreePath classPath, MethodTree mth) {
        TypeElement override = elements.getTypeElement("java.lang.Override"); //NOI18N
        if (override == null) {
            return false;
        }
        Element element = trees.getElement(new TreePath(new TreePath(classPath, mth), annotation.getAnnotationType()));
        return element != null && types.isSameType(element.asType(), override.asType());
    }

    private static boolean isThisAssignment(StatementTree statement, String name) {
        if (!(statement instanceof ExpressionStatementTree es)
                || !(es.getExpression() instanceof AssignmentTree assignment)) {
            return false;
        }
        if (assignment.getVariable().getKind() != Kind.MEMBER_SELECT) {
            return false;
        }
        MemberSelectTree select = (MemberSelectTree) assignment.getVariable();
        return "this".equals(identifierName(select.getExpression())) //NOI18N
                && name.equals(select.getIdentifier().toString())
                && assignment.getExpression().getKind() == Kind.IDENTIFIER
                && name.equals(((IdentifierTree) assignment.getExpression()).getName().toString());
    }

    private static boolean hasSuperCall(MethodTree ctor, TreeUtilities treeUtilities, TreePath classPath) {
        if (!(ctor.getBody() instanceof BlockTree block)) {
            // be conservative with unusual constructor bodies
            return true;
        }
        TreePath bodyPath = new TreePath(new TreePath(classPath, ctor), block);
        for (StatementTree statement : block.getStatements()) {
            if (treeUtilities.isSynthetic(new TreePath(bodyPath, statement))) {
                // the implicit super(...) call javac injects into
                // resolved constructor bodies
                continue;
            }
            if (statement instanceof ExpressionStatementTree es
                    && es.getExpression().getKind() == Kind.METHOD_INVOCATION
                    && "super".equals(identifierName(((MethodInvocationTree) es.getExpression()).getMethodSelect()))) { //NOI18N
                return true;
            }
        }
        return false;
    }

    private static String identifierName(Tree tree) {
        if (tree.getKind() == Kind.IDENTIFIER) {
            return ((IdentifierTree) tree).getName().toString();
        }
        return null;
    }

    private static final class ConvertClassToRecordFixImpl extends JavaFix {

        private final String text;
        private final Preferences prefs;

        public ConvertClassToRecordFixImpl(TreePathHandle clsHandle, String text, Preferences prefs) {
            super(clsHandle);
            this.text = text;
            this.prefs = prefs;
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
            Set<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
            modifiers.addAll(cls.getModifiers().getFlags());
            modifiers.remove(Modifier.FINAL);
            ModifiersTree mods = make.Modifiers(modifiers, cls.getModifiers().getAnnotations());
            gu.copyComments(cls.getModifiers(), mods, true);
            gu.copyComments(cls.getModifiers(), mods, false);
            // record components are the plain non-static fields
            List<String> componentNames = new ArrayList<>();
            Map<String, VariableTree> components = new HashMap<>();
            for (Tree member : cls.getMembers()) {
                if (member.getKind() == Kind.VARIABLE && !((VariableTree) member).getModifiers().getFlags().contains(Modifier.STATIC)) {
                    VariableTree field = (VariableTree) member;
                    componentNames.add(field.getName().toString());
                    components.put(field.getName().toString(), field);
                }
            }
            Trees trees = wc.getTrees();
            Types types = wc.getTypes();
            Elements elements = wc.getElements();
            TreeUtilities treeUtilities = wc.getTreeUtilities();
            List<Tree> members = new ArrayList<>();
            boolean removeObjectMethods = prefs.getBoolean(OPTION_REMOVE_OBJECT_METHODS, false);
            for (Tree member : cls.getMembers()) {
                if (member.getKind() == Kind.METHOD) {
                    MethodTree mth = (MethodTree) member;
                    if (removeObjectMethods && isRecordObjectMethod(mth, trees, types, elements, path)) {
                        // replaced by the methods inherited from java.lang.Record
                        continue;
                    }
                    if (mth.getReturnType() == null) {
                        if (isRedundantCanonicalConstructor(mth, componentNames, components, trees, types, treeUtilities, path)) {
                            // the implicit canonical constructor replaces the redundant one
                            continue;
                        }
                    } else if (mth.getParameters().isEmpty() && components.containsKey(mth.getName().toString())) {
                        if (isRedundantAccessor(mth, components.get(mth.getName().toString()), trees, types, path)) {
                            // the implicit component accessor replaces the redundant one
                            continue;
                        }
                    }
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

