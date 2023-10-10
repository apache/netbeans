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

import com.sun.source.util.TreePath;
import java.awt.Dialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.ui.ConstructorPanel;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
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
public class ConstructorGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            final ArrayList<CodeGenerator> ret = new ArrayList<>();
            final JTextComponent component = context.lookup(JTextComponent.class);
            final CompilationController controller = context.lookup(CompilationController.class);
            if (component == null || controller == null) {
                return ret;
            }
            TreePath path = context.lookup(TreePath.class);
            final TreeUtilities tu = controller.getTreeUtilities();
            path = tu.getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
            if (path == null) {
                return ret;
            }
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ioe) {
                return ret;
            }
            final TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (typeElement == null || !typeElement.getKind().isClass() || NestingKind.ANONYMOUS == typeElement.getNestingKind()) {
                return ret;
            }
            final WorkingCopy wc = context.lookup(WorkingCopy.class);
            final Set<? extends VariableElement> uninitializedFields = tu.getUninitializedFields(path);
            final List<ExecutableElement> inheritedConstructors = new ArrayList<>();
            TypeMirror superClassType = typeElement.getSuperclass();
            TypeElement superClass = null;
            if (superClassType.getKind() == TypeKind.DECLARED) {
                superClass = (TypeElement) ((DeclaredType) superClassType).asElement();
                Elements elements = controller.getElements();
                for (ExecutableElement executableElement : ElementFilter.constructorsIn(superClass.getEnclosedElements())) {
                    PackageElement currentPackage = elements.getPackageOf(typeElement);
                    PackageElement ctorPackage = elements.getPackageOf(executableElement);
                    Set<Modifier> ctorMods = executableElement.getModifiers();
                    if ((currentPackage != ctorPackage && !(ctorMods.contains(Modifier.PUBLIC) || ctorMods.contains(Modifier.PROTECTED)))
                            || ctorMods.contains(Modifier.PRIVATE)) {
                        continue;
                    }
                    inheritedConstructors.add(executableElement);
                }
            }
            ElementHandle<? extends Element> constructorHandle = null;
            ElementNode.Description constructorDescription = null;
            if (typeElement.getKind() != ElementKind.ENUM && inheritedConstructors.size() == 1) {
                constructorHandle = ElementHandle.create(inheritedConstructors.get(0));
            } else if (inheritedConstructors.size() > 1) {
                List<ElementNode.Description> constructorDescriptions = new ArrayList<>();
                for (ExecutableElement constructorElement : inheritedConstructors) {
                    constructorDescriptions.add(ElementNode.Description.create(controller, constructorElement, null, true, false));
                }
                constructorDescription = ElementNode.Description.create(controller, superClass, constructorDescriptions, false, false);
            }
            ElementNode.Description fieldsDescription = null;
            if (!uninitializedFields.isEmpty()) {
                List<ElementNode.Description> fieldDescriptions = new ArrayList<>();
                for (VariableElement variableElement : uninitializedFields) {
                    fieldDescriptions.add(ElementNode.Description.create(controller, variableElement, null, true, variableElement.getModifiers().contains(Modifier.FINAL)));
                }
                fieldsDescription = ElementNode.Description.create(controller, typeElement, fieldDescriptions, false, false);
            }
            if (constructorHandle != null || constructorDescription != null || fieldsDescription != null) {
                ret.add(new ConstructorGenerator(component, TreePathHandle.create(path, controller), constructorHandle, constructorDescription, fieldsDescription, wc));
            }
            return ret;
        }
    }

    private final JTextComponent component;
    private final TreePathHandle typeHandle;
    private final ElementHandle<? extends Element> constructorHandle;
    private final ElementNode.Description constructorDescription;
    private final ElementNode.Description fieldsDescription;
    private final WorkingCopy existingWorkingCopy;
    
    /** Creates a new instance of ConstructorGenerator */
    private ConstructorGenerator(JTextComponent component, TreePathHandle typeHandle, ElementHandle<? extends Element> constructorHandle, ElementNode.Description constructorDescription, ElementNode.Description fieldsDescription,
            WorkingCopy wc) {
        this.component = component;
        this.typeHandle = typeHandle;
        this.constructorHandle = constructorHandle;
        this.constructorDescription = constructorDescription;
        this.fieldsDescription = fieldsDescription;
        this.existingWorkingCopy = wc;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(ConstructorGenerator.class, "LBL_constructor"); //NOI18N
    }

    @Override
    public void invoke() {
        final List<ElementHandle<? extends Element>> fieldHandles;
        final List<ElementHandle<? extends Element>> constrHandles;
        final int caretOffset = component.getCaretPosition();
        
        if (constructorDescription != null || fieldsDescription != null) {
            ConstructorPanel panel = new ConstructorPanel(constructorDescription, fieldsDescription);
            DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_constructor")); //NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            dialog.setVisible(true);
            if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue()) {
                return;
            }
            if (constructorHandle == null) {
                constrHandles = panel.getInheritedConstructors();
            } else {
                constrHandles = null;
            }
            fieldHandles = panel.getVariablesToInitialize();
        } else {
            fieldHandles = null;
            constrHandles = null;
        }
        try {
            if (existingWorkingCopy == null) {
                JavaSource js = JavaSource.forDocument(component.getDocument());
                if (js != null) {
                    ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                    @Override
                        public void run(WorkingCopy copy) throws IOException {
                            doGenerateConstructor(copy, fieldHandles, constrHandles, caretOffset);
                        }
                    });
                    GeneratorUtils.guardedCommit(component, mr);
                }
            } else {
                doGenerateConstructor(existingWorkingCopy, fieldHandles, constrHandles, caretOffset);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void doGenerateConstructor(WorkingCopy copy,
            List<ElementHandle<? extends Element>> fieldHandles,
            List<ElementHandle<? extends Element>> constrHandles, int caretOffset) throws IOException {
        copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
        TreePath path = typeHandle.resolve(copy);
        if (path == null) {
            path = copy.getTreeUtilities().pathFor(caretOffset);
        }
        path = copy.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
        if (path == null) {
            String message = NbBundle.getMessage(ConstructorGenerator.class, "ERR_CannotFindOriginalClass"); //NOI18N
            org.netbeans.editor.Utilities.setStatusBoldText(component, message);
        } else {
            try {
                ArrayList<VariableElement> variableElements = new ArrayList<>();
                if (fieldHandles != null) {
                    for (ElementHandle<? extends Element> elementHandle : fieldHandles) {
                        VariableElement field = (VariableElement) elementHandle.resolve(copy);
                        if (field == null) {
                            return;
                        }
                        variableElements.add(field);
                    }
                }
                if (constrHandles != null && !constrHandles.isEmpty()) {
                    ArrayList<ExecutableElement> constrElements = new ArrayList<>();
                    for (ElementHandle<? extends Element> elementHandle : constrHandles) {
                        ExecutableElement constr = (ExecutableElement) elementHandle.resolve(copy);
                        if (constr == null) {
                            return;
                        }
                        constrElements.add(constr);
                    }
                    GeneratorUtils.generateConstructors(copy, path, variableElements, constrElements, caretOffset);
                } else {
                    GeneratorUtils.generateConstructor(copy, path, variableElements, constructorHandle != null ? (ExecutableElement) constructorHandle.resolve(copy) : null, caretOffset);
                }
            } catch (GeneratorUtils.DuplicateMemberException dme) {
                if (dme.getPos() >= 0) {
                    component.setCaretPosition(dme.getPos());
                }
                String message = NbBundle.getMessage(ConstructorGenerator.class, "ERR_ConstructorAlreadyExists"); //NOI18N
                org.netbeans.editor.Utilities.setStatusBoldText(component, message);
            }
        }
    }
}
