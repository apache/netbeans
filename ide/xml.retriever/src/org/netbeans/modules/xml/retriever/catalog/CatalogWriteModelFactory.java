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
package org.netbeans.modules.xml.retriever.catalog;

import org.netbeans.modules.xml.retriever.catalog.impl.CatalogModelFactoryImpl;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModelFactory;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Nam Nguyen
 */
public abstract class CatalogWriteModelFactory extends CatalogModelFactory {
    
    /**
     * Given a FileObject that belongs to a project this method will return a
     * CatalogModel object specific to it.
     * If there are initialization errors, CatalogModelException will be thrown.
     * If FileOwnerQuery.getOwner(anyFileObjectExistingInAProject); returns null
     * then assersion error will occur.
     * @param anyFileObjectExistingInAProject any FileObject inside a project for which CatalogModel is needed
     * @throws org.netbeans.modules.xml.xam.locator.api.CatalogModelException
     */
    public abstract CatalogWriteModel getCatalogWriteModelForProject(FileObject anyFileObjectExistingInAProject) throws CatalogModelException;
    
     /**
     * Given a FileObject this method will return a CatalogWriteModel object specific to it.
     * If there are initialization errors, CatalogModelException will be thrown.
     * @param fileObjectOfCatalogFile any FileObject on which the catalog entries have to be created or appended.
     * @throws org.netbeans.modules.xml.xam.locator.api.CatalogModelException
     */
    public abstract CatalogWriteModel getCatalogWriteModelForCatalogFile(FileObject fileObjectOfCatalogFile) throws CatalogModelException;
    

    private static CatalogWriteModelFactory implObj = null;
    
    public static CatalogWriteModelFactory getInstance(){
        if(implObj == null) {
            implObj = new CatalogModelFactoryImpl();
        }
        return implObj;
    }
}
