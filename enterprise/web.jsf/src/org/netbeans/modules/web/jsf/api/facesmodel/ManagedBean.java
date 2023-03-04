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

package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;

import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.api.metamodel.ManagedProperty;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "managed-bean" element represents a JavaBean, of a
 * particular class, that will be dynamically instantiated
 * at runtime (by the default VariableResolver implementation)
 * if it is referenced as the first element of a value binding
 * expression, and no corresponding bean can be identified in
 * any scope.  In addition to the creation of the managed bean,
 * and the optional storing of it into the specified scope,
 * the nested managed-property elements can be used to
 * initialize the contents of settable JavaBeans properties of
 * the created instance.
 * @author Petr Pisl, ads
 */
public interface ManagedBean extends FacesConfigElement, DescriptionGroup, 
    IdentifiableElement , FacesManagedBean
{
    /**
     * Defines the legal values for the &lt;managed-bean-scope&gt;
     * element's body content, which includes all of the scopes
     * normally used in a web application, plus the "none" value
     * indicating that a created bean should not be stored into
     * any scope.
     */
    public enum Scope{
        REQUEST("request"),
        SESSION("session"),
        APPLICATION("application"),
        VIEW("view"),
        NONE("none");
        
        private String scope;
        
        Scope(String scope){
            this.scope = scope;
        }
        
        public String toString(){
            return scope;
        }
    }
    
    String MANAGED_BEAN_CLASS = JSFConfigQNames.MANAGED_BEAN_CLASS.getLocalName();
    String MANAGED_BEAN_SCOPE = JSFConfigQNames.MANAGED_BEAN_SCOPE.getLocalName();
    String MANAGED_BEAN_EXTENSION = JSFConfigQNames.MANAGED_BEAN_EXTENSION.getLocalName();
    String MANAGED_PROPERTY = JSFConfigQNames.MANAGED_PROPERTY.getLocalName();
    String MAP_ENTRIES = JSFConfigQNames.MAP_ENTRIES.getLocalName();
    String LIST_ENTRIES = JSFConfigQNames.LIST_ENTRIES.getLocalName();
    
    void setManagedBeanName(String name);
    
    void setManagedBeanClass(String beanClass);
    
    void setManagedBeanScope(ManagedBean.Scope scope);
    
    void setManagedBeanScope( String scope);
    
    List<ManagedBeanExtension> getManagedBeanExtensions();
    void addManagedBeanExtension( ManagedBeanExtension  extension );
    void removeManagedBeanExtension( ManagedBeanExtension extension );
    void addManagedBeanExtension( int index , ManagedBeanExtension extension );
    
    List<ManagedBeanProps> getManagedProps();
    void addManagedBeanProps( ManagedBeanProps props );
    void removeManagedBeanProps( ManagedBeanProps props );
    void addManagedBeanProps( int index , ManagedBeanProps props );
    
    void setEager( Boolean eager );
}
