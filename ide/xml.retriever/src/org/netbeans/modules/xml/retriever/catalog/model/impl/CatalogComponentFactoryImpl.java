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
package org.netbeans.modules.xml.retriever.catalog.model.impl;

import java.util.List;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.retriever.catalog.model.NextCatalog;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponent;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponentFactory;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogVisitor;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.w3c.dom.Element;

public class CatalogComponentFactoryImpl implements CatalogComponentFactory {
    private CatalogModelImpl model;
    
    public CatalogComponentFactoryImpl(CatalogModelImpl model) {
        this.model = model;
    }
    
    public CatalogComponent create(Element element, CatalogComponent context) {
        if (context == null) {
            if (areSameQName(CatalogQNames.CATALOG, element)) {
                return new CatalogImpl(model, element);
            } else {
                return null;
            }
        } else {
            return new CreateVisitor().create(element, context);
        }
    }
    
    
    public NextCatalog createNextCatalog() {
        return new NextCatalogImpl(model);
    }
    
    public org.netbeans.modules.xml.retriever.catalog.model.System createSystem() {
        return new SystemImpl(model);
    }
    
    public Catalog createCatalog() {
        return new CatalogImpl(model);
    }
    
    public static boolean areSameQName(CatalogQNames q, Element e) {
        return q.getQName().equals(AbstractDocumentComponent.getQName(e));
    }
    
    public static class CreateVisitor extends CatalogVisitor.Default {
        Element element;
        CatalogComponent created;
        
        CatalogComponent create(Element element, CatalogComponent context) {
            this.element = element;
            context.accept(this);
            return created;
        }
        
        private boolean isElementQName(CatalogQNames q) {
            return areSameQName(q, element);
        }
        
        public void visit(Catalog context) {
            if (isElementQName(CatalogQNames.SYSTEM)) {
                created = new SystemImpl((CatalogModelImpl)context.getModel(), element);
            }
            if (isElementQName(CatalogQNames.NEXTCATALOG)) {
                created = new NextCatalogImpl((CatalogModelImpl)context.getModel(), element);
            }
        }
        
        public void visit(org.netbeans.modules.xml.retriever.catalog.model.System context) {
            
        }
        
        public void visit(NextCatalog context) {
            
        }
        
    }
}
