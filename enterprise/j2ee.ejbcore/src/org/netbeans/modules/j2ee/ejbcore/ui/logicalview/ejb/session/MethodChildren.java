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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ui.ElementOpen;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModelSupport;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.SessionMethodController;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.ComponentMethodModel;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.ComponentMethodViewStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode.ViewType;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */
public class MethodChildren extends ComponentMethodModel {
    private static final Logger LOG = Logger.getLogger(MethodChildren.class.getName());
    private ComponentMethodViewStrategy mvs;
    private final SessionMethodController controller;
    private final MethodsNode.ViewType viewType;
    private final SessionChildren session;
    
    public MethodChildren(SessionChildren session, ClasspathInfo cpInfo, EjbJar ejbModule, SessionMethodController smc, MethodsNode.ViewType viewType) {
        super(cpInfo, ejbModule, smc.getBeanClass(), getHomeInterface(smc, viewType));
        controller = smc;
        this.viewType = viewType;
        this.session = session;
        mvs = new SessionStrategy();
    }

    private static String getHomeInterface(SessionMethodController smc, MethodsNode.ViewType viewType) {
        if (viewType == ViewType.NO_INTERFACE) {
            return null;
        } else {
            return viewType == ViewType.LOCAL ? smc.getLocalHome() : smc.getHome();
        }
    }

    @Override
    protected Collection<String> getInterfaces() {
        if (viewType == ViewType.LOCAL) {
            return controller.getLocalInterfaces();
        } else if (viewType == ViewType.REMOTE) {
            return controller.getRemoteInterfaces();
        } else {
            return controller.getLocalInterfaces();
        }
    }
    
    @Override
    public ComponentMethodViewStrategy createViewStrategy() {
        return mvs;
    }

    @Override
    public void fireTypeChange() {
        session.propertyChange(new PropertyChangeEvent(this, TYPE_CHANGE, "", "")); //NOI18N
    }

    private class SessionStrategy implements ComponentMethodViewStrategy {
        
        @Override
        public void deleteImplMethod(MethodModel me, String implClass, FileObject implClassFO) throws IOException {
            switch (viewType){
                case NO_INTERFACE:{
                    controller.delete(me);
                    break;
                }
                case LOCAL:
                case REMOTE:{
                    controller.delete(me, viewType == viewType.LOCAL);
                    break;
                }
            }
        }

        @Override
        public Image getBadge(MethodModel me) {
            return null;
        }

        @Override
        public Image getIcon(MethodModel me) {
            IconVisitor iv = new IconVisitor();
            return ImageUtilities.loadImage(iv.getIconUrl(controller.getMethodTypeFromInterface(me)));
        }

        @Override
        public void openMethod(final MethodModel me, final String implClass, FileObject implClassFO) {
            final List<ElementHandle<ExecutableElement>> methodHandle = new ArrayList<ElementHandle<ExecutableElement>>();
            try {
                if (implClassFO == null) {
                    LOG.log(Level.WARNING, "No fileObject found for class={0}.", implClass);
                }
                JavaSource javaSource = JavaSource.forFileObject(implClassFO);
                javaSource.runUserActionTask(new Task<CompilationController>() {
                    @Override
                    public void run(CompilationController controller) throws IOException {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        TypeElement typeElement = controller.getElements().getTypeElement(implClass);
                        for (ExecutableElement executableElement : ElementFilter.methodsIn(typeElement.getEnclosedElements())) {
                            if (MethodModelSupport.isSameMethod(controller, executableElement, me)) {
                                methodHandle.add(ElementHandle.create(executableElement));
                            }
                        }
                    }
                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
            if (methodHandle.size() > 0) {
                ElementOpen.open(implClassFO, methodHandle.get(0));
            }
        }

    }

}
