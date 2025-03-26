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

import com.sun.source.tree.Tree;
import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.sun.source.util.TreePath;

import org.netbeans.api.java.source.*;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.modules.java.editor.codegen.ui.GetterSetterPanel;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.java.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.refactoring.java.api.ui.JavaRefactoringActionsFactory;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Balek
 */
public class GetterSetterGenerator implements CodeGenerator {

    private static final EnumSet<Tree.Kind> TREE_KINDS = EnumSet.copyOf(TreeUtilities.CLASS_TREE_KINDS);
    static {
        TREE_KINDS.remove(Tree.Kind.RECORD); // no getters/setters for records
    }

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
            CodeStyle codeStyle = CodeStyle.getDefault(component.getDocument());
            TreePath path = context.lookup(TreePath.class);
            path = controller.getTreeUtilities().getPathElementOfKind(TREE_KINDS, path);
            if (path == null) {
                return ret;
            }
            try {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            } catch (IOException ioe) {
                return ret;
            }
            Elements elements = controller.getElements();
            ElementUtilities eu = controller.getElementUtilities();
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (typeElement == null || !typeElement.getKind().isClass()) {
                return ret;
            }
            Map<Element, List<ElementNode.Description>> gDescriptions = new LinkedHashMap<>();
            Map<Element, List<ElementNode.Description>> sDescriptions = new LinkedHashMap<>();
            Map<Element, List<ElementNode.Description>> gsDescriptions = new LinkedHashMap<>();
            for (VariableElement variableElement : ElementFilter.fieldsIn(elements.getAllMembers(typeElement))) {
                if (ERROR.contentEquals(variableElement.getSimpleName())) {
                    continue;
                }
                ElementNode.Description description = ElementNode.Description.create(controller, variableElement, null, true, false);
                boolean hasGetter = eu.hasGetter(typeElement, variableElement, codeStyle);
                boolean hasSetter = variableElement.getModifiers().contains(Modifier.FINAL) || eu.hasSetter(typeElement, variableElement, codeStyle);
                if (!hasGetter) {
                    List<ElementNode.Description> descriptions = gDescriptions.get(variableElement.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<>();
                        gDescriptions.put(variableElement.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(description);
                }
                if (!hasSetter) {
                    List<ElementNode.Description> descriptions = sDescriptions.get(variableElement.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<>();
                        sDescriptions.put(variableElement.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(description);
                }
                if (!hasGetter && !hasSetter) {
                    List<ElementNode.Description> descriptions = gsDescriptions.get(variableElement.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<>();
                        gsDescriptions.put(variableElement.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(description);
                }
            }
            if (!gDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : gDescriptions.entrySet()) {
                    descriptions.add(ElementNode.Description.create(controller, entry.getKey(), entry.getValue(), false, false));
                }
                Collections.reverse(descriptions);
                ret.add(new GetterSetterGenerator(component, ElementNode.Description.create(controller, typeElement, descriptions, false, false), GeneratorUtils.GETTERS_ONLY, codeStyle));
            }
            if (!sDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : sDescriptions.entrySet()) {
                    descriptions.add(ElementNode.Description.create(controller, entry.getKey(), entry.getValue(), false, false));
                }
                Collections.reverse(descriptions);
                ret.add(new GetterSetterGenerator(component, ElementNode.Description.create(controller, typeElement, descriptions, false, false), GeneratorUtils.SETTERS_ONLY, codeStyle));
            }
            if (!gsDescriptions.isEmpty()) {
                List<ElementNode.Description> descriptions = new ArrayList<>();
                for (Map.Entry<Element, List<ElementNode.Description>> entry : gsDescriptions.entrySet()) {
                    descriptions.add(ElementNode.Description.create(controller, entry.getKey(), entry.getValue(), false, false));
                }
                Collections.reverse(descriptions);
                ret.add(new GetterSetterGenerator(component, ElementNode.Description.create(controller, typeElement, descriptions, false, false), 0, codeStyle));
            }
            return ret;
        }
    }

    private final JTextComponent component;
    private final ElementNode.Description description;
    private final int type;
    private final CodeStyle codestyle;

    /** Creates a new instance of GetterSetterGenerator */
    private GetterSetterGenerator(JTextComponent component, ElementNode.Description description, int type, CodeStyle codeStyle) {
        this.component = component;
        this.description = description;
        this.type = type;
        this.codestyle = codeStyle;
    }

    @Override
    public String getDisplayName() {
        if (type == GeneratorUtils.GETTERS_ONLY) {
            return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter"); //NOI18N
        }
        if (type == GeneratorUtils.SETTERS_ONLY) {
            return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_setter"); //NOI18N
        }
        return org.openide.util.NbBundle.getMessage(GetterSetterGenerator.class, "LBL_getter_and_setter"); //NOI18N
    }

    @Override
    public void invoke() {
        final int caretOffset = component.getCaretPosition();
        final GetterSetterPanel panel = new GetterSetterPanel(description, type);
        String title;
        if (type == GeneratorUtils.GETTERS_ONLY) {
            title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_getter"); //NOI18N
        } else if (type == GeneratorUtils.SETTERS_ONLY) {
            title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_setter"); //NOI18N
        } else {
            title = NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_getter_and_setter"); //NOI18N
        }
        final DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, title);
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                List<ElementHandle<? extends Element>> vars = panel.getVariables();
                dialogDescriptor.setValid(vars != null && !vars.isEmpty());
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() == dialogDescriptor.getDefaultValue()) {
            if (panel.isPerformEnsapsulate()) {
                performEncapsulate(panel.getVariables());
            } else {
                JavaSource js = JavaSource.forDocument(component.getDocument());
                if (js != null) {
                    try {
                        ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {

                            @Override
                            public void run(WorkingCopy copy) throws IOException {
                                copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                Element e = description.getElementHandle().resolve(copy);
                                TreePath path = e != null ? copy.getTrees().getPath(e) : copy.getTreeUtilities().pathFor(caretOffset);
                                path = copy.getTreeUtilities().getPathElementOfKind(TREE_KINDS, path);
                                if (path == null) {
                                    String message = NbBundle.getMessage(GetterSetterGenerator.class, "ERR_CannotFindOriginalClass"); //NOI18N
                                    org.netbeans.editor.Utilities.setStatusBoldText(component, message);
                                } else {
                                    ArrayList<VariableElement> variableElements = new ArrayList<>();
                                    for (ElementHandle<? extends Element> elementHandle : panel.getVariables()) {
                                        VariableElement elem = (VariableElement) elementHandle.resolve(copy);
                                        if (elem == null) {
                                            String message = NbBundle.getMessage(GetterSetterGenerator.class, "ERR_CannotFindOriginalMember"); //NOI18N
                                            org.netbeans.editor.Utilities.setStatusBoldText(component, message);
                                            return;
                                        }
                                        variableElements.add(elem);
                                    }
                                    GeneratorUtils.generateGettersAndSetters(copy, path, variableElements, type, caretOffset);
                                }
                            }
                        });
                        GeneratorUtils.guardedCommit(component, mr);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
    }
    
    private void performEncapsulate(final List<ElementHandle<? extends Element>> variables) {
        try {
            JavaSource js = JavaSource.forDocument(component.getDocument());
            final List<String> getters = new ArrayList<>();
            final List<String> setters = new ArrayList<>();
            final List<TreePathHandle> handles = new ArrayList<>(variables.size());
            js.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController parameter) throws Exception {
                    createGetterSetterLists(parameter, variables, handles, getters, setters, codestyle);
                }
            }, true);
            
            ProgressUtils.runOffEventDispatchThread(new Runnable() {

                @Override
                public void run() {
                    doDefaultEncapsulate(handles, getters, setters);
                }
            },
                    NbBundle.getMessage(GetterSetterGenerator.class, "LBL_EncapsulateFields"),
                    new AtomicBoolean(),
                    false);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
        
    private void createGetterSetterLists(CompilationController cc, List<ElementHandle<? extends Element>> variables, List<? super TreePathHandle> handles, List<String> getters, List<String> setters, CodeStyle codestyle) {
        for (ElementHandle handle:variables) {
            final Element el = handle.resolve(cc);
            handles.add(TreePathHandle.create(el, cc));
            boolean isStatic = el.getModifiers().contains(Modifier.STATIC);
            if (type!=GeneratorUtils.GETTERS_ONLY) {
                setters.add(CodeStyleUtils.computeSetterName(el.getSimpleName(), isStatic, codestyle));
            } else {
                setters.add(null);
            }
            if (type!=GeneratorUtils.SETTERS_ONLY) {
                getters.add(CodeStyleUtils.computeGetterName(el.getSimpleName(), el.asType().getKind() == TypeKind.BOOLEAN, isStatic, codestyle));
            } else {
                getters.add(null);
            }
        }
    }

    private void doDefaultEncapsulate(List<TreePathHandle> variables, List<String> getters, List<String> setters) {
        RefactoringSession encapsulate = RefactoringSession.create(NbBundle.getMessage(GetterSetterGenerator.class, "LBL_EncapsulateFields"));
        final Iterator<String> setIterator = setters.iterator();
        final Iterator<String> getIterator = getters.iterator();
        ClasspathInfo cpinfo = ClasspathInfo.create(component.getDocument());
        for (Iterator<TreePathHandle> it = variables.iterator(); it.hasNext();) {
            EncapsulateFieldRefactoring refactoring = new EncapsulateFieldRefactoring(it.next());
            refactoring.setSetterName(setIterator.next());
            refactoring.setGetterName(getIterator.next());
            refactoring.setFieldModifiers(EnumSet.of(Modifier.PRIVATE));
            refactoring.setMethodModifiers(EnumSet.of(Modifier.PUBLIC));
            Problem p = refactoring.prepare(encapsulate);
            if (p!=null) {
                doFullEncapsulate();
                return;
            }
        }
        encapsulate.doRefactoring(true);
    }
    
    private void doFullEncapsulate() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                Action encapsulateAction = JavaRefactoringActionsFactory.encapsulateFieldsAction().createContextAwareInstance(Lookups.fixed(((DataObject)component.getDocument().getProperty(BaseDocument.StreamDescriptionProperty)).getNodeDelegate()));
                encapsulateAction.actionPerformed(null);
            }
            
        });
    }    
}
