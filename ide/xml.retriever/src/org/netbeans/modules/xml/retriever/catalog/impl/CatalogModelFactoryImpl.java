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
package org.netbeans.modules.xml.retriever.catalog.impl;

import java.io.IOException;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.openide.filesystems.FileObject;
import org.w3c.dom.ls.LSResourceResolver;

/**
 *
 * @author girix
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.locator.CatalogModelFactory.class)
public class CatalogModelFactoryImpl extends CatalogWriteModelFactory{
    private static Logger logger = Logger.getLogger(CatalogModelFactoryImpl.class.getName());
    
    
    private static WeakHashMap <Project, CatalogWriteModel> projcat = new WeakHashMap<Project, CatalogWriteModel>();
    
    private static WeakHashMap <FileObject, CatalogWriteModel> foCat = new WeakHashMap<FileObject, CatalogWriteModel>();
    
    private static int count = 0;
    
    public CatalogWriteModel getCatalogWriteModelForProject(FileObject anyFileObjectExistingInAProject) throws CatalogModelException {
        logger.entering("CatalogModelFactoryImpl","getCatalogModelForProject");
        Project project = FileOwnerQuery.getOwner(anyFileObjectExistingInAProject);
        assert(project != null);
        CatalogWriteModel result = null;
        try {
            CatalogWriteModel cwm = foCat.get(Utilities.getProjectCatalogFileObject(project));
            if(cwm != null){
                return cwm;
            }
            result = new XAMCatalogWriteModelImpl(project);
            foCat.put(result.getCatalogFileObject(), result);
            return result;
        } catch (IOException ex) {
            throw new CatalogModelException(ex);
        }
    }
    
    
    private static final WeakHashMap <Project, CatalogModel> proj2cm = new WeakHashMap<Project, CatalogModel>();
    
    public CatalogModel getCatalogModel(ModelSource modelSource) throws CatalogModelException {
        if(modelSource == null)
            throw new IllegalArgumentException("modelSource arg is null.");
        CatalogModel catalogModel = modelSource.getLookup().lookup(CatalogModel.class);
        if(catalogModel == null){
            FileObject fo = modelSource.getLookup().lookup(FileObject.class);
            if(fo == null)
                throw new IllegalArgumentException("ModelSource must have FileObject in its lookup");
            return getCatalogModel(fo);
        }
        return catalogModel;
    }
    
    public CatalogModel getCatalogModel(FileObject fo) throws CatalogModelException{
        CatalogModel catalogModel = null;
        Project project = FileOwnerQuery.getOwner(fo);
        if(project != null){
            synchronized (proj2cm) {
                catalogModel = proj2cm.get(project);
            }
            if(catalogModel != null) {
                return catalogModel;
            }
            try {
                // note: the CMI does not reference the project, just extracts a project catalog FO from it.
                catalogModel = new CatalogModelImpl(project);
            } catch (IOException ex) {
                throw new CatalogModelException(ex);
            }
            synchronized (proj2cm) {
                CatalogModel cm2 = proj2cm.put(project, catalogModel);
                if (cm2 != null) {
                    // return the already escaped instanc eback
                    proj2cm.put(project, cm2);
                    return cm2;
                }
            }
            return catalogModel;
        }
        catalogModel = new CatalogModelImpl();
        return catalogModel;
    }
    
    public LSResourceResolver getLSResourceResolver() {
        return new LSResourceResolverImpl();
    }
    
    public CatalogWriteModel getCatalogWriteModelForCatalogFile(FileObject fileObjectOfCatalogFile) throws CatalogModelException {
        try{
            return new XAMCatalogWriteModelImpl(fileObjectOfCatalogFile);
        } catch (IOException ex) {
            throw new CatalogModelException(ex);
        }
    }
}
