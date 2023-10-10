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
package org.netbeans.modules.jakarta.web.beans.impl.model;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.jakarta.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.jakarta.web.beans.impl.model.results.ResultImpl;


/**
 * @author ads
 *
 */
public class MultiLookupStrategy extends SingleResultLookupStrategy {
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.ResultLookupStrategy#getType(org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelImplementation, javax.lang.model.type.DeclaredType, javax.lang.model.element.VariableElement)
     */
    @Override
    public TypeMirror getType( WebBeansModelImplementation model, 
            DeclaredType parent, VariableElement element ) 
    {
        return ParameterInjectionPointLogic.getParameterType( 
                model.getHelper().getCompilationController(), element , parent , 
                FieldInjectionPointLogic.INSTANCE_INTERFACE);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.ResultLookupStrategy#getType(org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelImplementation, javax.lang.model.type.TypeMirror)
     */
    @Override
    public TypeMirror getType( WebBeansModelImplementation model,
            TypeMirror typeMirror ) {
        return ParameterInjectionPointLogic.getParameterType( 
                typeMirror , FieldInjectionPointLogic.INSTANCE_INTERFACE );
    }
    
    @Override
    protected DependencyInjectionResult filterEnabled( DependencyInjectionResult result, 
            WebBeansModelImplementation model, AtomicBoolean cancel)
    {
        if ( result instanceof ResultImpl ){
            EnableBeansFilter filter = new EnableBeansFilter((ResultImpl)result,
                    model , true );
            return filter.filter(cancel);
        }
        return result;
    }
}
