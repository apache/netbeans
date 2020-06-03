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
import java.awt.Dialog;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.refactoring.codegen.ui.DelegatePanel;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class DelegateMethodGenerator implements CodeGenerator {

    private static final String ERROR = "<error>"; //NOI18N

    public static class Factory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
//            JTextComponent component = context.lookup(JTextComponent.class);
//            CompilationController controller = context.lookup(CompilationController.class);
//            TreePath path = context.lookup(TreePath.class);
//            path = path != null ? Utilities.getPathElementOfKind(Tree.Kind.CLASS, path) : null;
//            if (component == null || controller == null || path == null)
//                return ret;
//            try {
//                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
//            } catch (IOException ioe) {
//                return ret;
//            }
//            List<ElementNode.Description> descriptions = computeUsableFieldsDescriptions(controller, path);
//            if (!descriptions.isEmpty()) {
//                Collections.reverse(descriptions);
//                ret.add(new DelegateMethodGenerator(component, ElementNode.Description.create(descriptions)));
//            }
            return ret;
        }
    }

    private JTextComponent component;
    private ElementNode.Description description;
    
    /** Creates a new instance of DelegateMethodGenerator */
    private DelegateMethodGenerator(JTextComponent component, ElementNode.Description description) {
        this.component = component;
        this.description = description;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_method"); //NOI18N
    }

    @Override
    public void invoke() {
        UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, CsmRefactoringUtils.GENERATE_TRACKING, "DELEGATE_METHOD"); // NOI18N
        final DelegatePanel panel = new DelegatePanel(component, description);
        DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_delegate")); //NOI18N
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
//                        public void run(WorkingCopy copy) throws IOException {
//                            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
//                            TreePath path = copy.getTreeUtilities().pathFor(caretOffset);
//                            path = Utilities.getPathElementOfKind(Tree.Kind.CLASS, path);
//                            int idx = GeneratorUtils.findClassMemberIndex(copy, (ClassTree)path.getLeaf(), caretOffset);
//                            ElementHandle<? extends Element> handle = panel.getDelegateField();
//                            VariableElement delegate = handle != null ? (VariableElement)handle.resolve(copy) : null;
//                            ArrayList<ExecutableElement> methods = new ArrayList<ExecutableElement>();
//                            for (ElementHandle<? extends Element> elementHandle : panel.getDelegateMethods())
//                                methods.add((ExecutableElement)elementHandle.resolve(copy));
//                            generateDelegatingMethods(copy, path, delegate, methods, idx);
//                        }
//                    });
//                    GeneratorUtils.guardedCommit(component, mr);
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
        }
    }

    public static ElementNode.Description getAvailableMethods(final JTextComponent component, final CsmField elementHandle) {
//        if (elementHandle.getKind().isField()) {
//            JavaSource js = JavaSource.forDocument(component.getDocument());
//            if (js != null) {
//                try {
//                    final int caretOffset = component.getCaretPosition();
//                    final ElementNode.Description[] description = new ElementNode.Description[1];
//                    js.runUserActionTask(new Task<CompilationController>() {
//
//                        public void run(CompilationController controller) throws IOException {
//                            description[0] = getAvailableMethods(controller, caretOffset, elementHandle);
//                        }
//                    }, true);
//                    return description[0];
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
//        }
        return null;
    }

//    static List<ElementNode.Description> computeUsableFieldsDescriptions(CompilationInfo info, TreePath path) {
//        Elements elements = info.getElements();
//        TypeElement typeElement = (TypeElement) info.getTrees().getElement(path);
//        if (typeElement == null || !typeElement.getKind().isClass()) {
//            return Collections.emptyList();
//        }
//        Trees trees = info.getTrees();
//        Scope scope = trees.getScope(path);
//        Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<Element, List<ElementNode.Description>>();
//        TypeElement cls;
//        while (scope != null && (cls = scope.getEnclosingClass()) != null) {
//            DeclaredType type = (DeclaredType) cls.asType();
//            for (VariableElement field : ElementFilter.fieldsIn(elements.getAllMembers(cls))) {
//                if (!ERROR.contentEquals(field.getSimpleName()) && !field.asType().getKind().isPrimitive() && trees.isAccessible(scope,
//                        field, type)) {
//                    List<ElementNode.Description> descriptions = map.get(field.getEnclosingElement());
//                    if (descriptions == null) {
//                        descriptions = new ArrayList<ElementNode.Description>();
//                        map.put(field.getEnclosingElement(), descriptions);
//                    }
//                    descriptions.add(ElementNode.Description.create(field, null, false, false));
//                }
//            }
//            scope = scope.getEnclosingScope();
//        }
//        List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
//        for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet()) {
//            descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(), false, false));
//        }
//
//        return descriptions;
//    }
        
