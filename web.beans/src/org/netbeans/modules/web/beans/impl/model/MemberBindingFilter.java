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
package org.netbeans.modules.web.beans.impl.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;


/**
 * @author ads
 *
 */
class MemberBindingFilter<T extends Element> extends Filter<T> {
    
    private static final String NON_BINDING_MEMBER_ANNOTATION =
                "javax.enterprise.util.Nonbinding";    // NOI18N
    
    private MemberBindingFilter( Class<T> clazz ){
        myClass = clazz;
    }
    
    static <T extends Element> MemberBindingFilter<T> get( Class<T> clazz ) {
        assertElement(clazz);
        // could be changed to cached ThreadLocal variable
        if ( clazz.equals( Element.class )){
            return (MemberBindingFilter<T>) new MemberBindingFilter<Element>(
                    Element.class);
        }
        else if ( clazz.equals( TypeElement.class ) ){
            return (MemberBindingFilter<T>)new MemberBindingFilter<TypeElement>(
                    TypeElement.class);
        }
        return null;
    }

    void init( Collection<AnnotationMirror> bindingAnnotations,
            WebBeansModelImplementation impl )
    {
        myImpl = impl;
        myBindingAnnotations = bindingAnnotations;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.TypeFilter#filter(java.util.Set)
     */
    void filter( Set<T> set ) {
        super.filter(set);
        if ( set.size() == 0 ){
            return;
        }
        /*
         * Binding annotation could have members. See example :
         * @BindingType
         * @Retention(RUNTIME)
         * @Target({METHOD, FIELD, PARAMETER, TYPE})
         * public @interface PayBy {
         * PaymentMethod value();
         * @NonBinding String comment();
         * }    
         * One need to check presence of member in binding annotation at 
         * injected point and compare this member with member in annotation
         * for discovered type.
         * Members with  @Nonbinding annotation should be ignored. 
         */
         for (AnnotationMirror annotation : getBindingAnnotations()) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> 
                elementValues = annotation.getElementValues();
            Set<ExecutableElement> bindingMembers = collectBindingMembers(
                    annotation , getImplementation() );
            checkMembers(elementValues, bindingMembers, set );
        }
    }
    
    Class<T> getElementClass(){
        return myClass;
    }
    
    private void checkMembers(
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues,
            Set<ExecutableElement> members, Set<T> set )
    {
        MemberCheckerFilter<T> filter = MemberCheckerFilter.get( getElementClass());
        filter.init( elementValues , members, getImplementation());
        filter.filter(set);
    }
    
    
    static Set<ExecutableElement> collectBindingMembers( AnnotationMirror annotation ,
            WebBeansModelImplementation impl ) 
    {
        DeclaredType annotationType  = annotation.getAnnotationType();
        TypeElement annotationElement = (TypeElement)annotationType.asElement();
        List<? extends Element> members = annotationElement.getEnclosedElements();
        Set<ExecutableElement> bindingMembers = new HashSet<ExecutableElement>();
        for (Element member : members) {
            if ( member instanceof ExecutableElement ){
                ExecutableElement exec = (ExecutableElement)member;
                if ( isBindingMember( exec , impl )){
                    bindingMembers.add( exec );
                }
            }
        }
        return bindingMembers;
    }
    
    private static boolean isBindingMember( ExecutableElement element , 
            WebBeansModelImplementation impl )
    {
        List<? extends AnnotationMirror> annotationMirrors = 
            impl.getHelper().getCompilationController().getElements().
                    getAllAnnotationMirrors( element);
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            TypeElement annotation = (TypeElement)annotationMirror.
                getAnnotationType().asElement();
            if ( annotation == null ){
                continue;
            }
            Name name = annotation.getQualifiedName();
            if ( NON_BINDING_MEMBER_ANNOTATION.contentEquals(name)){
                return false;
            }
        }
        return true;
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private Collection<AnnotationMirror> getBindingAnnotations(){
        return myBindingAnnotations;
    }

    private WebBeansModelImplementation myImpl;
    private Collection<AnnotationMirror> myBindingAnnotations;
    private Class<T> myClass;
}
