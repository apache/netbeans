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

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider;


/**
 * @author ads
 *
 */
abstract class AbstractObjectProvider<T extends AbstractObjectProvider.Refreshable> 
    implements ObjectProvider<T> 
{
    
    AbstractObjectProvider(String annotation , AnnotationModelHelper helper)
    {
        myHelper = helper;
        myAnnotationName = annotation;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createInitialObjects()
     */
    @Override
    public List<T> createInitialObjects() throws InterruptedException {
        final List<T> result = new LinkedList<T>();
        getHelper().getAnnotationScanner().findAnnotations(
                getAnnotation(), 
                EnumSet.of(ElementKind.CLASS, ElementKind.INTERFACE), 
                new AnnotationHandler() {
                        @Override
                        public void handleAnnotation(TypeElement type, 
                                Element element, AnnotationMirror annotation) 
                        {
                            result.add(createTypeElement(type));
                        }
        });
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#createObjects(javax.lang.model.element.TypeElement)
     */
    @Override
    public List<T> createObjects( TypeElement type ) {
        if ((type.getKind() == ElementKind.CLASS || type.getKind() == ElementKind.INTERFACE)
                && getHelper().hasAnnotation(type.getAnnotationMirrors(), 
                getAnnotation())) 
        {
            return Collections.singletonList(createTypeElement(type));
        }
        return Collections.emptyList();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider#modifyObjects(javax.lang.model.element.TypeElement, java.util.List)
     */
    @Override
    public boolean modifyObjects( TypeElement type, List<T> objects ) {
        assert objects.size() ==1;
        T object = objects.get(0);
        assert object!= null;
        if ( ! object.refresh(type)){
            objects.remove(0);
            return true;
        }
        return false;
    }
    
    protected abstract T createTypeElement( TypeElement element );
    
    public static List<Element> getAnnotatedMembers( final String annotationName,
            final AnnotationModelHelper helper )
    {
        final List<Element> result = new LinkedList<Element>();
        try {
            helper.getAnnotationScanner().findAnnotations(
                    annotationName, 
                    EnumSet.of(ElementKind.FIELD, ElementKind.METHOD), 
                    new AnnotationHandler() {
                            @Override
                            public void handleAnnotation(TypeElement type, 
                                    Element element, AnnotationMirror annotation) 
                            {
                                result.add(element);
                            }
            });
        }
        catch (InterruptedException e) {
            FieldInjectionPointLogic.LOGGER.warning("Finding annotation "+
                    annotationName+" was interrupted"); // NOI18N
        }
        return result;
    }
    
    protected AnnotationModelHelper getHelper(){
        return myHelper;
    }
    
    protected String getAnnotation(){
        return myAnnotationName;
    }
    
    public static List<Element> getNamedMembers( AnnotationModelHelper helper )
    {
         List<Element> namedMembers = getAnnotatedMembers(
                 FieldInjectionPointLogic.NAMED_QUALIFIER_ANNOTATION, helper);
         return namedMembers;
    }
    
    static interface Refreshable {
        boolean refresh( TypeElement type );
    }
    
    private AnnotationModelHelper myHelper;
    private String myAnnotationName;

}
