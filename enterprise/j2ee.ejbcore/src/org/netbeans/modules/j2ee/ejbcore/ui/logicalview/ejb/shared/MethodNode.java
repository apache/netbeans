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

import java.awt.Image;
import javax.swing.Action;
import org.openide.actions.OpenAction;
import org.openide.cookies.OpenCookie;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.Utilities;
import java.util.Collection;
import java.io.IOException;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.j2ee.core.api.support.java.method.MethodModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

// TODO: RETOUCHE listening on sources
public class MethodNode extends AbstractNode implements /*MDRChangeListener,*/ OpenCookie {

    private final ClasspathInfo cpInfo;
    private final String implBean;
    private final FileObject implBeanFO;
    private final MethodModel method;
    private final ComponentMethodViewStrategy cmvs;
    
    public MethodNode(ClasspathInfo cpInfo, MethodModel method, String implBean, ComponentMethodViewStrategy cmvs) {
        this(cpInfo, method, implBean, cmvs, new InstanceContent());
    }
    
    private MethodNode(ClasspathInfo cpInfo, MethodModel method, String implBean, ComponentMethodViewStrategy cmvs, InstanceContent ic) {
        super(Children.LEAF, new AbstractLookup(ic));
        ic.add(this);
        ic.add(method);
//        disableDelegation(FilterNode.DELEGATE_DESTROY);
        this.cpInfo = cpInfo;
        this.method = method;
        this.implBean = implBean;
        this.cmvs = cmvs;
        this.implBeanFO = getFileObject(cpInfo, implBean);
        
        // TODO: listeners - WeakListener was used here before change to JMI, how to use it now?
        // unregister in appropriate point or play with ActiveQueue (openide utilities)
//        ((MDRChangeSource) method).addListener(this);  
    }
    
    public Image getIcon(int type) {
        Image badge = cmvs.getBadge(method);
        Image icon = cmvs.getIcon(method);
        if(badge != null){
            return ImageUtilities.mergeImages(icon, badge, 7,7);
        }
        return icon;
    }

    @Override
    public String getName() {
        return method.getName();
    }
    
    public boolean canDestroy() {
        final boolean[] result = new boolean[] { false };
        try {
            JavaSource javaSource = JavaSource.create(cpInfo);
            javaSource.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    if ("findByPrimaryKey".equals(method.getName())) { //NOI18N
                        if (isEntityBeanMethod()) {
                            result[0] = false;
                        }
                    } else if (method.getModifiers().contains(Modifier.ABSTRACT) &&
                            (isGetter(method) || isSetter(method)) && isEntityBeanMethod()) {
                        result[0] = false;
                    }
                    result[0] = true;
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }
    
    public void destroy() throws IOException {
//        ((MDRChangeSource) method).removeListener(this);
        
        if (implBeanFO != null) {
            cmvs.deleteImplMethod(method, implBean, implBeanFO);
        }
        super.destroy();
    }

//    @Override
//    public void change(MDRChangeEvent e) {
//        // TODO: listeners - filtering of events is possible. Is it needed?
//        fireIconChange();
//    }
    
    public Action[] getActions(boolean context) {
//        List l = new ArrayList(Arrays.asList(getOriginal().getActions(context)));
//        return (Action[]) l.toArray(new Action[l.size()]);
        // XXX method node actions
        return new Action[0];
    }
    
    public Action getPreferredAction() {
        return SystemAction.get(OpenAction.class);
    }
    
    //implementation of OpenCookie
    public void open() {
        cmvs.openMethod(method, implBean, implBeanFO);
    }

    private boolean isEntityBeanMethod() throws IOException {
        
        final boolean[] result = new boolean[] { false };
            JavaSource javaSource = JavaSource.create(cpInfo);
        javaSource.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                Elements elements = controller.getElements();
                TypeElement entityBean = elements.getTypeElement("javax.ejb.EntityBean"); // NOI18N
                TypeElement entityBeanJakarta = elements.getTypeElement("jakarta.ejb.EntityBean"); // NOI18N
                TypeElement implBeanElement = elements.getTypeElement(implBean);
                if (implBeanElement != null && entityBean != null) {
                    result[0] |= controller.getTypes().isSubtype(implBeanElement.asType(), entityBean.asType());
                }
                if (implBeanElement != null && entityBeanJakarta != null) {
                    result[0] |= controller.getTypes().isSubtype(implBeanElement.asType(), entityBeanJakarta.asType());
                }
            }
        }, true);
        return result[0];
        
    }

    private boolean isGetter(MethodModel method) {
        boolean isVoid = "void".equals(method.getReturnType());
        if (method.getName().indexOf("get") == 0 &&
            !isVoid &&
            method.getParameters().size() == 0) {
            return true;
        }
        return false;
    }
    
    private boolean isSetter(MethodModel method) {
        boolean isVoid = "void".equals(method.getReturnType());
        if (method.getName().indexOf("set") == 0 &&
            isVoid &&
            method.getParameters().size() == 1) {
            return true;
        }
        return false;
    }

    private static FileObject getFileObject(ClasspathInfo cpInfo, final String className) {
        final FileObject[] result = new FileObject[1];
        try {
            JavaSource javaSource = JavaSource.create(cpInfo);
            javaSource.runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = controller.getElements().getTypeElement(className);
                    if (typeElement != null) {
                        result[0] = SourceUtils.getFile(ElementHandle.create(typeElement), controller.getClasspathInfo());
                    }
                }
            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }
    
}

