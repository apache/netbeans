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

package org.netbeans.modules.xml.schema.model.resolver;

import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.resolver.FileObjectModelAccessProvider;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.locator.CatalogModel;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A simple CatalogModel is intended for JUnit tests.
 * It is necessary to resolve references between different XAM based
 * models. Only file path references can be used with the TestCatalogModel.
 * Don't use GlobalCatalog or project references. It's good idea to put
 * all necessary files to the same folder. Files can also be located inside
 * of ZIP file. 
 *
 * @author Nikita Krjukov
 */

public class TestCatalogModel2 implements CatalogModel {

    private static TestCatalogModel2 singleton = new TestCatalogModel2(true);

    public static CatalogModel getDefault() {
        return singleton;
    }

    private Map<FileObject, Document> mFileToDocumentMap;
    private boolean mDocumentPooling = true;

    public TestCatalogModel2(boolean documentPooling) {
        mDocumentPooling = documentPooling;
    }

    public ModelSource getModelSource(URI locationURI,
            ModelSource modelSourceOfSourceDocument) throws CatalogModelException {
        //
        FileObject sourceFo = modelSourceOfSourceDocument.getLookup().lookup(FileObject.class);
        assert sourceFo != null : "Source file object has to be specified in the lookup";
        //
        FileObject fo = sourceFo.getParent().getFileObject(locationURI.toString());
        // assert fo != null : "Unknown file: " + locationURI.toString();
        if (fo == null) {
            return null;
        }
        //
        Document doc = getDocument(fo);
        assert doc != null : "Can't load the document: " + locationURI.toString();
        //
        Lookup lookup = Lookups.fixed(fo, doc, this, FileObjectModelAccessProvider.getDefault());
        ModelSource ms = new ModelSource(lookup, modelSourceOfSourceDocument.isEditable());
        //
        return ms;
    }

    private Document getDocument(FileObject fo){
        Document result = null;
        if (mDocumentPooling) {
            result = documentPool().get(fo);
        }
        //
        if (result == null) {
            try {
                result = Util.loadDocument(fo.getInputStream());
                if (mDocumentPooling) {
                    documentPool().put(fo, result);
                }
            } catch (Exception ex) {
                return null;
            }
        }
        //
        return result;
    }
    
    private Map<FileObject,Document> documentPool() {
        if (mFileToDocumentMap == null) {
            mFileToDocumentMap = new HashMap<FileObject,Document>();
        }
        return mFileToDocumentMap;
    }
    
    public void setDocumentPooling(boolean v) {
        mDocumentPooling = v;
        if (! mDocumentPooling) {
            clearDocumentPool();
        }
    }

    public void clearDocumentPool() {
        mFileToDocumentMap = null;
    }

    //------------------------------------

    public ModelSource getModelSource(URI locationURI) throws CatalogModelException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }
}

