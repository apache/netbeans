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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.IdentifiableElement;
import org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean;
import org.netbeans.modules.web.jsf.api.facesmodel.Redirect;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author Petr Pisl, ads
 */
public enum FacesAttributes implements Attribute{
    ID(IdentifiableElement.ID , String.class),
    VERSION( FacesConfig.VERSION , String.class),
    METADATA_COMPLETE( FacesConfig.METADATA_COMPLETE, Boolean.class ),
    EAGER( ManagedBean.EAGER , Boolean.class ),
    INCLUDE_VIEW_PARAMS( Redirect.INCLUDE_VIEW_PARAMS, Boolean.class),
    LANG("xml:lang", String.class);
    
    private String name;
    private Class type;
    
    FacesAttributes(String name){
        this(name, String.class );
    }
    
    FacesAttributes(String name, Class type){
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public Class getMemberType() {
        return null;
    }
    
}
