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
 * RuntimeCatalogModel.java
 *
 * Created on January 18, 2007, 2:59 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.completion;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.spi.ModelAccessProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author girix
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.locator.CatalogModel.class)
public class RuntimeCatalogModel implements CatalogModel{
    private URL baseUrl;

    /** Creates a new instance of RuntimeCatalogModel */
    public RuntimeCatalogModel() {
    }

    private RuntimeCatalogModel(URL baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }
    
    public ModelSource getModelSource(URI locationURI,
            ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        URL inputURL = null;
        try {
            UserCatalog cat = UserCatalog.getDefault();
            // mainly for unit tests
            if (cat == null) {
                return null;
            }
            EntityResolver resolver = cat.getEntityResolver();
            if (inputURL == null) {
                InputSource src = resolver.resolveEntity(null, locationURI.toString());
                if (src != null) {
                    inputURL = new URL(src.getSystemId());
                }
            }

            if (inputURL == null) {
                javax.xml.transform.Source isrc = ((javax.xml.transform.URIResolver) resolver).
                    resolve(locationURI.toString(), null);
                if (isrc != null) {
                    inputURL = new URL(isrc.getSystemId());
                }
            }
            if(inputURL == null && baseUrl != null && (!locationURI.isAbsolute())) {
                inputURL = new URL(baseUrl, locationURI.toString());
            }
            if(inputURL != null)
                return createModelSource(inputURL);
        } catch (Exception ex) {
            throw new CatalogModelException(ex);
        }
        
        return null;
    }
    
    private ModelSource createModelSource(URL url) throws CatalogModelException{
        try {
            Document d = AbstractDocumentModel.getAccessProvider().loadSwingDocument(url.openStream());
            if (d != null) {
                List<Object> lookup = new ArrayList<>(5);
                FileObject fo = URLMapper.findFileObject(url);
                if(fo != null) {
                    lookup.add(fo);
                    lookup.add(fileObjectBasedModel);
                    File file = FileUtil.toFile(fo);
                    if(file != null) {
                        lookup.add(file);
                    }
                }
                RuntimeCatalogModel rcm = new RuntimeCatalogModel(url);
                lookup.add(rcm);
                lookup.add(d);
                return new ModelSource(Lookups.fixed(lookup.toArray()), false);
            }
        } catch (Exception ex) {
            throw new CatalogModelException(ex);
        }

        return null;
    }
    
    public InputSource resolveEntity(String publicId,
            String systemId) throws SAXException, IOException {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }
    
    public LSInput resolveResource(String type, String namespaceURI,
            String publicId, String systemId, String baseURI) {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }

    private static final ModelAccessProvider fileObjectBasedModel = new ModelAccessProvider() {

        @Override
        public Object getModelSourceKey(ModelSource source) {
            return source.getLookup().lookup(FileObject.class);
        }

    };
}
