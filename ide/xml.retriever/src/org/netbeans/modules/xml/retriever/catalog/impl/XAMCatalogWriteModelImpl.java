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
 * XAMCatalogWriteModelImpl.java
 *
 * Created on December 14, 2006, 12:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogModel;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogModelFactory;
import org.netbeans.modules.xml.retriever.catalog.model.NextCatalog;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.modules.xml.retriever.catalog.model.System;
import org.openide.loaders.DataObject;

/**
 *
 * @author girix
 */
public class XAMCatalogWriteModelImpl extends CatalogModelImpl implements CatalogWriteModel {
    
    protected XAMCatalogWriteModelImpl(Project prj) throws CatalogModelException, IOException{
        super(prj);
        modelSource = createModelSource(super.catalogFileObject);
    }
    
    /**
     * Constructor for creating a CatalogWriteModel for the given file object.
     * The catalogFileObject should not be null
     */
    protected XAMCatalogWriteModelImpl(FileObject catalogFileObject) throws CatalogModelException, IOException{
        super(catalogFileObject);
        modelSource = createModelSource(super.catalogFileObject);
    }
    
    
    //for unit tests
    boolean unitTestSaveStrategy = false;
    public  XAMCatalogWriteModelImpl(File myProjectRootFile) throws IOException, CatalogModelException{
        super(myProjectRootFile);
        unitTestSaveStrategy = true;
        modelSource = createModelSource(super.catalogFileObject);
    }
    
    
    private CatalogModel catalogModel;
    private ModelSource modelSource;
    
    /**
     * This method will be called by the constructor for instantiating the protected
     * object "modelSource" (that is returned by getModelSource()).
     * Since, this impl is based on XAM, it has a dependency on ModelSource. ModelSource's lookup
     * must contain at the very least FileObject and javax.swing.Document objects for XAM to work.
     * NOTE: Unit test env needs to overwrite this method and return proper test env model source.
     * and also the ModelSource must contain proper Swing document and FileObject in the lookup.
     **/
    protected ModelSource createModelSource(FileObject catFileObject) throws CatalogModelException {
        return Utilities.createModelSource(super.catalogFileObject, true);
        
    }
    
    
    
    public URI searchURI(URI locationURI) {
        if(locationURI == null)
            return null;
        URI strRes = null;
        if(catalogFileObject != null){
            //look up in the global catalog
            File publicCatalogFile = FileUtil.toFile(catalogFileObject);
            if(publicCatalogFile.isFile()){
                try {
                    strRes = resolveUsingApacheCatalog(publicCatalogFile, locationURI.toString());
                } catch (IOException ex) {
                    return null;
                } catch (CatalogModelException ex) {
                    return null;
                }
            }
        }
        return strRes;
    }
    
    public synchronized void addURI(URI locationURI, FileObject fileObj) throws IOException {
        URI fileObjURI = FileUtil.toFile(fileObj).toURI();
        addURI(locationURI, fileObjURI);
    }
    
    public synchronized void addURI(URI locationURI, URI fileObjURI) throws IOException {
        if(this.catalogFileObject == null)
            return;
        //remove the old entry if exists
        removeURI(locationURI);
        
        URI master = FileUtil.toFile(this.catalogFileObject).toURI();
        
        String finalDestStr = Utilities.relativize(master, fileObjURI);
        URI finalDestStrURI;
        try {
            finalDestStrURI = new URI(finalDestStr);
        } catch (URISyntaxException ex) {
            throw new IOException("Invalid URI: "+finalDestStr);
        }
        
        System sys = getCatalogModel().getFactory().createSystem();
        
        getCatalogModel().startTransaction();
        try{
            getCatalogModel().getRootComponent().addSystem(sys);
            sys.setSystemIDAttr(locationURI);
            sys.setURIAttr(finalDestStrURI);
        }finally{
            getCatalogModel().endTransaction();
        }
        save();
    }
    
