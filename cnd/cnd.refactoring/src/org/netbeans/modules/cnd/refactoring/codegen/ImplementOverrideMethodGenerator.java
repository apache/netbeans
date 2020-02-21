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
package org.netbeans.modules.cnd.refactoring.codegen;

import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.refactoring.codegen.ui.ImplementOverridePanel;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class ImplementOverrideMethodGenerator implements CodeGenerator {

    public static class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
//            JTextComponent component = context.lookup(JTextComponent.class);
//            CompilationController controller = context.lookup(CompilationController.class);
//            TreePath path = context.lookup(TreePath.class);
//            path = path != null ? Utilities.getPathElementOfKind(Tree.Kind.CLASS, path) : null;
//            if (component == null || controller == null || path == null) {
//                return ret;
//            }
//            try {
//                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
//            } catch (IOException ioe) {
//                return ret;
//            }
//            TypeElement typeElement = (TypeElement) controller.getTrees().getElement(path);
//            if (typeElement == null || !typeElement.getKind().isClass()) {
//                return ret;
//            }
//            Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<Element, List<ElementNode.Description>>();
//            for (ExecutableElement method : GeneratorUtils.findUndefs(controller, typeElement)) {
//                List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
//                if (descriptions == null) {
//                    descriptions = new ArrayList<ElementNode.Description>();
//                    map.put(method.getEnclosingElement(), descriptions);
//                }
//                descriptions.add(ElementNode.Description.create(method, null, true, false));
//            }
//            List<ElementNode.Description> implementDescriptions = new ArrayList<ElementNode.Description>();
//            for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet()) {
//                implementDescriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
//            }
//            if (!implementDescriptions.isEmpty()) {
//                ret.add(new ImplementOverrideMethodGenerator(component, ElementNode.Description.create(implementDescriptions), true));
//            }
//            map = new LinkedHashMap<Element, List<ElementNode.Description>>();
//            ArrayList<Element> orderedElements = new ArrayList<Element>();
//            for (ExecutableElement method : GeneratorUtils.findOverridable(controller, typeElement)) {
//                List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
//                if (descriptions == null) {
//                    descriptions = new ArrayList<ElementNode.Description>();
//                    Element e = method.getEnclosingElement();
//                    map.put(e, descriptions);
//                    if (!orderedElements.contains(e)) {
//                        orderedElements.add(e);
//                    }
//                }
//                descriptions.add(ElementNode.Description.create(method, null, true, false));
//            }
//            List<ElementNode.Description> overrideDescriptions = new ArrayList<ElementNode.Description>();
//            for (Element e : orderedElements) {
//                overrideDescriptions.add(ElementNode.Description.create(e, map.get(e), false, false));
//            }
//            if (!overrideDescriptions.isEmpty()) {
//                ret.add(new ImplementOverrideMethodGenerator(component, ElementNode.Description.create(overrideDescriptions), false));
//            }
            return ret;
        }
    }
    private JTextComponent component;
    private ElementNode.Description description;
    private boolean isImplement;

    /** Creates a new instance of OverrideMethodGenerator */
    private ImplementOverrideMethodGenerator(JTextComponent component, ElementNode.Description description, boolean isImplement) {
        this.component = component;
        this.description = description;
        this.isImplement = isImplement;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(ImplementOverrideMethodGenerator.class, isImplement ? "LBL_implement_method" : "LBL_override_method"); //NOI18N
    }

    @Override
    public void invoke() {
        UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, CsmRefactoringUtils.GENERATE_TRACKING, "IMPLEMENTE_OVERRIDE_METHOD"); // NOI18N
        final ImplementOverridePanel panel = new ImplementOverridePanel(description, isImplement);
        DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel,
                NbBundle.getMessage(ConstructorGenerator.class, isImplement ? "LBL_generate_implement" : "LBL_generate_override")); //NOI18N  //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dialogDescriptor.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dialog.dispose();
        }
        if (dialogDescriptor.getValue() == dialogDescriptor.getDefaultValue()) {
//            JavaSource js = JavaSource.forDocument(component.getDocument());
//            if (js != null) {
//                try {
//                    final int caretOffset = component.getCaretPosition();
//                    ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
//
//                        public void run(WorkingCopy copy) throws IOException {
//                            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
//                            TreePath path = copy.getTreeUtilities().pathFor(caretOffset);
//                            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
//                            int idx = GeneratorUtils.findClassMemberIndex(copy, (ClassTree) path.getLeaf(), caretOffset);
//                            ArrayList<ExecutableElement> methodElements = new ArrayList<ExecutableElement>();
//                            for (ElementHandle<? extends Element> elementHandle : panel.getSelectedMethods()) {
//                                methodElements.add((ExecutableElement) elementHandle.resolve(copy));
//                            }
//                            if (isImplement) {
//                                GeneratorUtils.generateAbstractMethodImplementations(copy, path, methodElements, idx);
//                            } else {
//                                GeneratorUtils.generateMethodOverrides(copy, path, methodElements, idx);
//                            }
//                        }
//                    });
//                    GeneratorUtils.guardedCommit(component, mr);
//                    int span[] = mr.getSpan("methodBodyTag"); // NOI18N
//                    if (span != null) {
//                        component.setSelectionStart(span[0]);
//                        component.setSelectionEnd(span[1]);
//                    }
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
        }
    }
}
