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


/**
 * @author ads
 *
 */
class NamedStereotypeObjectProvider extends AbstractObjectProvider<NamedStereotype> {
    
    NamedStereotypeObjectProvider(AnnotationModelHelper helper){
        super( StereotypeChecker.STEREOTYPE, helper );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractObjectProvider#createInitialObjects()
     */
    @Override
    public List<NamedStereotype> createInitialObjects()
            throws InterruptedException
    {
        final List<NamedStereotype> result = new LinkedList<NamedStereotype>();
        getHelper().getAnnotationScanner().findAnnotations(
                getAnnotation(), 
                EnumSet.of(ElementKind.ANNOTATION_TYPE), 
                new AnnotationHandler() {
                        @Override
                        public void handleAnnotation(TypeElement type, 
                                Element element, AnnotationMirror annotation) 
                        {
                            if ( hasNamed( type , getHelper() )) {
                                result.add(createTypeElement(type));
                            }
                        }
        });
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractObjectProvider#createObjects(javax.lang.model.element.TypeElement)
     */
    @Override
    public List<NamedStereotype> createObjects( TypeElement type ) {
        if (type.getKind() == ElementKind.ANNOTATION_TYPE &&
                getHelper().hasAnnotation(type.getAnnotationMirrors(), 
                getAnnotation())) 
        {
            if ( hasNamed(type, getHelper())){
                return Collections.singletonList(createTypeElement(type));
            }
        }
        return Collections.emptyList();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.AbstractObjectProvider#createTypeElement(javax.lang.model.element.TypeElement)
     */
    @Override
    protected NamedStereotype createTypeElement( TypeElement element ) {
        return new NamedStereotype(getHelper(), element);
    }

    static  boolean hasNamed( TypeElement type , AnnotationModelHelper helper ) {
        if (AnnotationObjectProvider.hasAnnotation(type, 
                FieldInjectionPointLogic.NAMED_QUALIFIER_ANNOTATION, helper))
        {
            return true; 
        }
        List<AnnotationMirror> stereotypes = WebBeansModelProviderImpl.
            getAllStereotypes(type, helper.getHelper());
        for (AnnotationMirror annotationMirror : stereotypes) {
            TypeElement annotation = (TypeElement)annotationMirror.
                getAnnotationType().asElement();
            if (annotation!= null && AnnotationObjectProvider.hasAnnotation(annotation, 
                    FieldInjectionPointLogic.NAMED_QUALIFIER_ANNOTATION, helper))
            {
                return true; 
            }
        }
        
        return false;
    }
    
}