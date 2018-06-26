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
package org.netbeans.modules.web.beans.analysis.analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;


/**
 * @author ads
 *
 */
public abstract class AbstractTypedAnalyzer {
    
    public void analyze( Element element, TypeMirror elementType, 
            AtomicBoolean cancel , CdiAnalysisResult result )
    {
        CompilationInfo compInfo = result.getInfo();
        List<TypeMirror> list = getRestrictedTypes(element, compInfo, cancel);
        if ( list == null ){
            return;
        }
        result.requireCdiEnabled(element);
        for (TypeMirror type : list) {
            if ( cancel.get()){
                return;
            }
            boolean isSubtype = hasBeanType(element, elementType, type, compInfo);
            if (!isSubtype) {
                addError(element, result );
            }
        }
        // check @Specializes types restriction conformance
        if ( cancel.get()){
            return;
        }
        if ( AnnotationUtil.hasAnnotation(element, AnnotationUtil.SPECIALIZES, 
                compInfo))
        {
            result.requireCdiEnabled(element);
            checkSpecializes( element , elementType , list,  cancel , result );
        }
    }
    
    protected abstract void checkSpecializes( Element element, TypeMirror elementType,
            List<TypeMirror> restrictedTypes, AtomicBoolean cancel , CdiAnalysisResult result );

    protected boolean hasBeanType( Element subject, TypeMirror elementType, 
            TypeMirror requiredBeanType,CompilationInfo compInfo )
    {
        return compInfo.getTypes().isSubtype(elementType, requiredBeanType);
    }
    
    protected abstract void addError ( Element element , 
            CdiAnalysisResult result );

    protected void collectAncestors(TypeElement type , Set<TypeElement> ancestors, 
            CompilationInfo compInfo )
    {
        TypeMirror superclass = type.getSuperclass();
        addAncestor( superclass, ancestors, compInfo);
        List<? extends TypeMirror> interfaces = type.getInterfaces();
        for (TypeMirror interfaze : interfaces) {
            addAncestor(interfaze, ancestors, compInfo);
        }
    }
    
    private void addAncestor( TypeMirror parent , Set<TypeElement> ancestors,
            CompilationInfo compInfo)
    {
        if ( parent == null ){
            return;
        }
        Element parentElement = compInfo.getTypes().asElement( parent );
        if ( parentElement instanceof TypeElement ){
            if ( ancestors.contains( (TypeElement)parentElement))
            {
                return;
            }
            ancestors.add( (TypeElement)parentElement);
            collectAncestors((TypeElement)parentElement, ancestors, compInfo);
        }
    }
    
    protected List<TypeMirror> getRestrictedTypes(Element element, 
            CompilationInfo compInfo , AtomicBoolean cancel)
    {
        AnnotationMirror typedMirror = AnnotationUtil.getAnnotationMirror(element, 
                AnnotationUtil.TYPED, compInfo);
        if ( typedMirror == null ){
            return null;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> values = 
            typedMirror.getElementValues();
        AnnotationValue restrictedTypes = null;
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : 
            values.entrySet() ) 
        {
            ExecutableElement key = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( AnnotationUtil.VALUE.contentEquals(key.getSimpleName())){ 
                restrictedTypes = value;
                break;
            }
        }
        if ( restrictedTypes == null ){
            return Collections.emptyList();
        }
        if ( cancel.get() ){
            return Collections.emptyList();
        }
        Object value = restrictedTypes.getValue();
        if ( value instanceof List<?> ){
            List<TypeMirror> result = new ArrayList<TypeMirror>(((List<?>)value).size());
            for( Object type : (List<?>)value){
                AnnotationValue annotationValue = (AnnotationValue)type;
                type = annotationValue.getValue();
                if (type instanceof TypeMirror){
                    result.add( (TypeMirror)type);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
    
    protected Set<TypeElement> getUnrestrictedBeanTypes( TypeElement element,
            CompilationInfo compInfo)
    {
        Set<TypeElement> set = new HashSet<TypeElement>();
        set.add( element );
        collectAncestors(element, set, compInfo);
        return set;
    }
    
    protected Set<TypeElement> getElements( Collection<TypeMirror> types ,
            CompilationInfo info  )
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        for (TypeMirror typeMirror : types) {
            Element element = info.getTypes().asElement(typeMirror);
            if ( element instanceof TypeElement  ){
                result.add( (TypeElement)element);
            }
        }
        return result;
    }
    
}
