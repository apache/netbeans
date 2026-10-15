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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.ui.ElementNode;
import org.netbeans.modules.java.editor.codegen.ui.ImplementOverridePanel;
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
public class ImplementOverrideMethodGenerator implements CodeGenerator {

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
            TypeElement typeElement = (TypeElement)controller.getTrees().getElement(path);
            if (typeElement == null || typeElement.getKind() == ElementKind.ANNOTATION_TYPE) {
                return ret;
            }
            ElementUtilities eu = controller.getElementUtilities();
            if (typeElement.getKind().isClass() || typeElement.getKind().isInterface() && SourceVersion.RELEASE_8.compareTo(controller.getSourceVersion()) <= 0) {
                ElementNode.Description root = getImplementDescriptions(controller, typeElement);
                if (root != null) {
                    ret.add(new ImplementOverrideMethodGenerator(component, ElementHandle.create(typeElement), root, true));
                }
            }
            if (typeElement.getKind().isClass() || typeElement.getKind().isInterface()) {
                Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<>();
                ArrayList<Element> orderedElements = new ArrayList<>();
                for (ExecutableElement method : eu.findOverridableMethods(typeElement)) {
                    List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
                    if (descriptions == null) {
                        descriptions = new ArrayList<>();
                        Element e = method.getEnclosingElement();
                        map.put(e, descriptions);
                        if( !orderedElements.contains( e ) ) {
                            orderedElements.add( e );
                        }
                    }
                    descriptions.add(ElementNode.Description.create(controller, method, null, true, false));
                }
                List<ElementNode.Description> overrideDescriptions = new ArrayList<>();
                for (Element e : orderedElements) {
                    overrideDescriptions.add(ElementNode.Description.create(controller, e, map.get( e ), false, false));
                }
                if (!overrideDescriptions.isEmpty()) {
                    ret.add(new ImplementOverrideMethodGenerator(component, ElementHandle.create(typeElement), ElementNode.Description.create(overrideDescriptions), false));
                }
            }
            return ret;
        }
    }
    
    private static final List<ElementHandle<? extends Element>> NOT_READY = new ArrayList<>();

    /**
     * For testing purposes only
     */
    static BiFunction<CompilationInfo, TypeElement, List<ElementHandle<? extends Element>>> testOverrideMethodsSelection = null;
    
    /**
     * Returns callback which fills the list of elements to implement. The callback will execute in Swing thread upon query to the {@link Future#get()}.
     * @param controller
     * @param typeElement
     * @return 
     */
    public static Future< List<ElementHandle<? extends Element>> > selectMethodsToImplement(final CompilationInfo controller, final TypeElement typeElement) {
        final ElementNode.Description root = getImplementDescriptions(controller, typeElement);
        if (root == null) {
            return null;
        }
        return new RunnableFuture< List<ElementHandle<? extends Element>> >() {
            private boolean cancelled;
            private  List<ElementHandle<? extends Element>>  result = NOT_READY;
            
            @Override
            public synchronized boolean cancel(boolean mayInterruptIfRunning) {
                cancelled = true;
                return true;
            }

            @Override
            public synchronized boolean isCancelled() {
                return cancelled;
            }

            @Override
            public synchronized boolean isDone() {
                return cancelled || result != NOT_READY;
            }

            @Override
            public  List<ElementHandle<? extends Element>>  get() throws InterruptedException, ExecutionException {
                boolean ok = true;
                synchronized (this) {
                    if (isDone()) {
                        return result == NOT_READY ? null : result;
                    }
                }
                if (testOverrideMethodsSelection != null || SwingUtilities.isEventDispatchThread()) {
                    run();
                    return result;
                } else {
                    try {
                        SwingUtilities.invokeAndWait(this);
                    } catch (InvocationTargetException ex) {
                        ok = false;
                    }
                }
                synchronized (this) {
                    if (!ok) {
                        cancelled = true;
                        return null;
                    }
                    return result;
                }
            }
            
            @Override
            public void run() {
                List<ElementHandle<? extends Element>> tmp;
                if (testOverrideMethodsSelection != null) {
                    tmp = testOverrideMethodsSelection.apply(controller, typeElement);
                } else {
                    tmp = displaySelectionDialog(root, true);
                }
                synchronized (this) {
                    this.result = tmp;
                }
            }

            @Override
            public  List<ElementHandle<? extends Element>>  get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return get(); // sorry, no timeout
            }
        };
    }
    
    private static ElementNode.Description getImplementDescriptions(CompilationInfo controller, TypeElement typeElement) {
        ElementUtilities eu = controller.getElementUtilities();
        Map<Element, List<ElementNode.Description>> map = new LinkedHashMap<>();
        for (ExecutableElement method : eu.findUnimplementedMethods(typeElement, true)) {
            List<ElementNode.Description> descriptions = map.get(method.getEnclosingElement());
            if (descriptions == null) {
                descriptions = new ArrayList<>();
                map.put(method.getEnclosingElement(), descriptions);
            }
            boolean mustImplement = !method.getModifiers().contains(Modifier.DEFAULT);
            descriptions.add(ElementNode.Description.create(controller, method, null, true, mustImplement));
        }
        List<ElementNode.Description> implementDescriptions = new ArrayList<>();
        for (Map.Entry<Element, List<ElementNode.Description>> entry : map.entrySet()) {
            implementDescriptions.add(ElementNode.Description.create(controller, entry.getKey(), entry.getValue(), false, false));
        }
        return  implementDescriptions.isEmpty() ? null : ElementNode.Description.create(implementDescriptions);
    }

    private final JTextComponent component;
    private final ElementHandle<TypeElement> handle;
    private final ElementNode.Description description;
    private final boolean isImplement;
    
    /** Creates a new instance of OverrideMethodGenerator */
    private ImplementOverrideMethodGenerator(JTextComponent component, ElementHandle<TypeElement> handle, ElementNode.Description description, boolean isImplement) {
        this.component = component;
        this.handle = handle;
        this.description = description;
        this.isImplement = isImplement;
    }

    @Override
    public String getDisplayName() {
        return org.openide.util.NbBundle.getMessage(ImplementOverrideMethodGenerator.class, isImplement ? "LBL_implement_method" : "LBL_override_method"); //NOI18N
    }
    
    private static List<ElementHandle<? extends Element>> displaySelectionDialog(ElementNode.Description root, boolean isImplement) {
        final ImplementOverridePanel panel = new ImplementOverridePanel(root, isImplement);
        final DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, 
                NbBundle.getMessage(ConstructorGenerator.class, isImplement ?  "LBL_generate_implement" : "LBL_generate_override")); //NOI18N
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                List<ElementHandle<? extends Element>> meths = panel.getSelectedMethods();
                dialogDescriptor.setValid(meths != null && !meths.isEmpty());
            }
        });
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        dialog.setVisible(true);
        if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue()) {
            return null;
        }
        return panel.getSelectedMethods();
    }

    @Override
    public void invoke() {
        final int caretOffset = component.getCaretPosition();
        final List<ElementHandle<? extends Element>> methodList = displaySelectionDialog(description, isImplement);
        if (methodList != null) {
            JavaSource js = JavaSource.forDocument(component.getDocument());
            if (js != null) {
                try {
                    ModificationResult mr = js.runModificationTask(new Task<WorkingCopy>() {
                        @Override
                        public void run(WorkingCopy copy) throws IOException {
                            copy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                            Element e = handle.resolve(copy);
                            TreePath path = e != null ? copy.getTrees().getPath(e) : copy.getTreeUtilities().pathFor(caretOffset);
                            path = copy.getTreeUtilities().getPathElementOfKind(TreeUtilities.CLASS_TREE_KINDS, path);
                            if (path == null) {
                                String message = NbBundle.getMessage(ImplementOverrideMethodGenerator.class, "ERR_CannotFindOriginalClass"); //NOI18N
                                org.netbeans.editor.Utilities.setStatusBoldText(component, message);
                            } else {
                                ArrayList<ExecutableElement> methodElements = new ArrayList<>();
                                for (ElementHandle<? extends Element> elementHandle : methodList) {
                                    ExecutableElement methodElement = (ExecutableElement)elementHandle.resolve(copy);
                                    if (methodElement != null) {
                                        methodElements.add(methodElement);
                                    }
                                }
                                if (!methodElements.isEmpty()) {
                                    if (isImplement) {
                                        GeneratorUtils.generateAbstractMethodImplementations(copy, path, methodElements, caretOffset);
                                    } else {
                                        GeneratorUtils.generateMethodOverrides(copy, path, methodElements, caretOffset);
                                    }
                                }
                            }
                        }
                    });
                    GeneratorUtils.guardedCommit(component, mr);
                    int span[] = mr.getSpan("methodBodyTag"); // NOI18N
                    if(span != null) {
                        component.setSelectionStart(span[0]);
                        component.setSelectionEnd(span[1]);
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