//    static ElementNode.Description getAvailableMethods(CompilationInfo controller, int caretOffset, final ElementHandle<? extends Element> elementHandle) {
//        VariableElement field = (VariableElement) elementHandle.resolve(controller);
//        if (field.asType().getKind() == TypeKind.DECLARED) {
//            DeclaredType type = (DeclaredType) field.asType();
//            Trees trees = controller.getTrees();
//            Scope scope = controller.getTreeUtilities().scopeFor(caretOffset);
//            Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<Element, List<ElementNode.Description>>();
//            for (ExecutableElement method : ElementFilter.methodsIn(controller.getElements().getAllMembers((TypeElement) type.asElement()))) {
//                if (trees.isAccessible(scope, method, type)) {
//                    List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
//                    if (descriptions == null) {
//                        descriptions = new ArrayList<ElementNode.Description>();
//                        map.put(method.getEnclosingElement(), descriptions);
//                    }
//                    descriptions.add(ElementNode.Description.create(method, null, true, false));
//                }
//            }
//            List<ElementNode.Description> descriptions = new ArrayList<ElementNode.Description>();
//            for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet()) {
//                descriptions.add(ElementNode.Description.create(entry.getKey(), entry.getValue(),
//                        false, false));
//            }
//            if (!descriptions.isEmpty()) {
//                Collections.reverse(descriptions);
//            }
//            return ElementNode.Description.create(descriptions);
//        }
//
//        return null;
//    }
    
//    static void generateDelegatingMethods(WorkingCopy wc, TreePath path, VariableElement delegate, Iterable<? extends ExecutableElement> methods, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            TreeMaker make = wc.getTreeMaker();
//            ClassTree nue = (ClassTree)path.getLeaf();
//            for (ExecutableElement executableElement : methods)
//                nue = make.insertClassMember(nue, index, createDelegatingMethod(wc, delegate, executableElement, (DeclaredType)te.asType()));
//            wc.rewrite(path.getLeaf(), nue);
//        }
//    }
    
//    private static MethodTree createDelegatingMethod(WorkingCopy wc, VariableElement delegate, ExecutableElement method, DeclaredType type) {
//        TreeMaker make = wc.getTreeMaker();
//
//        boolean useThisToDereference = false;
//        String delegateName = delegate.getSimpleName().toString();
//
//        for (VariableElement ve : method.getParameters()) {
//            if (delegateName.equals(ve.getSimpleName().toString())) {
//                useThisToDereference = true;
//                break;
//            }
//        }
//
//        List<ExpressionTree> args = new ArrayList<ExpressionTree>();
//
//        Iterator<? extends VariableElement> it = method.getParameters().iterator();
//        while(it.hasNext()) {
//            VariableElement ve = it.next();
//            args.add(make.Identifier(ve.getSimpleName()));
//        }
//
//        ExpressionTree methodSelect = useThisToDereference ? make.MemberSelect(make.Identifier("this"), delegate.getSimpleName()) : make.Identifier(delegate.getSimpleName()); //NOI18N
//        ExpressionTree exp = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(methodSelect, method.getSimpleName()), args);
//        StatementTree stmt = method.getReturnType().getKind() == TypeKind.VOID ? make.ExpressionStatement(exp) : make.Return(exp);
//        BlockTree body = make.Block(Collections.singletonList(stmt), false);
//        MethodTree prototype = GeneratorUtilities.get(wc).createMethod((DeclaredType)delegate.asType(), method);
//
//        return make.Method(prototype.getModifiers(), prototype.getName(), prototype.getReturnType(), prototype.getTypeParameters(), prototype.getParameters(), prototype.getThrows(), body, (ExpressionTree) prototype.getDefaultValue());
//    }
}
