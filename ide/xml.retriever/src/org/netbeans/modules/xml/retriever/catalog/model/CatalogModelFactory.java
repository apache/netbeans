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
package org.netbeans.modules.xml.retriever.catalog.model;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.xml.xam.AbstractModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.retriever.catalog.model.impl.CatalogModelImpl;

public class CatalogModelFactory extends AbstractModelFactory<CatalogModel> {
    /**
     * Creates a new instance of CatalogModelFactory
     */
    private CatalogModelFactory() {
    }
    
    private static CatalogModelFactory instance = new CatalogModelFactory();
    
    public static CatalogModelFactory getInstance() {
        return instance;
    }
    
    protected CatalogModel createModel(ModelSource source) {
        return new CatalogModelImpl(source);
    }
    
    public static final String CATALOG_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\""
            +" standalone=\"no\"?>"+"\n"+
            "<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"system\"/>";
    
    public CatalogModel getModel(ModelSource source) {
        Document doc = (Document) source.getLookup().lookup(Document.class);
        if( (doc != null) && doc.getLength() <= 5){
            //means the catalog file is empty now
            try {
                doc.remove(0, doc.getLength());
                doc.insertString(0, CATALOG_TEMPLATE, null);
            } catch (BadLocationException ex) {
                return null;
            }
        }
        
        CatalogModel cm =(CatalogModel) super.getModel(source);
        try {
            cm.sync();
        } catch (IOException ex) {
        }
        return cm;
    }
}
