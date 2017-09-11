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
