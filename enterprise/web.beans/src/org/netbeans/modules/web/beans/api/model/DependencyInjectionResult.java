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
package org.netbeans.modules.web.beans.api.model;

import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;


/**
 * Represent eligible for injection element search result. 
 * 
 * @author ads
 *
 */
public interface DependencyInjectionResult {
    
    enum ResultKind {
        /**
         * This kind correspond to Error result only
         */
        DEFINITION_ERROR,
        /**
         * This kind represents at least InjectableResult and ResolutionResult.
         * Also there could be additional hints with set of eligible for injection
         * elements ( which are disabled , turned off alternatives, .... )
         * represented by ApplicableResult.
         */
        INJECTABLE_RESOLVED,
        /**
         * - No eligible for injection element found at all 
         * - Only disabled beans are found
         * - Ambiguous dependencies result   
         * It could be represented by Error only ( nothing found
         * at all ) or Error, ResolutionResult and ApplicableResult with 
         * information about probable eligible for injection elements. 
         */
        RESOLUTION_ERROR,
        /**
         * This kind is like INJECTABLE_RESOLVED but it can contain 
         * several eligible for injection elements from very beginning.
         * It is used when multiple eligible for injection elements are
         * valid result. F.e. it is normal to find a number elements 
         * via programmatic lookup .   
         * It is represented at least by ApplicableResult and ResolutionResult.
         */
        INJECTABLES_RESOLVED,
    }
    
    /**
     * @return element injection point which is used for injectable search
     */
    VariableElement getVariable();
    
    TypeMirror getVariableType();
    
    ResultKind getKind();
    
    interface Error extends DependencyInjectionResult {
        
        String getMessage();
    }
    
    interface ResolutionResult extends DependencyInjectionResult, Result {
        
        /**
         * Check whether <code>element</code> is alternative.
         * <code>element</code> could be eligible for injection element
         * ( which is found as result here ) or stereotype. 
         * @param element checked element 
         * @return true if <code>element</code> is alternative
         */
        boolean isAlternative( Element element );
        
        boolean hasAlternative( Element element );
    }
    
    interface InjectableResult extends DependencyInjectionResult {
        /**
         * <code>null</code> is returned if there is no eligible element for injection
         * ( no element which could be a pretender).
         * 
         * it could be a result of unsatisfied or ambiguous dependency.
         * F.e. unsatisfied dependency : there is a pretender satisfy typesafe 
         * resolution but something incorrect ( parameterized type is not valid , etc. ). 
         * Ambiguous dependency : there are a number of appropriate elements.
         *
         * 
         * @return element ( type definition, production field/method) 
         * that is used in injected point identified by {@link #getVariable()}
         */
        Element getElement();
    }
    
    interface ApplicableResult extends DependencyInjectionResult, BeansResult {
        public Set<TypeElement> getTypeElements();
        
        public Set<Element> getProductions();
    }
}
