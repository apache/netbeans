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

package org.netbeans.modules.xml.retriever.catalog.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogElement;
import org.netbeans.modules.xml.retriever.catalog.CatalogEntry;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class CatalogWriteModelImpl extends CatalogModelImpl implements CatalogWriteModel{
    
    public static final String PROPERTY_CHANGE_PROPERTY_KEY= "CatalogModelImpl.PropertyChange.Property";
    
    PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private static Logger logger = Logger.getLogger(CatalogWriteModelImpl.class.getName());
    
    private DocumentModel.State currentStateOfCatalog;
    
    private CatalogFileWrapper catalogWrapper = null;
    
    /**
     * Constructor for public catalog files
     */
    protected CatalogWriteModelImpl(Project prj) throws IOException{
        super(prj);
    }
    
    /**
     * Constructor for creating a CatalogWriteModel for the given file object.
     * The catalogFileObject should not be null
     */
    protected CatalogWriteModelImpl(FileObject catalogFileObject) throws IOException{
        super(catalogFileObject);
    }
    
    
    //for unit tests
    boolean unitTestSaveStrategy = false;
    public  CatalogWriteModelImpl(File myProjectRootFile) throws IOException{
        super(myProjectRootFile);
        unitTestSaveStrategy = true;
    }
    //For unit tests
    public CatalogWriteModelImpl(){
        
    }
    
    public URI searchURI(URI locationURI){
        if(locationURI == null)
            return null;
        bootStrapCatalog();
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
    
    
    public DocumentModel.State getState(){
        return currentStateOfCatalog;
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
        
        bootStrapCatalog();
        
        URI master = FileUtil.toFile(this.catalogFileObject).toURI();
        
        String finalDestStr = Utilities.relativize(master, fileObjURI);
        CatalogEntry catEnt = new CatalogEntryImpl(CatalogElement.system, locationURI.toString(), finalDestStr);
        catalogWrapper.addSystem(catEnt);
    }
    
    
    /*public void addURI(URI locationURI, FileObject fileObj, FileObject referringFileObject) throws IOException {
        HashMap<String, String> extraAttrs = calculateExtraAttributes(fileObj, referringFileObject);
        if(extraAttrs == null){
            addURI(locationURI, fileObj);
            return;
        }
        
        if(this.catalogFileObject == null)
            return;
        //remove the old entry if exists
        //TO DO: Handle multiple files refering same URI from same dir
        //removeURI(locationURI);
        
        bootStrapCatalog();
        
        URI master = FileUtil.toFile(this.catalogFileObject).toURI();
        URI fileObjURI = FileUtil.toFile(fileObj).toURI();
        
        String finalDestStr = Utilities.relativize(master, fileObjURI);
        CatalogEntry catEnt = new CatalogEntryImpl(CatalogElement.system,
                locationURI.toString(), finalDestStr, extraAttrs);
        catalogWrapper.addSystem(catEnt);
        
    }*/
    
    
    /*protected HashMap<String, String> calculateExtraAttributes(FileObject fileObj,  FileObject referringFileObject){
        /*Get the FO of this URI
         *Find out if FO belongs to this project
         * Yes : return empty map
         * No: and belongs to some project which has common path, calculate extra attributes
         *     xprojectCatalogFileLocation and referencingFiles
         */
        /*if(fileObj == null)
            return null;
        Project foprj = FileOwnerQuery.getOwner(fileObj);
        Project myprj = FileOwnerQuery.getOwner(this.catalogFileObject);
        
        if(myprj == null)
            return null;
        if(myprj.equals(foprj))
            return null;
        if(foprj == null)
            return null;
        
        //get the path to the cross proj catalog
        URI master = FileUtil.toFile(this.catalogFileObject).toURI();
        FileObject catFO = null;
        try {
            catFO = Utilities.getCatalogFile(fileObj);
        } catch (IOException ex) {
        }
        if(catFO == null)
            return null;
        
        URI catURI = FileUtil.toFile(catFO).toURI();
        
        //relativize to this cat file
        String catStr = Utilities.relativize(master, catURI).toString();
        
        HashMap <String, String> result = new HashMap<String, String>();
        
        result.put(CatalogAttribute.xprojectCatalogFileLocation.toString(), catStr);
        
        
        URI refURI = FileUtil.toFile(referringFileObject).toURI();

        //relativize to this cat file
        String refStr = Utilities.relativize(master, refURI).toString();
        
        result.put(CatalogAttribute.referencingFiles.toString(), refStr);
        
        return result;
        
    }*/
    
    
    public String toString(){
        return "This Public Catalog FO:"+this.catalogFileObject;
    }
    
    public synchronized void removeURI(URI locationURI) throws IOException {
        logger.finer("ENTRING:"+locationURI);
        if(this.catalogFileObject == null)
            return;
        bootStrapCatalog();
        List<CatalogEntry> catEntList = catalogWrapper.getSystems();
        if(catEntList == null)
            return;
        CatalogEntry remVal = null;
        for(CatalogEntry catEnt : catEntList){
            if(catEnt.getSource().equals(locationURI.toString()))
                remVal = catEnt;
        }
        logger.finer("Removing Value: "+remVal);
        if(remVal == null)
            return;
        int index = catEntList.indexOf(remVal);
        catalogWrapper.deleteSystem(index);
        if(catEntList.size() == 1){
            //file has no entry, so just delete it
            logger.finer("There are no more entries so removing catalog file");
            //publicCatWrap.cleanInstance();
            //publicCatWrap = null;
        }
        logger.finer("RETURN: "+catEntList.size());
    }
    
    public Collection<CatalogEntry> getCatalogEntries() {
        if(this.catalogFileObject == null)
            return Collections.emptyList();
        bootStrapCatalog();
        List<CatalogEntry> catEntList = catalogWrapper.getSystems();
        if(catEntList == null)
            return Collections.emptyList();
        for(CatalogEntry catEnt: catEntList)
            ((CatalogEntryImpl)catEnt).setCatalogModel(this);
        return catEntList;
    }
    
    public boolean isWellformed() {
        bootStrapCatalog();
        currentStateOfCatalog = catalogWrapper.getCatalogState();
        if(currentStateOfCatalog == DocumentModel.State.NOT_WELL_FORMED)
            return false;
        else
            return true;
    }
    
    public FileObject getCatalogFileObject() {
        return this.catalogFileObject;
    }
    
    public void addPropertychangeListener(PropertyChangeListener pcl) {
        this.pcs.addPropertyChangeListener(PROPERTY_CHANGE_PROPERTY_KEY, pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        this.pcs.removePropertyChangeListener(PROPERTY_CHANGE_PROPERTY_KEY, pcl);
    }
    
    private synchronized void bootStrapCatalog(){
        if(catalogWrapper == null){
            try {
                catalogWrapper = CatalogFileWrapperDOMImpl.getInstance(this.catalogFileObject, unitTestSaveStrategy);
                
                if(catalogWrapper == null)
                    throw new IllegalStateException("Could not get CatalogFileWrapper");
                currentStateOfCatalog = catalogWrapper.getCatalogState();
                catalogWrapper.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        pcs.firePropertyChange(evt);
                        currentStateOfCatalog = catalogWrapper.getCatalogState();
                    }
                });
            } catch (IOException ex) {
                throw new IllegalStateException(ex);
            }
        }
        if(catalogWrapper != null){
            if(catalogWrapper.getCatalogState() == DocumentModel.State.NOT_WELL_FORMED)
                throw new IllegalStateException("Catalog file not wellformed");
        }
        
        
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
            removeNextCatalog(new URI(nextCatalogFileURIStr));
        } catch (URISyntaxException ex) {
        } catch (IOException ex) {
        }
        
        bootStrapCatalog();
        
        CatalogEntry catEnt = new CatalogEntryImpl(CatalogElement.nextCatalog,
                nextCatalogFileURIStr, null);
        catalogWrapper.addNextCatalog(catEnt);
    }
    
    public void removeNextCatalog(URI nextCatalogFileRelativeURI) throws IOException {
        logger.finer("ENTRING:"+nextCatalogFileRelativeURI);
        if(this.catalogFileObject == null)
            return;
        
        bootStrapCatalog();
        
        List<CatalogEntry> catEntList = catalogWrapper.getNextCatalogs();
        if(catEntList == null)
            return;
        CatalogEntry remVal = null;
        for(CatalogEntry catEnt : catEntList){
            if(catEnt.getSource().equals(nextCatalogFileRelativeURI.toString()))
                remVal = catEnt;
        }
        logger.finer("Removing Value: "+remVal);
        if(remVal == null)
            return;
        int index = catEntList.indexOf(remVal);
        catalogWrapper.deleteNextCatalog(index);
        if(catEntList.size() == 1){
            //file has no entry, so just delete it
            logger.finer("There are no more entries so removing catalog file");
            //publicCatWrap.cleanInstance();
            //publicCatWrap = null;
        }
        logger.finer("RETURN: "+catEntList.size());
        
    }
    
}
