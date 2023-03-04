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
package org.netbeans.modules.web.beans.model.spi;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.beans.api.model.BeanArchiveType;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.InterceptorsResult;


/**
 * @author ads
 *
 */
public interface WebBeansModelProvider {

    DependencyInjectionResult lookupInjectables( VariableElement element , DeclaredType parentType, AtomicBoolean cancel);
    
    boolean isDynamicInjectionPoint( VariableElement element );
    
    boolean isInjectionPoint( VariableElement element ) throws InjectionPointDefinitionError;
    
    List<AnnotationMirror> getQualifiers( Element element , boolean all );

    List<Element> getNamedElements(AtomicBoolean cancel);

    String getName( Element element);

    List<ExecutableElement> getObservers( VariableElement element,
            DeclaredType parentType);

    List<VariableElement> getEventInjectionPoints( ExecutableElement element,
            DeclaredType parentType);

    VariableElement getObserverParameter( ExecutableElement element);
    
    String getScope( Element element ) throws CdiException;

    CompilationController getCompilationController();

    TypeMirror resolveType( String fqn);

    boolean hasImplicitDefaultQualifier( Element element );

    Collection<TypeElement> getDecorators( TypeElement element );

    InterceptorsResult getInterceptors( Element element );

    Collection<AnnotationMirror> getInterceptorBindings( Element element );

    BeanArchiveType getBeanArchiveType();

    boolean isCdi11OrLater();
}
