/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
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

    public static class Factory implements CodeGenerator.Factory {

        private static final String ERROR = "<error>"; //NOI18N

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
            List<ElementNode.Description> descriptions = new ArrayList<>();
            for (Element element : typeElement.getEnclosedElements()) {
                switch (element.getKind()) {
                    case METHOD:
                        if (element.getSimpleName().contentEquals("toString") && ((ExecutableElement) element).getParameters().isEmpty()) { //NOI18N
                            return ret;
                        }
                        break;
                    case FIELD:
                        if (!ERROR.contentEquals(element.getSimpleName()) && !element.getModifiers().contains(Modifier.STATIC)) {
                            descriptions.add(ElementNode.Description.create(controller, element, null, true, true));
                        }
                }
            }
            ret.add(new ToStringGenerator(component, ElementNode.Description.create(controller, typeElement, descriptions, false, false)));
            return ret;
        }
    }
    private final JTextComponent component;
    private final ElementNode.Description description;

    /** Creates a new instance of ToStringGenerator */
    private ToStringGenerator(JTextComponent component, ElementNode.Description description) {
        this.component = component;
        this.description = description;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(ToStringGenerator.class, "LBL_tostring"); //NOI18N
    }

    @Override
    public void invoke() {
        final int caretOffset = component.getCaretPosition();
        final ToStringPanel panel = new ToStringPanel(description);
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
                            ArrayList<VariableElement> fields = new ArrayList<>();
                            for (ElementHandle<? extends Element> elementHandle : panel.getVariables()) {
                                VariableElement field = (VariableElement) elementHandle.resolve(copy);
                                if (field == null) {
                                    return;
                                }
                                fields.add(field);
                            }
                            MethodTree mth = createToStringMethod(copy, fields, cls.getSimpleName().toString());
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

    private static MethodTree createToStringMethod(WorkingCopy wc, Iterable<? extends VariableElement> fields, String typeName) {
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

        ExpressionTree exp = make.Literal(typeName + '{');
        boolean first = true;
        for (VariableElement variableElement : fields) {
            StringBuilder sb = new StringBuilder();
            if (!first) {
                sb.append(", ");
            }
            sb.append(variableElement.getSimpleName().toString()).append('=');
            exp = make.Binary(Tree.Kind.PLUS, exp, make.Literal(sb.toString()));
            exp = make.Binary(Tree.Kind.PLUS, exp, make.Identifier(variableElement.getSimpleName()));
            first = false;
        }
        StatementTree stat = make.Return(make.Binary(Tree.Kind.PLUS, exp, make.Literal('}'))); //NOI18N
        BlockTree body = make.Block(Collections.singletonList(stat), false);

        return make.Method(modifiers, "toString", make.Identifier("String"), Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), body, null); //NOI18N
    }
}
