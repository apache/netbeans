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
package org.netbeans.modules.jakarta.web.beans.model;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.jakarta.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.jakarta.web.beans.impl.model.ResultLookupStrategy;
import org.netbeans.modules.jakarta.web.beans.impl.model.SingleResultLookupStrategy;
import org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelImplementation;
import org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelProviderImpl;



/**
 * @author ads
 *
 */
public class TestWebBeansModelProviderImpl extends WebBeansModelProviderImpl {
    
    private static final ResultLookupStrategy SINGLE_STRATEGY = new TestResultStrategy(null);

    TestWebBeansModelProviderImpl(TestWebBeansModelImpl testWebBeansModelImpl )
    {
        super( testWebBeansModelImpl );
    }
    
    @Override
    protected TestWebBeansModelImpl getModel() {
        return (TestWebBeansModelImpl)super.getModel();
    }

    @Override
    protected DependencyInjectionResult findParameterInjectable( VariableElement element,
            DeclaredType parentType, ResultLookupStrategy strategy, AtomicBoolean cancel  )
    {
        return super.findParameterInjectable(element, parentType, strategy, cancel);
    }

    @Override
    protected DependencyInjectionResult doFindVariableInjectable( VariableElement element,
            TypeMirror elementType, boolean injectRequired, AtomicBoolean cancel  )
    {
        return super.doFindVariableInjectable(element, elementType,
                injectRequired, cancel);
    }

    @Override
    protected DependencyInjectionResult findVariableInjectable( VariableElement element,
            DeclaredType parentType, ResultLookupStrategy strategy, AtomicBoolean cancel  )
    {
        return super.findVariableInjectable(element, parentType, strategy, cancel );
    }

    protected DependencyInjectionResult findParameterInjectable( VariableElement element,
            DeclaredType parentType, AtomicBoolean cancel )
    {
        return findParameterInjectable(element, parentType, SINGLE_STRATEGY, cancel);
    }

    protected DependencyInjectionResult findVariableInjectable( VariableElement element, 
            DeclaredType parentType, AtomicBoolean cancel  )
    {
        return findVariableInjectable(element, parentType, SINGLE_STRATEGY, cancel);
    }
    
}

class TestResultStrategy extends SingleResultLookupStrategy implements ResultLookupStrategy {
    
    TestResultStrategy( ResultLookupStrategy delegate ){
        myStartegy = delegate;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.SingleResultLookupStrategy#getResult(org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelImplementation, org.netbeans.modules.jakarta.web.beans.api.model.Result)
     */
    @Override
    public DependencyInjectionResult getResult( WebBeansModelImplementation model, DependencyInjectionResult result, AtomicBoolean cancel ){
        if ( myStartegy != null && ((TestWebBeansModelImpl)model).isFull() ){
            return myStartegy.getResult(model,result, cancel);
        }
        else {
            filterBeans(result , model, cancel );
            return result;
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.SingleResultLookupStrategy#getType(org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelImplementation, javax.lang.model.type.DeclaredType, javax.lang.model.element.VariableElement)
     */
    @Override
    public TypeMirror getType( WebBeansModelImplementation model,
            DeclaredType parent, VariableElement element )
    {
        if ( myStartegy != null ){
            return myStartegy.getType(model, parent, element);
        }
        return super.getType(model, parent, element);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.SingleResultLookupStrategy#getType(org.netbeans.modules.jakarta.web.beans.impl.model.WebBeansModelImplementation, javax.lang.model.type.TypeMirror)
     */
    @Override
    public TypeMirror getType( WebBeansModelImplementation model,
            TypeMirror typeMirror )
    {
        if ( myStartegy != null ){
            return myStartegy.getType(model, typeMirror);
        }
        return super.getType(model, typeMirror);
    }
    
    private ResultLookupStrategy myStartegy ;
}
