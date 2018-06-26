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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

/**
 * @author ads
 */
class DefaultBindingTypeFilter<T extends Element> extends Filter<T> {

    static <T extends Element> DefaultBindingTypeFilter<T> get( Class<T> clazz )
    {
        assertElement(clazz);
        // could be changed to cached ThreadLocal access
        if (clazz.equals(Element.class)) {
            return (DefaultBindingTypeFilter<T>) new DefaultBindingTypeFilter<Element>();
        }
        else if (clazz.equals(TypeElement.class)) {
            return (DefaultBindingTypeFilter<T>) new DefaultBindingTypeFilter<TypeElement>();
        }
        return null;
    }

    void init( WebBeansModelImplementation impl ) {
        myImpl = impl;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.netbeans.modules.web.beans.impl.model.Filter#filter(java.util.Set)
     */
    @Override
    void filter( Set<T> set ) {
        super.filter(set);
        for (Iterator<T> iterator = set.iterator(); iterator
                .hasNext();)
        {
            Element element = iterator.next();
            List<? extends AnnotationMirror> allAnnotationMirrors = getImplementation()
                    .getHelper().getCompilationController().getElements()
                    .getAllAnnotationMirrors(element);
            Set<String> qualifierNames = new HashSet<String>();
            for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
                DeclaredType annotationType = annotationMirror
                        .getAnnotationType();
                TypeElement annotationElement = (TypeElement) annotationType
                        .asElement();
                if ( annotationElement == null ){
                    continue;
                }
                String annotationName = annotationElement.getQualifiedName()
                    .toString();
                if ( WebBeansModelProviderImpl.ANY_QUALIFIER_ANNOTATION.equals(
                        annotationName) || 
                        WebBeansModelProviderImpl.NAMED_QUALIFIER_ANNOTATION.equals(
                                annotationName))
                {
                    continue;
                }
                if (isQualifier(annotationElement)) {
                    qualifierNames.add(annotationName);
                }
            }
            if ( qualifierNames.contains(
                    WebBeansModelProviderImpl.DEFAULT_QUALIFIER_ANNOTATION))
            {
                continue;
            }
            if ( (element instanceof TypeElement) && (
                AnnotationObjectProvider.checkSuper((TypeElement)element, 
                        WebBeansModelProviderImpl.DEFAULT_QUALIFIER_ANNOTATION, 
                        getImplementation().getHelper())!=null ))
            {
                    continue;
            }
            else if ( element instanceof ExecutableElement ){
                Element specialized = 
                    MemberCheckerFilter.getSpecialized( (ExecutableElement)element, 
                            getImplementation(), 
                            WebBeansModelProviderImpl.DEFAULT_QUALIFIER_ANNOTATION);
                if ( specialized!= null){
                    continue;
                }
            }
            if (qualifierNames.size() != 0) {
                iterator.remove();
            }
        }
    }

    private boolean isQualifier( TypeElement annotationElement ) {
        return AnnotationObjectProvider.isQualifier(annotationElement, 
                getImplementation().getHelper(), false );
    }

    private WebBeansModelImplementation getImplementation() {
        return myImpl;
    }

    private WebBeansModelImplementation myImpl;

}
