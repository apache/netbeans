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

package org.netbeans.modules.websvc.api.jaxws.project;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.jaxws.catalog.Catalog;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogModel;
import org.netbeans.modules.websvc.jaxws.catalog.CatalogModelFactory;
import org.netbeans.modules.websvc.jaxws.catalog.System;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author mkuchtiak
 */
public class CatalogUtils {

    public static void copyCatalogEntriesForAllClients(FileObject catalog, FileObject jaxWsCatalog, JaxWsModel jaxWsModel)
        throws IOException {

        CatalogModel sourceModel = getCatalogModel(catalog);
        CatalogModel targetModel = getCatalogModel(jaxWsCatalog);
        Catalog cat1 = sourceModel.getRootComponent();
        Catalog cat2 = targetModel.getRootComponent();
        List<System> systemElements = cat1.getSystems();
        targetModel.startTransaction();
        for (Client client : jaxWsModel.getClients()) {
            String clientName = client.getName();
            for (System systemElement : systemElements) {
                String uri = systemElement.getURIAttr();
                String prefix = "xml-resources/web-service-references/"+clientName+"/wsdl/"; //NOI18N
                if (uri != null) {
                    int index = uri.indexOf(prefix);
                    if (index >= 0) {
                        System system = targetModel.getFactory().createSystem();
                        try {
                            system.setSystemIDAttr(new URI(systemElement.getSystemIDAttr()));
                            system.setURIAttr(new URI(uri.substring(index + prefix.length() - 5)));
                        } catch (URISyntaxException ex) {
                            ex.printStackTrace();
                        }
                        cat2.addSystem(system);
                    }
                }
            }
        }
        try {
            targetModel.endTransaction();
        }
        catch (IllegalStateException ex) {
            IOException io = new IOException("Cannot modify catalog", ex);      // NOI18N
            throw Exceptions.attachLocalizedMessage(io, 
                    NbBundle.getMessage(CatalogUtils.class, 
                            "ERR_ModifyCatalog", ex.getLocalizedMessage()));    // NOI18N
        }
    }

    public static void copyCatalogEntriesForClient(FileObject catalog, FileObject jaxWsCatalog, String clientName)
        throws IOException {

        CatalogModel sourceModel = getCatalogModel(catalog);
        CatalogModel targetModel = getCatalogModel(jaxWsCatalog);
        Catalog cat1 = sourceModel.getRootComponent();
        List<System> systemElements = cat1.getSystems();
        if (systemElements.size() > 0) {
            Catalog cat2 = targetModel.getRootComponent();
            targetModel.startTransaction();
            for (System systemElement : systemElements) {
                String uri = systemElement.getURIAttr();
                String prefix = "xml-resources/web-service-references/"+clientName+"/wsdl/"; //NOI18N
                if (uri != null) {
                    int index = uri.indexOf(prefix);
                    if (index >= 0) {
                        System system = null;
                        String systemId = systemElement.getSystemIDAttr();
                        if (systemId != null) {
                            for (System s : cat2.getSystems()) {
                                if (systemId.equals(s.getSystemIDAttr())) {
                                    system = s;
                                    break;
                                }
                            }
                        }
                        if (system == null) {
                            system = targetModel.getFactory().createSystem();
                            cat2.addSystem(system);
                        }
                        try {
                            system.setSystemIDAttr(new URI(systemElement.getSystemIDAttr()));
                            system.setURIAttr(new URI(uri.substring(index + prefix.length() - 5)));
                        } catch (URISyntaxException ex) {
                            ex.printStackTrace();
                        }                        
                    }
                }
            }
            try {
                targetModel.endTransaction();
            }
            catch (IllegalStateException ex) {
                IOException io = new IOException("Cannot modify catalog", ex);      // NOI18N
                throw Exceptions.attachLocalizedMessage(io, 
                        NbBundle.getMessage(CatalogUtils.class, 
                                "ERR_ModifyCatalog", ex.getLocalizedMessage()));    // NOI18N
            }
        }
    }

    public static void updateCatalogEntriesForClient(FileObject jaxWsCatalog, String clientName)
         throws IOException {

        CatalogModel catalogModel = getCatalogModel(jaxWsCatalog);
        Catalog cat = catalogModel.getRootComponent();
        String prefix = "xml-resources/web-service-references/"+clientName+"/wsdl/"; //NOI18N
        catalogModel.startTransaction();
        for (System systemElement : cat.getSystems()) {
            String uri = systemElement.getURIAttr();
            int index = uri.indexOf(prefix);
            if (index >= 0) {
                try {
                    systemElement.setURIAttr(new URI(uri.substring(index + prefix.length() - 5)));
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        }
        catalogModel.endTransaction();
    }

    public static CatalogModel getCatalogModel(FileObject thisFileObj)
        throws IOException {
        ModelSource source = createModelSource(thisFileObj, true);
        return CatalogModelFactory.getInstance().getModel(source);
    }

    private static ModelSource createModelSource(final FileObject thisFileObj,
            boolean editable) throws IOException {
        assert thisFileObj != null : "Null file object.";

        final DataObject dobj;
        try {
            dobj = DataObject.find(thisFileObj);
            final EditorCookie editor = dobj.getCookie(EditorCookie.class);
            if (editor != null) {
                Lookup proxyLookup = Lookups.proxy(
                   new Lookup.Provider() {
                        public Lookup getLookup() {
                            try {
                                return Lookups.fixed(new Object[] {editor.openDocument(), dobj, thisFileObj});
                            } catch (IOException ex) {
                                return Lookups.fixed(new Object[] {dobj, thisFileObj});
                            }
                        }

                    }
                );
                return new ModelSource(proxyLookup, editable);
            }
        } catch (DataObjectNotFoundException ex) {
            java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE, // NOI18N
                ex.getMessage(), ex);
        }
        return null;
    }
}
