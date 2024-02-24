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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared;

import java.io.IOException;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypesEvent;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.ElementUtilities.ElementAcceptor;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

public abstract class ComponentMethodModel extends Children.Keys<MethodModel> {
    
    private final ClasspathInfo cpInfo;
    private final String homeInterface;
    private Collection<String> interfaces;
    private String implBean;
    private EjbJar ejbModule;
    private final ClassIndexListener classIndexListener;

    public static final String TYPE_CHANGE = "TYPE_CHANGE";
    
    public ComponentMethodModel(ClasspathInfo cpInfo, EjbJar ejbModule, String implBean, String homeInterface) {
        this.cpInfo = cpInfo;
        this.homeInterface = homeInterface;
        this.implBean = implBean;
        this.ejbModule = ejbModule;
        this.classIndexListener = new ClassIndexListenerImpl();
    }
    
    private void updateKeys() {
        interfaces = getInterfaces();
        final List<MethodModel> keys = new ArrayList<MethodModel>();
        try {
            JavaSource javaSource = JavaSource.create(cpInfo);
            javaSource.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(final CompilationController controller) throws IOException {
                    Elements elements = controller.getElements();
                    final ElementUtilities elementUtilities = controller.getElementUtilities();
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    for (String className : getInterfaces()) {
                        final TypeElement intf = elements.getTypeElement(className);

                        if (intf != null) {
                            // from home interface we want only direct methods
                            if (className.equals(homeInterface)) {
                                for (ExecutableElement executableElement : ElementFilter.methodsIn(intf.getEnclosedElements())) {
                                    MethodModel methodModel = MethodModelSupport.createMethodModel(controller, executableElement);
                                    if (methodModel != null){
                                        keys.add(methodModel);
                                    }
                                }
                            } else if(intf.getKind() == ElementKind.CLASS) {
                                if (hasNoInterfaceView(intf)) {
                                    Iterable<? extends Element> methods = elementUtilities.getMembers(intf.asType(), new ElementAcceptor() {
                                        @Override
                                        public boolean accept(Element e, TypeMirror type) {
                                            TypeElement parent = elementUtilities.enclosingTypeElement(e);
                                            return ElementKind.METHOD == e.getKind() &&
                                                e.getEnclosingElement().equals(intf) &&
                                                e.getModifiers().contains(Modifier.PUBLIC);
                                        }
                                    });
                                    for (Element method : methods) {
                                        MethodModel methodModel = MethodModelSupport.createMethodModel(controller, (ExecutableElement) method);
                                        if (methodModel != null && !keys.contains(methodModel)){
                                            keys.add(methodModel);
                                        }
                                    }
                                }
                            } else {
                                Iterable<? extends Element> methods = elementUtilities.getMembers(intf.asType(), new ElementAcceptor() {
                                    @Override
                                    public boolean accept(Element e, TypeMirror type) {
                                        TypeElement parent = elementUtilities.enclosingTypeElement(e);
                                        boolean isInInterface = ElementKind.INTERFACE == parent.getKind();
                                        boolean isFromJavaxEjb = parent.getQualifiedName().toString().startsWith("javax.ejb."); // NOI18N
                                        boolean isFromJakartaEjb = parent.getQualifiedName().toString().startsWith("jakarta.ejb."); // NOI18N
                                        return isInInterface && !isFromJavaxEjb && !isFromJakartaEjb && ElementKind.METHOD == e.getKind();
                                    }
                                });
                                for (Element method : methods) {
                                    MethodModel methodModel = MethodModelSupport.createMethodModel(controller, (ExecutableElement) method);
                                    if (methodModel != null){
                                        keys.add(methodModel);
                                    }
                                }
                            }
                        }
                    }
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setKeys(keys);
            }
        });
    }

    private boolean hasNoInterfaceView(final TypeElement intf) {
        // see chapter 4.9.8 of the EJB 3.1 specification
        try {
            boolean hasNoInterfaceView = ejbModule.getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Boolean>() {
                @Override
                public Boolean run(EjbJarMetadata metadata) throws Exception {
                    Session session = (Session) metadata.findByEjbClass(implBean);
                    if (session != null) {
                        if (session.isLocalBean()) {
                            return true;
                        }

                        if (session.getLocal() == null && session.getRemote()  == null
                                && session.getLocalHome() == null && session.getHome() == null) {
                            for (TypeMirror typeMirror : intf.getInterfaces()) {
                                String ifaceFqn = typeMirror.toString();
                                if (!ifaceFqn.equals("java.io.Serializable") //NOI18N
                                        && !ifaceFqn.equals("java.io.Externalizable") //NOI18N
                                        && !ifaceFqn.startsWith("javax.ejb.")  //NOI18N
                                        && !ifaceFqn.startsWith("jakarta.ejb.")) { //NOI18N
                                    return false;
                                }
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });
            return hasNoInterfaceView;
        } catch (MetadataModelException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return false;
    }

    @Override
    protected void addNotify() {
        if(implBean == null){
            implBean = getImplBean();
        }
        super.addNotify();
        registerListeners();
        updateKeys();
    }
    
    private void registerListeners() {
        try {
            JavaSource javaSource = JavaSource.create(cpInfo);
            javaSource.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    ClassIndex classIndex = controller.getClasspathInfo().getClassIndex();
                    classIndex.addClassIndexListener(classIndexListener);
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    private void removeListeners() {
        try {
            JavaSource javaSource = JavaSource.create(cpInfo);
            javaSource.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    ClassIndex classIndex = controller.getClasspathInfo().getClassIndex();
                    classIndex.removeClassIndexListener(classIndexListener);
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.<MethodModel>emptySet());
        removeListeners();
        super.removeNotify();
    }

    /*
     * Subclasses have to override this if no-arg constructor is used
     */
    protected String getImplBean(){
       return null; 
    }

    public abstract ComponentMethodViewStrategy  createViewStrategy();

    public abstract void fireTypeChange();

    protected abstract Collection<String> getInterfaces();
    
    @Override
    protected Node[] createNodes(MethodModel key) {
        ComponentMethodViewStrategy cmvs = createViewStrategy();
        return new Node[] { new MethodNode(cpInfo, key, implBean, cmvs) };
    }

    private class ClassIndexListenerImpl implements ClassIndexListener {

        @Override
        public void typesAdded(TypesEvent event) {
            handleTypes(event);
        }

        @Override
        public void typesRemoved(TypesEvent event) {
            handleTypes(event);
        }

        @Override
        public void typesChanged(TypesEvent event) {
            handleTypes(event);
        }

        @Override
        public void rootsAdded(RootsEvent event) {
            // ignore
        }

        @Override
        public void rootsRemoved(RootsEvent event) {
            // ignore
        }

        private void handleTypes(TypesEvent event) {
            for (ElementHandle<TypeElement> elementHandle : event.getTypes()) {
                if (interfaces.contains(elementHandle.getQualifiedName())) {
                    fireTypeChange();
                    updateKeys();
                    return;
                }
            }
        }
        
    }
    
}
