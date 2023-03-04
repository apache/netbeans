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
 * ProjectCatalogSupport.java
 *
 * Created on December 15, 2006, 4:23 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.catalog;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;

/**
 * Extend this class to implement custom URIs that could be added and resolved by the project CatalogModel(s).
 * The project catalog resolver (org.netbeans.modules.xml.retriever.catalog.CatalogModel 
 * and org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel) will look for 
 * this implementation in the Project.lookup(). When present any URI that is not understood by the 
 * resolver will be deligated to the impl of this class. 
 * 
 * One of the usecase of this class is when a special URI has to be written in to the
 * catalog file by a project that is referencing artifacts from another dependent sub-project.
 *
 * @author girix
 */
public abstract class ProjectCatalogSupport {
    
    /** Creates a new instance of ProjectCatalogSupport */
    public ProjectCatalogSupport() {
    }
    
    /**
     * This method will be called by the CatalogWriteModel if the FileObject passed
     * in the org.netbeans.modules.xml.retriever.catalog.addURI(URI LEFT, FileObject rightFO);
     * does not belong to same project as of the catalog. The impl of this class must
     * return a URI similar to the following:
     * sample URI = nb-uri:project.project_identifier_name/src/a/b/c/xyz.xsd
     *
     * @param foTobeAddedInCat FileObject that is supposed to be added to the catalog file of the project
     * @return URI constructed for the passed FO. A URI could look something like this: nb-uri:project.project_identifier_name/src/a/b/c/xyz.xsd 
     */
    public abstract URI constructProjectProtocol(FileObject foTobeAddedInCat);
    
    
    /**
     * This method is called when the CatalogModel looks in to the catalog file and
     * gets back a URI that is not a standard URI. If the parameter URI is
     * understood by the impl of this class, return true, else false.
     * 
     * This method is for extension purpose. If a project wants to add a new sort of
     * URI in to the catalog file in the future, it will have to implement this method.
     *
     * @param uriStoredInCatFile URI found in the catalog file.
     * @return True if this URI is understood by this impl. False otherwise.
     */
    
    public abstract  boolean isProjectProtocol(URI uriStoredInCatFile);
    
    
    
    /**
     * This method will be called after isProjectProtocol(URI) returns true. 
     * The impl has to interpret the URI parameter (that looks similar to sample 
     * URI mentioned above) and get the equivalent FileObject associated with that 
     * URI. A null should be returned in case if the file is not found on 
     * disk (and hence can not create corresponding FileObject)
     *
     * @param uriToBeResolved URI that is to be interpreted by the impl.
     * @return FileObject associated with the URI.
     */
    public abstract FileObject resolveProjectProtocol(URI uriToBeResolved);
    
    
    
    /**
     * This method should add catalog entry in to the project catalog given 
     * 2 file objects. The impl muse calculate the key and value pair for the
     * catalog entry and call CatalogWriteModel for adding the entry. The URI 
     * returned must be the calculated key of the catalog entry.
     * @param source The FileObject that is making this entry to the catalog. This arg may be 
     * used for calculating the Key of the catalog entry.
     * @param target The FileObject that is referenced by this indirection via catalog entry.
     * @return The key (Left Hand Side) of the catalog entry that was added new in to the catalog file. If nothing was added then null.
     */
    public abstract URI createCatalogEntry(FileObject source, FileObject target) throws
            CatalogModelException, IOException ;
    
    /**
     * This method should implement the implementation logic for removing a catalog entry with the parameter URI as the key (LHS).
     * The impl must verify if there entry is indeed present in the catalog and then do a delete.
     * @return True if the delete was successful false otherwise.
     * @param uri URI to be removed from the catalog.
     */
    public abstract boolean removeCatalogEntry(URI uri) throws IOException;
    
}
