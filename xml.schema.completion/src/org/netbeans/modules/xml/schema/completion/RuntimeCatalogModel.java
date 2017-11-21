/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.swing.text.Document;
import org.netbeans.api.xml.services.UserCatalog;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
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
    
    /** Creates a new instance of RuntimeCatalogModel */
    public RuntimeCatalogModel() {
    }
    
    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
        throw new RuntimeException("Method not implemented"); //NOI18N
    }
    
    public ModelSource getModelSource(URI locationURI,
            ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        InputStream inputStream = null;
        try {
            UserCatalog cat = UserCatalog.getDefault();
            // mainly for unit tests
            if (cat == null) {
                return null;
            }
            EntityResolver resolver = cat.getEntityResolver();
            InputSource src = resolver.resolveEntity(null, locationURI.toString());
            if(src != null) {
                inputStream = new URL(src.getSystemId()).openStream();
            } else {
                javax.xml.transform.Source isrc = ((javax.xml.transform.URIResolver)resolver).
                        resolve(locationURI.toString(), null);
                if(isrc != null)
                    inputStream = new URL(isrc.getSystemId()).openStream();
            }
            if(inputStream != null)
                return createModelSource(inputStream);
        } catch (Exception ex) {
            throw new CatalogModelException(ex);
        }
        
        return null;
    }
    
    private ModelSource createModelSource(InputStream is) throws CatalogModelException{
        try {
            Document d = AbstractDocumentModel.getAccessProvider().loadSwingDocument(is);
            if(d != null)
                return new ModelSource(Lookups.fixed(new Object[]{this,d}), false);
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
    
}
