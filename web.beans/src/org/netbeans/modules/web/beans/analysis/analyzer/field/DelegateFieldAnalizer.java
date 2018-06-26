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
package org.netbeans.modules.web.beans.analysis.analyzer.field;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.FieldElementAnalyzer.FieldAnalyzer;
import org.netbeans.modules.web.beans.hints.EditorAnnotationsHelper;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class DelegateFieldAnalizer implements FieldAnalyzer {

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.FieldElementAnalyzer.FieldAnalyzer#analyze(javax.lang.model.element.VariableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, org.netbeans.api.java.source.CompilationInfo, java.util.List, java.util.concurrent.atomic.AtomicBoolean)
     */
    @Override
    public void analyze( VariableElement element, TypeMirror elementType,
            TypeElement parent, AtomicBoolean cancel,
            CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        if (!AnnotationUtil.hasAnnotation(element, AnnotationUtil.DELEGATE_FQN, 
                compInfo))
        {
            return;
        }
        result.requireCdiEnabled(element);
        if (!AnnotationUtil.hasAnnotation(element, AnnotationUtil.INJECT_FQN,
                compInfo))
        {
            result.addError(element,  NbBundle.getMessage(
                            DelegateFieldAnalizer.class,
                            "ERR_DelegateHasNoInject"));        // NOI18N
        }
        Element clazz = element.getEnclosingElement();
        if (!AnnotationUtil.hasAnnotation(clazz, AnnotationUtil.DECORATOR,
                compInfo))
        {
            result.addError(element,  NbBundle.getMessage(
                            DelegateFieldAnalizer.class,
                            "ERR_DelegateIsNotInDecorator"));   // NOI18N
        }
        EditorAnnotationsHelper helper = EditorAnnotationsHelper.getInstance(
                result);
        if ( helper != null ){
            helper.addDelegate(result, element );
        }
        if ( cancel.get()){
            return;
        }
        checkDelegateType(element, elementType, parent, result );
    }

    private void checkDelegateType( VariableElement element,
            TypeMirror elementType, TypeElement parent,
            CdiAnalysisResult result  )
    {
        Collection<TypeMirror> decoratedTypes = getDecoratedTypes( parent , 
                result.getInfo() );
        for (TypeMirror decoratedType : decoratedTypes) {
            if ( !result.getInfo().getTypes().isSubtype( elementType,decoratedType )){
                result.addError(element, NbBundle.getMessage(
                        DelegateFieldAnalizer.class,
                        "ERR_DelegateTypeHasNoDecoratedType"));   // NOI18N
                return;
            }
        }
    }

    public static Collection<TypeMirror> getDecoratedTypes( TypeElement element , 
            CompilationInfo info ) 
    {
        TypeElement serializable = info.getElements().getTypeElement(
                Serializable.class.getCanonicalName());
        Collection<TypeMirror> result = new LinkedList<TypeMirror>();
        collectDecoratedTypes( element.asType() , result , serializable, info );
        return result;
    }

    private static void collectDecoratedTypes( TypeMirror type,
            Collection<TypeMirror> result, TypeElement serializable, 
            CompilationInfo info)
    {
        List<? extends TypeMirror> directSupertypes = info.getTypes().
            directSupertypes(type);
        for (TypeMirror superType : directSupertypes) {
            Element element = info.getTypes().asElement(superType);
            if( element == null || element.equals( serializable)  )
            {
                continue;
            }
            if ( element.getKind() == ElementKind.INTERFACE ){
                result.add( superType );
            }
            collectDecoratedTypes(superType, result, serializable, info);
        }
    }

}
