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
package org.netbeans.modules.web.beans.impl.model.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler;
import org.netbeans.modules.web.beans.api.model.InterceptorsResult;
import org.netbeans.modules.web.beans.impl.model.StereotypeChecker;
import org.netbeans.modules.web.beans.impl.model.WebBeansModelProviderImpl;


/**
 * @author ads
 *
 */
public class InterceptorsResultImpl implements InterceptorsResult {
    
    static final String INTERCEPTORS = "javax.interceptor.Interceptors";    // NOI18N
    
    public InterceptorsResultImpl( Element element , 
            List<TypeElement> enabledInterceptors, 
            Set<TypeElement> disabledIntercaptors,
            AnnotationModelHelper helper )
    {
        mySubjectElement = element;
        myHelper = helper;
        myEnabledInterceptors = enabledInterceptors;
        myDisabledInterceptors = disabledIntercaptors;
        initDeclaredInterceptors();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result#getAllStereotypes(javax.lang.model.element.Element)
     */
    @Override
    public List<AnnotationMirror> getAllStereotypes( Element element ) {
        return WebBeansModelProviderImpl.getAllStereotypes(element, 
                getHelper().getHelper());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result#getStereotypes(javax.lang.model.element.Element)
     */
    @Override
    public List<AnnotationMirror> getStereotypes( Element element ) {
        return getStereotypes(element, getHelper() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.BeansResult#isDisabled(javax.lang.model.element.Element)
     */
    @Override
    public boolean isDisabled( Element element ) {
        return myDisabledInterceptors.contains(element);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.InterceptorsResult#getElement()
     */
    @Override
    public Element getElement() {
        return mySubjectElement;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.InterceptorsResult#getResolvedInterceptors()
     */
    @Override
    public List<TypeElement> getResolvedInterceptors() {
        int enabledSize = myEnabledInterceptors.size();
        int disabledSize = myDisabledInterceptors.size();
        ArrayList<TypeElement> result = new ArrayList<TypeElement>( enabledSize +
                disabledSize );
        result.addAll( myEnabledInterceptors );
        result.addAll( myDisabledInterceptors );
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.InterceptorsResult#getDeclaredInterceptors()
     */
    @Override
    public List<TypeElement> getDeclaredInterceptors() {
        return myDeclaredInterceptors;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.InterceptorsResult#getAllInterceptors()
     */
    @Override
    public List<TypeElement> getAllInterceptors() {
        int enabledSize = myEnabledInterceptors.size();
        int disabledSize = myDisabledInterceptors.size();
        int declaredSize = myDeclaredInterceptors.size();
        ArrayList<TypeElement> result = new ArrayList<TypeElement>( enabledSize +
                disabledSize +declaredSize);
        result.addAll( myEnabledInterceptors );
        result.addAll( myDeclaredInterceptors );
        result.addAll( myDisabledInterceptors );
        return result;
    }
    

    private void initDeclaredInterceptors() {
        final LinkedHashSet<TypeElement> result = new LinkedHashSet<TypeElement>();
        AnnotationParser parser = AnnotationParser.create( getHelper());
        parser.expectClassArray("value", new ArrayValueHandler() {
            
            @Override
            public Object handleArray( List<AnnotationValue> arrayMembers ) {
                for (AnnotationValue arrayMember : arrayMembers) {
                    TypeMirror typeMirror = (TypeMirror) arrayMember.getValue();
                    Element element = getController().getTypes().
                        asElement( typeMirror );
                    if ( element instanceof TypeElement ){
                        result.add( (TypeElement)element );
                    }
                }
                return null;
            }
        }, null);
        Element subjectElement = getElement();
        if ( subjectElement instanceof ExecutableElement ){
            TypeElement enclosingType = getController().getElementUtilities().
                enclosingTypeElement( subjectElement);
            fillDeclaredAnnotations(parser, enclosingType);
        }
        fillDeclaredAnnotations(parser, subjectElement);
        myDeclaredInterceptors = new ArrayList<TypeElement>( result );
    }

    private void fillDeclaredAnnotations( AnnotationParser parser,
            Element subjectElement )
    {
        List<? extends AnnotationMirror> annotationMirrors = 
            getController().getElements().getAllAnnotationMirrors( subjectElement );
        AnnotationMirror annotationMirror = getHelper().getAnnotationsByType( 
                annotationMirrors).get(INTERCEPTORS);
        if ( annotationMirror != null ){
            parser.parse(annotationMirror);
        }
    }
    
    static List<AnnotationMirror> getStereotypes( Element element , 
            AnnotationModelHelper helper) 
    {
        List<AnnotationMirror> result = new LinkedList<AnnotationMirror>();
        List<? extends AnnotationMirror> annotationMirrors = 
            helper.getCompilationController().getElements().
                getAllAnnotationMirrors( element );
        StereotypeChecker checker = new StereotypeChecker( helper.getHelper());
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            TypeElement annotationElement = (TypeElement)annotationMirror.
                getAnnotationType().asElement();
            if ( annotationElement!= null && 
                    WebBeansModelProviderImpl.isStereotype( annotationElement, 
                            checker ) )
            {
                result.add( annotationMirror );
            }
        }
        return result; 
    }
    
    private AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    private CompilationController getController(){
        return getHelper().getCompilationController();
    }

    private Element mySubjectElement;
    private List<TypeElement> myEnabledInterceptors;
    private Collection<TypeElement> myDisabledInterceptors;
    private List<TypeElement> myDeclaredInterceptors;
    private AnnotationModelHelper myHelper;
}
