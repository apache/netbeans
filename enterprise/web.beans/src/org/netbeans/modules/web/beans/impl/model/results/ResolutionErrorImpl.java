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

import java.util.Set;

import javax.lang.model.element.Element;

import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ApplicableResult;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.Error;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult.ResolutionResult;


/**
 * @author ads
 *
 */
public class ResolutionErrorImpl extends InjectablesResultImpl implements Error,
        ResolutionResult, ApplicableResult
{
    
    public ResolutionErrorImpl( ResultImpl origin, String message , 
            Set<Element> enabledBeans)
    {
        super( origin , enabledBeans );
        myMessage = message;
    }

    public ResolutionErrorImpl( ResultImpl origin, String message ) {
        super( origin );
        myMessage = message;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.api.model.Result.Error#getMessage()
     */
    @Override
    public String getMessage() {
        return myMessage;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.results.ResultImpl#getKind()
     */
    @Override
    public ResultKind getKind() {
        return ResultKind.RESOLUTION_ERROR;
    }
    
    private final String myMessage;

}
