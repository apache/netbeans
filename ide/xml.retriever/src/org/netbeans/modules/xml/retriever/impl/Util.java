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
package org.netbeans.modules.xml.retriever.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.retriever.Retriever;
import org.netbeans.modules.xml.retriever.XMLCatalogProvider;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel;
import org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.spi.project.CacheDirectoryProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;

/**
 *
 * @author Samaresh
 */
public class Util {
    private static final String SYSTEM_PRIVATE_CATALOG_FILE = "xml.retriever/catalog.xml";
    private static final String SYSTEM_PRIVATE_CATALOG_DIR = "xml.retriever";

    private Util() {
    }
    
    private static String findProjectCacheRelative(Project p) throws IOException {
        FileObject projectDirFO = p.getProjectDirectory();
        CacheDirectoryProvider cdp = p.getLookup().lookup(CacheDirectoryProvider.class);
        FileObject cacheFO = null;
        
        if (cdp == null || (cacheFO = cdp.getCacheDirectory()) == null) {
            return null;
        }
        if (FileUtil.isParentOf(projectDirFO, cacheFO)) {
            return FileUtil.getRelativePath(projectDirFO, cacheFO);
        } else {
            String s = cacheFO.toURI().toString();
            // strip the trailing / marking a directory
            return s.substring(0, s.length() - 1);
        }
    }
    
    /**
     * Returns a FileObject corresponding to the cache catalog; either project-local (system = false)
     * or platform (system = true)
     * 
     * @param sourceFileObject
     * @param system
     * @return 
     */
    public static FileObject findCacheCatalog(FileObject sourceFileObject) {
        URI privateCatalogURI;
        Project prj = FileOwnerQuery.getOwner(sourceFileObject);
        if(prj == null)
            return null;
        
        FileObject prjrtfo = prj.getProjectDirectory();
        File prjrt = FileUtil.toFile(prjrtfo);
        if(prjrt == null)
            return null;
        
        String catalogstr = Utilities.DEFAULT_PRIVATE_CATALOG_URI_STR;
        try{
            String cachedirstr = findProjectCacheRelative(prj);
            if (cachedirstr != null) {
                catalogstr = cachedirstr+"/"+Utilities.PRIVATE_CATALOG_URI_STR;
            }
            privateCatalogURI = new URI(catalogstr);
        }catch(Exception e){
            return null;
        }
        URI cacheURI = prjrt.toURI().resolve(privateCatalogURI);
        File cacheFile = new File(cacheURI);
        return FileUtil.toFileObject(cacheFile);
    }
    
    public static FileObject findSystemCatalog() {
        File f = Places.getCacheSubfile(SYSTEM_PRIVATE_CATALOG_FILE);
        if (f.exists()) {
            return FileUtil.toFileObject(f);
        } else {
            return null;
        }
    }
    
    public static boolean retrieveAndCache(URI locationURI, FileObject sourceFileObject, boolean newThread, boolean chainCatalog) {
        return retrieveAndCache(locationURI, sourceFileObject, newThread, chainCatalog, locationURI);
    }
    
