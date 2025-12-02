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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Scope;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.awt.Dialog;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
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
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ScanUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.java.editor.codegen.ui.DelegatePanel;
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
public class DelegateMethodGenerator implements CodeGenerator {

    private static final String ERROR = "<error>"; //NOI18N
    private static final Logger log = Logger.getLogger(DelegateMethodGenerator.class.getName());

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
            List<ElementNode.Description> descriptions = computeUsableFieldsDescriptions(controller, path);
            if (!descriptions.isEmpty()) {
                Collections.reverse(descriptions);
                ret.add(new DelegateMethodGenerator(component, ElementHandle.create(typeElement), ElementNode.Description.create(descriptions)));
            }
            return ret;
        }
    }

    private final JTextComponent component;
    private final ElementHandle<TypeElement> handle;
    private final ElementNode.Description description;
    
    /** Creates a new instance of DelegateMethodGenerator */
    private DelegateMethodGenerator(JTextComponent component, ElementHandle<TypeElement> handle, ElementNode.Description description) {
        this.component = component;
        this.handle = handle;
        this.description = description;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_delegate_method"); //NOI18N
    }

    @Override
    public void invoke() {
        final DelegatePanel panel = new DelegatePanel(component, handle, description);
        final DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_delegate")); //NOI18N
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                List<ElementHandle<? extends Element>> meths = panel.getDelegateMethods();
                dialogDescriptor.setValid(meths != null && !meths.isEmpty());
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() == dialogDescriptor.getDefaultValue()) {
            JavaSource js = JavaSource.forDocument(component.getDocument());
            ElementHandle<? extends Element> delegateField = panel.getDelegateField();
            if (delegateField != null && delegateField.getKind() == ElementKind.CLASS) {//#165261: exit when just class node selected
                return;
            }
            if (js != null) {
                try {
                    final int caretOffset = component.getCaretPosition();
                    ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                        @Override
                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element e = handle.resolve(copy);
                            TreePath path = e != null ? copy.getTrees().getPath(e) : copy.getTreeUtilities().pathFor(caretOffset);
                            path = copy.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
                            if (path == null) {
                                String message = NbBundle.getMessage(DelegateMethodGenerator.class, "ERR_CannotFindOriginalClass"); //NOI18N
                                Utilities.setStatusBoldText(component, message);
                            } else {
                                ElementHandle<? extends Element> handle = panel.getDelegateField();
                                VariableElement delegate = handle != null ? (VariableElement)handle.resolve(copy) : null;
                                ArrayList<ExecutableElement> methods = new ArrayList<>();
                                for (ElementHandle<? extends Element> elementHandle : panel.getDelegateMethods()) {
                                    methods.add((ExecutableElement)elementHandle.resolve(copy));
                                }
                                generateDelegatingMethods(copy, path, delegate, methods, caretOffset);
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

    public static ElementNode.Description getAvailableMethods(final JTextComponent component, final ElementHandle<? extends TypeElement> typeElementHandle, final ElementHandle<? extends VariableElement> fieldHandle) {
        if (fieldHandle.getKind().isField()) {
            final JavaSource js = JavaSource.forDocument(component.getDocument());
            if (js != null) {
                final int caretOffset = component.getCaretPosition();
                final ElementNode.Description[] description = new ElementNode.Description[1];
                final AtomicBoolean cancel = new AtomicBoolean();
                BaseProgressUtils.runOffEventDispatchThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ScanUtils.waitUserActionTask(js, new Task<CompilationController>() {
                                @Override
                                public void run(CompilationController controller) throws IOException {
                                    if (controller.getPhase().compareTo(Phase.RESOLVED) < 0) {
                                            Phase phase = controller.toPhase(Phase.RESOLVED);
                                        if (phase.compareTo(Phase.RESOLVED) < 0) {
                                            if (log.isLoggable(Level.SEVERE)) {
                                                log.log(Level.SEVERE, "Cannot reach required phase. Leaving without action.");
                                            }
                                            return;
                                        }
                                    }
                                    if (cancel.get()) {
                                        return;
                                    }
                                    description[0] = getAvailableMethods(controller, caretOffset, typeElementHandle, fieldHandle);
                                }
                            });
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                }, NbBundle.getMessage(DelegateMethodGenerator.class, "LBL_Get_Available_Methods"), cancel, false);
                cancel.set(true);
                return description[0];
            }
        }
        return null;
    }

    static List<ElementNode.Description> computeUsableFieldsDescriptions(CompilationInfo info, TreePath path) {
        Elements elements = info.getElements();
        Trees trees = info.getTrees();
        Scope scope = trees.getScope(path);
        Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<>();
        TypeElement cls;
        while (scope != null && (cls = scope.getEnclosingClass()) != null) {
            DeclaredType type = (DeclaredType) cls.asType();
            for (VariableElement field : ElementFilter.fieldsIn(elements.getAllMembers(cls))) {
                TypeMirror fieldType = field.asType();
                if (!ERROR.contentEquals(field.getSimpleName()) && !fieldType.getKind().isPrimitive() && fieldType.getKind() != TypeKind.ARRAY
                        && (fieldType.getKind() != TypeKind.DECLARED || ((DeclaredType)fieldType).asElement() != cls) && trees.isAccessible(scope,
                        field, type)) {
                    List<ElementNode.Description> descriptions = map.get(field.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<>();
                        map.put(field.getEnclosingElement(), descriptions);
                    }
                    descriptions.add(ElementNode.Description.create(info, field, null, false, false));
                }
            }
            scope = scope.getEnclosingScope();
        }
        List<ElementNode.Description> descriptions = new ArrayList<>();
        for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet()) {
            descriptions.add(ElementNode.Description.create(info, entry.getKey(), entry.getValue(), false, false));
        }
        
        return descriptions;
    }
        
    static ElementNode.Description getAvailableMethods(CompilationInfo controller, int caretOffset, final ElementHandle<? extends TypeElement> typeElementHandle, final ElementHandle<? extends VariableElement> fieldHandle) {
        final TypeElement origin = typeElementHandle.resolve(controller);
        final VariableElement field = ScanUtils.checkElement(controller, fieldHandle.resolve(controller));
        assert origin != null && field != null;
        final ElementUtilities eu = controller.getElementUtilities();
        final Trees trees = controller.getTrees();
        final Scope scope = controller.getTreeUtilities().scopeFor(caretOffset);
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror type) {
                if (e.getKind() == ElementKind.METHOD && trees.isAccessible(scope, e, (DeclaredType)type)) {
                    Element impl = eu.getImplementationOf((ExecutableElement)e, origin);
                    return impl == null || (!impl.getModifiers().contains(Modifier.FINAL) && impl.getEnclosingElement() != origin);                    
                }
                return false;
            }
        };
        Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<>();
        for (ExecutableElement method : ElementFilter.methodsIn(eu.getMembers(field.asType(), acceptor))) {
            List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
            if (descriptions == null) {
                descriptions = new ArrayList<>();
                map.put(method.getEnclosingElement(), descriptions);
            }
            descriptions.add(ElementNode.Description.create(controller, method, null, true, false));
        }
        List<ElementNode.Description> descriptions = new ArrayList<>();
        for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet()) {
            descriptions.add(ElementNode.Description.create(controller, entry.getKey(), entry.getValue(),
                    false, false));
        }
        if (!descriptions.isEmpty()) {
            Collections.reverse(descriptions);
        }
        return ElementNode.Description.create(descriptions);
    }
    
    public static void generateDelegatingMethods(WorkingCopy wc, TreePath path, VariableElement delegate, Iterable<? extends ExecutableElement> methods, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            List<Tree> members = new ArrayList<>();
            for (ExecutableElement executableElement : methods) {
                members.add(createDelegatingMethod(wc, delegate, executableElement, (DeclaredType)te.asType()));
            }
            wc.rewrite(clazz, GeneratorUtils.insertClassMembers(wc, clazz, members, offset));
        }        
    }
    
    private static MethodTree createDelegatingMethod(WorkingCopy wc, VariableElement delegate, ExecutableElement method, DeclaredType type) {
        TreeMaker make = wc.getTreeMaker();
        
        boolean useThisToDereference = false;
        Name delegateName = delegate.getSimpleName();
        
        for (VariableElement ve : method.getParameters()) {
            if (delegateName.contentEquals(ve.getSimpleName())) {
                useThisToDereference = true;
                break;
            }
        }
        
        List<ExpressionTree> args = new ArrayList<>();
        
        Iterator<? extends VariableElement> it = method.getParameters().iterator();
        while(it.hasNext()) {
            VariableElement ve = it.next();
            args.add(make.Identifier(ve.getSimpleName()));
        }

        ExpressionTree methodSelect = method.getModifiers().contains(Modifier.STATIC)
                ? make.QualIdent(method.getEnclosingElement())
                : useThisToDereference ? make.MemberSelect(make.Identifier("this"), delegateName) : make.Identifier(delegateName); //NOI18N
        ExpressionTree exp = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(methodSelect, method.getSimpleName()), args);
        StatementTree stmt = method.getReturnType().getKind() == TypeKind.VOID ? make.ExpressionStatement(exp) : make.Return(exp);
        BlockTree body = make.Block(Collections.singletonList(stmt), false);
        
        MethodTree prototype = GeneratorUtilities.get(wc).createMethod(delegate.asType().getKind() == TypeKind.DECLARED ? (DeclaredType)delegate.asType() : type, method);
        ModifiersTree mt = prototype.getModifiers();
        try {
            if (wc.getElements().getTypeElement("java.lang.Override") != null //NOI18N
                    && wc.getSourceVersion().compareTo(SourceVersion.RELEASE_5) >= 0
                    && wc.getTypes().asMemberOf(type, method) != null) {
                //add @Override annotation:
                mt = make.addModifiersAnnotation(mt, make.Annotation(make.Identifier("Override"), Collections.<ExpressionTree>emptyList())); //NOI18N
            }
        } catch (IllegalArgumentException iae) {}
        
        return make.Method(mt, prototype.getName(), prototype.getReturnType(), prototype.getTypeParameters(), prototype.getParameters(), prototype.getThrows(), body, (ExpressionTree) prototype.getDefaultValue());
    }
}
