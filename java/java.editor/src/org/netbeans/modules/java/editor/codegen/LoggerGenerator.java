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
package org.netbeans.modules.java.editor.codegen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.JTextComponent;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;

import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.completion.Utilities;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Dusan Balek
 */
public class LoggerGenerator implements CodeGenerator {

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
            path = controller.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
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
            for (VariableElement ve : ElementFilter.fieldsIn(typeElement.getEnclosedElements())) {
                TypeMirror type = ve.asType();
                if (type.getKind() == TypeKind.DECLARED && ((TypeElement)((DeclaredType)type).asElement()).getQualifiedName().contentEquals(Logger.class.getName())) {
                    return ret;
                }
            }
            List<ElementNode.Description> descriptions = new ArrayList<>();
            ret.add(new LoggerGenerator(component, ElementNode.Description.create(controller, typeElement, descriptions, false, false)));
            return ret;
        }
    }
    private final JTextComponent component;
    private final ElementNode.Description description;

    /** Creates a new instance of ToStringGenerator */
    private LoggerGenerator(JTextComponent component, ElementNode.Description description) {
        this.component = component;
        this.description = description;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(LoggerGenerator.class, "LBL_logger"); //NOI18N
    }

    @Override
    public void invoke() {
        final int caretOffset = component.getCaretPosition();
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
                            String message = NbBundle.getMessage(LoggerGenerator.class, "ERR_CannotFindOriginalClass"); //NOI18N
                            org.netbeans.editor.Utilities.setStatusBoldText(component, message);
                        } else {
                            ClassTree cls = (ClassTree) path.getLeaf();
                            CodeStyle cs = CodeStyle.getDefault(component.getDocument());
                            Set<Modifier> mods = EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
                            List<String> names = Utilities.varNamesSuggestions(null, ElementKind.FIELD, mods, "LOG", null, copy.getTypes(), copy.getElements(), e.getEnclosedElements(), cs);
                            VariableTree var = createLoggerField(copy.getTreeMaker(), cls, names.size() > 0 ? names.get(0) : "LOG", mods); //NOI18N
                            copy.rewrite(cls, GeneratorUtils.insertClassMembers(copy, cls, Collections.singletonList(var), caretOffset));
                        }
                    }
                });
                GeneratorUtils.guardedCommit(component, mr);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static VariableTree createLoggerField(TreeMaker make, ClassTree cls, CharSequence name, Set<Modifier> mods) {
        ModifiersTree modifiers = make.Modifiers(mods, Collections.<AnnotationTree>emptyList());
        final List<ExpressionTree> none = Collections.<ExpressionTree>emptyList();
        IdentifierTree className = make.Identifier(cls.getSimpleName());
        MemberSelectTree classType = make.MemberSelect(className, "class"); // NOI18N
        MemberSelectTree getName  = make.MemberSelect(classType, "getName"); // NOI18N
        MethodInvocationTree initClass = make.MethodInvocation(none, getName, none);
        final ExpressionTree logger = make.QualIdent(Logger.class.getName());
        MemberSelectTree getLogger = make.MemberSelect(logger, "getLogger"); // NOI18N
        MethodInvocationTree initField = make.MethodInvocation(none, getLogger, Collections.nCopies(1, initClass));
        return make.Variable(modifiers, name, logger, initField); // NOI18N
    }
}