    public static boolean retrieveAndCache(URI locationURI, FileObject sourceFileObject, boolean newThread, boolean chainCatalog, URI original) {
        URI privateCatalogURI = null;
        URI privateCacheURI = null;
        File cacheFile;
        File prjrt = null;
        FileObject prjrtfo = null;
        
        Project prj = FileOwnerQuery.getOwner(sourceFileObject);
        if(prj == null) {
            File f = Places.getCacheSubfile(SYSTEM_PRIVATE_CATALOG_FILE);
            File dir = Places.getCacheSubdirectory(SYSTEM_PRIVATE_CATALOG_DIR);
            privateCatalogURI = f.toURI();
            privateCacheURI = dir.toURI();
            cacheFile = dir;
            
        } else {
            prjrtfo = prj.getProjectDirectory();
            prjrt = FileUtil.toFile(prjrtfo);
            if(prjrt == null)
                return false;

            //determine the cache dir
            String catalogstr = Utilities.DEFAULT_PRIVATE_CATALOG_URI_STR;
            String cachestr = Utilities.DEFAULT_PRIVATE_CAHCE_URI_STR;
            try{
                String cachedirstr = findProjectCacheRelative(prj);
                if(cachedirstr != null) {
                    catalogstr = cachedirstr+"/"+Utilities.PRIVATE_CATALOG_URI_STR;
                    cachestr = cachedirstr+"/"+Utilities.PRIVATE_CAHCE_URI_STR;
                }
                privateCatalogURI = new URI(catalogstr);
                privateCacheURI = new URI(cachestr);
            }catch(Exception e){
                return false;
            }

            //retrieve
            URI cacheURI = prjrt.toURI().resolve(privateCacheURI);
            cacheFile = new File(cacheURI);
        }
        if(!cacheFile.isDirectory())
            cacheFile.mkdirs();
        FileObject cacheFO = FileUtil.toFileObject(FileUtil.normalizeFile(cacheFile));
        if(cacheFO == null)
            return false;
        Retriever ret = Retriever.getDefault();
        FileObject result;
        try {
            ((RetrieverImpl) ret).setNewThread(newThread);
            result = ret.retrieveResource(cacheFO, privateCatalogURI, locationURI);
        } catch (UnknownHostException ex) {
            result = null;
        } catch (IOException ex) {
            result = null;
        } catch (URISyntaxException ex) {
            result = null;
        }
        
        /*if(result == null)
            return false;*/
        
        if (!chainCatalog)  {
            return true;
        }
        
        //add private catalog as next catalog file to the public and peer catalog
        XMLCatalogProvider catProv = (XMLCatalogProvider) prj.getLookup().
                lookup(XMLCatalogProvider.class);
        FileObject publicCatFO = null;
        FileObject peerCatFO = null;
        if(catProv != null){
            
            //get public catalog
            URI publicCatURI = catProv.getProjectWideCatalog();
            if(publicCatURI != null){

                URI pubcatURI = prjrt.toURI().resolve(publicCatURI);
                if(pubcatURI != null){

                    File pubcatFile = new File(pubcatURI);
                    if(!pubcatFile.isFile())
                        try {
                            pubcatFile.createNewFile();
                        } catch (IOException ex) {
                        }
                    publicCatFO = FileUtil.toFileObject(FileUtil.
                            normalizeFile(pubcatFile));
                }
            }
            
            //get peer catalog
            URI peerCatURI = catProv.getCatalog(sourceFileObject);
            if(peerCatURI != null){
                URI peercatURI = prjrt.toURI().resolve(peerCatURI);
                if(peercatURI != null){
                    File peercatFile = new File(peercatURI);
                    if(!peercatFile.isFile())
                        try {
                            peercatFile.createNewFile();
                        } catch (IOException ex) {
                        }
                    peerCatFO = FileUtil.toFileObject(FileUtil.
                            normalizeFile(peercatFile));
                }
            }
        }
        //get the catalog write model
        //add next cat entry to public catalog
        URI cacheCatFullURI = FileUtil.toFile(prjrtfo).toURI().resolve(privateCatalogURI);
        CatalogWriteModel catWriter = null;
        try {
            if(publicCatFO == null){
                //get the public catalog legacy way
                catWriter = CatalogWriteModelFactory.getInstance().
                        getCatalogWriteModelForProject(sourceFileObject);
            } else{
                catWriter = CatalogWriteModelFactory.getInstance().
                        getCatalogWriteModelForCatalogFile(publicCatFO);
            }
        } catch (CatalogModelException ex) {}
        if(catWriter == null){
            //return true. May be public cat had the priv cat entry already
            return true;
        }
        try {
            catWriter.addNextCatalog(cacheCatFullURI, true);
        } catch (IOException ex) {
        }
        
        //add the next cat entry to peer catalog
        if(publicCatFO != peerCatFO){
            //get the catalog write model
            catWriter = null;
            try {
                if(peerCatFO == null){
                    //get the public catalog legacy way
                    catWriter = CatalogWriteModelFactory.getInstance().
                            getCatalogWriteModelForProject(sourceFileObject);
                } else{
                    catWriter = CatalogWriteModelFactory.getInstance().
                            getCatalogWriteModelForCatalogFile(peerCatFO);
                }
            } catch (CatalogModelException ex) {}
            if(catWriter == null){
                //return true. May be public cat had the priv cat entry already
                return true;
            }
            try {
                catWriter.addNextCatalog(cacheCatFullURI, true);
            } catch (IOException ex) {
            }
        }
        return true;
    }
    
    public static FileObject getProjectCatalogFileObject(Project prj, boolean create) throws IOException {
        if(prj == null)
            return null;
        
        FileObject result = null;
        FileObject myProjectRootFileObject = prj.getProjectDirectory();
        
        //see if this prj has XMLCatalogProvider. If yes use it.
        XMLCatalogProvider catProv =  prj.getLookup().lookup(org.netbeans.modules.xml.retriever.XMLCatalogProvider.class);
        if(catProv != null){
            URI caturi = catProv.getProjectWideCatalog();
            if(caturi != null){
                caturi = FileUtil.toFile(myProjectRootFileObject).toURI().resolve(caturi);
                File catFile = new File(caturi);
                if(!catFile.isFile()){
                    catFile.createNewFile();
                }
                result = FileUtil.toFileObject(FileUtil.normalizeFile(catFile));
            }
        }
        
        if(result == null){
            String fileName = CatalogWriteModel.PUBLIC_CATALOG_FILE_NAME+CatalogWriteModel.CATALOG_FILE_EXTENSION;
            result = myProjectRootFileObject.getFileObject(fileName);
            if(result == null && create){
                result = myProjectRootFileObject.createData(fileName);
            }
        }
        return result;
    }
    

}
