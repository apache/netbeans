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
package org.netbeans.modules.web.beans.impl.model;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;


/**
 * @author ads
 *
 */
public interface ResultLookupStrategy {
    
    ResultLookupStrategy SINGLE_LOOKUP_STRATEGY = new SingleResultLookupStrategy();
    
    ResultLookupStrategy MULTI_LOOKUP_STRATEGY = new MultiLookupStrategy();

    DependencyInjectionResult getResult(WebBeansModelImplementation model, DependencyInjectionResult result, AtomicBoolean cancel );
    
    TypeMirror getType( WebBeansModelImplementation model,
            DeclaredType parent, VariableElement element);
    
    TypeMirror getType(  WebBeansModelImplementation model, TypeMirror typeMirror );
    
}
