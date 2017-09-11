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
 * CatalogModel.java
 *
 * Created on October 11, 2005, 1:11 AM
 */

package org.netbeans.modules.xml.retriever.catalog;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import org.netbeans.modules.xml.xam.dom.DocumentModel;
import org.netbeans.modules.xml.xam.locator.*;
import org.openide.filesystems.FileObject;

/**
 * API interface for all the operations exposed
 * by the CatalogModel. There will be one Catalog file per Project.
 * @author girix
 */
public interface CatalogWriteModel extends CatalogModel {
    
    public static final String CATALOG_FILE_EXTENSION = ".xml";
    
    public static final String PUBLIC_CATALOG_FILE_NAME = "catalog";
    
    /**
     * Given the location parameter (schemaLocation for schema and location for wsdl)
     * this method should return the parget URI after looking up in the public catalog file
     * This method will just look up in the public catalog file and return result.
     * If not found in the catalog a null will be returned.
     *
     * @param locationURI
     * @return URI
     */
    public URI searchURI(URI locationURI);
    
    
    /**
     * Adds an URI to FileObject (in the same project) mapping in to the catalog.
     * URI already present will be overwritten.
     *
     * This call might throw IllegalStateException if the catalog files are corrupted.
     * Call isResolverStateValid() before calling this method to detect and avoid above exception
     *
     * @param locationURI
     * @param fileObj
     */
    public void addURI(URI locationURI, FileObject fileObj) throws IOException;
    
    /**
     * Adds an URI to URI mapping in to the catalog.
     * URI already present will be overwritten.
     *
     * This call might throw IllegalStateException if the catalog files are corrupted.
     * Call isResolverStateValid() before calling this method to detect and avoid above exception
     *
     * @param locationURI
     * @param alternateURI
     */
    
    public void addURI(URI locationURI, URI alternateURI) throws IOException;
    
    /**
     * Remove a URI from the catalog.
     * @param locationURI  - locationURI to be removed.
     */
    public void removeURI(URI locationURI) throws IOException;
    
    
    /**
     * Returns list of all registered catalog entries
     *
     * This call might throw IllegalStateException if the catalog files are corrupted.
     * Call isResolverStateValid() before calling this method to detect and avoid above exception
     */
    public Collection<CatalogEntry> getCatalogEntries();
    
    
    /**
     * This method tell if the resolver is in a sane state to retrive the correct values.
     * If false is returned means there is some problem with the resolver. For more information
     * call getState() to get the exact status message. This method should be called before calling
     * most of the resolver methods.
     */
    public boolean isWellformed();
    
    
    /**
     * Returns the current satus of the resolver.
     * Consult the return value and display appropriate messages to the user
     */
    public DocumentModel.State getState();
    
    
    /**
     * Returns the FileObject of the catalog file that this object is bound to.
     */
    public FileObject getCatalogFileObject();
    
    public void addPropertychangeListener(PropertyChangeListener pcl);
    
    public void removePropertyChangeListener(PropertyChangeListener pcl);
    
    
    /**
     * Adds nextCatalogFileURI to the catalog file as nextCatalog entry. If
     * relativize is true and nextCatalogFileURI is absolute, then nextCatalogFileURI is
     * relativized against this catalog file URI itself before writing.
     */
    public void addNextCatalog(URI nextCatalogFileURI, boolean relativize)  throws IOException;
    
    public void removeNextCatalog(URI nextCatalogFileRelativeURI)  throws IOException;
    
    
}
