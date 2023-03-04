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
 * RetrieverImpl.java
 *
 * Created on February 21, 2006, 9:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.retriever.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.*;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.XMLCatalogProvider;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.retriever.catalog.Utilities.DocumentTypesEnum;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class RetrieverImpl extends Retriever {
    
    /** Creates a new instance of RetrieverImpl */
    public RetrieverImpl() {
    }
    
    File seedFile = null;
    RetrieverEngineImpl instance;
    private boolean newThread = false;
    
    void setNewThread(boolean thread) {
        this.newThread = thread;
    }
    
    public FileObject retrieveResource(FileObject destinationDir, URI relativePathToCatalogFile, URI resourceToRetrieve) throws UnknownHostException, URISyntaxException, IOException {
        return retrieveResource(destinationDir, relativePathToCatalogFile, resourceToRetrieve, false);
    }
    
    public FileObject retrieveResource(FileObject destinationDir,URI resourceToRetrieve) throws UnknownHostException, URISyntaxException, IOException {
        return retrieveResource(destinationDir, null, resourceToRetrieve);
    }
    
    
    public FileObject retrieveResourceClosureIntoSingleDirectory(FileObject destinationDir, URI resourceToRetrieve) throws UnknownHostException, URISyntaxException, IOException {
        return retrieveResource(destinationDir, null, resourceToRetrieve, true);
    }
    
    public FileObject retrieveResource(FileObject destinationDir, 
            URI relativePathToCatalogFile, URI resourceToRetrieve, 
            boolean save2singleFolder) 
            throws UnknownHostException, URISyntaxException, IOException {
        Project prj = FileOwnerQuery.getOwner(destinationDir);
        if(relativePathToCatalogFile == null){
            assert(prj != null);
            //check if this project has XMLCatalogProvider in its lookup
            XMLCatalogProvider catProvider = (XMLCatalogProvider) prj.getLookup().
                    lookup(XMLCatalogProvider.class);
            if(catProvider == null){
                //there is no catalog provider so just use the legacy projectwide catalog approach
                return retrieveResourceImpl(destinationDir, resourceToRetrieve, null, save2singleFolder);
            }
            relativePathToCatalogFile = catProvider.getProjectWideCatalog();
            if(relativePathToCatalogFile == null){
                //somehow this provider does not give me this info. So follow legacy.
                return retrieveResourceImpl(destinationDir, resourceToRetrieve, null, save2singleFolder);
            }
            //use this relativePathToCatalogFile for the new catalog file.
        }
        URI cfuri = null;
        if(!relativePathToCatalogFile.isAbsolute()){
            if (prj != null) {
                FileObject prjRtFO = prj.getProjectDirectory();
                cfuri = FileUtil.toFile(prjRtFO).toURI().resolve(relativePathToCatalogFile);
            } else {
                // For Maven based projects the project directory doesn't contain cached catalogs. 
                //  In these cases should be used catalog.xml within destination directory.
                cfuri = destinationDir.getParent().toURL().toURI().resolve(Utilities.PRIVATE_CATALOG_URI_STR);
            }
        }else{
            cfuri = relativePathToCatalogFile;
        }
        File cffile = new File(cfuri);
        if(!cffile.isFile())
            cffile.createNewFile();
        FileObject catalogFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(cffile));
        return retrieveResourceImpl(destinationDir, resourceToRetrieve, catalogFileObject, save2singleFolder);
    }
    
    private FileObject retrieveResourceImpl(FileObject destinationDir, 
            URI resourceToRetrieve, FileObject catalogFileObject, boolean save2singleFolder) 
            throws UnknownHostException, URISyntaxException, IOException {
        instance = new RetrieverEngineImpl(FileUtil.toFile(destinationDir), newThread);
        instance.setFileOverwrite(overwriteFiles);
        instance.setSave2SingleFolder(save2singleFolder);
        if(catalogFileObject != null)
            instance.setCatalogFile(catalogFileObject);
        instance.setShowErrorPopup(false);
        RetrieveEntry rent = null;
        rent = new RetrieveEntry(null, resourceToRetrieve.toString(), null, 
                null, DocumentTypesEnum.schema, this.retrieveRecursively);
        instance.addResourceToRetrieve(rent);
        instance.start();
        File result = instance.getSeedFileLocation();
        seedFile = result;
        if(result == null)
            return null;
        //createCatalog(result);
        return FileUtil.toFileObject(FileUtil.normalizeFile(result));
    }
    
    
    private Project getProject(File result) {
        FileObject fob = FileUtil.toFileObject(result);
        return FileOwnerQuery.getOwner(fob);
    }
    
    public File getProjectCatalog() {
        if(seedFile == null)
            return null;
        FileObject prjRootFo = getProject(seedFile).getProjectDirectory();
        File prjRt = FileUtil.toFile(prjRootFo);
        File catalogFile = new File(prjRt, CatalogWriteModel.PUBLIC_CATALOG_FILE_NAME+CatalogWriteModel.CATALOG_FILE_EXTENSION);
        return catalogFile;
    }
    
    public Map<RetrieveEntry, Exception> getRetrievedResourceExceptionMap() {
        if(instance != null)
            return instance.getRetrievedResourceExceptionMap();
        else
            return null;
    }
    
    public File retrieveResource(File targetFolder, URI source) throws UnknownHostException, URISyntaxException, IOException {
        FileObject fobj = retrieveResource(FileUtil.toFileObject(FileUtil.normalizeFile(targetFolder)), source);
        if(fobj != null)
            return FileUtil.toFile(fobj);
        return null;
    }

    boolean retrieveRecursively = true;
    public void setRecursiveRetrieve(boolean retrieveRecursively) {
        this.retrieveRecursively = retrieveRecursively;
    }

    boolean overwriteFiles = true;
    public void setOverwriteFilesWithSameName(boolean overwriteFiles) {
        this.overwriteFiles = overwriteFiles;
    }
    
}
