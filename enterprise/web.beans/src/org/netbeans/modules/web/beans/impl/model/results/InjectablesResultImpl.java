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
package org.netbeans.modules.web.beans.impl.model.results;

import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.Element;

import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ApplicableResult;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ResolutionResult;


/**
 * @author ads
 *
 */
public class InjectablesResultImpl extends ResultImpl implements 
        ResolutionResult, ApplicableResult
{
    
    public InjectablesResultImpl( ResultImpl origin, Set<Element> enabledBeans)
    {
        super(origin.getVariable(), origin.getVariableType(), 
                origin.getTypeElements(), origin.getProductions(), 
                origin.getHelper());
        myEnabled = enabledBeans;
    }

    public InjectablesResultImpl( ResultImpl origin ) {
        super(origin.getVariable(), origin.getVariableType(), 
                origin.getTypeElements(), origin.getProductions(), 
                origin.getHelper());
        myEnabled =Collections.emptySet();
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result.ApplicableResult#isDisabled(javax.lang.model.element.Element)
     */
    @Override
    public boolean isDisabled( Element element ) {
        return !myEnabled.contains( element );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.results.ResultImpl#getKind()
     */
    @Override
    public ResultKind getKind() {
        return ResultKind.INJECTABLES_RESOLVED;
    }

    private final Set<Element> myEnabled;
}
