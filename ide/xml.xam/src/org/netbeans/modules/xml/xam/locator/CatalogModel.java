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

/*
 * Locator.java
 *
 * Created on March 29, 2006, 3:44 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xam.locator;

import java.net.URI;
import org.netbeans.modules.xml.xam.ModelSource;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;

/**
 *
 * @author girix
 */
public interface CatalogModel extends EntityResolver, LSResourceResolver{
    
    /**
     * Given the location parameter (schemaLocation for schema and location for wsdl)
     * this method should return ModelSource object containing the concrete FileObject
     * of the locally stored file. This method will just look up in the public catalog file
     * and return result. It Will NOT do relative path resolution.
     *
     * @param locationURI
     * @return ModelSource
     * @throws CatalogModelException
     * CatalogModelException will be throw for the following:
     * <ol>
     * <li>If the file that was supposed to be there but not found. This case a
     * FileNotFoundException is wrapped inside</li>
     * <li>If a (java)File object could not be created from the retrived catalog
     * entry.URISyntaxException will be wrapped inside DepResolverException.</li>
     * <li>IOException will be wrapped around if a (NB)FileObject could not be
     * created from the File object for various reasons by NB module</li>
     * </ol>
     */
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException;
    
    
    /**
     * Given the location parameter (schemaLocation for schema and location for wsdl)
     * this method should return ModelSource object containing the concrete FileObject
     * of the locally stored file. This method will just look up in the public catalog file
     * and return result. If not found in the catalog will then do relative path resolution
     * against modelSourceOfSourceDocument's FileObject. Relative locations should be resolved using this method
     *
     * @param locationURI
     * @param modelSourceOfSourceDocument
     * @return ModelSource
     * @throws CatalogModelException
     * CatalogModelException will be throw for the following:
     * <ol>
     * <li>If the file that was supposed to be there but not found. This case a
     * FileNotFoundException is wrapped inside</li>
     * <li>If a (java)File object could not be created from the retrived catalog
     * entry.URISyntaxException will be wrapped inside DepResolverException.</li>
     * <li>IOException will be wrapped around if a (NB)FileObject could not be
     * created from the File object for various reasons by NB module</li>
     * </ol>
     */
    public ModelSource getModelSource(URI locationURI, ModelSource modelSourceOfSourceDocument) throws CatalogModelException;
    
}
