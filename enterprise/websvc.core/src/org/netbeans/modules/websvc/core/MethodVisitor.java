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

package org.netbeans.modules.websvc.core;

import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePathScanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.CompilationInfo;

/**
 *
 * @author rico
 */
public class MethodVisitor {
    
    private String operationName;
    private ExecutableElement method;
    private CompilationInfo info;
    private boolean hasWebMethod;
    private boolean hasPublicMethod;
    private List<ExecutableElement> publicMethods;
    
    /** Creates a new instance of MethodLocator */
    public MethodVisitor(CompilationInfo info) {
        this.info = info;
    }
    
    public ExecutableElement getMethod(String operationName){
        this.operationName = operationName;
        new JavaMethodVisitor().scan(info.getCompilationUnit(), null);
        return method;
    }
    
    public boolean hasWebMethod(){
        new WebMethodVisitor().scan(info.getCompilationUnit(), null);
        return hasWebMethod;
    }
    
    public List<ExecutableElement> getPublicMethods(){
        new PublicMethodVisitor().scan(info.getCompilationUnit(), null);
        return publicMethods;
    }
    
    public boolean hasPublicMethod(){
        new PublicMethodVisitor().scan(info.getCompilationUnit(), null);
        return hasPublicMethod;
    }
    
    private class PublicMethodVisitor extends TreePathScanner<Void, Void>{
        public PublicMethodVisitor(){
            publicMethods = new ArrayList<ExecutableElement>();
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if(el != null){
                TypeElement te = (TypeElement) el;
                List<ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                for(ExecutableElement m: methods){
                    if(m.getModifiers().contains(Modifier.PUBLIC)){
                        hasPublicMethod = true;
                        publicMethods.add(m);
                    }
                    
                }
            }
            return null;
        }
    }
    
    private class WebMethodVisitor extends TreePathScanner<Void, Void>{
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if(el != null){
                TypeElement te = (TypeElement) el;
                List<ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                for(ExecutableElement m: methods){
                    if(hasWebMethodAnnotation(m)){
                        hasWebMethod = true;
                        break;
                    }
                }
            }
            return null;
        }
    }
    
    private class JavaMethodVisitor extends TreePathScanner<Void, Void>{
        
        public JavaMethodVisitor(){
            
        }
        
        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if(el != null){
                TypeElement te = (TypeElement) el;
                List<ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                for(ExecutableElement m: methods){
                    if(isMethodFor(m, operationName)){
                        method = m;
                        break;
                    }
                    
                }
            }
            return null;
        }
    }
    
    /**
     *  Determines if the method has a WebMethod annotation and if it does
     *  that the exclude attribute is not set
     */
    private boolean hasWebMethodAnnotation(ExecutableElement method){
        boolean isWebMethod = false;
        List<? extends AnnotationMirror> methodAnnotations = method.getAnnotationMirrors();
        for (AnnotationMirror anMirror : methodAnnotations) {
            if (JaxWsUtils.hasFqn(anMirror, "javax.jws.WebMethod")) {       // NOI18N
                //WebMethod found, set boolean to true
                isWebMethod = true;
                //Now determine if "exclude" is present and set to true
                Map<? extends ExecutableElement, 
                        ? extends AnnotationValue> expressions = anMirror.
                            getElementValues();
                for(Entry<? extends ExecutableElement, 
                        ? extends AnnotationValue> entry: expressions.entrySet()) 
                {
                    if (entry.getKey().getSimpleName().contentEquals("exclude")) { //NOI18N
                        String value = (String)expressions.get(entry.getKey()).
                            getValue();
                        if ("true".equals(value)){
                            isWebMethod = false;
                            break;
                        }
                    }
                }
            }
            break;
        }
        return isWebMethod;
    }
    
    /**
     *  Determines if the WSDL operation is the corresponding Java method
     */
    private boolean isMethodFor(ExecutableElement method, String operationName){
        if(method.getSimpleName().toString().equals(operationName)){
            return true;
        }
        
        //if method name is not the same as the operation name, look at WebMethod annotation
        List<? extends AnnotationMirror> methodAnnotations = 
                method.getAnnotationMirrors();
        for (AnnotationMirror anMirror : methodAnnotations) {
            if (JaxWsUtils.hasFqn(anMirror, "javax.jws.WebMethod")) {   // NOI18N
                Map<? extends ExecutableElement, 
                        ? extends AnnotationValue> expressions = 
                                anMirror.getElementValues();
                for(Entry<? extends ExecutableElement, 
                        ? extends AnnotationValue> entry: expressions.entrySet()) 
                {
                    if (entry.getKey().getSimpleName().
                            contentEquals("operationName"))  //NOI18N
                    {
                        String name = (String)expressions.get(entry.getKey()).
                            getValue();
                        if (operationName.equals(name)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
