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
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.JTextComponent;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.modules.java.editor.codegen.ui.ToStringPanel;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class ToStringGenerator implements CodeGenerator {

    private static final String ERROR = "<error>"; //NOI18N

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CompilationController controller = context.lookup(CompilationController.class);
            if (component == null || controller == null) {
                return ret;
            }
            TreePath path = context.lookup(TreePath.class);
            path = path != null ? controller.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path) : null;
            if (path == null) {
                return ret;
            }
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ioe) {
                return ret;
            }
            TypeElement typeElement = (TypeElement) controller.getTrees().getElement(path);
            if (typeElement == null || !typeElement.getKind().isClass()) {
                return ret;
            }
            ToStringGenerator generator = createToStringGenerator(component, controller, typeElement, false);
            if (generator == null) {
                return ret;
            }
            ret.add(generator);
            return ret;
        }
    }
    private final JTextComponent component;
    private final ElementNode.Description description;
    private final boolean useStringBuilder;
    private final boolean supportsStringBuilder;

    @CheckForNull
    static ToStringGenerator createToStringGenerator(JTextComponent component, CompilationController controller, TypeElement typeElement, boolean useStringBuilder) {
        List<ElementNode.Description> descriptions = new ArrayList<>();

        // add ordinal() and name() for enums
        if (typeElement.getKind() == ElementKind.ENUM) {
            Element enumElement = controller.getTypes().asElement(typeElement.getSuperclass());
            for (Element element : ElementFilter.methodsIn(enumElement.getEnclosedElements())) {
                Name name = element.getSimpleName();
                if (name.contentEquals("ordinal")) { //NOI18N
                    descriptions.add(0, ElementNode.Description.create(controller, element, null, true, true));
                } else if (name.contentEquals("name")) { //NOI18N
                    descriptions.add(ElementNode.Description.create(controller, element, null, true, true));
                }
            }
        }

        for (Element element : typeElement.getEnclosedElements()) {
            switch (element.getKind()) {
                case METHOD:
                    if (element.getSimpleName().contentEquals("toString") && ((ExecutableElement) element).getParameters().isEmpty() //NOI18N
                            && !controller.getElementUtilities().isSynthetic(element)) { // e.g record
                        return null;
                    }
                    break;
                case FIELD:
//                case RECORD_COMPONENT: // record components will show up as fields for some reason
                    if (!ERROR.contentEquals(element.getSimpleName()) && !element.getModifiers().contains(Modifier.STATIC)) {
                        descriptions.add(ElementNode.Description.create(controller, element, null, true, true));
                    }
                    break;
                default:
                    break;
            }
        }
        return new ToStringGenerator(
                component,
                ElementNode.Description.create(controller, typeElement, descriptions, false, false),
                useStringBuilder,
                supportsStringBuilder(controller)
        );
    }

    private static boolean supportsStringBuilder(CompilationController controller) {
        return SourceVersion.RELEASE_5.compareTo(controller.getSourceVersion()) <= 0
               && controller.getElements().getTypeElement("java.lang.StringBuilder") != null; // NOI18N
    }

    /** Creates a new instance of ToStringGenerator */
    private ToStringGenerator(JTextComponent component, ElementNode.Description description, boolean useStringBuilder, boolean supportsStringBuilder) {
        this.component = component;
        this.description = description;
        this.useStringBuilder = useStringBuilder;
        this.supportsStringBuilder = supportsStringBuilder;
    }

    ElementNode.Description getDescription() {
        return description;
    }

    boolean useStringBuilder() {
        return useStringBuilder;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(ToStringGenerator.class, "LBL_tostring"); //NOI18N
    }

    @Override
    public void invoke() {
        final int caretOffset = component.getCaretPosition();
        final ToStringPanel panel = new ToStringPanel(description, useStringBuilder, supportsStringBuilder);
        DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ToStringGenerator.class, "LBL_generate_tostring")); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue()) {
            return;
        }
        JavaSource js = JavaSource.forDocument(component.getDocument());
        if (js != null) {
            try {
                ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {

                    @Override
                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Element e = description.getElementHandle().resolve(copy);
                        TreePath path = e != null ? copy.getTrees().getPath(e) : copy.getTreeUtilities().pathFor(caretOffset);
                        path = copy.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
                        if (path == null) {
                            String message = NbBundle.getMessage(ToStringGenerator.class, "ERR_CannotFindOriginalClass"); //NOI18N
                            org.netbeans.editor.Utilities.setStatusBoldText(component, message);
                        } else {
                            ClassTree cls = (ClassTree) path.getLeaf();
                            List<Element> fields = new ArrayList<>();
                            for (ElementHandle<? extends Element> elementHandle : panel.getVariables()) {
                                Element field = elementHandle.resolve(copy);
                                if (field == null) {
                                    return;
                                }
                                fields.add(field);
                            }
                            MethodTree mth = createToStringMethod(copy, fields, cls.getSimpleName().toString(), panel.useStringBuilder());
                            copy.rewrite(cls, GeneratorUtils.insertClassMembers(copy, cls, Collections.singletonList(mth), caretOffset));
                        }
                    }
                });
                GeneratorUtils.guardedCommit(component, mr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static MethodTree createToStringMethod(WorkingCopy wc, Iterable<? extends Element> fields, String typeName, boolean useStringBuilder) {
        TreeMaker make = wc.getTreeMaker();
        Set<Modifier> mods = EnumSet.of(Modifier.PUBLIC);
        List<AnnotationTree> annotations = new LinkedList<>();
        if (GeneratorUtils.supportsOverride(wc)) {
            TypeElement override = wc.getElements().getTypeElement("java.lang.Override"); //NOI18N
            if (override != null) {
                annotations.add(wc.getTreeMaker().Annotation(wc.getTreeMaker().QualIdent(override), Collections.<ExpressionTree>emptyList()));
            }
        }
        ModifiersTree modifiers = make.Modifiers(mods, annotations);
        BlockTree body = createToStringMethodBody(make, typeName, fields, useStringBuilder);
        return make.Method(modifiers, "toString", make.Identifier("String"), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null); //NOI18N
    }

    private static BlockTree createToStringMethodBody(TreeMaker make, String typeName, Iterable<? extends Element> fields, boolean useStringBuilder) {
        List<StatementTree> statements;
        if (useStringBuilder) {
            statements = createToStringMethodBodyWithStringBuilder(make, typeName, fields);
        } else {
            statements = createToStringMethodBodyWithPlusOperator(make, typeName, fields);
        }
        BlockTree body = make.Block(statements, false);
        return body;
    }

    private static List<StatementTree> createToStringMethodBodyWithPlusOperator(TreeMaker make, String typeName, Iterable<? extends Element> fields) {
        ExpressionTree exp = make.Literal(typeName + '{');
        boolean first = true;
        for (Element variableElement : fields) {
            StringBuilder sb = new StringBuilder();
            if (!first) {
                sb.append(", ");
            }
            sb.append(variableElement.getSimpleName().toString()).append('=');
            exp = make.Binary(Tree.Kind.PLUS, exp, make.Literal(sb.toString()));
            exp = make.Binary(Tree.Kind.PLUS, exp, makeExpression(make, variableElement));
            first = false;
        }
        StatementTree stat = make.Return(make.Binary(Tree.Kind.PLUS, exp, make.Literal('}'))); //NOI18N
        return Collections.singletonList(stat);
    }

    private static List<StatementTree> createToStringMethodBodyWithStringBuilder(TreeMaker make, String typeName, Iterable<? extends Element> fields) {
        List<StatementTree> statements = new ArrayList<>();
        final ExpressionTree stringBuilder = make.QualIdent(StringBuilder.class.getName());
        NewClassTree newStringBuilder = make.NewClass(null, Collections.emptyList(), stringBuilder, Collections.emptyList(), null);
        VariableTree variable = make.Variable(make.Modifiers(Collections.emptySet()), "sb", stringBuilder, newStringBuilder); // NOI18N
        statements.add(variable); // StringBuilder sb = new StringBuilder();
        IdentifierTree varName = make.Identifier(variable.getName());
        statements.add(make.ExpressionStatement(createAppendInvocation( // sb.append("typeName{");
                make,
                varName,
                Collections.singletonList(make.Literal(typeName + '{'))
        )));
        boolean first = true;
        for (Element variableElement : fields) {
            StringBuilder sb = new StringBuilder();
            if (!first) {
                sb.append(", "); // NOI18N
            }
            sb.append(variableElement.getSimpleName().toString()).append('=');
            // sb.append("fieldName=").append(fieldName); or sb.append(", fieldName=").append(fieldName);
            statements.add(make.ExpressionStatement(createAppendInvocation(
                    make,
                    createAppendInvocation(
                            make,
                            varName,
                            Collections.singletonList(make.Literal(sb.toString()))),
                    Collections.singletonList(makeExpression(make, variableElement)))
            ));
            first = false;
        }
        statements.add(make.ExpressionStatement(createAppendInvocation( // sb.append('}');
                make,
                varName,
                Collections.singletonList(make.Literal('}'))
        )));
        statements.add(make.Return(make.MethodInvocation( // return sb.toString();
                Collections.emptyList(),
                make.MemberSelect(varName, "toString"), // NOI18N
                Collections.emptyList()
        )));
        return statements;
    }

    private static MethodInvocationTree createAppendInvocation(TreeMaker make, ExpressionTree expression, List<? extends ExpressionTree> arguments){
        return make.MethodInvocation( // sb.append()
                Collections.emptyList(),
                make.MemberSelect(expression, "append"), // NOI18N
                arguments
        );
    }

    private static ExpressionTree makeExpression(TreeMaker make, Element element) {
        return element.getKind() == ElementKind.METHOD
                ? make.MethodInvocation(Collections.emptyList(), make.Identifier(element.getSimpleName()), Collections.emptyList())
                : make.Identifier(element.getSimpleName());
    }

}
