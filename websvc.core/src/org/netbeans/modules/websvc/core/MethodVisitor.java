/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
