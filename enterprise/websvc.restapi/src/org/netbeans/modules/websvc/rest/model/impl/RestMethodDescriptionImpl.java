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

package org.netbeans.modules.websvc.rest.model.impl;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;

/**
 *
 * @author Peter Liu
 */
public abstract class RestMethodDescriptionImpl {
    private String name;
    private String returnType;
    
    public RestMethodDescriptionImpl(ExecutableElement methodElement) {       
        this.name = methodElement.getSimpleName().toString();
        this.returnType = methodElement.getReturnType().toString();
    }
    
    public String getName() {
        return name;
    }
    
    public String getReturnType() {
        return returnType;
    }
    
    public Status refresh(Element element) {
        ExecutableElement methodElement = (ExecutableElement) element;
        boolean isModified = false;
        
        String newValue = methodElement.getSimpleName().toString();
        if (!name.equals(newValue)) {
            name = newValue;
            isModified = true;
        }
        
        newValue = methodElement.getReturnType().toString();
        if (!returnType.equals(newValue)) {
            returnType = newValue;
            isModified = true;
        }
        
        if (isModified) {
            return Status.UNMODIFIED;
        }
        
        return Status.UNMODIFIED;
    }
}
