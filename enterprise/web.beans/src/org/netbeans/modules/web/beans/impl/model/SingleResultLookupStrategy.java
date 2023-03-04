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
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;

public class SingleResultLookupStrategy implements ResultLookupStrategy {
    
    protected SingleResultLookupStrategy(){
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.ResultLookupStrategy#getResult(org.netbeans.modules.web.beans.impl.model.WebBeansModelImplementation, org.netbeans.modules.web.beans.api.model.Result)
     */
    @Override
    public DependencyInjectionResult getResult( WebBeansModelImplementation model , DependencyInjectionResult result, AtomicBoolean cancel ) {
        /*
         * Simple filtering related to production elements types.
         * F.e. there could be injection point with String type.
         * String is unproxyable type ( it is final ) so it cannot 
         * be used as injectable type. Only appropriate production element
         * is valid injectable. But String will be found as result of previous
         * procedure. So it should be removed.      
         */
        filterBeans( result , model, cancel );
        
        result = filterEnabled(result , model, cancel);
        
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.ResultLookupStrategy#getType(org.netbeans.modules.web.beans.impl.model.WebBeansModelImplementation, javax.lang.model.type.DeclaredType, javax.lang.model.element.VariableElement)
     */
    @Override
    public TypeMirror getType( WebBeansModelImplementation model, 
            DeclaredType parent, VariableElement element ) 
    {
        return model.getHelper().getCompilationController().getTypes().
            asMemberOf(parent, element );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.ResultLookupStrategy#getType(org.netbeans.modules.web.beans.impl.model.WebBeansModelImplementation, javax.lang.model.type.TypeMirror)
     */
    @Override
    public TypeMirror getType( WebBeansModelImplementation model,
            TypeMirror typeMirror ) {
        return typeMirror;
    }

    protected void filterBeans( DependencyInjectionResult result, WebBeansModelImplementation model, AtomicBoolean cancel ) {
        if ( result instanceof ResultImpl ){
            BeansFilter filter = BeansFilter.get();
            filter.filter(((ResultImpl)result).getTypeElements() );
        }
    }
    
    protected DependencyInjectionResult filterEnabled( DependencyInjectionResult result, 
            WebBeansModelImplementation model, AtomicBoolean cancel)
    {
        if ( result instanceof ResultImpl ){
            EnableBeansFilter filter = new EnableBeansFilter((ResultImpl)result,
                    model , false );
            return filter.filter(cancel);
        }
        return result;
    }
}