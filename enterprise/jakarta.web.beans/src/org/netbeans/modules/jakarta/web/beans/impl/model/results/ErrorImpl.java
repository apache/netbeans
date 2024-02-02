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
package org.netbeans.modules.jakarta.web.beans.impl.model.results;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.netbeans.modules.jakarta.web.beans.api.model.DependencyInjectionResult.Error;


/**
 * @author ads
 *
 */
public class ErrorImpl extends BaseResult implements Error {

    public ErrorImpl( VariableElement var, TypeMirror type,
            String error ) 
    {
        super(var, type);
        myMessage  =error;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.Result.Error#getMessage()
     */
    @Override
    public String getMessage(){
        return myMessage;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.api.model.Result#getKind()
     */
    @Override
    public ResultKind getKind() {
        return ResultKind.RESOLUTION_ERROR;
    }

    private String myMessage;
}
