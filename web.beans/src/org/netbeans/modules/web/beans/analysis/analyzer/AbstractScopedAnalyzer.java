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
                scope, model.getCompilationController(), AnnotationUtil.NORMAL_SCOPE_FQN);
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