    public void removeURI(URI locationURI) throws IOException {
        System delete = null;
        getCatalogModel().sync();
        for(System sys: getCatalogModel().getRootComponent().getSystems()){
            if(sys.getSystemIDAttr().equals(locationURI.toString())){
                delete = sys;
                break;
            }
        }
        if(delete != null){
            getCatalogModel().startTransaction();
            try{
                getCatalogModel().getRootComponent().removeSystem(delete);
            }finally{
                getCatalogModel().endTransaction();
            }
            save();
        }
    }
    
    public Collection<CatalogEntry> getCatalogEntries() {
        ArrayList<CatalogEntry> result = new ArrayList<CatalogEntry>();
        if(getCatalogModel() == null || getCatalogModel().getRootComponent() == null)
            return result;
        for(System sys: getCatalogModel().getRootComponent().getSystems()){
            CatalogEntry catEnt = new CatalogEntryImpl(CatalogElement.system, sys.getSystemIDAttr(),
                    sys.getURIAttr());
            result.add(catEnt);
        }
        for(NextCatalog nc: getCatalogModel().getRootComponent().getNextCatalogs()){
            CatalogEntry catEnt = new CatalogEntryImpl(CatalogElement.nextCatalog, nc.getCatalogAttr(),
                    null);
            result.add(catEnt);
        }
        return result;
    }
    
    public boolean isWellformed() {
        return getCatalogModel().getState().equals(Model.State.VALID);
    }
    
    public org.netbeans.modules.xml.xam.dom.DocumentModel.State getState() {
        return getCatalogModel().getState();
    }
    
    public FileObject getCatalogFileObject() {
        return super.catalogFileObject;
    }
    
    public void addPropertychangeListener(PropertyChangeListener pcl) {
        getCatalogModel().addPropertyChangeListener(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        getCatalogModel().removePropertyChangeListener(pcl);
    }
    
    public void addNextCatalog(URI nextCatalogFileURI, boolean relativize) throws IOException {
        if(this.catalogFileObject == null)
            return;
        
        String nextCatalogFileURIStr = nextCatalogFileURI.toString();
        if(nextCatalogFileURI.isAbsolute() && relativize){
            //then resolve URI relative to this catalog file
            nextCatalogFileURIStr = Utilities.relativize(FileUtil.toFile(this.catalogFileObject).
                    toURI(), nextCatalogFileURI);
        }
        try {
            nextCatalogFileURI = new URI(nextCatalogFileURIStr);
        } catch (URISyntaxException ex) {
            throw new IOException("Invalid URI: "+nextCatalogFileURIStr);
        }
        
        try {
            removeNextCatalog(nextCatalogFileURI);
        } catch (IOException ex) {
        }
        
        NextCatalog nc = getCatalogModel().getFactory().createNextCatalog();
        getCatalogModel().startTransaction();
        try{
            getCatalogModel().getRootComponent().addNextCatalog(nc);
            nc.setCatalogAttr(nextCatalogFileURI);
        }finally{
            getCatalogModel().endTransaction();
        }
        save();
    }
    
    public void removeNextCatalog(URI nextCatalogFileRelativeURI) throws IOException {
        NextCatalog delete = null;
        for(NextCatalog nc: getCatalogModel().getRootComponent().getNextCatalogs()){
            if(nc.getCatalogAttr().equals(nextCatalogFileRelativeURI.toString())){
                delete = nc;
                break;
            }
        }
        if(delete != null){
            getCatalogModel().startTransaction();
            try{
                getCatalogModel().getRootComponent().removeNextCatalog(delete);
            }finally{
                getCatalogModel().endTransaction();
            }
            save();
        }
    }
    
    protected void save() {
        FileObject fo = (FileObject) getCatalogModel().getModelSource().getLookup().lookup(FileObject.class);
        try {
            DataObject dobj = DataObject.find(fo);
            SaveCookie saveCookie = (SaveCookie) dobj.getCookie(SaveCookie.class);
            if(saveCookie != null)
                saveCookie.save();
        } catch (IOException ex) {
        }
    }
    
    public CatalogModel getCatalogModel() {
        return CatalogModelFactory.getInstance().getModel(getModelSource());
    }
    
    public void setCatalogModel(CatalogModel catalogModel) {
        this.catalogModel = catalogModel;
    }
    
    public ModelSource getModelSource() {
        return modelSource;
    }
}
