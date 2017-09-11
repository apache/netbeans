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
