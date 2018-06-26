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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.beans.impl.model.results;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.impl.model.WebBeansModelProviderImpl;


/**
 * @author ads
 *
 */
public class ResultImpl extends BaseResult implements DependencyInjectionResult.ResolutionResult {
    
    private static final String ALTERNATIVE = 
        "javax.enterprise.inject.Alternative";   // NOI18N
    
    public ResultImpl( VariableElement var, TypeMirror elementType ,
            Set<TypeElement> declaredTypes, 
            Set<Element> productionElements,
            AnnotationModelHelper helper ) 
    {
        super( var, elementType );
        myDeclaredTypes = declaredTypes;
        myProductions = productionElements;
        myHelper = helper;
    }
    
    public ResultImpl( VariableElement var, TypeMirror elementType ,
            TypeElement declaredType, AnnotationModelHelper helper ) 
    {
        super( var, elementType );
        myDeclaredTypes =Collections.singleton( declaredType );
        myProductions = Collections.emptySet();
        myHelper = helper;
    }
    
    public ResultImpl( VariableElement var, TypeMirror elementType ,
            AnnotationModelHelper helper ) 
    {
        super( var, elementType );
        myDeclaredTypes =Collections.emptySet();
        myProductions = Collections.emptySet();
        myHelper = helper;
    }

    public Set<TypeElement> getTypeElements() {
        return myDeclaredTypes;
    }
    
    public Set<Element> getProductions() {
        return myProductions;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result#getKind()
     */
    @Override
    public ResultKind getKind() {
        return null;
    } 

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result.InjectableResult#getStereotypes(javax.lang.model.element.Element)
     */
    @Override
    public List<AnnotationMirror> getAllStereotypes( Element element ) {
        return WebBeansModelProviderImpl.getAllStereotypes(element, 
                getHelper().getHelper());
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result.InjectableResult#getStereotypes(javax.lang.model.element.Element)
     */
    @Override
    public List<AnnotationMirror> getStereotypes( Element element ) {
        return InterceptorsResultImpl.getStereotypes(element, getHelper() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result.InjectableResult#isAlternative(javax.lang.model.element.Element)
     */
    @Override
    public boolean isAlternative( Element element ) {
        if (hasAlternative(element)){
            return true;
        }
        for (AnnotationMirror annotationMirror : getAllStereotypes(element)) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            if ( hasAlternative( annotationType.asElement()) ){
                return true;
            }
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result.ResolutionResult#hasAlternative(javax.lang.model.element.Element)
     */
    @Override
    public boolean hasAlternative( Element element ){
        List<? extends AnnotationMirror> annotations = getController().
            getElements().getAllAnnotationMirrors(element);
        return getHelper().hasAnnotation(annotations, ALTERNATIVE);
    }
    
    AnnotationModelHelper  getHelper(){
        return myHelper;
    }
    
    private CompilationController getController(){
        return getHelper().getCompilationController();
    }
    
    private Set<TypeElement> myDeclaredTypes;
    private Set<Element> myProductions;
    private final AnnotationModelHelper myHelper;
}
