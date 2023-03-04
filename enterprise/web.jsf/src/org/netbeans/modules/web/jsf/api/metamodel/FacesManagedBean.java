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
package org.netbeans.modules.web.jsf.api.metamodel;

import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;


/**
 * @author ads
 *
 */
public interface FacesManagedBean extends JsfModelElement {
    
    String MANAGED_BEAN_NAME = JSFConfigQNames.MANAGED_BEAN_NAME.getLocalName();
    
    String EAGER = "eager";

    Boolean getEager();
    
    String getManagedBeanName();
    
    String getManagedBeanClass();
    
    /**
     * Obtaining scope for the managed bean
     * @return The scope of the managed bean.  null
     * is returned if in the document is not supported value.
     * "scope" property could be also a EL expression.
     * In the latter case one should use method 
     * {@link #getManagedBeanScopeString()} 
     */
    ManagedBean.Scope getManagedBeanScope();
    
    /**
     * Accessor methods for scope property as string.
     * Required to use in case of scope as EL expression. 
     * @return
     */
    String getManagedBeanScopeString();
    
    List<ManagedProperty> getManagedProperties();
}
