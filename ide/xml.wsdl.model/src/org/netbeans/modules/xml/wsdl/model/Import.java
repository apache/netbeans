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

package org.netbeans.modules.xml.wsdl.model;

import org.netbeans.modules.xml.xam.locator.CatalogModelException;

/**
 *
 * @author rico
 * Represents an import wsdl statement to import other namespaces.
 */
public interface Import extends WSDLComponent {
    public static final String NAMESPACE_URI_PROPERTY = "namespaceURI";
    public static final String LOCATION_PROPERTY = "location";
    
    void setNamespace(String namespaceURI);
    String getNamespace();
    
    void setLocation(String locationURI);
    String getLocation();
    
    /**
     * Returns the imported WSDL model.
     *
     * @return a WSDL model object if the import location or namespace resolves 
     * into a model source and the model source is well-formed; 
     * @throws CatalogModelException if location or namespace values cannot resolve;
     */    
    WSDLModel getImportedWSDLModel() throws CatalogModelException;
}
