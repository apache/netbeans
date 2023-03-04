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

package org.netbeans.modules.websvc.rest.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationController;

import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Peter Liu
 */
public class RestServiceDescriptionImpl extends PersistentObject implements RestServiceDescription {
    
    private String name;
    private String uriTemplate;
    private String className;
    private Map<String, RestMethodDescriptionImpl> methods;
    private boolean isRest;
  
    public RestServiceDescriptionImpl(AnnotationModelHelper helper, TypeElement typeElement) {
        super(helper, typeElement);
        
        this.name = typeElement.getSimpleName().toString();
        this.uriTemplate = Utils.getUriTemplate(typeElement);
        this.className = typeElement.getQualifiedName().toString();
        this.isRest = true;
  
        initMethods(typeElement);
    }
    
    
    private void initMethods(TypeElement typeElement) {
        methods = new HashMap<String, RestMethodDescriptionImpl>();
        
        for (Element element : typeElement.getEnclosedElements()) {
            if (element!= null && element.getKind() == ElementKind.METHOD) {
                addMethod(element);
            }
        }
        
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !"java.lan.Object".equals(superclass.toString())) {
            CompilationController controller = getHelper().getCompilationController();
            TypeElement superClassEl = (TypeElement)controller.getTypes().asElement( superclass );
            for (Element element : superClassEl.getEnclosedElements()) {
                if (element!= null && element.getKind() == ElementKind.METHOD) {
                    addMethod(element);
                }
            }
        }

    }
    
    public String getName() {
        return name;
    }
    
    public String getUriTemplate() {
        return uriTemplate;
    }
    
    public List<RestMethodDescription> getMethods() {
        List<RestMethodDescription> list = new ArrayList<RestMethodDescription>();
        
        for (RestMethodDescriptionImpl method : methods.values()) {
            list.add((RestMethodDescription) method);
        }
        
        return list;
    }
 
    public String getClassName() {
        return className;
    }
 
    public boolean isRest() {
        return isRest;
    }
    
    public Status refresh(TypeElement typeElement) {
        if (typeElement.getKind() == ElementKind.INTERFACE) {
            return Status.REMOVED;
        }
        
        boolean isRest = false;
        boolean isModified = false;
        
        if (Utils.hasUriTemplate(typeElement)) {
            isRest = true;
        }
        
        String newValue = typeElement.getSimpleName().toString();
        
        
        // Refresh the resource name.
        if (this.name != newValue) {
            this.name = newValue;
            isModified = true;
        }
        
        // Refresh the class name.
        newValue = typeElement.getQualifiedName().toString();
        if (this.className != newValue) {
            this.className = newValue;
            isModified = true;
        }
        
        // Refresh the uriTemplate.
        newValue = Utils.getUriTemplate(typeElement);
        if (!this.uriTemplate.equals(newValue)) {
            this.uriTemplate = newValue;
            isModified = true;
        }
        
        Map<String, RestMethodDescriptionImpl> prevMethods = methods;
        methods = new HashMap<String, RestMethodDescriptionImpl>();
        
        // Refresh all the methods.
        boolean modified = checkForHTTPMethods(prevMethods, typeElement);
        if (modified) {
            isModified = true;
        }
        
        // check Superclass
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass != null && !"java.lang.Object".equals(superclass.toString())) {
            CompilationController controller = getHelper().getCompilationController();
            TypeElement superClassEl = (TypeElement)controller.getTypes().asElement( superclass );
            if (superClassEl != null) {
                modified = checkForHTTPMethods(prevMethods, superClassEl);
                if (modified) {
                    isModified = true;
                }
            }
        }
        
        if (methods.size() != prevMethods.size()) {
            isModified = true;
        }
        
        if (!isRest) {
            this.isRest = false;
            return Status.REMOVED;
        }
        
        if (isModified) {
            return Status.MODIFIED;
        }
        
        return Status.UNMODIFIED;
    }
    
    private boolean checkForHTTPMethods(Map<String, RestMethodDescriptionImpl> prevMethods, TypeElement typeElement) {
        boolean modified = false;
        for (Element element : typeElement.getEnclosedElements()) {
            if (element!= null && element.getKind() == ElementKind.METHOD) {
                String methodName = element.getSimpleName().toString();
                
                RestMethodDescriptionImpl method = prevMethods.get(methodName);
                
                if (method != null) {
                    Status status = method.refresh(element);
                    
                    switch (status) {
                    case REMOVED:
                        if (addMethod(element)) {
                            isRest = true;
                        }
                        modified = true;
                        break;
                    case MODIFIED:
                        isRest = true;
                        modified = true;
                        methods.put(methodName, method);
                        break;
                    case UNMODIFIED:
                        isRest = true;
                        methods.put(methodName, method);
                        break;
                    }
                } else {
                    if (addMethod(element)) {
                        isRest = true;
                        modified = true;
                    }
                }
            }
        }
        return modified;
    }
    
    @Override
    public FileObject getFile(){
        return SourceUtils.getFile(getTypeElementHandle(), getHelper().getClasspathInfo());
    }
    
    private boolean addMethod(Element element) {
        RestMethodDescriptionImpl method = RestMethodDescriptionFactory.create(element);
        
        if (method != null) {
            methods.put(element.getSimpleName().toString(), method);
            
            return true;
        }
        return false;
    }
    
    public String toString() {
        return name + "[" + uriTemplate + "]"; //NOI18N
    }
}
