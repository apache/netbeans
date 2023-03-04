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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.EntityMethodController;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.ComponentMethodModel;
import static org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.ComponentMethodModel.TYPE_CHANGE;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.ComponentMethodViewStrategy;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.IconVisitor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Chris Webster
 * @author Martin Adamek
 */

public class MethodChildren extends ComponentMethodModel {

    private ComponentMethodViewStrategy mvs;
    private final EntityMethodController controller;
    private final boolean local;
    private final FileObject ddFile;
    private final Entity entity;
    private final EntityChildren entityChildren;
    
    public MethodChildren(EntityChildren entityChildren, ClasspathInfo cpInfo, EjbJar ejbModule, EntityMethodController smc, Entity model, boolean local, FileObject ddFile) {
        super(cpInfo, ejbModule, smc.getBeanClass(), local ? smc.getLocalHome() : smc.getHome());
        controller = smc;
        this.local = local;
        this.ddFile = ddFile;
        this.entity = model;
        this.entityChildren = entityChildren;
        mvs = new EntityStrategy();
    }

    protected Collection<String> getInterfaces() {
        if (local) {
            return controller.getLocalInterfaces();
        } else {
            return controller.getRemoteInterfaces();
        }
    }

    public ComponentMethodViewStrategy createViewStrategy() {
        return mvs;
    }

    @Override
    public void fireTypeChange() {
        entityChildren.propertyChange(new PropertyChangeEvent(this, TYPE_CHANGE, "", "")); //NOI18N
    }

    private class EntityStrategy implements ComponentMethodViewStrategy {
        
        public void deleteImplMethod(MethodModel me, String implClass, FileObject implClassFO) throws IOException {
            String methodName = me.getName();
            if (methodName.startsWith("find") ||     //NOI18N
                methodName.startsWith("ejbSelect")) {   //NOI18N
                controller.deleteQueryMapping(me, ddFile);
            }
            controller.delete(me,local);
        }

        public Image getBadge(MethodModel me) {
            return null;
        }

        public Image getIcon(MethodModel me) {
            IconVisitor iv = new IconVisitor();
            return ImageUtilities.loadImage(iv.getIconUrl(controller.getMethodTypeFromInterface(me)));
        }

        public void openMethod(final MethodModel me, final String implClass, FileObject implClassFO) {
            if (controller.getMethodTypeFromInterface(me).getKind() == MethodType.Kind.FINDER) {
                try {
                    DataObject ddFileDO = DataObject.find(ddFile);
                    OpenCookie c = ddFileDO.getLookup().lookup(OpenCookie.class);
                    if (c != null) {
                        c.open();
                    }
                } catch (DataObjectNotFoundException donf) {
                    Exceptions.printStackTrace(donf);
                }
            }
            final List<ElementHandle<ExecutableElement>> methodHandle = new ArrayList<ElementHandle<ExecutableElement>>();
            try {
                JavaSource javaSource = JavaSource.forFileObject(implClassFO);
                javaSource.runUserActionTask(new Task<CompilationController>() {
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
