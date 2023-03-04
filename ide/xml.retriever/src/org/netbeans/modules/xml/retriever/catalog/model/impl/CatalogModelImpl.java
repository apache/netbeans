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

import java.util.Set;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogQNames;
import org.netbeans.modules.xml.xam.ComponentUpdater;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.retriever.catalog.model.Catalog;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponent;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogComponentFactory;
import org.netbeans.modules.xml.retriever.catalog.model.CatalogModel;
import org.w3c.dom.Element;

public class CatalogModelImpl extends AbstractDocumentModel<CatalogComponent> implements CatalogModel {
    private CatalogComponentFactory factory;
    private Catalog catalog;
    
    public CatalogModelImpl(ModelSource source) {
        super(source);
        factory = new CatalogComponentFactoryImpl(this);
    }
    
    public Catalog getRootComponent() {
        return catalog;
    }

    protected ComponentUpdater<CatalogComponent> getComponentUpdater() {
        return new SyncUpdateVisitor();
    }

    public CatalogComponent createComponent(CatalogComponent parent, Element element) {
        return getFactory().create(element, parent);
    }

    public Catalog createRootComponent(Element root) {
        Catalog newRegistry = (Catalog) getFactory().create(root, null);
        if (newRegistry != null) {
            catalog = newRegistry;
        }
        return newRegistry;
    }

    public CatalogComponentFactory getFactory() {
        return factory;
    }
    
    public Set<QName> getQNames() {
        return CatalogQNames.getMappedQNames();
    }
        
}
