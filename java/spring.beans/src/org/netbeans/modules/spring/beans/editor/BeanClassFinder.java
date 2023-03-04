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
package org.netbeans.modules.spring.beans.editor;

import org.netbeans.modules.spring.beans.utils.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.*;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.modules.spring.api.Action;
import org.netbeans.modules.spring.api.beans.model.SpringBean;
import org.netbeans.modules.spring.api.beans.model.SpringBeans;
import org.netbeans.modules.spring.api.beans.model.SpringConfigModel;
import org.netbeans.modules.spring.beans.BeansAttributes;
import org.netbeans.modules.spring.java.JavaUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Finds the actual class which is implementing the specified bean. 
 * 
 * Uses a recursive logic as follows
 * 
 * <pre>
 * findImplementationClass(bean) :
 *   type = null;
 * 
 *   if(bean has parent) {
 *      getMergedAttributes(parent); // walk the ancestor chain and find all attributes
 *   }
 * 
 *   if(bean has factory-bean attribute defined) {
 *      type = findImplementationClass(factory-bean);
 *   } else if(bean has class attribute defined) {
 *      type = class attrib value;
 *   } else if(bean has parent attribute) {
 *      type = findImplementationClass(parent);
 *   }
 * 
 *   if(bean has factory-method) {
 *      type = findFactoryMethodReturnType(type, factory-method-name);
 *   }
 * 
 *   return type;
 * </pre>
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class BeanClassFinder {

    private FileObject fileObject;
    private Set<String> walkedBeanNames;  
    private String startBeanName;
    private SpringBean startBean;

    public BeanClassFinder(Map<String, String> beanAttribs, FileObject fileObject) {
        this(fileObject, SpringXMLConfigEditorUtils.getMergedBean(beanAttribs, fileObject), getBeanIdOrName(beanAttribs));
    }
    
    public BeanClassFinder(SpringBean bean, FileObject fileObject) {
        this(fileObject, SpringXMLConfigEditorUtils.getMergedBean(bean, fileObject), getBeanIdOrName(bean));
    }
    
    private BeanClassFinder(FileObject fileObject, SpringBean startBean, String startBeanName) {
        this.fileObject = fileObject;
        this.startBean = startBean;
        this.startBeanName = startBeanName;
        this.walkedBeanNames = new HashSet<String>();
    }

    public String findImplementationClass(boolean immediateAction) {
        walkedBeanNames.add(startBeanName);
        return findImplementationClass(startBean, immediateAction);
    }

    private String findImplementationClass(SpringBean logicalBean, boolean immediateAction) {
        String implClass = null;
        if (logicalBean == null) {
            return null;
        }

        boolean staticFlag = false;
        
        if (StringUtils.hasText(logicalBean.getFactoryBean())) {
            implClass = findImplementationClass(logicalBean.getFactoryBean(), immediateAction);
            staticFlag = false;
        } else if (StringUtils.hasText(logicalBean.getClassName())) {
            implClass = logicalBean.getClassName();
            staticFlag = true;
        }

        if(logicalBean.getFactoryMethod() != null && implClass != null) {
            implClass = getFactoryMethodReturnTypeName(
                    implClass, logicalBean.getFactoryMethod(), staticFlag, immediateAction);
        }
        
        return implClass;
    }

    private String findImplementationClass(final String beanName, final boolean immediateAction) {
        if(walkedBeanNames.contains(beanName)) {
            // possible circular dep - bail out
            return null;
        }
        
        final String[] clazz = {null};
        try {
            SpringConfigModel model = SpringConfigModel.forFileObject(fileObject);
            if (model == null) {
                return null;
            }

            model.runReadAction(new Action<SpringBeans>() {

                public void run(SpringBeans springBeans) {
                    SpringBean bean = springBeans.findBean(beanName);
                    bean = SpringXMLConfigEditorUtils.getMergedBean(bean, fileObject);
                    if(bean == null) {
                        return;
                    }
                    
                    String beanName = getBeanIdOrName(bean);
                    walkedBeanNames.add(beanName);
                    clazz[0] = findImplementationClass(bean, immediateAction);
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return clazz[0];
    }

    /**
     * Tries to search for a factory method with a specified name on the class.
     * Due to current limitations of the model, if more than one factory method
     * is found, we are not able to disambiguate based on the parameter types of
     * the factory method.
     *
     * Hence we return a null in such a scenario.
     *
     */
    @NbBundle.Messages("title.class.resolver=Resolving Class")
    private String getFactoryMethodReturnTypeName(final String implClass, final String factoryMethodName,
            final boolean staticFlag, final boolean immediateAction) {
        final String[] retVal = {null};

        if (!StringUtils.hasText(factoryMethodName)) {
            return null;
        }

        JavaSource js = JavaUtils.getJavaSource(fileObject);
        if (js == null) {
            return null;
        }

        ClassResolver classResolver = new ClassResolver(js, implClass, factoryMethodName, staticFlag, retVal);
        runClassResolverAsUserActionTask(js, classResolver);
        if (!immediateAction && !classResolver.wasClassResolved() && SourceUtils.isScanInProgress()) {
            ScanDialog.runWhenScanFinished(classResolver, Bundle.title_class_resolver());
        }

        return retVal[0];
    }

    private void runClassResolverAsUserActionTask(JavaSource javaSource, ClassResolver classResolver) {
        try {
            javaSource.runUserActionTask(classResolver, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private class ClassResolver implements Runnable, CancellableTask<CompilationController> {

        private final AtomicBoolean wasClassResolved = new AtomicBoolean(false);
        private final JavaSource javaSource;
        private final String implClass;
        private final String factoryMethodName;
        private final boolean staticFlag;
        private final String[] resolvedClass;

        public ClassResolver(JavaSource javaSource, String implClass, String factoryMethodName,
                boolean staticFlag, String[] resolvedClass) {
            this.javaSource = javaSource;
            this.implClass = implClass;
            this.factoryMethodName = factoryMethodName;
            this.staticFlag = staticFlag;
            this.resolvedClass = resolvedClass;
        }

        @Override
        public void run() {
            runClassResolverAsUserActionTask(javaSource, this);
        }

        @Override
        public void cancel() {
        }

        @Override
        public void run(CompilationController controller) throws Exception {
            controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
            if (implClass == null) {
                return;
            }
            TypeElement te = JavaUtils.findClassElementByBinaryName(implClass, controller);
            if (te == null) {
                return;
            }

            FactoryMethodFinder factoryMethodFinder = new FactoryMethodFinder(
                    te, factoryMethodName, staticFlag, controller.getElementUtilities());
            List<ExecutableElement> methods = factoryMethodFinder.findMethods();
            if (methods.size() != 1) {
                return;
            }

            ExecutableElement method = methods.get(0);
            if (method.getReturnType().getKind() != TypeKind.DECLARED) {
                return;
            }

            wasClassResolved.set(true);
            DeclaredType dt = (DeclaredType) method.getReturnType();
            resolvedClass[0] = ElementUtilities.getBinaryName((TypeElement) dt.asElement());
        }

        public boolean wasClassResolved() {
            return wasClassResolved.get();
        }
    }

    private static class FactoryMethodFinder {
        private TypeElement te;
        private String factoryMethodName;
        private boolean staticFlag;
        private ElementUtilities eu;

        public FactoryMethodFinder(TypeElement te, String factoryMethodName, boolean staticFlag, ElementUtilities eu) {
            this.te = te;
            this.factoryMethodName = factoryMethodName;
            this.staticFlag = staticFlag;
            this.eu = eu;
        }
        
        public List<ExecutableElement> findMethods() {
            Iterable<? extends Element>  list = eu.getMembers(te.asType(), new ElementUtilities.ElementAcceptor() {

                public boolean accept(Element e, TypeMirror type) {
                    if (e.getKind() == ElementKind.METHOD) {
                        TypeElement te = (TypeElement) e.getEnclosingElement();
                        if (te.getQualifiedName().contentEquals("java.lang.Object")) { // NOI18N
                            return false;
                        }

                        // match name
                        if (!e.getSimpleName().toString().equals(factoryMethodName)) {
                            return false;
                        }

                        ExecutableElement method = (ExecutableElement) e;
                        // match static
                        boolean isStatic = method.getModifiers().contains(Modifier.STATIC);
                        if (isStatic != staticFlag) {
                            return false;
                        }
                        return true;
                    }
                    
                    return false;
                }
            });
            
            List<ExecutableElement> retList = new ArrayList<ExecutableElement>();
            for(Element e : list) {
                ExecutableElement ee = (ExecutableElement) e;
                retList.add(ee);
            }
            
            return retList;
        }
    }
    
    private static String getBeanIdOrName(SpringBean bean) {
        if(bean.getId() != null) {
            return bean.getId();
        }
        
        if(bean.getNames().size() > 0) {
            return bean.getNames().get(0);
        }
        
        return null;
    }
    
    private static String getBeanIdOrName(Map<String, String> beanAttribs) {
        String name = beanAttribs.get(BeansAttributes.ID);
        if(name != null) {
            return name;
        }
        
        name = beanAttribs.get(BeansAttributes.NAME);
        if(StringUtils.hasText(name)) {
            name = StringUtils.tokenize(name, SpringXMLConfigEditorUtils.BEAN_NAME_DELIMITERS).get(0);
        }
        
        return name;
    }
}
