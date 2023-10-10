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

package org.netbeans.modules.web.jsf.palette.items;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.j2ee.persistence.wizard.jpacontroller.JpaControllerUtil;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.api.ConfigurationUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelUtilities;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author Po-Ting Wu
 */
public abstract class EntityClass {
    public static final int FORM_TYPE_EMPTY = 0;
    public static final int FORM_TYPE_DETAIL = 1;
    public static final int FORM_TYPE_NEW = 2;
    public static final int FORM_TYPE_EDIT = 3;
    
    protected String variable = "";
    protected String bean = "";
    protected int formType = FORM_TYPE_EMPTY;

    protected JsfLibrariesSupport jsfLibrariesSupport;
    
    protected abstract String getName();

    public String getVariable() {
        return variable;
    }
    
    public void setVariable(String variable) {
        this.variable = variable;
    }
    
    public String getBean() {
        return bean;
    }
    
    public void setBean(String bean) {
        this.bean = bean;
    }
    
    public int getFormType() {
        return formType;
    }
    
    public void setFormType(int formType) {
        this.formType = formType;
    }
   
    public boolean handleTransfer(JTextComponent targetComponent) {
        try {
            jsfLibrariesSupport = PaletteUtils.getJsfLibrariesSupport(targetComponent);
            if (jsfLibrariesSupport == null) {
                return false;
            }
            Caret caret = targetComponent.getCaret();
            int position0 = Math.min(caret.getDot(), caret.getMark());
            int position1 = Math.max(caret.getDot(), caret.getMark());
            int len = targetComponent.getDocument().getLength() - position1;
            boolean containsFView = targetComponent.getText(0, position0).contains("<f:view>")
                    && targetComponent.getText(position1, len).contains("</f:view>");
            String body = createBody(targetComponent, !containsFView);
            JSFPaletteUtilities.insert(body, targetComponent);
            jsfLibrariesSupport.importLibraries(DefaultLibraryInfo.HTML, DefaultLibraryInfo.JSF_CORE);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
            return false;
        }

        return true;
    }
    
    protected abstract String createBody(JTextComponent target, boolean surroundWithFView) throws IOException;
    
    static boolean isId(ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? JpaControllerUtil.guessField(method) : method;
        if (element != null) {
            if (JpaControllerUtil.isAnnotatedWith(element, "jakarta.persistence.Id")
                    || JpaControllerUtil.isAnnotatedWith(element, "jakarta.persistence.EmbeddedId")
                    || JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.Id")
                    || JpaControllerUtil.isAnnotatedWith(element, "javax.persistence.EmbeddedId")
                    ) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    static String getTemporal(ExecutableElement method, boolean isFieldAccess) {
        Element element = isFieldAccess ? JpaControllerUtil.guessField(method) : method;
        if (element != null) {
            AnnotationMirror annotationMirror = JpaControllerUtil.findAnnotation(element, "jakarta.persistence.Temporal"); // NOI18N
            if(annotationMirror == null) {
                annotationMirror = JpaControllerUtil.findAnnotation(element, "javax.persistence.Temporal"); // NOI18N
            }
            if (annotationMirror != null) {
                Collection<? extends AnnotationValue> attributes = annotationMirror.getElementValues().values();
                if (attributes.iterator().hasNext()) {
                    AnnotationValue annotationValue = attributes.iterator().next();
                    if (annotationValue != null) {
                        return annotationValue.getValue().toString();
                    }
                }
            }
        }
        return null;
    }

    static FileObject getFO(JTextComponent target) {
        Document doc = target.getDocument();
        if (doc != null) {
            return NbEditorUtilities.getFileObject(doc);
        }
        return null;
    }
    
    static ClasspathInfo createClasspathInfo(FileObject fileObject) {
        return ClasspathInfo.create(
                ClassPath.getClassPath(fileObject, ClassPath.BOOT),
                ClassPath.getClassPath(fileObject, ClassPath.COMPILE),
                ClassPath.getClassPath(fileObject, ClassPath.SOURCE)
                );
    }
    
    public static boolean isEntityClass(TypeElement typeElement) {
        if (JpaControllerUtil.isAnnotatedWith(typeElement, "jakarta.persistence.Entity")
                || JpaControllerUtil.isAnnotatedWith(typeElement, "javax.persistence.Entity")) {
            return true;
        }
        return false;
    }

    static String getDateTimeFormat(String temporal) {
        if ("DATE".equals(temporal)) {
            return "MM/dd/yyyy";
        } else if ("TIME".equals(temporal)) {
            return "HH:mm:ss";
        } else {
            return "MM/dd/yyyy HH:mm:ss";
        }
    }

    public void addManagedBean(FileObject referenceFO, String beanName, String className) {
        try {
            WebModule webModule = WebModule.getWebModule(referenceFO);
            if (webModule == null) {
                return;
            }

            FileObject cfFileObject = null;
            FileObject documentBase = webModule.getDocumentBase();
            for (String file : JSFConfigUtilities.getConfigFiles(webModule)) {
                cfFileObject = documentBase.getFileObject(file);
                if (cfFileObject != null) {
                    break;
                }
            }

            if (cfFileObject == null) {
                return;
            }

            final FacesConfig facesConfig = ConfigurationUtils.getConfigModel(cfFileObject, true).getRootComponent();
            final ManagedBean managedBean = facesConfig.getModel().getFactory().createManagedBean();

            managedBean.setManagedBeanName(beanName);
            managedBean.setManagedBeanClass(className);
            managedBean.setManagedBeanScope(ManagedBean.Scope.REQUEST);
            // Description description = facesConfig.getModel().getFactory().createDescription();
            // description.setValue("Managed Bean created by JSF Form");
            // managedBean.addDescription(description);

            JSFConfigModelUtilities.doInTransaction(facesConfig.getModel(), new Runnable() {
                @Override
                public void run() {
                    facesConfig.addManagedBean(managedBean);
                }
            });
            JSFConfigModelUtilities.saveChanges(facesConfig.getModel());
        } catch (IllegalStateException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, ex.getMessage(), ex);
        }
    }
}
