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

package org.netbeans.modules.xml.retriever;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Map;
import org.netbeans.modules.xml.retriever.RetrieveEntry;
import org.netbeans.modules.xml.retriever.impl.RetrieverImpl;
import org.netbeans.modules.xml.retriever.impl.CertificationPanel;
import org.openide.filesystems.FileObject;

/**
 * The Retriever interface supports retrieving the closure of XML documents into
 * a project.
 * @author girix
 * @see The created catalog can be edited programatically by the following interface impl
 *  org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel
 * To get the impl instance use  org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory
 */
public abstract class Retriever {    

    /**
     * Retrieves an XML resource given the resource's URI. Call getRetrievedResourceExceptionMap() to
     * get exception messages that occured dring retrieve process.
     * @param destinationDir A folder inside a NB project (ONLY) to which the
     *  retrieved resource will be copied. All referenced resources will be
     * 	copied relative to this directory.
     * @param relativePathToCatalogFile represents the URI to the catalog file
     *  which should contain the mappings for the retrieved resources. The
     *  path should be relative to the project directory. This file will be
     * created if it does not exist. Passing null will use the default
     *  catalog file for the project from {@link org.netbeans.modules.xml.retriever.XMLCatalogProvider#getProjectWideCatalog()}
     * @param resourceToRetrieve URI of the XML resource that will be retrieved
     *  and stored within destinationDir
     * @return FileObject of the retrieved resource in the local file system
     */
    public abstract FileObject retrieveResource(
            FileObject destinationDir,
            URI relativePathToCatalogFile,
            URI resourceToRetrieve)
            throws UnknownHostException, URISyntaxException, IOException;
    
    /**
     * Retrieves an XML resource given the resource's URI. Call getRetrievedResourceExceptionMap() to
     * get exception messages that occured dring retrieve process. Calling this
     * method is equivalent to calling <code> retrieveResource(FileObject, null, URI) </code>
     *
     * This method will use #XMLCatalogProvider.getProjectWideCatalog() to store
     * the mapping between the retrieved URL's and local files.
     * @param destinationDir   A folder inside a NB project (ONLY) to which the retrieved resource will be copied to. All retrieved imported/included resources will be copied relative to this directory.
     * @param resourceToRetrieve URI of the XML resource that will be retrieved into the project
     *
     * @return FileObject of the retrieved resource in the local file system
     */
    public abstract FileObject retrieveResource(FileObject destinationDir, URI resourceToRetrieve)
    throws UnknownHostException, URISyntaxException, IOException;
    
    
    
    /**
     * Retrieves an XML resource given the resource's URI. Call getRetrievedResourceExceptionMap() to
     * get exception messages that occured dring retrieve process. The files
     * retrieved will be stored in <code>destinationDir</code> and names will
     * use a uniquifying algorithm. This method will not attempt to preserve
     * relative references. Relative references will be retrieved and the
     * referencing files will remain unchanged; thus the catalog may contain
     * relative references.
     *
     * This method will use #XMLCatalogProvider.getProjectWideCatalog() to store
     * the mapping between the retrieved URL's and local files.
     * @param destinationDir   A folder inside a NB project (ONLY) to which the retrieved resource will be copied to. All retrieved imported/included resources will be copied relative to this directory.
     * @param resourceToRetrieve URI of the XML resource that will be retrieved into the project
     *
     * @return FileObject of the retrieved resource in the local file system
     */
    public abstract FileObject retrieveResourceClosureIntoSingleDirectory(
            FileObject destinationDir, URI resourceToRetrieve)
            throws UnknownHostException, URISyntaxException, IOException;
    
    
    
    /**
     * Returns a global OASIS catalog file that has all the mappings of retrieved entries
     * in this project. This must be called after retrieveResource is called to get the
     * most latest entries (along with the old ones that are already in this project).
     *
     * The #retrieveResource(FileObject,URI,URI) method should be
     * used instead of this method to control the catalog file used.
     */
    @Deprecated()
    public abstract File getProjectCatalog();
        
    /**
     * Returns a map that maps retrieved entries that had exceptions while
     * retrieving, along with the exceptions.
     * @return returns a map or null incase if there were no exceptions.
     */
    public abstract Map<RetrieveEntry, Exception> getRetrievedResourceExceptionMap();
    
    /**
     * Added this back just to fix build break.
     * This method will be removed soon. Please make sure not to use this method anymore.
     * @deprecated Please use {@link retrieveResource(FileObject, URI)} instead
     */
    public abstract File retrieveResource(File targetFolder, URI source)
    throws UnknownHostException, URISyntaxException, IOException;
    
    /**
     * Must be called before calling any retrieveResource* method.
     * Instruct the retriever NOT to pull down the imported files recursively (closure) 
     * by passing retrieveRecursively = false or true otherwise (true is default)
     */
    public abstract void setRecursiveRetrieve(boolean retrieveRecursively);
    
    /**
     * Must be called before calling any retrieveResource* method.
     * Instruct the retriever NOT to overwrite files with same name 
     * by passing overwriteFiles = false or true otherwise (true is default)
     */
    public abstract void setOverwriteFilesWithSameName(boolean overwriteFiles);
    
    
    /**
     * Returns a default implementation of the Retriever.
     */
    public static Retriever getDefault(){
        return new RetrieverImpl();
    }
    
    /**
     * Returns the certification panel.
     */
    public static javax.swing.JPanel getCertificationPanel(java.security.cert.X509Certificate cert) {
        return new CertificationPanel(cert);
    }
    
}
