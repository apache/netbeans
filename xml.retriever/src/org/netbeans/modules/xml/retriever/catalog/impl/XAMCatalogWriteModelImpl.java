/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
