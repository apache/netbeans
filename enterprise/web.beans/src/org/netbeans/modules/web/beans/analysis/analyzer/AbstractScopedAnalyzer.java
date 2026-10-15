/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.beans.analysis.analyzer;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.web.beans.analysis.analyzer.ModelAnalyzer.Result;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;


/**
 * @author ads
 *
 */
public abstract class AbstractScopedAnalyzer  {
    
    public void analyzeScope( Element element, 
            WebBeansModel model, AtomicBoolean cancel , Result result )
    {
        try {
            String scope = model.getScope( element );
            if ( cancel.get() ){
                return;
            }
            TypeElement scopeElement = model.getCompilationController().
                getElements().getTypeElement( scope );
            if ( scopeElement == null ){
                return;
            }
            checkScope( scopeElement , element , model, cancel, result );
        }
        catch (CdiException e) {
            result.requireCdiEnabled(element, model);
            informCdiException(e, element, model, result  );
        }
    }
    
    protected abstract void checkScope( TypeElement scopeElement, Element element, 
            WebBeansModel model, AtomicBoolean cancel, Result result  );
    
    protected boolean hasTypeVarParameter(TypeMirror type ){
        if ( type.getKind() == TypeKind.TYPEVAR){
            return true;
        }
        if ( type instanceof DeclaredType ){
            List<? extends TypeMirror> typeArguments = 
                ((DeclaredType)type).getTypeArguments();
            for (TypeMirror typeArg : typeArguments) {
                if ( hasTypeVarParameter(typeArg)){
                    return true;
                }
            }
        }
        else if ( type instanceof ArrayType ){
            return hasTypeVarParameter(((ArrayType)type).getComponentType());
        }
        return false;
    }
    
    protected boolean isPassivatingScope( TypeElement scope, WebBeansModel model ) {
        AnnotationMirror normalScope = AnnotationUtil.getAnnotationMirror(
                scope, model.getCompilationController(), AnnotationUtil.NORMAL_SCOPE_FQN_JAKARTA);
        if (normalScope == null) {
            normalScope = AnnotationUtil.getAnnotationMirror(
                    scope, model.getCompilationController(), AnnotationUtil.NORMAL_SCOPE_FQN);
        }
        if ( normalScope==null){
            return false;
        }
        Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = 
            normalScope.getElementValues();
        boolean isPassivating = false;
        for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry: 
            elementValues.entrySet()) 
        {
            ExecutableElement key = entry.getKey();
            if ( key.getSimpleName().contentEquals(AnnotationUtil.PASSIVATING)){
                isPassivating = Boolean.TRUE.toString().equals(entry.getValue().toString());
            }
        }
        return isPassivating;
    }
    
    protected boolean isSerializable( Element element, WebBeansModel model ) {
        TypeMirror elementType = element.asType();
        if ( elementType == null || elementType.getKind() == TypeKind.ERROR){
            return true;
        }
        return isSerializable(elementType, model);
    }
    
    protected boolean isSerializable( TypeMirror type, WebBeansModel model ) {
        TypeElement serializable = model.getCompilationController().getElements().
            getTypeElement(Serializable.class.getCanonicalName());
        if ( serializable == null ){
            return true;
        }
        TypeMirror serializableType = serializable.asType();
        if ( serializableType == null || serializableType.getKind() == TypeKind.ERROR){
            return true;
        }
        return model.getCompilationController().getTypes().isSubtype(type, 
                serializableType);
    }

    private void informCdiException(CdiException exception , Element element, 
            WebBeansModel model, Result result )
    {
            result.addError( element, model, exception.getMessage());
    }
}
