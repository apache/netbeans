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
import org.netbeans.modules.websvc.rest.model.api.SubResourceLocator;
import org.netbeans.modules.websvc.rest.model.impl.RestServicesImpl.Status;

/**
 *
 * @author Peter Liu
 */
public class SubResourceLocatorImpl extends RestMethodDescriptionImpl
        implements SubResourceLocator {
    
    private String uriTemplate;
    
    public SubResourceLocatorImpl(ExecutableElement methodElement) {
        super(methodElement);
        
        this.uriTemplate = Utils.getUriTemplate(methodElement);
    }
    
    public String getUriTemplate() {
        return uriTemplate;
    }
    
    public String getResourceType() {
        return null;
    }
    
    public Status refresh(Element element) {
        boolean isModified = false;
        
        if (super.refresh(element) == Status.MODIFIED) {
            isModified = true;
        }
        
        if (!Utils.hasUriTemplate(element)) {
            return Status.REMOVED;
        }
        
        String newValue = Utils.getUriTemplate(element);
        if (!uriTemplate.equals(newValue)) {
            uriTemplate = newValue;
            isModified = true;
        }
        
        if (isModified) {
            return Status.MODIFIED;
        }
        
        return Status.UNMODIFIED;
    }
}
