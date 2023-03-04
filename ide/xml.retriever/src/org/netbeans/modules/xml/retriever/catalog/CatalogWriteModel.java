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

/*
 * CatalogModel.java
 *
 * Created on October 11, 2005, 1:11 AM
 */

package org.netbeans.modules.xml.retriever.catalog;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.*;
import org.openide.filesystems.FileObject;

/**
 * API interface for all the operations exposed
 * by the CatalogModel. There will be one Catalog file per Project.
 * @author girix
 */
public interface CatalogWriteModel extends CatalogModel {
    
    public static final String CATALOG_FILE_EXTENSION = ".xml";
    
    public static final String PUBLIC_CATALOG_FILE_NAME = "catalog";
    
    /**
     * Given the location parameter (schemaLocation for schema and location for wsdl)
     * this method should return the parget URI after looking up in the public catalog file
     * This method will just look up in the public catalog file and return result.
     * If not found in the catalog a null will be returned.
     *
     * @param locationURI
     * @return URI
     */
    public URI searchURI(URI locationURI);
    
    
    /**
     * Adds an URI to FileObject (in the same project) mapping in to the catalog.
     * URI already present will be overwritten.
     *
     * This call might throw IllegalStateException if the catalog files are corrupted.
     * Call isResolverStateValid() before calling this method to detect and avoid above exception
     *
     * @param locationURI
     * @param fileObj
     */
    public void addURI(URI locationURI, FileObject fileObj) throws IOException;
    
    /**
     * Adds an URI to URI mapping in to the catalog.
     * URI already present will be overwritten.
     *
     * This call might throw IllegalStateException if the catalog files are corrupted.
     * Call isResolverStateValid() before calling this method to detect and avoid above exception
     *
     * @param locationURI
     * @param alternateURI
     */
    
    public void addURI(URI locationURI, URI alternateURI) throws IOException;
    
    /**
     * Remove a URI from the catalog.
     * @param locationURI  - locationURI to be removed.
     */
    public void removeURI(URI locationURI) throws IOException;
    
    
    /**
     * Returns list of all registered catalog entries
     *
     * This call might throw IllegalStateException if the catalog files are corrupted.
     * Call isResolverStateValid() before calling this method to detect and avoid above exception
     */
    public Collection<CatalogEntry> getCatalogEntries();
    
    
    /**
     * This method tell if the resolver is in a sane state to retrive the correct values.
     * If false is returned means there is some problem with the resolver. For more information
     * call getState() to get the exact status message. This method should be called before calling
     * most of the resolver methods.
     */
    public boolean isWellformed();
    
    
    /**
     * Returns the current satus of the resolver.
     * Consult the return value and display appropriate messages to the user
     */
    public DocumentModel.State getState();
    
    
    /**
     * Returns the FileObject of the catalog file that this object is bound to.
     */
    public FileObject getCatalogFileObject();
    
    public void addPropertychangeListener(PropertyChangeListener pcl);
    
    public void removePropertyChangeListener(PropertyChangeListener pcl);
    
    
    /**
     * Adds nextCatalogFileURI to the catalog file as nextCatalog entry. If
     * relativize is true and nextCatalogFileURI is absolute, then nextCatalogFileURI is
     * relativized against this catalog file URI itself before writing.
     */
    public void addNextCatalog(URI nextCatalogFileURI, boolean relativize)  throws IOException;
    
    public void removeNextCatalog(URI nextCatalogFileRelativeURI)  throws IOException;
    
    
}
