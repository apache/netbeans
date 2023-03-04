/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.beans.analysis.analyzer;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.web.beans.analysis.CdiAnalysisResult;


/**
 * @author ads
 *
 */
public abstract class AbstractProducerAnalyzer {
    
    protected abstract void hasTypeVar( Element element, TypeMirror type,
            CdiAnalysisResult result );
    
    protected abstract void hasWildCard(Element element, TypeMirror type,
            CdiAnalysisResult result);
    
    protected void checkType( Element element, TypeMirror type,
            CdiAnalysisResult result )
    {
        if ( type.getKind() == TypeKind.TYPEVAR ){
            hasTypeVar(element, type, result );
        }
        else if (hasWildCard(type)) {
            hasWildCard( element, type, result );
            return;
        }
    }
    
    protected boolean hasType(TypeMirror typeMirror, TypeKind kind ){
        if ( typeMirror instanceof DeclaredType ){
            List<? extends TypeMirror> typeArguments = 
                    ((DeclaredType)typeMirror).getTypeArguments();
            for (TypeMirror paramType : typeArguments) {
                if ( paramType.getKind() == kind ){
                    return true;
                }
                else {
                    if ( hasType(paramType, kind) ){
                        return true;
                    }
                }
            }
        }
        else if ( typeMirror instanceof ArrayType ){
            return hasType( ((ArrayType)typeMirror).getComponentType(), kind);
        }
        return false;
    }

    private boolean hasWildCard(TypeMirror typeMirror){
        return hasType(typeMirror, TypeKind.WILDCARD);
    }
}
