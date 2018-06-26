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
package org.netbeans.modules.web.beans.analysis.analyzer.method;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.analyzer.AbstractTypedAnalyzer;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;
import org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer;
import org.openide.util.NbBundle;


/**
 * @author ads
 *
 */
public class TypedMethodAnalyzer extends AbstractTypedAnalyzer implements
        MethodAnalyzer
{

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.MethodElementAnalyzer.MethodAnalyzer#analyze(javax.lang.model.element.ExecutableElement, javax.lang.model.type.TypeMirror, javax.lang.model.element.TypeElement, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    public void analyze( ExecutableElement element, TypeMirror returnType,
            TypeElement parent, AtomicBoolean cancel , CdiAnalysisResult result )
    {
        analyze(element, returnType, cancel , result );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractTypedAnalyzer#addError(javax.lang.model.element.Element, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void addError( Element element, CdiAnalysisResult result )
    {
        result.addError( element, NbBundle.getMessage(
                TypedMethodAnalyzer.class, "ERR_BadRestritedMethodType"));  // NOI18N
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analizer.AbstractTypedAnalyzer#hasBeanType(javax.lang.model.element.Element, javax.lang.model.type.TypeMirror, javax.lang.model.type.TypeMirror, org.netbeans.api.java.source.CompilationInfo)
     */
    @Override
    protected boolean hasBeanType( Element subject, TypeMirror returnType,
            TypeMirror requiredBeanType, CompilationInfo compInfo )
    {
        return compInfo.getTypes().isSubtype(returnType, requiredBeanType);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.analysis.analyzer.AbstractTypedAnalyzer#checkSpecializes(javax.lang.model.element.Element, javax.lang.model.type.TypeMirror, java.util.List, java.util.concurrent.atomic.AtomicBoolean, org.netbeans.modules.web.beans.analysis.analyzer.ElementAnalyzer.Result)
     */
    @Override
    protected void checkSpecializes( Element element, TypeMirror elementType,
            List<TypeMirror> restrictedTypes, AtomicBoolean cancel , CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        if (!AnnotationUtil.hasAnnotation(element, AnnotationUtil.PRODUCES_FQN, 
                compInfo))
        {
            return;
        }
        ExecutableElement method = (ExecutableElement)element;
        ExecutableElement overriddenMethod = compInfo.getElementUtilities().
            getOverriddenMethod(method);
        if ( overriddenMethod == null ){
            return;
        }
        TypeElement clazz = compInfo.getElementUtilities().
            enclosingTypeElement(method);
        TypeMirror superType = clazz.getSuperclass();
        TypeElement superClass = compInfo.getElementUtilities().
            enclosingTypeElement(overriddenMethod);
        if ( !superClass.equals( compInfo.getTypes().asElement( superType))){
            return;
        }
        if ( cancel.get()){
            return;
        }
        List<TypeMirror> restrictedSuper = getRestrictedTypes(overriddenMethod, 
                compInfo, cancel);
        if ( cancel.get()){
            return;
        }
        if ( restrictedSuper == null ) {
            if (!hasUnrestrictedOverridenType(elementType, 
                    restrictedTypes, compInfo,overriddenMethod, superClass) )
            {
                result.addError( element, NbBundle.getMessage(
                            TypedMethodAnalyzer.class, "ERR_BadSpecializesMethod"));  // NOI18N 
            }
        }
        else { 
            if (!hasRestrictedType(elementType, restrictedTypes, compInfo,
                    restrictedSuper))
            {
                result.addError( element,  NbBundle.getMessage(
                            TypedMethodAnalyzer.class, "ERR_BadSpecializesMethod"));  // NOI18N 
            }
        }
    }

    private boolean hasRestrictedType( TypeMirror elementType,
            List<TypeMirror> restrictedTypes, CompilationInfo compInfo,
            List<TypeMirror> restrictedSuper )
    {
        if ( elementType.getKind() == TypeKind.ARRAY ){
            for( TypeMirror mirror : restrictedSuper ){
                boolean found = false;
                for( TypeMirror restrictedType : restrictedTypes ){
                    if ( compInfo.getTypes().isSameType( restrictedType, mirror)){
                        found = true;
                        break;
                    }
                }
                if ( !found ){
                    return false;
                }
            }
            return true;
        }
        else {
            Set<TypeElement> specializedBeanTypes = getElements( 
                    restrictedSuper, compInfo);
            Set<TypeElement> restrictedElements = getElements(restrictedTypes, 
                    compInfo);
            restrictedElements.add( compInfo.getElements().getTypeElement( 
                    Object.class.getCanonicalName()));
            return restrictedElements.containsAll( specializedBeanTypes );
        }
    }

    private boolean hasUnrestrictedOverridenType( TypeMirror elementType,
            List<TypeMirror> restrictedTypes, CompilationInfo compInfo,
            ExecutableElement overriddenMethod, TypeElement superClass )
    {
        TypeMirror methodType = compInfo.getTypes().asMemberOf(
                (DeclaredType)superClass.asType(), overriddenMethod);
        TypeMirror returnOverriden = ((ExecutableType)methodType).getReturnType();
        if ( elementType.getKind() == TypeKind.ARRAY ){
            for( TypeMirror mirror : restrictedTypes ){
                if ( compInfo.getTypes().isSameType( mirror, returnOverriden)){
                    return true;
                }
            }
            return false;
        }
        else if ( returnOverriden.getKind().isPrimitive() ) {
            TypeElement boxed = compInfo.getTypes().boxedClass(
                    (PrimitiveType)returnOverriden);
            return hasUnrestrictedType(boxed, restrictedTypes, compInfo);
        }
        else if ( returnOverriden instanceof DeclaredType ){
            Element returnElement = compInfo.getTypes().asElement( returnOverriden);
            if ( returnElement instanceof TypeElement ){
                return hasUnrestrictedType((TypeElement)returnElement, 
                        restrictedTypes, compInfo);
            }
        }
        return true;
    }

    private boolean hasUnrestrictedType( TypeElement overriden,
            List<TypeMirror> restrictedTypes,CompilationInfo compInfo )
    {
        Set<TypeElement> specializedBeanTypes = getUnrestrictedBeanTypes(
                    overriden, compInfo);
        Set<TypeElement> restrictedElements = getElements(restrictedTypes, 
                compInfo);
        restrictedElements.add( compInfo.getElements().getTypeElement( 
                Object.class.getCanonicalName()));
        return restrictedElements.containsAll(specializedBeanTypes);
    }

}
